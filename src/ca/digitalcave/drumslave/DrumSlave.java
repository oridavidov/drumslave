package ca.digitalcave.drumslave;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.hardware.Zone;


public class DrumSlave {

	public static void main(String[] args) throws Exception {
		//Load config from disk
		ConfigFactory.loadConfig();
		//Save is not yet implemented; currently it prints out loaded config to console for verification
		ConfigFactory.saveConfig();


//		new SerialFactory().connect("/dev/tty.usbserial-A200294u");
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String readLine;
		while ((readLine = console.readLine()) != null){
			String[] signal = readLine.trim().split(":");
			if (signal.length == 2){
				float volume = Integer.parseInt(signal[1]) / 1024f;
				System.out.println(signal[0] + ":" + volume);
				Zone z = Zone.getZone(Integer.parseInt(signal[0]));
				System.out.println(z);
				z.play(volume);
			}
		}
	}
}
