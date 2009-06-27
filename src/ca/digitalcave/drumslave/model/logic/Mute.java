package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.options.OptionMapping;

public class Mute extends Logic {

	public final static String OPTION_STOP_FADEOUT_TIME = "Stop Time";
	private final static int DEFAULT_STOP_FADEOUT_TIME = 200;
	
	public Mute(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float value) {
		int stopFadeoutTime = getStopFadeoutTime(zone);
		zone.getPad().stop(stopFadeoutTime);
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		return new ArrayList<String>();
	}
	
	/**
	 * Returns the stop fadeout time for the given zone; if there are no overrides,
	 * it will return the default.
	 * @param zone
	 * @return
	 */
	protected int getStopFadeoutTime(Zone zone){
		int stopFadeoutTime = DEFAULT_STOP_FADEOUT_TIME;
		OptionMapping om = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
		if (om != null){
			String st = om.getOptions().get(OPTION_STOP_FADEOUT_TIME);
			if (st != null){
				try {
					stopFadeoutTime = Integer.parseInt(st);
				}
				catch (NumberFormatException nfe){}
			}
		}
		
		return stopFadeoutTime;
	}
	
	@Override
	public List<LogicOption> getLogicOptions() {
		List<LogicOption> logicOptions = new ArrayList<LogicOption>();
		LogicOption doubleTrigger = new LogicOption(LogicOptionType.OPTION_INTEGER, OPTION_STOP_FADEOUT_TIME, "ST");
		doubleTrigger.setDefaultValue(DEFAULT_STOP_FADEOUT_TIME);
		logicOptions.add(doubleTrigger);
		return logicOptions;
	}
}
