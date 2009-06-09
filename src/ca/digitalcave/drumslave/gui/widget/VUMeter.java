package ca.digitalcave.drumslave.gui.widget;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

public class VUMeter extends JLabel {
	public static final long serialVersionUID = 0l;

	private static final Color BRIGHT_RED = new Color(0xFF, 0x0, 0x0);
	private static final Color DARK_RED = new Color(0x5F, 0x0, 0x0);
	private static final Color BRIGHT_YELLOW = new Color(0xFF, 0xFF, 0x0);
	private static final Color DARK_YELLOW = new Color(0x5F, 0x5F, 0x0);
	private static final Color BRIGHT_GREEN = new Color(0x0, 0xFF, 0x0);
	private static final Color DARK_GREEN = new Color(0x0, 0x5F, 0x0);

	private float value;
	private float maxValue;
	private float maxValueFalloff;

	public void setValue(float value){
		this.value = value;
		repaint();
	}
	
	public void updateFalloff(){
		if (value > maxValue){
			maxValue = value;
		}
		
		if (maxValue - 0.1 < value && value > 0.2f){
			//We don't start to fall off until we get more than 1/10 away from peak
			maxValueFalloff = 0;
		}
		else {
			if (maxValueFalloff < 0.001f)
				maxValueFalloff = 0.001f;
			
			if (maxValue - 0.2 < value){
				maxValueFalloff *= 1.1f;
				maxValue -= maxValueFalloff;
			}
			else {
				maxValueFalloff *= 1.6;			
				maxValue -= maxValueFalloff;
			}
		}

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		int ledCount = this.getHeight() / 5;
		
		int ledHeight = this.getHeight() / ledCount;
		for (int i = 0; i < ledCount; i++) {
			float topCutoff = (float) i / ledCount;
			float bottomCutoff = (float) (i + 1) / ledCount;

			if (this.value > topCutoff 
					|| (this.maxValue > topCutoff && this.maxValue < bottomCutoff)){
				if (topCutoff < 0.5f){
					g.setColor(BRIGHT_GREEN);
				}
				else if (topCutoff < 0.8f) {
					g.setColor(BRIGHT_YELLOW);
				}
				else {
					g.setColor(BRIGHT_RED);
				}
			}
			else {
				if (topCutoff < 0.5f){
					g.setColor(DARK_GREEN);
				}
				else if (topCutoff < 0.8f) {
					g.setColor(DARK_YELLOW);
				}
				else {
					g.setColor(DARK_RED);
				}
			}

			g.fillRect(
					1, //x 
					1 + this.getHeight() - (ledHeight * (i + 1)), //y 
					this.getWidth() - 2,  //width
					ledHeight - 2); //height
		}
	}
}
