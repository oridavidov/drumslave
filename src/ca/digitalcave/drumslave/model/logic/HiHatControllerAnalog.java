package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.hardware.Zone;

/**
 * HiHat Controller Analog keeps track of how open a hi-hat controller is, and
 * will give this value to the HiHat Play logic.  This can be used in conjunction 
 * with a HiHat Controller Digital to give even more accurate (and responsive)
 * details.
 * 
 * @author wyatt
 *
 */
public class HiHatControllerAnalog extends Logic {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final Map<String, Float> openValueByPad = new ConcurrentHashMap<String, Float>();	
	
	//This is initialized the first time the HiHat Controller is instantiated.
	//TODO This will only let there be one HiHatControllerAnalog.  Is this right?
	static String HIHAT_CONTROLLER_ANALOG_NAME;
	
	public HiHatControllerAnalog(String name) {
		super(name);
		HIHAT_CONTROLLER_ANALOG_NAME = name;
	}
	
	public void execute(Zone zone, float rawValue) {
		openValueByPad.put(zone.getPad().getName(), rawValue);
		logger.finest(zone.getPad().getName() + " changed to " + rawValue);
	}
	
	public Float getAnalogValueByPad(String padName){
		return openValueByPad.get(padName);
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		return new ArrayList<String>();
	}
}
