package ca.digitalcave.drumslave.model.mapping;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.model.audio.Sample;


public class SampleMapping {

	private final static Map<String, Map<String, Map<String, String>>> zoneToSamples = new ConcurrentHashMap<String, Map<String, Map<String, String>>>();
	private static String currentConfig = null;
	private static Object mutex = new Object();

	public static String getSampleMapping(String padName, String zoneName){
		return getSampleMapping(getCurrentConfig(), padName, zoneName);
	}

	public static Set<String> getSampleConfigNames(){
		return Collections.unmodifiableSet(zoneToSamples.keySet());
	}

	public static String getCurrentConfig() {
		synchronized (mutex) {			
			return currentConfig;
		}
	}

	public static void setCurrentConfig(String currentConfig) {
		synchronized (mutex) {			
			if (zoneToSamples.get(currentConfig) == null)
				zoneToSamples.put(currentConfig, new ConcurrentHashMap<String, Map<String,String>>());
			SampleMapping.currentConfig = currentConfig;
		}
	}

	public static String getSampleMapping(String sampleGroupName, String padName, String zoneName){
		if (sampleGroupName == null)
			return null;
		if (zoneToSamples.get(sampleGroupName) == null)
			return null;
		if (zoneToSamples.get(sampleGroupName).get(padName) == null)
			return null;
		return zoneToSamples.get(sampleGroupName).get(padName).get(zoneName);
	}


	protected static void clearSampleMappings(){
		zoneToSamples.clear();
	}

	public static void addSampleMapping(String padName, String zoneName, String sampleName){
		addSampleMapping(getCurrentConfig(), padName, zoneName, sampleName);
	}

	public static void addSampleMapping(String sampleGroupName, String padName, String zoneName, String sampleName){
		if (padName == null || zoneName == null || sampleName == null)
			throw new RuntimeException("None of padName, zoneName, sampleName can be null");
		if (getSampleMapping(padName, zoneName) != null)
			throw new RuntimeException("A sample mapping for " + padName + ":" + zoneName + " already exists: " + getSampleMapping(padName, zoneName));

		//To save memory, we only load the samples which are actually mapped to
		// a zone.  Thus, instead of loading them all from a config file or something,
		// we load them once they are mapped here.
		if (Sample.getSample(sampleName) == null)
			throw new RuntimeException("Sample " + sampleName + " could not be found");

		if (zoneToSamples.get(sampleGroupName) == null)
			zoneToSamples.put(sampleGroupName, new ConcurrentHashMap<String, Map<String,String>>());
		if (zoneToSamples.get(sampleGroupName).get(padName) == null)
			zoneToSamples.get(sampleGroupName).put(padName, new ConcurrentHashMap<String, String>());
		zoneToSamples.get(sampleGroupName).get(padName).put(zoneName, sampleName);
	}
}
