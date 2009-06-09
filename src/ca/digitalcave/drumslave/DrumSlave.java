package ca.digitalcave.drumslave;

import java.io.File;

import javax.swing.SwingUtilities;

import org.homeunix.thecave.moss.swing.LookAndFeelUtil;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.Equalizer;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.serial.SerialFactory;



public class DrumSlave {

	public static void main(String[] args) throws Exception {
		//Load config from disk
		ConfigFactory.getInstance().loadConfig(ConfigType.HARDWARE, new File("etc/config/hardware.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC, new File("etc/config/logic.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.SAMPLE_MAPPING, new File("etc/config/sample-mappings.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC_MAPPING, new File("etc/config/logic-mappings.xml"));

		//Initialize LnF
		LookAndFeelUtil.setLookAndFeel();
		
		//Start up GUI
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				try {
					new Equalizer().openWindow();
				}
				catch (WindowOpenException woe){}
			}
		});

		new SerialFactory().connect("/dev/tty.usbserial-A200294u");
//		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
//		String readLine;
//		while ((readLine = console.readLine()) != null){
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
