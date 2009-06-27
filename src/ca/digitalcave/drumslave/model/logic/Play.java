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
					rawValue = Math.min(1, Math.max(0, rawValue));
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
	
	private static float SLOPE = 100; //Higher number is less of a drop-off
	private static float FALLOFF = 10000; //Higher numbers lets the slope take longer to reach 1.
	/**
	 * Function which starts at a high spike, and slowly gets to 1.  The higher the
	 * SLOPE constant, the smoother the slope left of the spike is; the higher the FALLOFF 
	 * constant, the slower the function goes to 1. 
	 * @param zone
	 * @return
	 */
	protected float getAdditiveVolumeAdjustment(Zone zone){
		if (lastPlayedTimePad.get(zone.getPad()) == null)
			return 1f;
		
//		float lastVolume = lastPlayedVelocityZone.get(zone);
		long timeDifference = System.currentTimeMillis() - lastPlayedTimePad.get(zone.getPad());
		return (float) (SLOPE / (timeDifference + SLOPE) - ((timeDifference / FALLOFF) * (timeDifference / FALLOFF)) + 1);
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
		logicOptions.add(new LogicOption(LogicOptionType.OPTION_BOOLEAN, OPTION_ADDITIVE_VOLUME, "AV"));
		LogicOption doubleTrigger = new LogicOption(LogicOptionType.OPTION_INTEGER, OPTION_DOUBLE_TRIGGER_THRESHOLD, "DTT");
		doubleTrigger.setDefaultValue(50);
		logicOptions.add(doubleTrigger);
		return logicOptions;
	}
}
