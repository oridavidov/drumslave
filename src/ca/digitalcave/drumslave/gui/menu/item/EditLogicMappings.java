/*
 * Created on Aug 6, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.item;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenuItem;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.config.logic.LogicEditor;

public class EditLogicMappings extends MossMenuItem{
	public static final long serialVersionUID = 0;

	public EditLogicMappings(MossFrame frame) {
		super(frame, "Edit Logic Mappings");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			new LogicEditor(null).openWindow();
			getFrame().updateContent();
		} 
		catch (WindowOpenException woe) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Problem opening Logic editor", woe);
		}
	}
}
