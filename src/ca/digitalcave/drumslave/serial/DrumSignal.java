package ca.digitalcave.drumslave.serial;

import ca.digitalcave.drumslave.model.hardware.Zone;

public class DrumSignal {

	public static void signal(String command){
		String[] signal = command.trim().split(":");
		if (signal.length == 2){
			float rawVelocity = Integer.parseInt(signal[1]);
//			System.out.println(signal[0] + ":" + rawVelocity);
			rawVelocity = (float) (Math.log10(rawVelocity) / 3); //==log_1000(rawVelocity), since 6.9 ~= log(1000)
//			System.out.println(signal[0] + ":" + rawVelocity);
			float volume = rawVelocity;
			Zone z = Zone.getZone(Integer.parseInt(signal[0]));
			if (z != null)
				z.play(volume);
		}
	}
}
