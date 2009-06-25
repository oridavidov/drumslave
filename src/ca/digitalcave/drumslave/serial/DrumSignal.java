package ca.digitalcave.drumslave.serial;

import ca.digitalcave.drumslave.model.hardware.Zone;

public class DrumSignal implements Runnable {
	public static final long serialVersionUID = 0;

	//	public static final Executor threadPool = new ThreadPoolExecutor(5, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private final String command;

	public DrumSignal(String command) {
		this.command = command;
	}

	public void run() {
		if (command == null)
			return;

		System.out.println(command);
		String[] commands = command.split(";");
		for (String command : commands) {
			String[] signal = command.trim().split(":");
			if (signal.length == 2){
				Zone z = Zone.getZone(Integer.parseInt(signal[0]));
				float rawVelocity = Integer.parseInt(signal[1]);

				//If the channel is digital, use raw value; otherwise, divide by max value.
				float volume;
				if (z.getChannel() >= 32)
					volume = rawVelocity;
				else 
					volume = rawVelocity / 1024;
				
				if (z != null){
					z.play(volume);
				}
			}			
		}
	}
}
