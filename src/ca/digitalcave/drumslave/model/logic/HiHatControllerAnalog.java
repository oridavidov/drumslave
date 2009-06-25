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
	
	public HiHatControllerAnalog(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
		openValueByPad.put(zone.getPad().getName(), rawValue);
		logger.finest(zone.getPad().getName() + " changed to " + rawValue);
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		return new ArrayList<String>();
	}
}
