package ca.digitalcave.drumslave.model.mapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SampleMapping {

	private final static Map<String, Map<String, String>> zoneToSamples = new ConcurrentHashMap<String, Map<String, String>>();
	public static String getSampleMapping(String padName, String zoneName){
		if (zoneToSamples.get(padName) == null)
			return null;
		return zoneToSamples.get(padName).get(zoneName);
	}
	protected static void clearSampleMappings(){
		zoneToSamples.clear();
	}
	public static void addSampleMapping(String padName, String zoneName, String sampleName){
		if (padName == null || zoneName == null || sampleName == null)
			throw new RuntimeException("None of padName, zoneName, sampleName can be null");
		if (getSampleMapping(padName, zoneName) != null)
			throw new RuntimeException("A sample mapping for " + padName + ":" + zoneName + " already exists: " + getSampleMapping(padName, zoneName));
		
		if (zoneToSamples.get(padName) == null)
			zoneToSamples.put(padName, new ConcurrentHashMap<String, String>());
		zoneToSamples.get(padName).put(zoneName, sampleName);
	}
}
