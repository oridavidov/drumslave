package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.homeunix.thecave.moss.swing.MossDialog;
import org.homeunix.thecave.moss.swing.MossFrame;

import ca.digitalcave.drumslave.gui.config.NullCapableListCellRenderer;
import ca.digitalcave.drumslave.gui.config.PadsComboBoxModel;
import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.InvalidModelException;
import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigSampleMapping;
import ca.digitalcave.drumslave.model.config.ConfigSampleMappingGroup;
import ca.digitalcave.drumslave.model.config.InvalidConfigurationException;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMappingConfigManager;

public class SampleEditor extends MossDialog implements ActionListener {
	public static final long serialVersionUID = 0l;

	private JComboBox padsComboBox;
	private JComboBox sampleGroupNamesComboBox;
	private PadSampleEditor padSampleEditor;
	
	private JButton addGroupButton = new JButton("Add");
	private JButton renameGroupButton = new JButton("Rename");
	private JButton deleteGroupButton = new JButton("Delete");
	
	private JButton saveButton = new JButton("Save");
	private JButton closeButton = new JButton("Close");
	
	private final Map<String, Map<String, Map<String, String>>> sampleMappings = new HashMap<String, Map<String, Map<String, String>>>();
	
	
	public SampleEditor(MossFrame owner) {
		super(owner, true);

		//Initialize the config map.  During editing, the map will be updated, not the
		// actual in-memory config.  If the user saves, we will then persist the map
		// to disk and load the configuration.
		for (String sampleConfigName : SampleMapping.getSampleGroups()) {
			sampleMappings.put(sampleConfigName, new HashMap<String, Map<String,String>>());
			for (Pad pad : Pad.getPads()) {
				sampleMappings.get(sampleConfigName).put(pad.getName(), new HashMap<String, String>());
				for (Zone zone : pad.getZones()) {
					sampleMappings.get(sampleConfigName).get(pad.getName()).put(zone.getName(), SampleMapping.getSampleMapping(sampleConfigName, pad.getName(), zone.getName()));
				}
			}
		}
	}
	
