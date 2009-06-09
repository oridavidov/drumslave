package ca.digitalcave.drumslave.serial;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleFactory implements CommunicationsFactory {

	public void connect() throws Exception {
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String readLine;
		while ((readLine = console.readLine()) != null){
			DrumSignal.signal(readLine);
		}
	}
}
