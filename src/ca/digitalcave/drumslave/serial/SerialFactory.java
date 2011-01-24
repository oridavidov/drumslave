package ca.digitalcave.drumslave.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This version of the TwoWaySerialComm example makes use of the 
 * SerialPortEventListener to avoid polling.
 *
 */
public class SerialFactory implements CommunicationsFactory {

	private final String portName;
	
	public SerialFactory(String portName) {
		this.portName = portName;
	}
	
	public void connect() throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error: Port is currently in use");
		}
		else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				InputStream in = serialPort.getInputStream();

				serialPort.addEventListener(new SerialReader(in));
				serialPort.notifyOnDataAvailable(true);

			}
			else {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Only serial ports are handled by this program.");
			}
		}     
	}

	/**
	 * Handles the input coming from the serial port. A new line character
	 * is treated as the end of a block in this example. 
	 */
	public class SerialReader implements SerialPortEventListener {
		private InputStream in;
		private int[] buffer = new int[3];
		
		public SerialReader (InputStream in){
			this.in = in;
		}

		public void serialEvent(SerialPortEvent arg0) {
			int data;

			try {
				while ((data = in.read()) > -1) {
					if ((data & 0xF0) == 0xF0) {
						buffer[0] = data;
						buffer[1] = in.read();
						buffer[2] = in.read();
						
						DrumSignal.threadPool.execute(new DrumSignal(buffer));		
					}
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}             
		}
	}
}