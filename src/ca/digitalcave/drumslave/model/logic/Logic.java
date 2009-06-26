package ca.digitalcave.drumslave.model.logic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.model.hardware.Zone;

public abstract class Logic {

	//Static resources, related to the Multiton functionality of this class
	private static Map<String, Logic> logics = new ConcurrentHashMap<String, Logic>();
	public static Logic getLogic(String name){
		return logics.get(name);
	}
	public static Collection<Logic> getLogics(){
		return Collections.unmodifiableCollection(logics.values());
	}
	protected static void clearLogics(){
		logics.clear();
	}

	//Non-static resources
	private final String name;
	private final String className;
	public Logic(String name) {
		if (name == null)
			throw new RuntimeException("Logic name cannot be null");
		if (logics.get(name) != null)
			throw new RuntimeException("Logic name must be unique");
		
		this.name = name;
		this.className = this.getClass().getName();
		
		logics.put(name, this);
	}
	
	public String getName() {
		return name;
	}
	public String getClassName() {
		return className;
	}
	
	/**
	 * The method which is called when the serial port receives notification of
	 * a new hit.
	 * @param zone
	 * @param value
	 */
	public abstract void execute(Zone zone, float value);
	
	/**
	 * This method is used to determine what the name(s) of the logical zones
	 * are, which will be used to map samples to.  Most logic implementations
	 * will just return a single string, namely the zone name.  However, some
	 * specialized logics, such as HiHat controllers, can map multiple samples
	 * to a single zone, to (for instance) use a splash, chic, etc.  
	 * @return
	 */
	public abstract List<String> getLogicalNames(Zone zone);
}
