package ca.digitalcave.drumslave.gui.config.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class SampleNamesComboBoxModel extends DefaultComboBoxModel {
	public static final long serialVersionUID = 0l;

	public SampleNamesComboBoxModel() {
		updateModel();
	}
	
	public void updateModel(){
		this.removeAllElements();
		
		List<String> sampleGroupNames = new ArrayList<String>(SampleMapping.getSampleGroups());
		Collections.sort(sampleGroupNames);
		
		for (String sampleGroupName : sampleGroupNames) {
			this.addElement(sampleGroupName);
		}
	}
}
