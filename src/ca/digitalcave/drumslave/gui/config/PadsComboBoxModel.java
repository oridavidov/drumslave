package ca.digitalcave.drumslave.gui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import ca.digitalcave.drumslave.model.hardware.Pad;

public class PadsComboBoxModel extends DefaultComboBoxModel {
	public static final long serialVersionUID = 0l;

	public PadsComboBoxModel() {
		List<Pad> pads = new ArrayList<Pad>(Pad.getPads());
		Collections.sort(pads, new Comparator<Pad>(){
			public int compare(Pad arg0, Pad arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		
		for (Pad pad : pads) {
			this.addElement(pad.getName());
		}
	}
}
