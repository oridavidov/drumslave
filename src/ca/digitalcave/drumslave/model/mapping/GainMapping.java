package ca.digitalcave.drumslave.model.mapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class GainMapping {

	private final static Map<String, Map<String, Float>> padToGains = new ConcurrentHashMap<String, Map<String,Float>>();

	public static final String MASTER = "Master"; //Gain mapping name for master channel.

	
	/**
	 * Returns the pad gain for the given sample mapping group and pad.  If the 
	 * gain has not been explicitly set, return 1.0 (effectively no gain change).
	 * @param sampleMappingGroupName
	 * @param padName
	 * @return
	 */
	public static float getPadGain(String sampleMappingGroupName, String padName){
		if (padToGains.get(sampleMappingGroupName) == null
				|| padToGains.get(sampleMappingGroupName).get(padName) == null)
			return 1.0f;
		return padToGains.get(sampleMappingGroupName).get(padName);
	}
	
	/**
	 * Convenience method to get the pad gain for the current selected sample group.
	 * @param padName
	 * @return
	 */
	public static float getPadGain(String padName){
		return getPadGain(SampleMapping.getSelectedSampleGroup(), padName);
	}
	
	/**
	 * Clear all gain mappings. 
	 */
	protected static void clearGainMappings(){
		padToGains.clear();
	}
	
	/**
	 * Convenience method to set the pad gain for the current selected sample group.
	 * @param padName
	 * @param gain
	 */
	public static void addGainMapping(String padName, float gain){
		addGainMapping(SampleMapping.getSelectedSampleGroup(), padName, gain);
	}
	
	/**
	 * Adds (or replaces, if it is already there) the gain mapping for the given 
	 * sample mapping group and pad name.
	 * @param sampleMappingGroupName
	 * @param padName
	 * @param gain
	 */
	public static void addGainMapping(String sampleMappingGroupName, String padName, float gain){
		if (sampleMappingGroupName == null || padName == null)
			throw new RuntimeException("None of sampleMappingGroupName, padName can be null");
		
		if (padToGains.get(sampleMappingGroupName) == null)
			padToGains.put(sampleMappingGroupName, new ConcurrentHashMap<String, Float>());
		padToGains.get(sampleMappingGroupName).put(padName, gain);
	}
}
