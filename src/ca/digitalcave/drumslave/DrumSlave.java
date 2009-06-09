package ca.digitalcave.drumslave;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.homeunix.thecave.moss.swing.LookAndFeelUtil;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.Equalizer;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.serial.CommunicationsFactory;
import ca.digitalcave.drumslave.serial.ConsoleFactory;



public class DrumSlave {
	private static Logger logger = Logger.getLogger(DrumSlave.class.getName());

	public static void main(String[] args) throws Exception {
		
		//Load config from disk
		ConfigFactory.getInstance().loadConfig(ConfigType.HARDWARE, new File("etc/config/hardware.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC, new File("etc/config/logic.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.SAMPLE_MAPPING, new File("etc/config/sample-mappings.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC_MAPPING, new File("etc/config/logic-mappings.xml"));

		//Start the communications link, whether serial line or console, on its own thread
		Thread communicationsThread = new Thread(new Runnable(){
			public void run() {
				try {
					CommunicationsFactory commLink;
					commLink = new ConsoleFactory();
					//commLink = new SerialFactory("/dev/tty.usbserial-A200294u");
					commLink.connect();
				}
				catch (Exception e){
					logger.log(Level.SEVERE, "Problem encountered while reading communications line", e);
				}
			}
		});
		communicationsThread.start();
		
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
	}
}
