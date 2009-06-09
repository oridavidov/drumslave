package ca.digitalcave.drumslave.serial;

import ca.digitalcave.drumslave.model.hardware.Zone;

public class DrumSignal {

	public static void signal(String command){
		String[] signal = command.trim().split(":");
		if (signal.length == 2){
			float volume = Integer.parseInt(signal[1]) / 1024f;
			System.out.println(signal[0] + ":" + volume);
			Zone z = Zone.getZone(Integer.parseInt(signal[0]));
			if (z != null)
				z.play(volume);
		}
	}
}
