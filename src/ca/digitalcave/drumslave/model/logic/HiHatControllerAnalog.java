package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.collections.HistorySet;

import ca.digitalcave.drumslave.model.hardware.Pad;
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
	private final Map<Pad, Float> currentValueByPad = new ConcurrentHashMap<Pad, Float>();
	private final Set<HistoryUnit> history = new HistorySet<HistoryUnit>(100);
	
	//How far back in time (in ms) do we look when finding the velocity?
	private final static int VELOCITY_HISTORY_PERIOD = 500;
	
	//This is initialized the first time the HiHat Controller is instantiated.
	//TODO This will only let there be one HiHatControllerAnalog.  Is this right?
	static String HIHAT_CONTROLLER_ANALOG_NAME = null;
	
	public HiHatControllerAnalog(String name) {
		super(name);
		if (HIHAT_CONTROLLER_ANALOG_NAME != null)
			throw new RuntimeException("You cannot have HiHatControllerAnalog logic assigned to multiple zones.");
		HIHAT_CONTROLLER_ANALOG_NAME = name;
	}
	
	public void execute(Zone zone, float rawValue) {
		currentValueByPad.put(zone.getPad(), rawValue);
		history.add(new HistoryUnit(System.currentTimeMillis(), rawValue));
		logger.finest(zone.getPad().getName() + " changed to " + rawValue);
	}
	
	public Float getAnalogValueByPad(Pad pad){
		return currentValueByPad.get(pad);
	}
	
	/**
	 * We define velocity as the total change in value over the past VELOCITY_HISTORY_PERIOD ms. 
	 * @param pad
	 * @return
	 */
	public Float getAnalogVelocityByPad(Pad pad, boolean closed){
		if (currentValueByPad.get(pad) == null)
			return 0f;
		
		float max = currentValueByPad.get(pad);
		float min = (closed ? 0f : 1f);
		
		long currentTime = System.currentTimeMillis();
		for (HistoryUnit historyUnit : history) {
			if (historyUnit.getTime() + VELOCITY_HISTORY_PERIOD > currentTime){
				if (max < historyUnit.getValue())
					max = historyUnit.getValue();
				if (min > historyUnit.getValue())
					min = historyUnit.getValue();
			}
		}
		
		return Math.max(0f, (max - min));
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		return new ArrayList<String>();
	}
	
	private class HistoryUnit {
		private final long time;
		private final float value;
		
		public HistoryUnit(long time, float value) {
			this.time = time;
			this.value = value;
		}
		
		public long getTime() {
			return time;
		}
		
		public float getValue() {
			return value;
		}
	}
}
