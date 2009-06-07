package ca.digitalcave.drumslave.gui.config.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import ca.digitalcave.drumslave.model.logic.Logic;

public class LogicsComboBoxModel extends DefaultComboBoxModel {
	public static final long serialVersionUID = 0l;

	public LogicsComboBoxModel() {
		List<Logic> logics = new ArrayList<Logic>(Logic.getLogics());
		Collections.sort(logics, new Comparator<Logic>(){
			public int compare(Logic arg0, Logic arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		
		this.addElement(null);
		for (Logic logic : logics) {
			this.addElement(logic.getName());
		}
	}
}
