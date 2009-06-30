package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.GainMapping;
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
	private final Map<String, Long> lastTimeByPad = new ConcurrentHashMap<String, Long>();
	private final Map<String, Boolean> openValueByPad = new ConcurrentHashMap<String, Boolean>();
//	private final Map<String, Long> lastClosedTimeByPad = new ConcurrentHashMap<String, Long>();
	
	private final static String LOGICAL_CHIC = "Chic";
	private final static String LOGICAL_SPLASH = "Splash";
	
//	private final static Map<String, HiHatControllerDigital> logicByPadName = new ConcurrentHashMap<String, HiHatControllerDigital>();
//	
//	public final static HiHatControllerDigital getHiHatControllerDigital(String padName){
//		if (padName == null)
//			return null;
//		return logicByPadName.get(padName);
//	}
	
	private static final long CHIC_SAMPLE_DEBOUNCE_PERIOD = 100; //in ms.
	
	//This is initialized the first time the HiHat Controller is instantiated.
	//TODO This will only let there be one HiHatControllerAnalog.  Is this right?
	static String HIHAT_CONTROLLER_DIGITAL_NAME;
	
	public HiHatControllerDigital(String name) {
		super(name);
		HIHAT_CONTROLLER_DIGITAL_NAME = name;
	}
	
	public void execute(Zone zone, float rawValue) {
		String padName = zone.getPad().getName();
		openValueByPad.put(padName, rawValue > 0.5f);
		if (lastTimeByPad.get(padName) == null)
			lastTimeByPad.put(padName, 0l);
		
		if (rawValue < 0.5f){
			logger.finest(padName + " opened");
		}
		else{
			logger.finest(padName + " closed");
			//If the HH is closed, we want to be sure that all non-closed sounds stop.
			zone.getPad().stop(10, LOGICAL_CHIC, zone.getName() + PlayHiHat.LOGICAL_TIGHT, zone.getName() + PlayHiHat.LOGICAL_CLOSED);
			if (lastTimeByPad.get(padName) + CHIC_SAMPLE_DEBOUNCE_PERIOD < System.currentTimeMillis()){
				zone.getPad().stop(10, LOGICAL_CHIC);
			
				Sample sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), padName, LOGICAL_CHIC));
				if (sample != null){
					HiHatControllerAnalog analog = (HiHatControllerAnalog) Logic.getLogic(HiHatControllerAnalog.HIHAT_CONTROLLER_ANALOG_NAME);
					Float volume = analog.getAnalogValueByPad(padName);
					if (volume != null){
						logger.finer("Playing HiHat Chic at volume " + volume);
						sample.play(volume, GainMapping.getPadGain(zone.getPad().getName()));
					}
				}
			}
		}
		
		//Keep track of the time
		lastTimeByPad.put(padName, System.currentTimeMillis());
	}
	
	public boolean isClosedByPad(String padName){
		if (openValueByPad.get(padName) == null)
			return false;
		return openValueByPad.get(padName);
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		List<String> logicalNames = new ArrayList<String>();
		logicalNames.add(LOGICAL_CHIC);
		logicalNames.add(LOGICAL_SPLASH);
		return logicalNames;
	}
}
