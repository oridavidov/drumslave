package ca.digitalcave.drumslave.model.options;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.model.hardware.Zone;

/**
 * This class provides a hybrid Multiton-like access to the OptionMapping object.  
 * Objects are initially created using the constructor, but can be
 * accessed later using the Multiton static getOptionMapping(padName, zoneName) method.
 * 
 * This class (and others in DrumSlave) provide easy thread-safe access
 * to various configuration elements without needing to pass around a 
 * reference to them, and without needing to traverse a singleton instance
 * of the configuration.
 *  
 * @author wyatt
 *
 */
public class OptionMapping {
	private final static Map<Zone, OptionMapping> optionMappings = new ConcurrentHashMap<Zone, OptionMapping>();
	
	private final String padName;
	private final String zoneName;
	private final Map<String, String> options;
	
	/**
	 * Clears all the option mappings from the static map.  Used before loading a new config file.
	 */
	protected static void clearOptionMappings(){
		optionMappings.clear();
	}
	
	private OptionMapping(String padName, String zoneName) {
		this.padName = padName;
		this.zoneName = zoneName;
		this.options = new ConcurrentHashMap<String, String>();
	}
	
	public static void addOptionMapping(String padName, String zoneName, String optionName, String optionValue){
		Zone zone = Zone.getZone(padName, zoneName);
		if (zone == null)
			throw new RuntimeException("Cannot create zone mapping; zone not found");
		if (optionMappings.get(zone) == null)
			optionMappings.put(zone, new OptionMapping(padName, zoneName));
		optionMappings.get(zone).options.put(optionName, optionValue);
	}

	/**
	 * Returns a collection of all option mappings currently defined. 
	 * @return
	 */
	public static Collection<OptionMapping> getOptionMappings(){
		return Collections.unmodifiableCollection(optionMappings.values());
	}
	
	/**
	 * Returns the Option mapping object associate with the given name.  If no such
	 * object exists, returns null. 
	 * @param name
	 * @return
	 */
	public static OptionMapping getOptionMapping(String padName, String zoneName){
		Zone zone = Zone.getZone(padName, zoneName);
		if (zone == null)
			return null;
		return optionMappings.get(zone);
	}
	
	public Map<String, String> getOptions() {
		return Collections.unmodifiableMap(options);
	}
	
	public String getPadName() {
		return padName;
	}
	
	public String getZoneName() {
		return zoneName;
	}
	
	@Override
	public String toString() {
		return padName + ":" + zoneName + " " + options;
	}
}
