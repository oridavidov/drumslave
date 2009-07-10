package ca.digitalcave.drumslave.model.logic;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.LogicMapping;
import ca.digitalcave.drumslave.model.options.OptionMapping;

/**
 * Play Secondary implements play functionality for groups of zones on the same 
 * pad.  Select one zone as the primary zone for a given pad by setting the logic
 * to Play; all other zones are set to Play Secondary.  If a secondary pad is hit
 * above a certain (configurable) threshold, we switch from the primary sensor to
 * the secondary.  If there are multiple secondary zones, and both are hit above
 * their thresholds, we play only the one which is furthest above the threshold.  
 * 
 * @author wyatt
 *
 */
public class PlaySecondary extends Play {

//	private final Logger logger = Logger.getLogger(this.getClass().getName());
//	private final Map<Pad, Float> amountAboveThresholdByPad = new ConcurrentHashMap<Pad, Float>();
//	protected final Map<String, Long> lastPlayedTime = new ConcurrentHashMap<String, Long>();
//	
	private final static Map<Pad, Zone> primaryZonesByPad = new ConcurrentHashMap<Pad, Zone>();
	
	private final static long SECONDARY_TIME_THRESHOLD = 100;
	private final static float DEFAULT_SECONDARY_VELOCITY_THRESHOLD = 0.25f;
	private final static String OPTION_SECONDARY_THRESHOLD = "Secondary Threshold";
	
	public PlaySecondary(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
		long currentTime = System.currentTimeMillis();
		
		//Make sure we know which is the primary zone for this second play zone.
		if (primaryZonesByPad.get(zone.getPad()) == null){
			Pad p = zone.getPad();
			for (Zone z : p.getZones()) {
				if (Logic.getLogic(LogicMapping.getLogicMapping(p.getName(), z.getName())).getClass().equals(Play.class)){
					primaryZonesByPad.put(p, z);
					break;
				}
			}
		}
		
		long lastPlayTime = (lastPlayedTimeByPad.get(zone.getPad()) != null ? lastPlayedTimeByPad.get(zone.getPad()) : 0);
		float lastPlayVelocity = (lastPlayedVelocityByPad.get(zone.getPad()) != null ? lastPlayedVelocityByPad.get(zone.getPad()) : 0);
		float secondaryVelocityThreshold = getSecondaryVelocityThreshold(zone);
		if (lastPlayTime + SECONDARY_TIME_THRESHOLD < currentTime)
			lastPlayVelocity = 0; //If we are out of the time threshold, last play doesn't count.
		
		float volumeToPlay = Math.max(rawValue, lastPlayVelocity);
		if (volumeToPlay - secondaryVelocityThreshold > lastPlayVelocity || lastPlayVelocity == 0){
			//Stop the last sample on the primary pad if it is available
			if (lastPlayVelocity > 0 && primaryZonesByPad.get(zone.getPad()) != null)
				getSample(primaryZonesByPad.get(zone.getPad())).stopLastSample();
			super.execute(zone, volumeToPlay);
		}
	}
	
	protected float getSecondaryVelocityThreshold(Zone zone){
		float secondaryVelocityThreshold = DEFAULT_SECONDARY_VELOCITY_THRESHOLD;
		OptionMapping om = OptionMapping.getOptionMapping(zone.getPad().getName(), zone.getName());
		if (om != null){
			String st = om.getOptions().get(OPTION_SECONDARY_THRESHOLD);
			if (st != null){
				try {
					secondaryVelocityThreshold = Float.parseFloat(st);
				}
				catch (NumberFormatException nfe){}
			}
		}
		
		return secondaryVelocityThreshold;
	}
	
	@Override
		public List<LogicOption> getLogicOptions() {
			List<LogicOption> logicOptions = super.getLogicOptions();
			
			LogicOption secondaryThreshold = new LogicOption(LogicOptionType.OPTION_RANGE, OPTION_SECONDARY_THRESHOLD, "ST");
			secondaryThreshold.setMinValue(0f);
			secondaryThreshold.setMaxValue(1f);
			secondaryThreshold.setRangeSteps(10);
			secondaryThreshold.setDefaultValue(DEFAULT_SECONDARY_VELOCITY_THRESHOLD);
			logicOptions.add(secondaryThreshold);
			
			return logicOptions;
		}
}
