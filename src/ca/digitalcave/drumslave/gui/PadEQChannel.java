package ca.digitalcave.drumslave.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

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
		
		Timer timer = new Timer(100, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (pad != null){
					vuMeter.setValue(pad.getLevel());
					vuMeter.updateFalloff();
				}				
			}
		});
		timer.start();
	}
	
	@Override
	public void updateContent() {
		super.updateContent();


	}
	
	public float getVolumeAdjustment(){
		return volumeAdjustment.getValue() / 100f;
	}
}
