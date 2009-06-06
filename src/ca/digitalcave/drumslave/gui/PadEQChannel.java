package ca.digitalcave.drumslave.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.widget.VUMeter;
import ca.digitalcave.drumslave.model.hardware.Pad;

public class PadEQChannel extends MossPanel {
	public static final long serialVersionUID = 0l;

	private final Pad pad;
	
	private VUMeter vuMeter;
	private JSlider volumeAdjustment;
	
	public PadEQChannel(Pad pad) {
		super(true);
		this.pad = pad;
		open();
	}
	
	@Override
	public void init() {
		super.init();

		vuMeter = new VUMeter();
		volumeAdjustment = new JSlider(JSlider.VERTICAL);
		
		volumeAdjustment.setMaximum(100);
		volumeAdjustment.setMinimum(0);
		
		vuMeter.setPreferredSize(new Dimension(15, 100));
		volumeAdjustment.setPreferredSize(new Dimension(25, 100));
		
		this.setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		centerPanel.add(vuMeter);
		centerPanel.add(volumeAdjustment);
		
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		namePanel.add(new JLabel(pad.getName()));
		
		this.add(centerPanel, BorderLayout.CENTER);
		
		this.add(namePanel, BorderLayout.SOUTH);
	}
	
	@Override
	public void updateContent() {
		super.updateContent();

		if (pad != null){
			vuMeter.setValue(0.5f);
		}
	}
	
	public float getVolumeAdjustment(){
		return volumeAdjustment.getValue() / 100f;
	}
}
