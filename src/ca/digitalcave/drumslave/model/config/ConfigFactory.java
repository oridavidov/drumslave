package ca.digitalcave.drumslave.model.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.common.OperatingSystemUtil;

import ca.digitalcave.drumslave.model.hardware.HardwareConfigManager;
import ca.digitalcave.drumslave.model.logic.PlayHDR;
import ca.digitalcave.drumslave.model.logic.LogicConfigManager;
import ca.digitalcave.drumslave.model.logic.Mute;
import ca.digitalcave.drumslave.model.logic.Play;
import ca.digitalcave.drumslave.model.logic.PlaySecondary;
import ca.digitalcave.drumslave.model.mapping.GainMappingConfigManager;
import ca.digitalcave.drumslave.model.mapping.LogicMappingConfigManager;
import ca.digitalcave.drumslave.model.mapping.SampleMappingConfigManager;
import ca.digitalcave.drumslave.model.options.OptionMappingConfigManager;
import ca.digitalcave.drumslave.model.options.SettingsConfigManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConfigFactory {

	private static ConfigFactory configFactorySingleton = new ConfigFactory();
	private final static Logger logger = Logger.getLogger(ConfigFactory.class.getName()); 	
	
	public static ConfigFactory getInstance() {
		return configFactorySingleton;
	}
	
	public void loadConfig(ConfigType configType){
		
		File configFile = null;
		switch (configType) {
		case HARDWARE:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "hardware.xml");
			break;
			
		case LOGIC:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "logic.xml");
			break;
			
		case LOGIC_MAPPING:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "logic-mapping.xml");
			break;
			
		case SAMPLE_MAPPING:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "sample-mapping.xml");
			break;
			
		case OPTION_MAPPING:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "option-mapping.xml");
			break;
			
		case SETTINGS:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "settings.xml");
			break;
			
		case GAIN_MAPPING:
			configFile = OperatingSystemUtil.getUserFile("DrumSlave", "gain-mapping.xml");
			break;
		}
		
		if (configFile == null)
			throw new RuntimeException("Couldn't map config type " + configType);
		
		try {
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
					
				case LOGIC:
					new LogicConfigManager().loadFromConfig(c.getLogics());
					break;
					
				case LOGIC_MAPPING:
					new LogicMappingConfigManager().loadFromConfig(c.getLogicMappings());
					break;
					
				case SAMPLE_MAPPING:
					new SampleMappingConfigManager().loadFromConfig(c.getSampleMappingGroups());
					break;
					
				case OPTION_MAPPING:
					new OptionMappingConfigManager().loadFromConfig(c.getOptionMappings());
					break;
					
				case SETTINGS:
					new SettingsConfigManager().loadFromConfig(c.getSettings());
					break;
					
				case GAIN_MAPPING:
					new GainMappingConfigManager().loadFromConfig(c.getGainMappings());
					break;					
				}
			}
			else {
				throw new RuntimeException("Loaded config is not an instance of Config; it is " + o.getClass().getName());
			}
		}
		catch (IOException ioe){
			if (configType == ConfigType.LOGIC){
				logger.info("Couldn't find logic config file; adding known defaults.  You can manually edit the resulting logic.xml file if you want to add custom logic.");
				
				new Play("Play");
				new PlayHDR("Play HDR");
				new PlaySecondary("Play Secondary");
				new Mute("Mute");
				
				saveConfig(ConfigType.LOGIC);
			}
			else {
				logger.info("Configuration file " + configFile.getAbsolutePath() + " not found.");
			}
		}
	}
	
	public void saveConfig(ConfigType configType){		
		Config config = new Config();
		File saveFile = null;
		
		switch (configType) {
		case HARDWARE:
			config.setPads(new HardwareConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "hardware.xml");
			break;
			
		case LOGIC:
			config.setLogics(new LogicConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "logic.xml");
			break;
			
		case LOGIC_MAPPING:
			config.setLogicMappings(new LogicMappingConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "logic-mapping.xml");
			break;
			
		case SAMPLE_MAPPING:
			config.setSampleMappingGroups(new SampleMappingConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "sample-mapping.xml");
			break;
			
		case OPTION_MAPPING:
			config.setOptionMappings(new OptionMappingConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "option-mapping.xml");
			break;
			
		case SETTINGS:
			config.setSettings(new SettingsConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "settings.xml");
			break;

		case GAIN_MAPPING:
			config.setGainMappings(new GainMappingConfigManager().saveToConfig());
			saveFile = OperatingSystemUtil.getUserFile("DrumSlave", "gain-mapping.xml");
			break;
		}
		
		if (saveFile == null)
			throw new RuntimeException("Couldn't map config type " + configType);
		
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(Config.class);
		try {
			if (!saveFile.getParentFile().exists())
				if (!saveFile.getParentFile().mkdirs())
					logger.log(Level.SEVERE, "Error encountered while creating config directory");

			xstream.toXML(config, new FileOutputStream(saveFile));
		}
		catch (IOException ioe){
			logger.log(Level.SEVERE, "Unable to write config file", ioe);
		}
	}
	
	public enum ConfigType {
		HARDWARE,
		LOGIC,
		LOGIC_MAPPING,
		SAMPLE_MAPPING,
		OPTION_MAPPING,
		GAIN_MAPPING,
		SETTINGS,
	}
}
