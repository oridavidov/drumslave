/*
 * Created on Aug 7, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenu;

public class EditMenu extends MossMenu {
	public static final long serialVersionUID = 0;
	
	public EditMenu(MossFrame frame) {
		super(frame, "Edit");
		
	}
	
	@Override
	public void updateMenus() {
		this.removeAll();

		this.add(new EditHardware(getFrame()));
		this.add(new EditLogicMappings(getFrame()));
		this.add(new EditSampleMappings(getFrame()));
		
		super.updateMenus();
	}
}