	@Override
	public void init() {
		super.init();
		padsComboBox = new JComboBox(new PadsComboBoxModel());
		sampleGroupNamesComboBox = new JComboBox(new SampleNamesComboBoxModel());
		padSampleEditor = new PadSampleEditor(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel sampleNamesComboBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel padChooserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel topPanel = new JPanel(new BorderLayout());
		
		padsComboBox.setPreferredSize(Formatter.getComponentSize(padsComboBox, 120));
		sampleGroupNamesComboBox.setPreferredSize(Formatter.getComponentSize(sampleGroupNamesComboBox, 120));
		saveButton.setPreferredSize(Formatter.getButtonSize(saveButton));
		closeButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		addGroupButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		renameGroupButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		deleteGroupButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		
		padsComboBox.setRenderer(new NullCapableListCellRenderer());
		
		padSampleEditor.setPad(Pad.getPad(padsComboBox.getSelectedItem().toString()));
		
		padsComboBox.addActionListener(this);
		sampleGroupNamesComboBox.addActionListener(this);
		saveButton.addActionListener(this);
		closeButton.addActionListener(this);
		addGroupButton.addActionListener(this);
		renameGroupButton.addActionListener(this);
		deleteGroupButton.addActionListener(this);
		
		buttonPanel.add(closeButton);
		buttonPanel.add(saveButton);
		
		sampleNamesComboBoxPanel.add(new JLabel("Sample Groups:"));
		sampleNamesComboBoxPanel.add(sampleGroupNamesComboBox);
		sampleNamesComboBoxPanel.add(addGroupButton);
		sampleNamesComboBoxPanel.add(renameGroupButton);
		sampleNamesComboBoxPanel.add(deleteGroupButton);
		
		topPanel.add(sampleNamesComboBoxPanel, BorderLayout.NORTH);
		topPanel.add(padChooserPanel, BorderLayout.SOUTH);
		
		padChooserPanel.add(padsComboBox);		
		
		this.setLayout(new BorderLayout());
		
		this.add(topPanel, BorderLayout.NORTH);
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
		else if (e.getSource().equals(sampleGroupNamesComboBox)){
			if (sampleGroupNamesComboBox.getSelectedItem() != null){
				SampleMapping.setSelectedSampleGroup(sampleGroupNamesComboBox.getSelectedItem().toString());
				padSampleEditor.updateContent();
				System.out.println(SampleMapping.getSelectedSampleGroup());
			}
		}
		else if (e.getSource().equals(saveButton)){
			try {
				saveChanges();
				
				this.closeWindowWithoutPrompting();
			}
			catch (InvalidConfigurationException ice){
				JOptionPane.showMessageDialog(this, ice.getMessage(), "Error Saving Configuration", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource().equals(closeButton)){
			this.closeWindow();
		}
		else if (e.getSource().equals(addGroupButton)){
			if (JOptionPane.YES_OPTION == confirmSave()){
				String groupName = JOptionPane.showInputDialog(this, "Enter new Sample Group Name");
				if (groupName != null && groupName.trim().length() > 0){
					try {
						saveChanges();
						SampleMapping.setSelectedSampleGroup(groupName);
						((SampleNamesComboBoxModel) sampleGroupNamesComboBox.getModel()).updateModel();
						sampleGroupNamesComboBox.setSelectedItem(groupName);
					} 
					catch (InvalidConfigurationException e1) {
						JOptionPane.showMessageDialog(this, e1.getMessage(), "Invalid Configuration: ", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		else if (e.getSource().equals(renameGroupButton)){
			if (JOptionPane.YES_OPTION == confirmSave()){
				String oldConfig = sampleGroupNamesComboBox.getSelectedItem().toString();
				String newConfig = JOptionPane.showInputDialog(this, "Rename Sample Group", SampleMapping.getSelectedSampleGroup());
				if (newConfig != null && newConfig.trim().length() > 0){
					try {
						saveChanges();
						
						SampleMapping.renameSampleGroup(oldConfig, newConfig);

						//Update the temporary config maps
						Map<String, Map<String, String>> map = sampleMappings.get(oldConfig);
						sampleMappings.remove(oldConfig);
						sampleMappings.put(newConfig, map);

						//Update the combobox with the new values
						((SampleNamesComboBoxModel) sampleGroupNamesComboBox.getModel()).updateModel();
						sampleGroupNamesComboBox.setSelectedItem(SampleMapping.getSelectedSampleGroup());
						
						saveChanges();
					}
					catch (InvalidConfigurationException ice){
						JOptionPane.showMessageDialog(this, ice.getMessage(), "Invalid Configuration: ", JOptionPane.ERROR_MESSAGE);						
					}
					catch (InvalidModelException ime){
						JOptionPane.showMessageDialog(this, ime.getMessage(), "Invalid Value: ", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		else if (e.getSource().equals(deleteGroupButton)){
			if (JOptionPane.YES_OPTION == confirmSave()){
				int reply = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the config group named " + sampleGroupNamesComboBox.getSelectedItem() + "?\nAll sample mappings associated with this group will be lost.", "Delete Sample Group", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION){
					try {
						saveChanges();
						SampleMapping.deleteSampleGroup(sampleGroupNamesComboBox.getSelectedItem().toString());
					}
					catch (InvalidConfigurationException ice){
						JOptionPane.showMessageDialog(this, ice.getMessage(), "Invalid Configuration: ", JOptionPane.ERROR_MESSAGE);						
					}
					catch (InvalidModelException ime){
						JOptionPane.showMessageDialog(this, ime.getMessage(), "Invalid Value", JOptionPane.ERROR_MESSAGE);
					}
					((SampleNamesComboBoxModel) sampleGroupNamesComboBox.getModel()).updateModel();
					sampleGroupNamesComboBox.setSelectedItem(SampleMapping.getSelectedSampleGroup());
				}
			}
		}
		
		this.updateContent();
		
		ConfigFactory.getInstance().saveConfig(ConfigType.SETTINGS);
	}
	
	private int confirmSave(){
		return JOptionPane.showConfirmDialog(this, "If you modify the sample groups, your changes to this point will be saved.  Continue?", "Save Changes", JOptionPane.YES_NO_OPTION);
	}
	
	private void saveChanges() throws InvalidConfigurationException {
		List<ConfigSampleMappingGroup> sampleMappingGroups = new ArrayList<ConfigSampleMappingGroup>();
		for (String sampleConfigName : this.sampleMappings.keySet()){
			System.out.println("Saving " + sampleConfigName);
			ConfigSampleMappingGroup configSampleMappingGroup = new ConfigSampleMappingGroup();
			configSampleMappingGroup.setName(sampleConfigName);
			List<ConfigSampleMapping> sampleMappings = new ArrayList<ConfigSampleMapping>();
			if (this.sampleMappings.get(sampleConfigName) != null){
				for (String padName : this.sampleMappings.get(sampleConfigName).keySet()) {
					for (String zoneName : this.sampleMappings.get(sampleConfigName).get(padName).keySet()) {
						if (this.sampleMappings.get(sampleConfigName).get(padName) != null 
								&& this.sampleMappings.get(sampleConfigName).get(padName).get(zoneName) != null){
							ConfigSampleMapping mapping = new ConfigSampleMapping();
							mapping.setPadName(padName);
							mapping.setZoneName(zoneName);
							mapping.setSampleName(this.sampleMappings.get(sampleConfigName).get(padName).get(zoneName));
							sampleMappings.add(mapping);
						}
					}
				}
			}
			configSampleMappingGroup.setSampleMappings(sampleMappings);
			sampleMappingGroups.add(configSampleMappingGroup);
		}
		
		//Verify that the config is valid
		for (ConfigSampleMappingGroup configSampleMappingGroup : sampleMappingGroups) {
			for (ConfigSampleMapping mapping : configSampleMappingGroup.getSampleMappings()) {
				if (Pad.getPad(mapping.getPadName()) == null)
					throw new InvalidConfigurationException("A pad with name " + mapping.getPadName() + " has not been defined in the hardware mappings.");
				if (Zone.getZone(mapping.getPadName(), mapping.getZoneName()) == null)
					throw new InvalidConfigurationException("A zone with name " + mapping.getPadName() + ":" + mapping.getZoneName() + " has not been defined in the hardware mappings.");
				if (Sample.getSample(mapping.getSampleName()) == null)
					throw new InvalidConfigurationException("A sample with name " + mapping.getSampleName() + " could not be found in the samples folder.");
			}
		}
		
		new SampleMappingConfigManager().loadFromConfig(new ArrayList<ConfigSampleMappingGroup>(sampleMappingGroups));
		ConfigFactory.getInstance().saveConfig(ConfigType.SAMPLE_MAPPING);
	}
	
	protected Map<String, Map<String, Map<String, String>>> getSampleMappings(){
		return sampleMappings;
	}
	
	protected String getSelectedConfigGroupName(){
		return sampleGroupNamesComboBox.getSelectedItem().toString();
	}
	
	@Override
	public boolean canClose() {
		int reply = JOptionPane.showConfirmDialog(this, "Are you sure you want to close this window?\nYou will lose any changes made to the configuration.", "Close Without Save", JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION)
			return true;
		else
			return false;
	}
}
