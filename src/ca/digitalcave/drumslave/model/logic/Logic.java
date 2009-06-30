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
		if (name == null)
			return null;
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
	
	
	public enum LogicOptionType {
		OPTION_BOOLEAN,
		OPTION_FLOAT,
		OPTION_INTEGER,
		OPTION_RANGE,
		OPTION_STRING,
	}
	
	public class LogicOption {
		//Required settings
		private final LogicOptionType logicOptionType;
		private final String name;
		private final String shortName;
		
		//Optional settings, depending on type.  We use floats
		// as they can be downcast to either int or boolean as
		// needed.
		private float maxValue;
		private float minValue;
		private float defaultValue;
//		private float floatValue;
//		private boolean boolValue;
		
		public LogicOption(LogicOptionType logicOptionType, String name, String shortName){
			this.logicOptionType = logicOptionType;
			this.name = name;
			this.shortName = shortName;
		}

		public LogicOptionType getLogicOptionType() {
			return logicOptionType;
		}
		public String getName() {
			return name;
		}
		public String getShortName() {
			return shortName;
		}
		
		public float getMaxValue() {
			return maxValue;
		}
		public void setMaxValue(float maxValue) {
			this.maxValue = maxValue;
		}
		public float getMinValue() {
			return minValue;
		}
		public void setMinValue(float minValue) {
			this.minValue = minValue;
		}
		public float getDefaultValue() {
			return defaultValue;
		}
		public void setDefaultValue(float defaultValue) {
			this.defaultValue = defaultValue;
		}
//		public float getFloatValue() {
//			return floatValue;
//		}
//		public void setFloatValue(float floatValue) {
//			this.floatValue = floatValue;
//		}
//		public boolean getBoolValue() {
//			return boolValue;
//		}
//		public void setBoolValue(boolean boolValue) {
//			this.boolValue = boolValue;
//		}
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
	
	/**
	 * Returns a list of LogicOptions, which are used to determine what options
	 * appear on the Logic config window.  All options will be stored in OptionMappings
	 * when entered.
	 * 
	 * The default implementation doesn't return anything; override this if a given
	 * logic class supports options.
	 * @return
	 */
	public List<LogicOption> getLogicOptions(){
		return null;
	}
}
