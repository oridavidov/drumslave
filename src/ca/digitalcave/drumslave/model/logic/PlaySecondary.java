package ca.digitalcave.drumslave.model.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.LogicMapping;

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
	
	private final static long SECONDARY_TIME_THRESHOLD = 2000;
	private final static float SECONDARY_VELOCITY_THRESHOLD = 0.5f;
	
	public PlaySecondary(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
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
		
		long lastPlayTime = (lastPlayedTimePad.get(zone.getPad()) != null ? lastPlayedTimePad.get(zone.getPad()) : 0);
		float lastPlayVelocity = (lastPlayedVelocityPad.get(zone.getPad()) != null ? lastPlayedVelocityPad.get(zone.getPad()) : 0);
		if (lastPlayTime + SECONDARY_TIME_THRESHOLD > System.currentTimeMillis()){
			if (rawValue > SECONDARY_VELOCITY_THRESHOLD){
				//Stop the last sample on the primary pad if it is available 
				if (primaryZonesByPad.get(zone.getPad()) != null)
					getSample(primaryZonesByPad.get(zone.getPad())).stopLastSample();
				super.execute(zone, Math.max(rawValue, lastPlayVelocity));
			}
		}
	}
}
