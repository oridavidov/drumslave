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
import ca.digitalcave.drumslave.model.mapping.LogicMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;
import ca.digitalcave.drumslave.model.options.OptionMapping;

public class Play extends Logic {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	protected final static Map<Zone, Long> lastPlayedTimeByZone = new ConcurrentHashMap<Zone, Long>();
	protected final static Map<Pad, Long> lastPlayedTimeByPad = new ConcurrentHashMap<Pad, Long>();
	protected final static Map<String, Long> lastPlayedTimeByHDRKey = new ConcurrentHashMap<String, Long>();
	protected final static Map<Pad, Float> lastPlayedVelocityByPad = new ConcurrentHashMap<Pad, Float>();
	protected final static Map<Pad, Zone> lastPlayedZoneByPad = new ConcurrentHashMap<Pad, Zone>();
	protected final static Map<String, List<Float>> recentPlayedVelocityByHDRKey = new ConcurrentHashMap<String, List<Float>>();
	
	public final static String OPTION_ADDITIVE_VOLUME = "Additive Volume";
	public final static String OPTION_DOUBLE_TRIGGER_THRESHOLD = "Double Trigger Threshold";
	public final static String OPTION_HDR_LOGICAL_KEY_NAME = "HDR Logical Key Name";
	
	protected final long DEFAULT_DOUBLE_TRIGGER_THRESHOLD = 50;
	protected final long DEFAULT_HDR_TRIGGER_THRESHOLD = 50;
	
	protected final Executor executor = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	public Play(String name) {
		super(name);
	}

