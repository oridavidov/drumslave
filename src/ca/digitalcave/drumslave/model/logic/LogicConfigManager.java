package ca.digitalcave.drumslave.model.logic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.config.ConfigLogic;
import ca.digitalcave.drumslave.model.config.ConfigManager;

public class LogicConfigManager implements ConfigManager<ConfigLogic>{

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public void loadFromConfig(List<ConfigLogic> configElements) {
		Logic.clearLogics();
		
		if (configElements == null)
			return;
		
		for (ConfigLogic configLogic : configElements) {
			try {
				@SuppressWarnings("unchecked")
				Class<Logic> logicImpl = (Class<Logic>) Class.forName(configLogic.getClassName());
				Constructor<Logic> constuctor = logicImpl.getConstructor(String.class);
				//Upon instantiation, the Logic will store itself in the Multiton under key 'name'.
				constuctor.newInstance(configLogic.getName());
			} 
			catch (ClassNotFoundException e) {
				logger.log(Level.CONFIG, "I could not find logic implementation '" + configLogic.getClassName() + ".", e);
			} 
			catch (Exception e) {
				logger.log(Level.CONFIG, "Error initializing '" + configLogic.getClassName() + ".", e);
			} 
		}
	}
	
	public List<ConfigLogic> saveToConfig() {
		List<ConfigLogic> configLogics = new ArrayList<ConfigLogic>();
		for (Logic logic : Logic.getLogics()) {
			ConfigLogic configLogic = new ConfigLogic();
			configLogic.setName(logic.getName());
			configLogic.setClassName(logic.getClassName());
			configLogics.add(configLogic);
		}
		
		return configLogics;
	}
}
