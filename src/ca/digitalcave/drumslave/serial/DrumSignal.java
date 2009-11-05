package ca.digitalcave.drumslave.serial;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.logic.LogicDelegate;

public class DrumSignal implements Runnable {
	public static final long serialVersionUID = 0;

	public static final Executor threadPool = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private final String command;

	public DrumSignal(String command) {
		this.command = command;
	}

	public void run() {
		if (command == null)
			return;

		Map<Pad, List<PlayedZone>> padCommands = new ConcurrentHashMap<Pad, List<PlayedZone>>();
		
		String commandToPrint = command.replaceAll("24:[0-9]+;", "").trim();
		if (commandToPrint.length() > 0)
			System.out.println(commandToPrint);
		String[] commands = command.split(";");
		for (String command : commands) {
			String[] signal = command.trim().split(":");
			if (signal.length == 2){
				Zone z = Zone.getZone(Integer.parseInt(signal[0]));
				float rawVelocity = Integer.parseInt(signal[1]);

				//If the channel is digital, use raw value; otherwise, divide by max value.
				if (z != null){
					float volume;
					if (z.getChannel() >= 32)
						volume = rawVelocity;
					else 
						volume = rawVelocity / 1024;

					//System.out.println(z.getChannel() + ":" + volume);
					//z.play(volume);
					if (padCommands.get(z.getPad()) == null)
						padCommands.put(z.getPad(), new ArrayList<PlayedZone>());
					padCommands.get(z.getPad()).add(new PlayedZone(z, volume));
				}
			}
		}
		
		//Once we have gone through all commands, we will pass
		// each set of PlayedZones to the logic delegate, which will
		// then determine which zones to play.
		for (Pad pad : padCommands.keySet()) {
			LogicDelegate.chooseZoneToPlay(padCommands.get(pad));
		}
	}
}