	public void execute(Zone zone, float rawValue) {
		long currentTime = System.currentTimeMillis();
		
		//See if there are any zone-specific overrides for various logic options
		long doubleTriggerThreshold = getDoubleTriggerThreshold(zone);
		boolean additiveVolume = isAdditiveVolume(zone);
		String hdrKey = getHDRKey(zone);
		
		//Check that this is not a double hit; we only want to play this if it
		// has been more than DOUBLE_TRIGGER_THRESHOLD (millis) since the last hit
		if (lastPlayedTimeByZone.get(zone) != null 
				&& lastPlayedTimeByZone.get(zone) + doubleTriggerThreshold > currentTime){
			logger.fine("Ignoring double trigger");
			return;
		}
		
		//If this was a 'double trigger' on the same pad (but not the same zone), then
		// we stop the last playing sound, and play the new one instead.
		if (lastPlayedTimeByPad.get(zone.getPad()) != null
				&& lastPlayedTimeByPad.get(zone.getPad()) + doubleTriggerThreshold > currentTime
				&& lastPlayedVelocityByPad.get(zone.getPad()) < rawValue){
			Sample sample = getSample(lastPlayedZoneByPad.get(zone.getPad()));
			if (sample != null){
				logger.fine("Stopping last played sound on pad; playing new sound instead, as it is louder");
				sample.stopLastSample();
			}
		}

		//If this zone uses additive volume, calculate the adjustment			
		if (additiveVolume){
			float volumeAdjustment = getAdditiveVolumeAdjustment(zone);
			System.out.println(volumeAdjustment);
			if (volumeAdjustment > 1){
				rawValue = rawValue * volumeAdjustment;
				rawValue = Math.min(1, Math.max(0, rawValue));
			}
		}

		//If the HDR key is set, we can either adjust volume based on recent hits 
		if (hdrKey != null){
			Long lastPlayedTimeHDR = lastPlayedTimeByHDRKey.get(hdrKey);
			List<Float> recentVelocities = recentPlayedVelocityByHDRKey.get(hdrKey);
			if (lastPlayedTimeHDR != null && recentVelocities != null && recentVelocities.size() > 0){
				logger.fine("Adjusting volume levels");
				recentPlayedVelocityByHDRKey.get(hdrKey).add(rawValue);

				Sample sample = getSample(zone);

				//Adjust the last played sample
				if (sample != null){
					sample.adjustLastVolume(getHDRAdjustedValue(hdrKey), GainMapping.getPadGain(zone.getPad().getName()));
				}
			}
		}



		lastPlayedTimeByZone.put(zone, currentTime);
		lastPlayedTimeByPad.put(zone.getPad(), currentTime);
		//			lastPlayedVelocityByZone.put(zone, rawValue);
		lastPlayedVelocityByPad.put(zone.getPad(), rawValue);
		lastPlayedZoneByPad.put(zone.getPad(), zone);

		//Reset the HDR values if this has taken too long.
		if (hdrKey != null){
			if (recentPlayedVelocityByHDRKey.get(hdrKey) == null 
					|| lastPlayedTimeByHDRKey.get(hdrKey) == null
					|| lastPlayedTimeByHDRKey.get(hdrKey) + DEFAULT_HDR_TRIGGER_THRESHOLD < currentTime)
				recentPlayedVelocityByHDRKey.put(hdrKey, new ArrayList<Float>());
			recentPlayedVelocityByHDRKey.get(hdrKey).add(rawValue);
			lastPlayedTimeByHDRKey.put(hdrKey, currentTime);
		}

		executor.execute(new PlayThread(zone, rawValue));
//		new PlayThread(zone, rawValue);
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
		if (lastPlayedTimeByPad.get(zone.getPad()) == null)
			return 1f;
		
//		float lastVolume = lastPlayedVelocityZone.get(zone);
		long timeDifference = System.currentTimeMillis() - lastPlayedTimeByPad.get(zone.getPad());
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
	
	/**
	 * Returns the HDR key.  This is the internal key, and to avoid confusion with
	 * multiple pads with the same key, we prepend the key with the pad name.
	 * @param zone
	 * @return
	 */
	protected String getHDRKey(Zone zone){
		OptionMapping om = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
		if (om != null){
			String hdrKey = om.getOptions().get(OPTION_HDR_LOGICAL_KEY_NAME);
			if (hdrKey != null && hdrKey.length() > 0){
				return zone.getPad().getName() + ":" + hdrKey;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the adjusted value, based on all available data points.
	 * @param padName
	 * @return
	 */
	private float getHDRAdjustedValue(String hdrKey){
		//Currently this is implemented as a simple average.  Perhaps later we 
		// can adjust this based on weight, etc.
		float total = 0;
		for (Float value : recentPlayedVelocityByHDRKey.get(hdrKey)) {
			total = total + value;
		}
		logger.finer("Volume adjusted to " + total + "/" + recentPlayedVelocityByHDRKey.get(hdrKey).size());
		return total / recentPlayedVelocityByHDRKey.get(hdrKey).size();
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
		String sampleName = null;
		String logicName = LogicMapping.getLogicMapping(zone.getPad().getName(), zone.getName());
		Logic logic = Logic.getLogic(logicName);
		if (logic != null){
			//We return the first sample name which is mapped.  Is it possible that
			// there are multiple logical sample names for a given zone?
			for (String logicalSampleName : logic.getLogicalNames(zone)) {
				sampleName = SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), logicalSampleName);
				if (sampleName != null)
					break;
			}
		}
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
		OptionMapping optionMapping = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
		String logicalName = null;
		if (optionMapping != null){
			logicalName = optionMapping.getOptions().get(OPTION_HDR_LOGICAL_KEY_NAME);
		}
		
		//If there is no HDR key defined, fall back to the zone name.
		if (logicalName == null)
			logicalName = zone.getName();
		
		return Collections.singletonList(logicalName);
	}
	
	@Override
	public List<LogicOption> getLogicOptions() {
		List<LogicOption> logicOptions = new ArrayList<LogicOption>();
		logicOptions.add(new LogicOption(LogicOptionType.OPTION_BOOLEAN, OPTION_ADDITIVE_VOLUME, "AV"));
		LogicOption doubleTrigger = new LogicOption(LogicOptionType.OPTION_INTEGER, OPTION_DOUBLE_TRIGGER_THRESHOLD, "DTT");
		doubleTrigger.setDefaultValue(DEFAULT_DOUBLE_TRIGGER_THRESHOLD);
		logicOptions.add(doubleTrigger);
		logicOptions.add(new LogicOption(LogicOptionType.OPTION_STRING, OPTION_HDR_LOGICAL_KEY_NAME, "HDR Key"));
		return logicOptions;
	}
}
