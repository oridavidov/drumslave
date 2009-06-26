package ca.digitalcave.drumslave.serial;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleFactory implements CommunicationsFactory {

	public void connect() throws Exception {
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String command;
		while ((command = console.readLine()) != null){
			DrumSignal.threadPool.execute(new DrumSignal(command));
//			new DrumSignal(command).run();
		}
	}
}
