package ca.digitalcave.drumslave.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;

/**
 * This version of the TwoWaySerialComm example makes use of the 
 * SerialPortEventListener to avoid polling.
 *
 */
public class SerialFactory {
    
    public void connect (String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        }
        else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
            
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                
                serialPort.addEventListener(new SerialReader(in));
                serialPort.notifyOnDataAvailable(true);

            }
            else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    public static void main (String[] args) throws Exception {
    	new SerialFactory().connect("/dev/tty.usbserial-A200294u");
    }
}