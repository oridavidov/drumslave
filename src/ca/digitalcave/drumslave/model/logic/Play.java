package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.GainMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;
import ca.digitalcave.drumslave.model.options.OptionMapping;

public class Play extends Logic {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	protected final static Map<Zone, Long> lastPlayedTimeZone = new ConcurrentHashMap<Zone, Long>();
	protected final static Map<Pad, Long> lastPlayedTimePad = new ConcurrentHashMap<Pad, Long>();
	protected final static Map<Zone, Float> lastPlayedVelocityZone = new ConcurrentHashMap<Zone, Float>();
	protected final static Map<Pad, Float> lastPlayedVelocityPad = new ConcurrentHashMap<Pad, Float>();
	
	public final static String OPTION_ADDITIVE_VOLUME = "Additive Volume";
	public final static String OPTION_DOUBLE_TRIGGER_THRESHOLD = "Double Trigger Threshold";
	
	protected final long DEFAULT_DOUBLE_TRIGGER_THRESHOLD = 50;
	
	protected final Executor executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	public Play(String name) {
		super(name);
	}

	public void execute(Zone zone, float rawValue) {
		//See if there are any zone-specific overrides for double trigger
		long doubleTriggerThreshold = getDoubleTriggerThreshold(zone);
		
		//Check that this is not a double hit; we only want to play this if it
		// has been more than DOUBLE_TRIGGER_THRESHOLD (millis) since the last hit
		if (lastPlayedTimeZone.get(zone) == null 
				|| lastPlayedTimeZone.get(zone) + doubleTriggerThreshold < System.currentTimeMillis()){
			
			//See if this zone uses additive volume; if so, calculate the adjustment
			boolean additiveVolume = isAdditiveVolume(zone);
			if (additiveVolume){
				float volumeAdjustment = getAdditiveVolumeAdjustment(zone);
				System.out.println(volumeAdjustment);
				if (volumeAdjustment > 1){
					rawValue = rawValue * volumeAdjustment;
				}
			}
			
			lastPlayedTimeZone.put(zone, System.currentTimeMillis());
			lastPlayedTimePad.put(zone.getPad(), System.currentTimeMillis());
			lastPlayedVelocityZone.put(zone, rawValue);
			lastPlayedVelocityPad.put(zone.getPad(), rawValue);
			
			executor.execute(new PlayThread(zone, rawValue));
		}
		else {
			logger.fine("Ignoring double trigger");
		}
	}
	
	/**
	 * Returns the double trigger threshold for the given zone; if there are no overrides,
	 * it will return the default.
	 * @param zone
	 * @return
	 */
	protected long getDoubleTriggerThreshold(Zone zone){
		long doubleTriggerThreshold = DEFAULT_DOUBLE_TRIGGER_THRESHOLD;
		OptionMapping om = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
		if (om != null){
			String dt = om.getOptions().get(OPTION_DOUBLE_TRIGGER_THRESHOLD);
			if (dt != null){
				try {
					doubleTriggerThreshold = Long.parseLong(dt);
				}
				catch (NumberFormatException nfe){}
			}
		}
		
		return doubleTriggerThreshold;
	}
	
	/**
	 *This is a function which maps a volume adjustment to a current volume, given 
	 * the time (in seconds) since this zone was last hit, and the volume at which 
	 * it was hit.  The graph of this function starts at 0 and quickly jumps up to 
	 * a max value of 2.  This drops off slowly, increasing in slope as the time 
	 * increases, until such time that it drops below 1 and we cease to care about it.
	 *Since we only use it between the numbers 1 and 2, this function can *only*
	 * increase in volume, never decrease.
	 *DOUBLE_TRIGGER_THRESHOLD determines the distance to the initial curve; this number
	 * should be between 0 and about 0.2.  At 0, there is nothing to prevent a double
	 * trigger from gaining volume from this function; at 0.2, there is about an 80 ms
	 * delay before we are willing to increase the volume at all.  Anything over 0.2 
	 * will probably just introduce too much of a delay, and should not be used for 
	 * the most part.
	 *HORIZONTAL_STRETCH determines the horizontal stretch factor, and directly affects
	 * how long the vibrations will last for.  A value of 1 drops off in under 2 seconds; 
	 * a value of 2 drops off at over 10.  A reasonable value for this may be something 
	 * like 1.9 or so, which will drop off at about 10 seconds.
	 * @param zone
	 * @return
	 */
	private static float DOUBLE_TRIGGER_THRESHOLD = 20f;
	private static float HORIZONTAL_STRETCH = 149f;
	protected float getAdditiveVolumeAdjustment(Zone zone){
		if (lastPlayedVelocityZone.get(zone) == null 
				|| lastPlayedTimeZone.get(zone) == null)
			return 1f;
		
		float lastVolume = lastPlayedVelocityZone.get(zone);
		long timeDifference = System.currentTimeMillis() - lastPlayedTimeZone.get(zone);
		return (float) (-1.0 * ((Math.pow((timeDifference-DOUBLE_TRIGGER_THRESHOLD), 8) / HORIZONTAL_STRETCH) / Math.pow(timeDifference, 6) + 2.1) * lastVolume);
	}
	
	/**
	 * Returns a boolean indicating that the given zone has been configured for
	 * additive volume or not, based on the logic options.
	 * @param zone
	 * @return
	 */
	protected boolean isAdditiveVolume(Zone zone){
		boolean additiveVolume = false;
		
		OptionMapping om = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
		if (om != null){
			String av = om.getOptions().get(OPTION_ADDITIVE_VOLUME);
			if (av != null){
				try {
					additiveVolume = Boolean.parseBoolean(av);
				}
				catch (NumberFormatException nfe){}
			}
		}
		
		return additiveVolume;
	}

	protected class PlayThread implements Runnable  { 
		public static final long serialVersionUID = 0l;
		
		private final Zone zone;
		private final float rawValue;
		
		public PlayThread(Zone zone, float rawValue) {
			this.zone = zone;
			this.rawValue = rawValue;
		}

		public void run() {
			Sample sample = getSample(zone);
			if (sample == null)
				return;

			//Play the sample
			sample.play(rawValue, GainMapping.getPadGain(zone.getPad().getName()));
		}
	}
	
	protected Sample getSample(Zone zone){
		//Verify there is a sample mapped to the zone
		String sampleName = SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), zone.getName());
		if (sampleName == null){
			logger.warning("No sample name is mapped to " + zone.getPad().getName() + ":" + zone.getName());
			return null;
		}
		Sample sample = Sample.getSample(sampleName);
		if (sample == null)
			logger.warning("No sample is mapped to name " + sampleName);

		return sample;
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		return Collections.singletonList(zone.getName());
	}
	
	@Override
	public List<LogicOption> getLogicOptions() {
		List<LogicOption> logicOptions = new ArrayList<LogicOption>();
		logicOptions.add(new LogicOption(LogicOptionType.OPTION_BOOLEAN, OPTION_ADDITIVE_VOLUME));
		LogicOption doubleTrigger = new LogicOption(LogicOptionType.OPTION_INTEGER, OPTION_DOUBLE_TRIGGER_THRESHOLD);
		doubleTrigger.setDefaultValue(50);
		logicOptions.add(doubleTrigger);
		return logicOptions;
	}
}
