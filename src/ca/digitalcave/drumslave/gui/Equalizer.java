package ca.digitalcave.drumslave.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.config.hardware.HardwareEditor;
import ca.digitalcave.drumslave.gui.menu.DrumSlaveMenuBar;
import ca.digitalcave.drumslave.model.hardware.Pad;

/**
 * Main Drum Slave window, showing current volume of each pad 
 * and volume adjustment knobs, etc. 
 * @author wyatt
 *
 */
public class Equalizer extends MossFrame {
	public static final long serialVersionUID = 0l;
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final List<PadEQChannel> eqChannels = new ArrayList<PadEQChannel>();
	
	@Override
	public void init() {
		super.init();
		
		this.setDocumentBasedApplication(false); //On OSX, close when the last window closes
		this.setJMenuBar(new DrumSlaveMenuBar(this));
		
		this.setLayout(new GridLayout(1, 0));
		this.setResizable(false);
		this.setTitle("Drum Slave");
	}
	
	@Override
	public void updateContent() {
		super.updateContent();
		
		//Verify that we have some hardware defined.
		if (Pad.getPads().size() == 0){
			try {
				JOptionPane.showMessageDialog(this, "No hardware mappings have been defined.  Please do this now.", "Hardware Map Needed", JOptionPane.INFORMATION_MESSAGE);
				new HardwareEditor(this).openWindow();
				if (Pad.getPads().size() == 0){
					JOptionPane.showMessageDialog(this, "I still cannot find any hardware mappings.  You must configure your hardware before you can run Drum Slave.  Exiting.", "Hardware Map Needed", JOptionPane.WARNING_MESSAGE);
					System.exit(0);
				}
			} 
			catch (WindowOpenException woe) {
				logger.log(Level.SEVERE, "Error opening Hardware Editor dialog", woe);
			}
		}
		
		for (PadEQChannel channel : eqChannels) {
			this.remove(channel);
		}
		eqChannels.clear();
		
		List<Pad> pads = new ArrayList<Pad>(Pad.getPads());
		Collections.sort(pads, new Comparator<Pad>(){
			public int compare(Pad arg0, Pad arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		
		for (Pad pad : pads){
			PadEQChannel eqChannel = new PadEQChannel(pad);
			eqChannels.add(eqChannel);
			this.add(eqChannel);
		}
		
		pack();
	}
}
