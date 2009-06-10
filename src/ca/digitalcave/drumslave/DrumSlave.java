package ca.digitalcave.drumslave;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.homeunix.thecave.moss.common.LogUtil;
import org.homeunix.thecave.moss.swing.LookAndFeelUtil;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.Equalizer;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.serial.CommunicationsFactory;
import ca.digitalcave.drumslave.serial.ConsoleFactory;
import ca.digitalcave.drumslave.serial.SerialFactory;



public class DrumSlave {
	private static Logger logger = Logger.getLogger(DrumSlave.class.getName());

	public static void main(String[] args) throws Exception {
		LogUtil.setLogLevel("ca.digitalcave", "FINEST", Level.INFO);
		
		//Load config from disk; first we want hardware, so that we can init GUI
		ConfigFactory.getInstance().loadConfig(ConfigType.HARDWARE, new File("etc/config/hardware.xml"));

		//Initialize LnF and start up GUI
		LookAndFeelUtil.setLookAndFeel();
		SwingUtilities.invokeLater(new GuiRunner());
		
		//Then we load the rest of the config.  Loading Samples, in particular,
		// before the GUI is initialized tends to hang the program.
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC, new File("etc/config/logic.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.SAMPLE_MAPPING, new File("etc/config/sample-mappings.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC_MAPPING, new File("etc/config/logic-mappings.xml"));
		ConfigFactory.getInstance().loadConfig(ConfigType.OPTION_MAPPING, new File("etc/config/option-mappings.xml"));

		//Start the communications link, whether serial line or console, on its own thread
		Thread communicationsThread = new Thread(new CommunicationsRunner(false), "Communications");
		communicationsThread.start();
	}

	private static class CommunicationsRunner implements Runnable {
		public static final long serialVersionUID = 0;
		
		private final boolean useSerialLink;
		
		public CommunicationsRunner(boolean useSerialLink) {
			this.useSerialLink = useSerialLink;
		}
		
		public void run() {
			try {
				CommunicationsFactory commLink;
				if (useSerialLink)
					commLink = new SerialFactory("/dev/tty.usbserial-A200294u");
				else
					commLink = new ConsoleFactory();
				commLink.connect();
			}
			catch (Exception e){
				logger.log(Level.SEVERE, "Problem encountered while reading communications line", e);
			}
		}
	}
	
	private static class GuiRunner implements Runnable {
		public static final long serialVersionUID = 0;
		public void run() {
			try {
				new Equalizer().openWindow();
			}
			catch (WindowOpenException woe){
				woe.printStackTrace();
			}
		}
	}
}
