package ca.digitalcave.drumslave.gui.widget;

import java.awt.Color;

import javax.swing.JLabel;

public class VUMeter extends JLabel {
	public static final long serialVersionUID = 0l;

	public VUMeter() {
		this.setBackground(Color.BLACK);
		this.setOpaque(true);
	}
	
	public void setValue(float value){
		this.setText(value + "f");
	}
}
