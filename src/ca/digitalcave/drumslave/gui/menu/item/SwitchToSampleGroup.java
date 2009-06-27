/*
 * Created on Aug 6, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.item;

import java.awt.event.ActionEvent;

import org.homeunix.thecave.moss.swing.MossCheckboxMenuItem;
import org.homeunix.thecave.moss.swing.MossFrame;

import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class SwitchToSampleGroup extends MossCheckboxMenuItem {
	public static final long serialVersionUID = 0;

	private final String sampleGroup;
	
	public SwitchToSampleGroup(MossFrame frame, String sampleGroup) {
		super(frame, sampleGroup);
		this.sampleGroup = sampleGroup;
		this.setSelected(this.sampleGroup.equals(SampleMapping.getSelectedSampleGroup()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SampleMapping.setSelectedSampleGroup(sampleGroup);
		getFrame().updateContent();
		ConfigFactory.getInstance().saveConfig(ConfigType.SETTINGS);
	}
	
	@Override
	public void updateMenus() {
		super.updateMenus();

		this.setSelected(this.sampleGroup.equals(SampleMapping.getSelectedSampleGroup()));
	}
}
