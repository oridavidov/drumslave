package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.GainMapping;

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
public class PlayHDR extends Play {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final Map<String, List<Float>> volumesByPad = new ConcurrentHashMap<String, List<Float>>();
	protected final static Map<String, Long> lastPlayedTime = new ConcurrentHashMap<String, Long>();
	
	public final static String OPTION_HDR_LOGICAL_KEY_NAME = "HDR Logical Key Name";

	
	public PlayHDR(String name) {
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
//			new PlayThread(zone, rawValue).run();
			
			volumesByPad.remove(padName);
			volumesByPad.put(padName, new ArrayList<Float>());
			volumesByPad.get(padName).add(rawValue);
		}
		else {
			logger.fine("Adjusting volume levels");
			if (volumesByPad.get(padName) == null)
				volumesByPad.put(padName, new ArrayList<Float>());
			volumesByPad.get(padName).add(rawValue);
			
			Sample sample = getSample(zone);

			//Adjust the last played sample
			sample.adjustLastVolume(getAdjustedValue(padName), GainMapping.getPadGain(padName));
		}
	}
	
	@Override
	public List<LogicOption> getLogicOptions() {
		List<LogicOption> logicOptions = super.getLogicOptions();
		logicOptions.add(new LogicOption(LogicOptionType.OPTION_STRING, OPTION_HDR_LOGICAL_KEY_NAME, "<Name>"));
		return logicOptions;
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
