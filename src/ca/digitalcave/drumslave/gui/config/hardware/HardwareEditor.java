package ca.digitalcave.drumslave.gui.config.hardware;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.homeunix.thecave.moss.swing.MossDialog;
import org.homeunix.thecave.moss.swing.MossFrame;

import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.config.ConfigFactory;
import ca.digitalcave.drumslave.model.config.ConfigPad;
import ca.digitalcave.drumslave.model.config.ConfigZone;
import ca.digitalcave.drumslave.model.config.InvalidConfigurationException;
import ca.digitalcave.drumslave.model.config.ConfigFactory.ConfigType;
import ca.digitalcave.drumslave.model.hardware.HardwareConfigManager;

public class HardwareEditor extends MossDialog implements ActionListener {
	public static final long serialVersionUID = 0l;
	private final static ZoneEditor[] zoneEditors = new ZoneEditor[40];
	private final JButton saveButton = new JButton("Save");
	private final JButton closeButton = new JButton("Close");
	
	public HardwareEditor(MossFrame owner) {
		super(owner, true);
		
	}

	
	@Override
	public void init() {
		super.init();
		
		JPanel gridPanel = new JPanel(new GridLayout(0, 4));
		
		for (int i = 0; i < zoneEditors.length; i++){
			zoneEditors[i] = new ZoneEditor(i);
			gridPanel.add(zoneEditors[i]);
		}
		
		saveButton.setPreferredSize(Formatter.getButtonSize(saveButton));
		closeButton.setPreferredSize(Formatter.getButtonSize(closeButton));
		
		saveButton.addActionListener(this);
		closeButton.addActionListener(this);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(closeButton);
		buttonPanel.add(saveButton);
		this.getRootPane().setDefaultButton(saveButton);
		
		this.setLayout(new BorderLayout());
		this.add(gridPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setTitle("Edit Hardware Mappings");
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(saveButton)){
			try {
				Map<String, ConfigPad> pads = new HashMap<String, ConfigPad>();
				for (ZoneEditor editor : zoneEditors) {
					if (editor.getPadName() != null 
							&& editor.getPadName().length() > 0 
							&& (editor.getZoneName() == null
									|| editor.getZoneName().length() == 0))
						throw new InvalidConfigurationException("There must be a zone specified for each pad entry.  Pad " + editor.getPadName() + " (channel " + editor.getChannel() + ") has no zone.");
					
					if (pads.get(editor.getPadName()) == null && editor.getPadName() != null && editor.getPadName().length() > 0){
						ConfigPad pad = new ConfigPad();
						pad.setName(editor.getPadName());
						pads.put(editor.getPadName(), pad);
					}

					ConfigZone zone = new ConfigZone();
					if (editor.getZoneName() != null && editor.getZoneName().length() > 0){
						zone.setName(editor.getZoneName());
						zone.setChannel(editor.getChannel());
						if (pads.get(editor.getPadName()) == null)
							throw new InvalidConfigurationException("A pad must be assigned for each zone; zone " + editor.getZoneName() + " (channel " + editor.getChannel() + ") has no pad mapped.");
						pads.get(editor.getPadName()).addZone(zone);
					}
				}

				//Verify that the config is valid.
				Set<Integer> channelNumbers = new HashSet<Integer>();				
				for (ConfigPad configPad : pads.values()) {
					Set<String> zoneNames = new HashSet<String>();
					if (configPad.getZones() == null)
						throw new RuntimeException("Each pad must have at least one zone assigned to it; " + configPad.getName() + " has none.");
					
					for (ConfigZone configZone : configPad.getZones()) {
						if (channelNumbers.contains(configZone.getChannel()))
							throw new RuntimeException("Channel " + configZone.getChannel() + " already exists.");
						channelNumbers.add(configZone.getChannel());

						if (zoneNames.contains(configZone.getName()))
							throw new RuntimeException("You can only have one zone of a given name per pad; zone " + configZone.getName() + " already exists for pad " + configPad.getName() + ".");
						zoneNames.add(configZone.getName());
					}
				}

				new HardwareConfigManager().loadFromConfig(new ArrayList<ConfigPad>(pads.values()));
				ConfigFactory.getInstance().saveConfig(ConfigType.HARDWARE);
				
				this.closeWindowWithoutPrompting();
			}
			catch (InvalidConfigurationException ice){
				JOptionPane.showMessageDialog(this, ice.getMessage(), "Error Saving Configuration", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource().equals(closeButton)){
			this.closeWindow();
		}
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
