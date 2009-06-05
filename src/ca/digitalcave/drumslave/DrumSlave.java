package ca.digitalcave.drumslave;

import ca.digitalcave.drumslave.config.SampleManager;
import ca.digitalcave.drumslave.hardware.HardwareManager;
import ca.digitalcave.drumslave.hardware.Pad;
import ca.digitalcave.drumslave.hardware.Zone;


public class DrumSlave {

	public static void main(String[] args) throws Exception {
		
		HardwareManager.loadHardware();
		SampleManager.loadSampleMappings();
		
//		new SerialFactory().connect("/dev/tty.usbserial-A200294u");
		

		Zone.getZone(32).play(0.25f);
		
		System.out.println(Pad.getPad("Ride"));
	}
}
