/*
 * Created on Aug 6, 2007 by wyatt
 */
package ca.digitalcave.drumslave.gui.menu.item;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenuItem;
import org.homeunix.thecave.moss.swing.exception.WindowOpenException;

import ca.digitalcave.drumslave.gui.config.hardware.HardwareEditor;

public class EditHardware extends MossMenuItem{
	public static final long serialVersionUID = 0;

	public EditHardware(MossFrame frame) {
		super(frame, "Edit Hardware");
		this.setMnemonic('H');
		this.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			new HardwareEditor(null).openWindow();
			getFrame().updateContent();
		} 
		catch (WindowOpenException woe) {
			Logger logger = Logger.getLogger(this.getClass().getName());
			logger.log(Level.SEVERE, "Problem opening Hardware editor", woe);
		}
	}
}
