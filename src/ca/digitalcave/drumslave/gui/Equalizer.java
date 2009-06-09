package ca.digitalcave.drumslave.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.homeunix.thecave.moss.swing.MossFrame;

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
	
	private final List<PadEQChannel> eqChannels = new ArrayList<PadEQChannel>();
	
	@Override
	public void init() {
		super.init();
		
		this.setJMenuBar(new DrumSlaveMenuBar(this));
		
		this.setLayout(new GridLayout(1, 0));
		this.setResizable(false);
		this.setTitle("Drum Slave");
	}
	
	@Override
	public void updateContent() {
		super.updateContent();
		
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
