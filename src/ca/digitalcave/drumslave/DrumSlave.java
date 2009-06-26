package ca.digitalcave.drumslave;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.homeunix.thecave.moss.common.LogUtil;
import org.homeunix.thecave.moss.common.OperatingSystemUtil;
import org.homeunix.thecave.moss.common.ParseCommands;
import org.homeunix.thecave.moss.common.ParseCommands.ParseResults;
import org.homeunix.thecave.moss.common.ParseCommands.ParseVariable;
import org.homeunix.thecave.moss.swing.LookAndFeelUtil;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.Equalizer;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.serial.CommunicationsFactory;
import ca.digitalcave.drumslave.serial.SerialFactory;



public class DrumSlave {
	private static Logger logger = Logger.getLogger(DrumSlave.class.getName());

	private static File samplesFolder = null; 
	private static boolean showConsole = false;

	public static void main(String[] args) {
		try {
			//Parse command line options, and print usage if needed.
			List<ParseVariable> variables = new LinkedList<ParseVariable>();
			variables.add(new ParseVariable("--keyboard", Boolean.class, false));
			variables.add(new ParseVariable("--console", Boolean.class, false));
			variables.add(new ParseVariable("--log-level", String.class, false));
			variables.add(new ParseVariable("--sample-folder", String.class, false));

			String help = "USAGE: java -jar Buddi.jar <options> <data file>, where options include:\n"
				+ "\t--keyboard\t\tUse keyboard for testing, instead of serial port\n"
				+ "\t--console\t\tDo not launch the GUI\n"
				+ "\t--log-level\tLEVEL\tSet log level: [SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST], default INFO\n";
			ParseResults results = ParseCommands.parse(args, help, variables);

			showConsole = results.getBoolean("--keyboard");

			LogUtil.setLogLevel(results.getString("--log-level"));

			if (results.getString("--sample-folder") != null){
				samplesFolder = new File(results.getString("--sample-folder"));
			}


			//Load config from disk; first we want hardware, so that we can init GUI
			ConfigFactory.getInstance().loadConfig(ConfigType.HARDWARE);
			ConfigFactory.getInstance().loadConfig(ConfigType.SETTINGS);
			ConfigFactory.getInstance().loadConfig(ConfigType.GAIN_MAPPING);

			if (!results.getBoolean("--console")){
				//Initialize LnF and start up GUI
				LookAndFeelUtil.setLookAndFeel();
				SwingUtilities.invokeLater(new GuiRunner());
			}

			//Then we load the rest of the config.  Loading Samples, in particular,
			// before the GUI is initialized tends to hang the program.
			ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC);
			ConfigFactory.getInstance().loadConfig(ConfigType.SAMPLE_MAPPING);
			ConfigFactory.getInstance().loadConfig(ConfigType.LOGIC_MAPPING);
			ConfigFactory.getInstance().loadConfig(ConfigType.OPTION_MAPPING);

			//Start the serial communications link if we have not specified keyboard input
			if (!showConsole){
				Thread communicationsThread = new Thread(new CommunicationsRunner(), "Communications");
				communicationsThread.start();
			}
		}
		catch (Throwable t){
			logger.log(Level.SEVERE, "Error launching Drum Slave", t);
			JOptionPane.showMessageDialog(null, "Error launching Drum Slave: " + t.getMessage());
		}
	}

	public static File getSamplesFolder(){
		if (samplesFolder == null)
			return (OperatingSystemUtil.getUserFile("DrumSlave", "samples"));
		return samplesFolder;
	}

	private static class CommunicationsRunner implements Runnable {
		public static final long serialVersionUID = 0;

		public void run() {
			try {
				CommunicationsFactory commLink;
				//commLink = new SerialFactory("/dev/tty.usbserial-FTE0U36U");
				commLink = new SerialFactory("/dev/tty.usbserial-A200294u");
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
				new Equalizer(showConsole).openWindow();
			}
			catch (WindowOpenException woe){
				woe.printStackTrace();
			}
		}
	}
}
