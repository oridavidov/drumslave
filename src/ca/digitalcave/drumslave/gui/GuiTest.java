package ca.digitalcave.drumslave.gui;

import java.io.File;

import ca.digitalcave.drumslave.gui.config.hardware.HardwareEditor;
import ca.digitalcave.drumslave.gui.config.logic.LogicEditor;
import ca.digitalcave.drumslave.gui.config.sample.SampleEditor;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;

public class GuiTest {

	public static void main(String[] args) throws Exception {
		ConfigFactory.getInstance().loadConfig(ConfigType.HARDWARE, new File("etc/config/hardware.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC, new File("etc/config/logic.xml"));
//		ConfigFactory.getInstance().loadConfig(ConfigType.SAMPLE_MAPPING, new File("etc/config/sample-mappings.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC_MAPPING, new File("etc/config/logic-mappings.xml"));
		
		new HardwareEditor(null).openWindow();
		new LogicEditor(null).openWindow();
		new SampleEditor(null).openWindow();
		new Equalizer().openWindow();
		
//		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
//		String readLine;
//		while ((readLine = console.readLine()) != null){
////			Zone.getCommandQueue().put(readLine);
//			String[] signal = readLine.trim().split(":");
//			if (signal.length == 2){
//				float volume = Integer.parseInt(signal[1]) / 1024f;
//				System.out.println(signal[0] + ":" + volume);
//				Zone z = Zone.getZone(Integer.parseInt(signal[0]));
//				System.out.println(z);
//				if (z != null)
//					z.play(volume);
//			}
//		}
	}
}
