package ca.digitalcave.drumslave.gui.config.logic;

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
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigLogicMapping;
import ca.digitalcave.drumslave.model.config.InvalidConfigurationException;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.logic.Logic;
import ca.digitalcave.drumslave.model.mapping.LogicMapping;
import ca.digitalcave.drumslave.model.mapping.LogicMappingConfigManager;

public class LogicEditor extends MossDialog implements ActionListener {
	public static final long serialVersionUID = 0l;

	private JComboBox padsComboBox;
	private PadLogicEditor padLogicEditor;
	private JButton saveButton = new JButton("Save");
	private JButton closeButton = new JButton("Close");
	
	private final Map<String, Map<String, String>> logicMappings = new HashMap<String, Map<String,String>>();
	
	
	public LogicEditor(MossFrame owner) {
		super(owner, true);

		//Initialize the config map.  During editing, the map will be updated, not the
		// actual in-memory config.  If the user saves, we will then persist the map
		// to disk and load the configuration.
		for (Pad pad : Pad.getPads()) {
			logicMappings.put(pad.getName(), new HashMap<String, String>());
			for (Zone zone : pad.getZones()) {
				logicMappings.get(pad.getName()).put(zone.getName(), LogicMapping.getLogicMapping(pad.getName(), zone.getName()));
			}
		}
	}
	
	@Override
	public void init() {
		super.init();
		padsComboBox = new JComboBox(new PadsComboBoxModel());
		padLogicEditor = new PadLogicEditor(this);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel padChooserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		padsComboBox.setPreferredSize(Formatter.getComponentSize(padsComboBox, 120));
		saveButton.setPreferredSize(Formatter.getButtonSize(saveButton));
		closeButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		
		padsComboBox.setRenderer(new NullCapableListCellRenderer());
		
		padLogicEditor.setPad(Pad.getPad(padsComboBox.getSelectedItem().toString()));
		
		padsComboBox.addActionListener(this);
		saveButton.addActionListener(this);
		closeButton.addActionListener(this);
		
		buttonPanel.add(closeButton);
		buttonPanel.add(saveButton);
		
		padChooserPanel.add(padsComboBox);		
		
		this.setLayout(new BorderLayout());
		
		this.add(padChooserPanel, BorderLayout.NORTH);
		this.add(padLogicEditor, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.getRootPane().setDefaultButton(saveButton);
		
		this.setTitle("Edit Logic Mappings");
	}
	
	@Override
	public void updateContent() {
		super.updateContent();

		this.pack();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(padsComboBox)){
			padLogicEditor.setPad(Pad.getPad(padsComboBox.getSelectedItem().toString()));
		}
		else if (e.getSource().equals(saveButton)){
			try {
				List<ConfigLogicMapping> logicMappings = new ArrayList<ConfigLogicMapping>();
				for (String padName : this.logicMappings.keySet()) {
					for (String zoneName : this.logicMappings.get(padName).keySet()) {
						if (this.logicMappings.get(padName).get(zoneName) != null){
							ConfigLogicMapping mapping = new ConfigLogicMapping();
							mapping.setPadName(padName);
							mapping.setZoneName(zoneName);
							mapping.setLogicName(this.logicMappings.get(padName).get(zoneName));
							logicMappings.add(mapping);
						}
					}
				}
				
				//Verify that the config is valid
				for (ConfigLogicMapping mapping : logicMappings) {
					if (Pad.getPad(mapping.getPadName()) == null)
						throw new InvalidConfigurationException("A pad with name " + mapping.getPadName() + " has not been defined in the hardware mappings.");
					if (Zone.getZone(mapping.getPadName(), mapping.getZoneName()) == null)
						throw new InvalidConfigurationException("A zone with name " + mapping.getPadName() + ":" + mapping.getZoneName() + " has not been defined in the hardware mappings.");
					if (Logic.getLogic(mapping.getLogicName()) == null)
						throw new InvalidConfigurationException("A logic function with name " + mapping.getLogicName() + " has not been defined in the logic definitions.");
				}
				
				new LogicMappingConfigManager().loadFromConfig(new ArrayList<ConfigLogicMapping>(logicMappings));
				ConfigFactory.getInstance().saveConfig(ConfigType.LOGIC_MAPPING, new File("etc/config/logic-mappings.xml"));
				
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
	
	protected Map<String, Map<String, String>> getLogicMappings(){
		return logicMappings;
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
