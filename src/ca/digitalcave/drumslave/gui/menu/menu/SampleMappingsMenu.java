/*
 * Created on Aug 7, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		List<String> sampleGroups = new ArrayList<String>(SampleMapping.getSampleGroups());
		Collections.sort(sampleGroups);

		for (String sampleGroup : sampleGroups) {
			this.add(new SwitchToSampleGroup(getFrame(), sampleGroup));
		}
		
		super.updateMenus();
	}
}
