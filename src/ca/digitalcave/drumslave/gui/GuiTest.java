package ca.digitalcave.drumslave.gui;

import java.io.File;

import ca.digitalcave.drumslave.gui.config.sample.SampleEditor;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;

public class GuiTest {

	public static void main(String[] args) throws Exception {
		ConfigFactory.getInstance().loadConfig(ConfigType.HARDWARE, new File("etc/config/hardware.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC, new File("etc/config/logic.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.SAMPLE_MAPPING, new File("etc/config/sample-mappings.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC_MAPPING, new File("etc/config/logic-mappings.xml"));
		
//		new LogicEditor(null).openWindow();
		new SampleEditor(null).openWindow();
//		new Equalizer().openWindow();
		
//		ConfigFactory.getInstance().saveConfig(ConfigType.LOGIC_MAPPING, null);
	}
}
