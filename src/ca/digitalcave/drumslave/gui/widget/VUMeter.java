package ca.digitalcave.drumslave.gui.widget;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

public class VUMeter extends JLabel {
	public static final long serialVersionUID = 0l;

	private static final Color BRIGHT_RED = new Color(0xFF, 0x0, 0x0);
	private static final Color DARK_RED = new Color(0x7F, 0x0, 0x0);
	private static final Color BRIGHT_YELLOW = new Color(0xFF, 0xFF, 0x0);
	private static final Color DARK_YELLOW = new Color(0x7F, 0x7F, 0x0);
	private static final Color BRIGHT_GREEN = new Color(0x0, 0xFF, 0x0);
	private static final Color DARK_GREEN = new Color(0x0, 0x7F, 0x0);
	
	private static final int ledCount = 20;
	
	private float value;
	
	public void setValue(float value){
		this.value = value;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		int ledHeight = this.getHeight() / ledCount;
		for (int i = 0; i < ledCount; i++) {
			float cutoff = (float) i / ledCount;
			if (cutoff < 0.5f){
				if (this.value > cutoff)
					g.setColor(BRIGHT_GREEN);
				else
					g.setColor(DARK_GREEN);
			}
			else if (cutoff < 0.8f) {
				if (this.value > cutoff)
					g.setColor(BRIGHT_YELLOW);
				else
					g.setColor(DARK_YELLOW);			}
			else { 
				if (this.value > cutoff)
					g.setColor(BRIGHT_RED);
				else
					g.setColor(DARK_RED);
			}
			
			g.fillRect(1, 1 + this.getHeight() - (ledHeight * (i + 1)), this.getWidth() - 2, ledHeight - 2);
		}
	}
}
