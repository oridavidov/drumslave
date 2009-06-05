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

	private final static Logger logger = Logger.getLogger(ConfigFactory.class.getName()); 
	
	public static void loadConfig(){
		try {
			//TODO Change this to store config in different place
			InputStream is = new FileInputStream(new File("etc/config.xml"));

			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(Config.class);
			Object o = xstream.fromXML(is);
			if (o instanceof Config){
				Config c = (Config) o;
				new HardwareConfigManager().loadFromConfig(c.getPads());
				new SampleConfigManager().loadFromConfig(c.getSamples());
				new LogicConfigManager().loadFromConfig(c.getLogics());
				new LogicMappingConfigManager().loadFromConfig(c.getLogicMappings());
				new SampleMappingConfigManager().loadFromConfig(c.getSampleMappings());
			}
			else {
				throw new RuntimeException("Loaded config is not an instance of Config; it is " + o.getClass().getName());
			}
		}
		catch (IOException ioe){
			logger.log(Level.WARNING, "Error encountered while loading config file", ioe);
		}
	}
	
	public static void saveConfig(){
		Config config = new Config();
		config.setPads(new HardwareConfigManager().saveToConfig());
		config.setSamples(new SampleConfigManager().saveToConfig());
		config.setLogics(new LogicConfigManager().saveToConfig());
		config.setLogicMappings(new LogicMappingConfigManager().saveToConfig());
		config.setSampleMappings(new SampleMappingConfigManager().saveToConfig());
		
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(Config.class);
		xstream.toXML(config, System.out);
	}
}
