/*
 * Created on Aug 7, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.menu;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenu;

import ca.digitalcave.drumslave.gui.menu.item.SwitchToSampleGroup;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class SampleMappingsMenu extends MossMenu {
	public static final long serialVersionUID = 0;
	
	public SampleMappingsMenu(MossFrame frame) {
		super(frame, "Sample Groups");
		
	}
	
	@Override
	public void updateMenus() {
		this.removeAll();

		for (String sampleGroup : SampleMapping.getSampleGroups()) {
			this.add(new SwitchToSampleGroup(getFrame(), sampleGroup));
		}
		
		super.updateMenus();
	}
}
