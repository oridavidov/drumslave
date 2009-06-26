package ca.digitalcave.drumslave.model.options;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Settings {

	private static final Map<String, String> settings = new ConcurrentHashMap<String, String>();
	
	public static void storeSetting(String name, String value){
		if (name == null || value == null)
			throw new RuntimeException("Both name and value must not be null");
		settings.put(name, value);
	}
	
	public static String getSetting(String name){
		if (name == null)
			throw new RuntimeException("Name cannot be null");
		return settings.get(name);
	}
	
	public static Set<String> getSettingNames(){
		return Collections.unmodifiableSet(settings.keySet());
	}
}
