package ca.digitalcave.drumslave.model.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;
import ca.digitalcave.drumslave.model.options.OptionMapping;

public class Play extends Logic {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final static Map<Zone, Long> lastPlayedTime = new ConcurrentHashMap<Zone, Long>();

	private final long DEFAULT_DOUBLE_TRIGGER_THRESHOLD = 200;

	public Play(String name) {
		super(name);
	}

	public void execute(Zone zone, float rawValue) {
		Thread thread = new Thread(new PlayThread(zone, rawValue), "PlayThread");
		thread.setDaemon(true);
		thread.start();
	}

	private class PlayThread implements Runnable  { 
		public static final long serialVersionUID = 0l;
		
		private final Zone zone;
		private final float rawValue;
		
		public PlayThread(Zone zone, float rawValue) {
			this.zone = zone;
			this.rawValue = rawValue;
		}

		public void run() {
			//Verify there is a sample mapped to the zone
			String sampleName = SampleMapping.getSampleMapping(zone.getPad().getName(), zone.getName());
			if (sampleName == null)
				throw new RuntimeException("No sample name is mapped to " + zone.getPad().getName() + ":" + zone.getName());
			Sample sample = Sample.getSample(sampleName);
			if (sample == null)
				throw new RuntimeException("No sample is mapped to name " + sampleName);

			//See if there are any zone-specific overrides for double trigger
			long doubleTriggerThreshold = DEFAULT_DOUBLE_TRIGGER_THRESHOLD;
			OptionMapping om = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
			if (om != null){
				String dt = om.getOptions().get(OptionMapping.DOUBLE_TRIGGER_THRESHOLD_NAME);
				if (dt != null)
					doubleTriggerThreshold = Long.parseLong(dt);
			}

			//Check that this is not a double hit; we only want to play this if it
			// has been more than DOUBLE_TRIGGER_THRESHOLD (millis) since the last hit
			if (lastPlayedTime.get(zone) == null 
					|| lastPlayedTime.get(zone) + doubleTriggerThreshold < System.currentTimeMillis()){
				sample.play(rawValue, zone.getPad().getGain());
				lastPlayedTime.put(zone, System.currentTimeMillis());
			}
			else {
				logger.fine("Ignoring double trigger");
			}
		}
	}
}
