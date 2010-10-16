package ca.digitalcave.drumslave.serial;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.logic.LogicDelegate;

public class DrumSignal implements Runnable {
	public static final long serialVersionUID = 0;

	public static final Executor threadPool = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private final int[] command;

	public DrumSignal(int[] command) {
		this.command = command;
	}

	public void run() {
		if (command == null || command.length < 3)
			return;
		
		//Verify checksum
		int checksum = 0x0;
		for (int i = 0; i < 3; i++){
			checksum ^= command[i] >> 4;
			checksum ^= command[i] & 0xF;
		}
		if (checksum != 0x0){
			Logger.getLogger(this.getClass().getName()).warning("Error: checksum does not verify.  Packet: " + 
					Integer.toHexString(command[0]) + " " + 
					Integer.toHexString(command[1]) + " " + 
					Integer.toHexString(command[2]));
			return;
		}

		Map<Pad, List<PlayedZone>> padCommands = new ConcurrentHashMap<Pad, List<PlayedZone>>();
		
		int channel = ((command[0] & 0xF) << 2) | (command[1] >> 6);
		int rawVelocity = ((command[1] & 0x3F) << 4) | (command[2] >> 4);
		
		Zone z = Zone.getZone(channel);

		//If the channel is digital, use raw value; otherwise, divide by max value.
		if (z != null){
			float volume;
			if (z.getChannel() >= 32)
				volume = rawVelocity;
			else 
				volume = rawVelocity / 1024f;

			//System.out.println(z.getChannel() + ":" + volume);
			z.play(volume);
			if (padCommands.get(z.getPad()) == null)
				padCommands.put(z.getPad(), new ArrayList<PlayedZone>());
			padCommands.get(z.getPad()).add(new PlayedZone(z, volume));
		}
		
		//Once we have gone through all commands, we will pass
		// each set of PlayedZones to the logic delegate, which will
		// then determine which zones to play.
		for (Pad pad : padCommands.keySet()) {
			LogicDelegate.chooseZoneToPlay(padCommands.get(pad));
		}
	}
}
