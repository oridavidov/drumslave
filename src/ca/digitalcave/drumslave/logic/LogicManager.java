package ca.digitalcave.drumslave.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.hardware.Zone;

public class LogicManager {

	private static Map<Zone, Logic> logics = new ConcurrentHashMap<Zone, Logic>();
	
	public static Logic getLogic(Zone zone){
		return logics.get(zone);
	}
	
	public static void loadLogicMappings(){
		//TODO Load from file or something
		logics.put(Zone.getZone("Bass", "Head"), new Play());
		logics.put(Zone.getZone("Ride", "Bow"), new Play());
	}
}
