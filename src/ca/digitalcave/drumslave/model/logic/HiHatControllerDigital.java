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
 * HiHat Controller Digital keeps track of whether a hihat controller is open at
 * all or not, and will give this value to the HiHat Play logic.  This can be 
 * used in conjunction with a HiHat Controller Analog to give even more accurate 
 * (and responsive) details.
 * 
 * @author wyatt
 *
 */
public class HiHatControllerDigital extends Logic {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final Map<String, Long> lastOpenTimeByPad = new ConcurrentHashMap<String, Long>();
	private final Map<String, Long> lastClosedTimeByPad = new ConcurrentHashMap<String, Long>();
	
	private final static String LOGICAL_CHIC = "Chic";
	private final static String LOGICAL_SPLASH = "Splash";
	
	private final static Map<String, HiHatControllerDigital> logicByPadName = new ConcurrentHashMap<String, HiHatControllerDigital>();
	
	public final static HiHatControllerDigital getHiHatControllerDigital(String padName){
		if (padName == null)
			return null;
		return logicByPadName.get(padName);
	}
	
	public HiHatControllerDigital(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
		String padName = zone.getPad().getName();
		if (lastOpenTimeByPad.get(padName) == null)
			lastOpenTimeByPad.put(padName, 0l);
		if (lastClosedTimeByPad.get(padName) == null)
			lastClosedTimeByPad.put(padName, 0l);
		
		if (rawValue < 0.5f){
			lastOpenTimeByPad.put(padName, System.currentTimeMillis());
			logger.finest(padName + " opened");
		}
		else{
			lastClosedTimeByPad.put(padName, System.currentTimeMillis());
			logger.finest(padName + " closed");

			Sample sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), padName, LOGICAL_CHIC));
			sample.play(0.5f, 1.0f); //TODO change volume based on how fast the pedal has gone down
		}
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		List<String> logicalNames = new ArrayList<String>();
		logicalNames.add(LOGICAL_CHIC);
		logicalNames.add(LOGICAL_SPLASH);
		return logicalNames;
	}
}
