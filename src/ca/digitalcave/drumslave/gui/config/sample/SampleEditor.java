package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.homeunix.thecave.moss.swing.MossDialog;
import org.homeunix.thecave.moss.swing.MossFrame;

import ca.digitalcave.drumslave.gui.config.NullCapableListCellRenderer;
import ca.digitalcave.drumslave.gui.config.PadsComboBoxModel;
import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigSampleMapping;
import ca.digitalcave.drumslave.model.config.InvalidConfigurationException;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMappingConfigManager;

public class SampleEditor extends MossDialog implements ActionListener {
	public static final long serialVersionUID = 0l;

	private JComboBox padsComboBox;
	private PadSampleEditor padSampleEditor;
	private JButton saveButton = new JButton("Save");
	private JButton closeButton = new JButton("Close");
	
	private final Map<String, Map<String, String>> sampleMappings = new HashMap<String, Map<String,String>>();
	
	
	public SampleEditor(MossFrame owner) {
		super(owner, true);

		//Initialize the config map.  During editing, the map will be updated, not the
		// actual in-memory config.  If the user saves, we will then persist the map
		// to disk and load the configuration.
		for (Pad pad : Pad.getPads()) {
			sampleMappings.put(pad.getName(), new HashMap<String, String>());
			for (Zone zone : pad.getZones()) {
				sampleMappings.get(pad.getName()).put(zone.getName(), SampleMapping.getSampleMapping(pad.getName(), zone.getName()));
			}
		}
	}
	
	@Override
	public void init() {
		super.init();
		padsComboBox = new JComboBox(new PadsComboBoxModel());
		padSampleEditor = new PadSampleEditor(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel padChooserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		padsComboBox.setPreferredSize(Formatter.getComponentSize(padsComboBox, 120));
		saveButton.setPreferredSize(Formatter.getButtonSize(saveButton));
		closeButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		
		padsComboBox.setRenderer(new NullCapableListCellRenderer());
		
		padSampleEditor.setPad(Pad.getPad(padsComboBox.getSelectedItem().toString()));
		
		padsComboBox.addActionListener(this);
		saveButton.addActionListener(this);
		closeButton.addActionListener(this);
		
		buttonPanel.add(closeButton);
		buttonPanel.add(saveButton);
		
		padChooserPanel.add(padsComboBox);		
		
		this.setLayout(new BorderLayout());
		
		this.add(padChooserPanel, BorderLayout.NORTH);
		this.add(padSampleEditor, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.getRootPane().setDefaultButton(saveButton);
		
		this.setTitle("Edit Sample Mappings");
	}
	
	@Override
	public void updateContent() {
		super.updateContent();

		this.pack();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(padsComboBox)){
			padSampleEditor.setPad(Pad.getPad(padsComboBox.getSelectedItem().toString()));
		}
		else if (e.getSource().equals(saveButton)){
			try {
				List<ConfigSampleMapping> sampleMappings = new ArrayList<ConfigSampleMapping>();
				for (String padName : this.sampleMappings.keySet()) {
					for (String zoneName : this.sampleMappings.get(padName).keySet()) {
						if (this.sampleMappings.get(padName).get(zoneName) != null){
							ConfigSampleMapping mapping = new ConfigSampleMapping();
							mapping.setPadName(padName);
							mapping.setZoneName(zoneName);
							mapping.setSampleName(this.sampleMappings.get(padName).get(zoneName));
							sampleMappings.add(mapping);
						}
					}
				}
				
				//Verify that the config is valid
				for (ConfigSampleMapping mapping : sampleMappings) {
					if (Pad.getPad(mapping.getPadName()) == null)
						throw new InvalidConfigurationException("A pad with name " + mapping.getPadName() + " has not been defined in the hardware mappings.");
					if (Zone.getZone(mapping.getPadName(), mapping.getZoneName()) == null)
						throw new InvalidConfigurationException("A zone with name " + mapping.getPadName() + ":" + mapping.getZoneName() + " has not been defined in the hardware mappings.");
					if (Sample.getSample(mapping.getSampleName()) == null)
						throw new InvalidConfigurationException("A sample with name " + mapping.getSampleName() + " could not be found in the samples folder.");
				}
				
				new SampleMappingConfigManager().loadFromConfig(new ArrayList<ConfigSampleMapping>(sampleMappings));
				ConfigFactory.getInstance().saveConfig(ConfigType.SAMPLE_MAPPING, new File("etc/config/sample-mappings.xml"));
				
				this.closeWindow();
			}
			catch (InvalidConfigurationException ice){
				JOptionPane.showMessageDialog(this, ice.getMessage(), "Error Saving Configuration", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource().equals(closeButton)){
			this.closeWindow();
		}
		
		this.updateContent();
	}
	
	protected Map<String, Map<String, String>> getSampleMappings(){
		return sampleMappings;
	}
	
	@Override
	public boolean canClose() {
		int reply = JOptionPane.showConfirmDialog(this, "Are you sure you want to close this window?  You will lose any changes made to the configuration.", "Close Without Save", JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION)
			return true;
		else
			return false;
	}
}
