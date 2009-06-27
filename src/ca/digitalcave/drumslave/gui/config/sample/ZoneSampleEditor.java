package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.config.NullCapableListCellRenderer;
import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class ZoneSampleEditor extends MossPanel implements ActionListener {
	public static final long serialVersionUID = 0l;
	
//	private Zone zone;
	private Pad pad;
	private String logicalName;
	private JLabel zoneName;
	private JComboBox sampleNames;
	private final SampleEditor sampleEditor;
	private final JCheckBox matchSampleNamesToPad;
	private final JComboBox padChooser;
	
	public ZoneSampleEditor(Pad pad, String logicalName, SampleEditor sampleEditor, JComboBox padChooser, JCheckBox matchSampleNamesToPad) {
		super(true);

		this.pad = pad;
		this.logicalName = logicalName;
		this.sampleEditor = sampleEditor;
		
		this.matchSampleNamesToPad = matchSampleNamesToPad;
		this.padChooser = padChooser;
		open();
	}
	
	@Override
	public void init() {
		super.init();
		
		zoneName = new JLabel(pad.getName() + ":" + logicalName);
		zoneName.setHorizontalAlignment(JLabel.RIGHT);
		sampleNames = new JComboBox(new SamplesComboBoxModel(padChooser, matchSampleNamesToPad));
		sampleNames.setRenderer(new NullCapableListCellRenderer());
		
		zoneName.setPreferredSize(Formatter.getComponentSize(zoneName, 200));
		sampleNames.setPreferredSize(Formatter.getComponentSize(sampleNames, 120));
		
		this.setLayout(new FlowLayout());
		this.add(zoneName);
		this.add(sampleNames);
		
		matchSampleNamesToPad.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				String selectedItem = sampleEditor.getSampleMappings().get(SampleMapping.getSelectedSampleGroup()).get(pad.getName()).get(logicalName);
				((SamplesComboBoxModel) sampleNames.getModel()).updateModel();				
				sampleNames.setSelectedItem(selectedItem);
			}
		});
		
		//Set the combo boxes according to the temporary config map
		if (sampleEditor.getSampleMappings().get(SampleMapping.getSelectedSampleGroup()) != null
				&& sampleEditor.getSampleMappings().get(SampleMapping.getSelectedSampleGroup()).get(pad.getName()) != null){
			sampleNames.setSelectedItem(sampleEditor.getSampleMappings().get(SampleMapping.getSelectedSampleGroup()).get(pad.getName()).get(logicalName));
		}
		
		sampleNames.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(sampleNames)){
			String sampleConfigName = sampleEditor.getSelectedConfigGroupName();
			if (sampleEditor.getSampleMappings().get(sampleConfigName) == null)
				sampleEditor.getSampleMappings().put(sampleConfigName, new HashMap<String, Map<String,String>>());
			if (sampleEditor.getSampleMappings().get(sampleConfigName).get(pad.getName()) == null)
				sampleEditor.getSampleMappings().get(sampleConfigName).put(pad.getName(), new HashMap<String, String>());
			sampleEditor.getSampleMappings().get(sampleConfigName).get(pad.getName()).put(logicalName, (String) sampleNames.getSelectedItem());
		}
	}
}
