package ca.digitalcave.drumslave.model.logic;

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
	
	protected final long DEFAULT_DOUBLE_TRIGGER_THRESHOLD = 80;
	
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
			String dt = om.getOptions().get(OptionMapping.DOUBLE_TRIGGER_THRESHOLD_NAME);
			if (dt != null)
				doubleTriggerThreshold = Long.parseLong(dt);
		}
		
		return doubleTriggerThreshold;
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
}
