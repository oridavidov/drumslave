package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.config.NullCapableListCellRenderer;
import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class ZoneSampleEditor extends MossPanel implements ActionListener {
	public static final long serialVersionUID = 0l;
	
	private Zone zone;
	private JLabel zoneName;
	private JComboBox sampleNames;
	private final SampleEditor sampleEditor;

	public ZoneSampleEditor(Zone zone, SampleEditor logicEditor) {
		super(true);
		this.zone = zone;
		this.sampleEditor = logicEditor;
		open();
	}
	
	@Override
	public void init() {
		super.init();
		
		zoneName = new JLabel(zone.getPad().getName() + ":" + zone.getName());
		zoneName.setHorizontalAlignment(JLabel.RIGHT);
		sampleNames = new JComboBox(new SamplesComboBoxModel());
		sampleNames.setRenderer(new NullCapableListCellRenderer());
		
		zoneName.setPreferredSize(Formatter.getComponentSize(zoneName, 200));
		sampleNames.setPreferredSize(Formatter.getComponentSize(sampleNames, 120));
		
		this.setLayout(new FlowLayout());
		this.add(zoneName);
		this.add(sampleNames);
		
		//Set the combo boxes according to the temporary config map
		if (sampleEditor.getSampleMappings().get(zone.getPad().getName()) != null){
			sampleNames.setSelectedItem(sampleEditor.getSampleMappings().get(zone.getPad().getName()).get(zone.getName()));
		}
		
		sampleNames.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(sampleNames)){
			sampleEditor.getSampleMappings().get(zone.getPad().getName()).put(zone.getName(), (String) sampleNames.getSelectedItem());
		}
	}
}
