package ca.digitalcave.drumslave.gui.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.mapping.GainMapping;

public class PadEQChannel extends MossPanel implements ChangeListener {
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
		
		volumeAdjustment.setMaximum(125);
		volumeAdjustment.setMinimum(0);
		volumeAdjustment.setValue(100);
		volumeAdjustment.setSnapToTicks(true);
		volumeAdjustment.setMajorTickSpacing(25);
		volumeAdjustment.setMinorTickSpacing(5);
		volumeAdjustment.setPaintTicks(true);
		volumeAdjustment.addChangeListener(this);
		volumeAdjustment.setPaintLabels(true);
		Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
		putLabel(labels, 125, "2");
		putLabel(labels, 100, "0 dB");
		putLabel(labels, 75, "-2");
		putLabel(labels, 50, "-6");
		putLabel(labels, 25, "-12");
		putLabel(labels, 0, "-96");
		volumeAdjustment.setLabelTable(labels);
		
		vuMeter.setPreferredSize(new Dimension(15, 150));
		volumeAdjustment.setPreferredSize(new Dimension(50, 150));
		
		this.setLayout(new BorderLayout());
		
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		if (pad != null)
			centerPanel.add(vuMeter);
		centerPanel.add(volumeAdjustment);
		
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (pad != null)
			namePanel.add(new JLabel(pad.getName()));
		else
			namePanel.add(new JLabel(GainMapping.MASTER));
		
		this.add(centerPanel, BorderLayout.CENTER);
		
		this.add(namePanel, BorderLayout.SOUTH);
		
		if (pad != null){
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
	}
	
	private void putLabel(Hashtable<Integer, JComponent> labels, int value, String label){
		JLabel l = new JLabel(label);
		Font curFont = l.getFont();
		l.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 8));
		labels.put(value, l);
	}
	
	@Override
	public void updateContent() {
		super.updateContent();

		if (pad != null)
			volumeAdjustment.setValue((int) (GainMapping.getPadGain(pad.getName()) * 100));
		else
			volumeAdjustment.setValue((int) (GainMapping.getPadGain(GainMapping.MASTER) * 100));
	}
	
	public void stateChanged(ChangeEvent e) {
		if (pad != null)
			GainMapping.addGainMapping(pad.getName(), volumeAdjustment.getValue() / 100f);
		else
			GainMapping.addGainMapping(GainMapping.MASTER, volumeAdjustment.getValue() / 100f);
		ConfigFactory.getInstance().saveConfig(ConfigType.GAIN_MAPPING);
	}
}
