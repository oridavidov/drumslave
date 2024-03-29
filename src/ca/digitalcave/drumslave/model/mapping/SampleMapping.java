package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.model.InvalidModelException;
import ca.digitalcave.drumslave.model.audio.Sample;

/**
 * This class maps between a zone and a sample, given a particular sample group.  Sample Groups
 * let you have multiple mappings (say, 'Rock', 'Acoustic', 'Jazz', 'Latin', etc) for a given 
 * set of hardware / logic (which tend to not change frequently).  This lets you change
 * quickly between setups.
 * @author wyatt
 *
 */
public class SampleMapping {

	private final static Map<String, Map<String, Map<String, String>>> sampleMappings = new ConcurrentHashMap<String, Map<String, Map<String, String>>>();
	private static final String DEFAULT_SAMPLE_GROUP = "Default";
	private static String selectedSampleGroup = DEFAULT_SAMPLE_GROUP;
	private static Object mutex = new Object();

	//***Sample Group methods*** 
	
	public static Set<String> getSampleGroups(){
		if (sampleMappings.keySet().size() == 0)
			addSampleGroup(getSelectedSampleGroup());
		return Collections.unmodifiableSet(sampleMappings.keySet());
	}

	public static String getSelectedSampleGroup() {
		synchronized (mutex) {
			if (selectedSampleGroup == null 
					&& sampleMappings.keySet().size() > 0)
				selectedSampleGroup = new ArrayList<String>(sampleMappings.keySet()).get(0); //Any selected item is better than none...
			if (selectedSampleGroup == null)
				throw new RuntimeException("The selected sample group cannot be null.");
			return selectedSampleGroup;
		}
	}

	public static void addSampleGroup(String sampleGroup) {
		if (sampleMappings.get(sampleGroup) == null)
			sampleMappings.put(sampleGroup, new ConcurrentHashMap<String, Map<String,String>>());
	}
	
	public static void setSelectedSampleGroup(String sampleGroup) {
		synchronized (mutex) {			
			if (sampleMappings.get(sampleGroup) == null)
				addSampleGroup(sampleGroup);
			SampleMapping.selectedSampleGroup = sampleGroup;
		}
	}
	
	public static void removeSampleGroup(String sampleGroupName) throws InvalidModelException {
		synchronized (mutex) {			
			if (sampleGroupName == null)
				throw new InvalidModelException("Config Group name cannot be null.");
			if (sampleMappings.get(sampleGroupName) == null)
				throw new InvalidModelException("There is no Config Group named " + sampleGroupName);
			sampleMappings.remove(sampleGroupName);
			if (sampleGroupName.equals(selectedSampleGroup)){
				if (sampleMappings.keySet().size() > 0)
					selectedSampleGroup = new ArrayList<String>(sampleMappings.keySet()).get(0); //Set the current config to an arbitrary entry
				else
					selectedSampleGroup = DEFAULT_SAMPLE_GROUP;
			}
		}
	}
	
	public static void renameSampleGroup(String oldSampleGroupName, String newSampleGroupName) throws InvalidModelException {
		synchronized (mutex) {
			if (oldSampleGroupName == null || sampleMappings.get(oldSampleGroupName) == null)
				throw new InvalidModelException("There is no existing Config Group named " + oldSampleGroupName);
			if (newSampleGroupName == null)
				throw new InvalidModelException("New Config Group name must not be null.");
			if (sampleMappings.get(newSampleGroupName) != null)
				throw new InvalidModelException("A Config Group named " + newSampleGroupName + " already exists.");
			Map<String, Map<String, String>> values = sampleMappings.get(oldSampleGroupName);
			sampleMappings.remove(oldSampleGroupName);
			sampleMappings.put(newSampleGroupName, values);
			if (selectedSampleGroup.equals(oldSampleGroupName))
				selectedSampleGroup = newSampleGroupName;
		}
	}
	
	
	
	
	//***Sample Mapping methods***
	
	

	public static String getSampleMapping(String sampleGroupName, String padName, String logicalName){
		if (sampleGroupName == null)
			return null;
		if (sampleMappings.get(sampleGroupName) == null)
			return null;
		if (sampleMappings.get(sampleGroupName).get(padName) == null)
			return null;
		return sampleMappings.get(sampleGroupName).get(padName).get(logicalName);
	}
	
//	/**
//	 * Returns all sample mappings
//	 * @return
//	 */
//	public static Map<String, Map<String, Map<String, String>>> getSampleMappings() {
//		return Collections.unmodifiableMap(sampleMappings);
//	}
	
	public static Map<String, String> getSampleMappingsByPad(String selectedSampleGroup, String padName){
		if (sampleMappings.get(selectedSampleGroup) == null)
			return null;
		if (sampleMappings.get(selectedSampleGroup).get(padName) == null)
			return null;
		return new HashMap<String, String>(sampleMappings.get(selectedSampleGroup).get(padName));
	}
	
	/**
	 * Returns all sample mappings for the given group, or null if the group name is invalid.
	 * @param groupName
	 * @return
	 */
	public static Map<String, Map<String, String>> getSampleMappingsByGroup(String groupName){
		if (groupName == null || sampleMappings.get(groupName) == null)
			return null;
		return Collections.unmodifiableMap(sampleMappings.get(groupName));
	}


	protected static void clearSampleMappings(){
		sampleMappings.clear();
		getSampleGroups();
	}

	public static void addSampleMapping(String padName, String logicalName, String sampleName){
		addSampleMapping(getSelectedSampleGroup(), padName, logicalName, sampleName);
	}

	public static void addSampleMapping(String sampleGroupName, String padName, String logicalName, String sampleName){
		if (padName == null || logicalName == null || sampleName == null)
			throw new RuntimeException("None of padName, logicalName, sampleName can be null");
		if (getSampleMapping(sampleGroupName, padName, logicalName) != null)
			throw new RuntimeException("A sample mapping for " + padName + ":" + logicalName + " already exists: " + getSampleMapping(sampleGroupName, padName, logicalName));

		//To save memory, we only load the samples which are actually mapped to
		// a zone.  Thus, instead of loading them all from a config file or something,
		// we load them once they are mapped here.
		if (Sample.getSample(sampleName) == null)
			throw new RuntimeException("Sample " + sampleName + " could not be found");

		if (sampleMappings.get(sampleGroupName) == null)
			sampleMappings.put(sampleGroupName, new ConcurrentHashMap<String, Map<String,String>>());
		if (sampleMappings.get(sampleGroupName).get(padName) == null)
			sampleMappings.get(sampleGroupName).put(padName, new ConcurrentHashMap<String, String>());
		sampleMappings.get(sampleGroupName).get(padName).put(logicalName, sampleName);
	}
}
