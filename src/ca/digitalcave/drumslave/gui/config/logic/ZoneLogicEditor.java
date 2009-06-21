package ca.digitalcave.drumslave.gui.config.logic;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.config.NullCapableListCellRenderer;
import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class ZoneLogicEditor extends MossPanel implements ActionListener {
	public static final long serialVersionUID = 0l;
	
	private Zone zone;
	private JLabel zoneName;
	private JComboBox logicNames;
	private final LogicEditor logicEditor;
	private final JPanel logicOptionsPanel;

	public ZoneLogicEditor(Zone zone, LogicEditor logicEditor) {
		super(true);
		this.zone = zone;
		this.logicEditor = logicEditor;
		this.logicOptionsPanel = new JPanel();
		open();
	}
	
	@Override
	public void init() {
		super.init();
		
		zoneName = new JLabel(zone.getPad().getName() + ":" + zone.getName());
		zoneName.setHorizontalAlignment(JLabel.RIGHT);
		logicNames = new JComboBox(new LogicsComboBoxModel());
		logicNames.setRenderer(new NullCapableListCellRenderer());
		
		zoneName.setPreferredSize(Formatter.getComponentSize(zoneName, 200));
		logicNames.setPreferredSize(Formatter.getComponentSize(logicNames, 120));
		
		this.setLayout(new FlowLayout());
		this.add(zoneName);
		this.add(logicNames);
		this.add(logicOptionsPanel);
		
		//Set the combo boxes according to the temporary config map
		if (logicEditor.getLogicMappings().get(zone.getPad().getName()) != null){
			logicNames.setSelectedItem(logicEditor.getLogicMappings().get(zone.getPad().getName()).get(zone.getName()));
		}
		
		logicNames.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(logicNames)){
			logicEditor.getLogicMappings().get(zone.getPad().getName()).put(zone.getName(), (String) logicNames.getSelectedItem());
			
			
		}
	}
}
