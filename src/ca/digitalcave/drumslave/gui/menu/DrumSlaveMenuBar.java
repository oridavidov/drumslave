package ca.digitalcave.drumslave.gui.menu;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenuBar;

import ca.digitalcave.drumslave.gui.menu.menu.EditMenu;
import ca.digitalcave.drumslave.gui.menu.menu.SampleMappingsMenu;

public class DrumSlaveMenuBar extends MossMenuBar {
	public static final long serialVersionUID = 0;

	public DrumSlaveMenuBar(MossFrame frame) {
		super(frame);
		
		this.add(new EditMenu(getFrame()));
		this.add(new SampleMappingsMenu(getFrame()));		
	}
	
	@Override
	public void updateMenus() {
//		this.removeAll();
//		
//		this.add(new EditMenu(getFrame()));
//		this.add(new SampleMappingsMenu(getFrame()));
		
		super.updateMenus();
	}
}
