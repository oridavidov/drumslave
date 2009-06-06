package ca.digitalcave.drumslave.model.audio;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigSample;
import ca.digitalcave.drumslave.model.config.ConfigSampleParam;

public class SampleConfigManager implements ConfigManager<ConfigSample>{

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public void loadFromConfig(List<ConfigSample> configElements) {
		Sample.clearSamples();
		
		if (configElements == null)
			return;
		
		String className = AudioSystemSample.class.getName();
		
		for (ConfigSample configLogic : configElements) {
			try {
				@SuppressWarnings("unchecked")
				Class<Sample> logicImpl = (Class<Sample>) Class.forName(className);
				Constructor<Sample> constuctor = logicImpl.getConstructor(String.class, Map.class);
				
				Map<String, String> params = new HashMap<String, String>();
				for (ConfigSampleParam param : configLogic.getParams()) {
					params.put(param.getName(), param.getValue());
				}
				
				//Upon instantiation, the Sample will store itself in the Multiton under key 'name'.
				constuctor.newInstance(configLogic.getName(), params);
			} 
			catch (ClassNotFoundException e) {
				logger.log(Level.CONFIG, "I could not find logic implementation '" + className + ".", e);
			} 
			catch (Exception e) {
				logger.log(Level.CONFIG, "Error initializing '" + className + ".", e);
			} 
		}
	}
	
	public List<ConfigSample> saveToConfig() {
		List<ConfigSample> configSamples = new ArrayList<ConfigSample>();
		for (Sample sample : Sample.getSamples()) {
			ConfigSample configSample = new ConfigSample();
			configSample.setName(sample.getName());
			List<ConfigSampleParam> params = new ArrayList<ConfigSampleParam>();
			if (sample.getParams() != null){
				for (String key : sample.getParams().keySet()) {
					ConfigSampleParam param = new ConfigSampleParam();
					param.setName(key);
					param.setValue(sample.getParams().get(key));
				}
			}
			configSample.setParams(params);
			configSamples.add(configSample);
		}
		
		return configSamples;
	}

}
