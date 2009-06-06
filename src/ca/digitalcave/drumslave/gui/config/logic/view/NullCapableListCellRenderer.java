package ca.digitalcave.drumslave.gui.config.logic.view;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class NullCapableListCellRenderer extends DefaultListCellRenderer {
	public static final long serialVersionUID = 0l;
	
	@Override
	public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
		super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
		
		if (arg1 == null)
			this.setText("<Unused>");
		
		return this;
	}

	
}
