package ca.digitalcave.drumslave.model.mapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LogicMapping {

	private final static Map<String, Map<String, String>> zoneToLogics = new ConcurrentHashMap<String, Map<String, String>>();
	
	public static String getLogicMapping(String padName, String zoneName){
		if (zoneToLogics.get(padName) == null)
			return null;
		return zoneToLogics.get(padName).get(zoneName);
	}
	
	protected static void clearLogicMappings(){
		zoneToLogics.clear();
	}
	
	public static void addLogicMapping(String padName, String zoneName, String logicName){
		if (padName == null || zoneName == null || logicName == null)
			throw new RuntimeException("None of padName, zoneName, logicName can be null");
		if (getLogicMapping(padName, zoneName) != null)
			throw new RuntimeException("A logic mapping for " + padName + ":" + zoneName + " already exists: " + getLogicMapping(padName, zoneName));
		
		if (zoneToLogics.get(padName) == null)
			zoneToLogics.put(padName, new ConcurrentHashMap<String, String>());
		zoneToLogics.get(padName).put(zoneName, logicName);
	}
}
