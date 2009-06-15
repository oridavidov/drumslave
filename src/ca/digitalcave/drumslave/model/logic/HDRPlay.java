package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

/**
 * HDR Play implements play functionality for groups of zones, whose sensors act
 * as a whole to give greater dynamic range to the zones as a whole.  For instance,
 * if you have two sensors on a given pad, you can set the gain on one of them higher than
 * the other, and use the HDR algorithm to find a good balance for actual playback.
 * 
 * You can have at most one HDR set of zones for a given pad; all zones which have HDR
 * logic on a given pad will work together.
 * @author wyatt
 *
 */
public class HDRPlay extends Play {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final Map<String, List<Float>> volumesByPad = new ConcurrentHashMap<String, List<Float>>();
	protected final static Map<String, Long> lastPlayedTime = new ConcurrentHashMap<String, Long>();
	
	public HDRPlay(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
		//See if there are any zone-specific overrides for double trigger
		long doubleTriggerThreshold = getDoubleTriggerThreshold(zone);
		
		String padName = zone.getPad().getName();
		logger.finest("Last play time is " + lastPlayedTime.get(zone));
		
		//Check that this is not an HDR hit; we only want to play a new sample if it
		// has been more than DOUBLE_TRIGGER_THRESHOLD (millis) since the last hit.
		// Otherwise, we will just adjust the volume on the last played sample.
		if (lastPlayedTime.get(padName) == null 
				|| lastPlayedTime.get(padName) + doubleTriggerThreshold < System.currentTimeMillis()){
			lastPlayedTime.put(padName, System.currentTimeMillis());
			logger.finest("Set play time to " + lastPlayedTime.get(padName));
			
			executor.execute(new PlayThread(zone, rawValue));
			
			volumesByPad.remove(padName);
			volumesByPad.put(padName, new ArrayList<Float>());
			volumesByPad.get(padName).add(rawValue);
		}
		else {
			logger.fine("Adjusting volume levels");
			if (volumesByPad.get(padName) == null)
				volumesByPad.put(padName, new ArrayList<Float>());
			volumesByPad.get(padName).add(rawValue);
			
			//Verify there is a sample mapped to the zone
			String sampleName = SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), zone.getName());
			if (sampleName == null)
				throw new RuntimeException("No sample name is mapped to " + zone.getPad().getName() + ":" + zone.getName());
			Sample sample = Sample.getSample(sampleName);
			if (sample == null)
				throw new RuntimeException("No sample is mapped to name " + sampleName);

			//Adjust the last played sample
			sample.adjustLastVolume(getAdjustedValue(padName), zone.getPad().getGain());
		}
	}
	
	
	/**
	 * Returns the adjusted value, based on all available data points.
	 * @param padName
	 * @return
	 */
	private float getAdjustedValue(String padName){
		float total = 0;
		
		for (Float value : volumesByPad.get(padName)) {
			total = total + value;
		}
		
		return total / volumesByPad.get(padName).size();
	}
}
