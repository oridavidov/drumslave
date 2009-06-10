package ca.digitalcave.drumslave.serial;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ca.digitalcave.drumslave.model.hardware.Zone;

public class DrumSignal implements Runnable {
	public static final long serialVersionUID = 0;

	public static final Executor threadPool = new ThreadPoolExecutor(5, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private final String command;

	public DrumSignal(String command) {
		this.command = command;
	}

	public void run() {
		if (command == null)
			return;
		
		String[] signal = command.trim().split(":");
		if (signal.length == 2){
			float rawVelocity = Integer.parseInt(signal[1]);
			//System.out.println(signal[0] + ":" + rawVelocity);
//			rawVelocity = (float) (Math.log10(rawVelocity) / 3); //==log_1000(rawVelocity), since 6.9 ~= log(1000)
			//System.out.println(signal[0] + ":" + rawVelocity);
			float volume = rawVelocity / 1024;
			Zone z = Zone.getZone(Integer.parseInt(signal[0]));
			if (z != null)
				z.play(volume);
		}
	}
}
