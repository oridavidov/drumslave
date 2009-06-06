package ca.digitalcave.drumslave.model.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.SampleConfigManager;
import ca.digitalcave.drumslave.model.hardware.HardwareConfigManager;
import ca.digitalcave.drumslave.model.logic.LogicConfigManager;
import ca.digitalcave.drumslave.model.mapping.LogicMappingConfigManager;
import ca.digitalcave.drumslave.model.mapping.SampleMappingConfigManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConfigFactory {

	private static ConfigFactory configFactorySingleton = new ConfigFactory();
	private final static Logger logger = Logger.getLogger(ConfigFactory.class.getName()); 	
	
	public static ConfigFactory getInstance() {
		return configFactorySingleton;
	}
	
	public void loadConfig(ConfigType configType, File configFile){
		try {
			//TODO Change this to store config in different place
			InputStream is = new FileInputStream(configFile);

			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(Config.class);
			Object o = xstream.fromXML(is);
			if (o instanceof Config){
				Config c = (Config) o;
				switch (configType) {
				case HARDWARE:
					new HardwareConfigManager().loadFromConfig(c.getPads());					
					break;
					
				case SAMPLES:
					new SampleConfigManager().loadFromConfig(c.getSamples());
					break;
					
				case LOGIC:
					new LogicConfigManager().loadFromConfig(c.getLogics());
					break;
					
				case LOGIC_MAPPING:
					new LogicMappingConfigManager().loadFromConfig(c.getLogicMappings());
					break;
					
				case SAMPLE_MAPPING:
					new SampleMappingConfigManager().loadFromConfig(c.getSampleMappings());
					break;
				}
			}
			else {
				throw new RuntimeException("Loaded config is not an instance of Config; it is " + o.getClass().getName());
			}
		}
		catch (IOException ioe){
			logger.log(Level.WARNING, "Error encountered while loading config file", ioe);
		}
	}
	
	public void saveConfig(ConfigType configType, File configFile){
		Config config = new Config();
		
		switch (configType) {
		case HARDWARE:
			config.setPads(new HardwareConfigManager().saveToConfig());					
			break;
			
		case SAMPLES:
			config.setSamples(new SampleConfigManager().saveToConfig());
			break;
			
		case LOGIC:
			config.setLogics(new LogicConfigManager().saveToConfig());
			break;
			
		case LOGIC_MAPPING:
			config.setLogicMappings(new LogicMappingConfigManager().saveToConfig());
			break;
			
		case SAMPLE_MAPPING:
			config.setSampleMappings(new SampleMappingConfigManager().saveToConfig());
			break;
		}
		
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(Config.class);
		xstream.toXML(config, System.out);
	}
	
	public enum ConfigType {
		HARDWARE,
		SAMPLES,
		LOGIC,
		LOGIC_MAPPING,
		SAMPLE_MAPPING
	}
}
