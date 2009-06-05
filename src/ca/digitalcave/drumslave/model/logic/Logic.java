package ca.digitalcave.drumslave.model.logic;

import java.util.Collection;
import java.util.Collections;
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
	
	public abstract void execute(Zone zone, float value);
}
