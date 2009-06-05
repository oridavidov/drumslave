package ca.digitalcave.drumslave.serial;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;

import ca.digitalcave.drumslave.hardware.Zone;

/**
 * Handles the input coming from the serial port. A new line character
 * is treated as the end of a block in this example. 
 */
public class SerialReader implements SerialPortEventListener 
{
	private InputStream in;
	private byte[] buffer = new byte[1024];

	public SerialReader ( InputStream in )
	{
		this.in = in;
	}

	public void serialEvent(SerialPortEvent arg0) {
		int data;

		try {
			int len = 0;
			while ((data = in.read()) > -1) {
				if (data == '\n') {
					break;
				}
				buffer[len++] = (byte) data;
			}

			String[] signal = new String(buffer,0,len).trim().split(":");
			if (signal.length == 2){
				float volume = Integer.parseInt(signal[1]) / 1024f;
				System.out.println(signal[0] + ":" + volume);
				Zone z = Zone.getZone(Integer.parseInt(signal[0]));
				System.out.println(z);
				z.play(volume);
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}             
	}

}