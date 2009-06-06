package ca.digitalcave.drumslave.gui.util;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class Formatter {
	public static Dimension getComponentSize(JComponent component, int minWidth){
		return new Dimension(Math.max(minWidth, component.getPreferredSize().width), Math.max(component.getPreferredSize().height, component.getSize().height));
	}

	public static Dimension getTextFieldSize(JTextField textField){
		return getComponentSize(textField, 125);
	}

	public static Dimension getButtonSize(JButton button){
		return getComponentSize(button, 100);
	}
}
