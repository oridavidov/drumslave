/*
 * Created on Aug 6, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.item;

import java.awt.event.ActionEvent;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenuItem;

import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class SwitchToSampleGroup extends MossMenuItem{
	public static final long serialVersionUID = 0;

	private final String sampleGroup;
	
	public SwitchToSampleGroup(MossFrame frame, String sampleGroup) {
		super(frame, sampleGroup);
		this.sampleGroup = sampleGroup;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SampleMapping.setSelectedSampleGroup(sampleGroup);
		getFrame().updateContent();
		ConfigFactory.getInstance().saveConfig(ConfigType.SETTINGS);
	}
}
