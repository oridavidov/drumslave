/*
 * Created on Aug 7, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.menu;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.KeyStroke;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenu;

import ca.digitalcave.drumslave.gui.menu.item.SwitchToSampleGroup;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class SampleMappingsMenu extends MossMenu {
	public static final long serialVersionUID = 0;
	
	public SampleMappingsMenu(MossFrame frame) {
		super(frame, "Sample Groups");
		this.setMnemonic('S');
	}
	
	@Override
	public void updateMenus() {
		this.removeAll();
		List<String> sampleGroups = new ArrayList<String>(SampleMapping.getSampleGroups());
		Collections.sort(sampleGroups);

		int count = 0;
		for (String sampleGroup : sampleGroups) {
			SwitchToSampleGroup group = new SwitchToSampleGroup(getFrame(), sampleGroup);
			group.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + count, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			count++;
			this.add(group);
		}
		
		super.updateMenus();
	}
}
