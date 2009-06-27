package ca.digitalcave.drumslave.gui.config.logic;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.homeunix.thecave.moss.swing.MossDecimalField;
import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.config.NullCapableListCellRenderer;
import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.logic.Logic;
import ca.digitalcave.drumslave.model.logic.Logic.LogicOption;
import ca.digitalcave.drumslave.model.logic.Logic.LogicOptionType;

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
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(zoneName);
		this.add(logicNames);
		this.add(logicOptionsPanel);
		
		//Set the combo boxes according to the temporary config map
		if (logicEditor.getLogicMappings().get(zone.getPad().getName()) != null){
			logicNames.setSelectedItem(logicEditor.getLogicMappings().get(zone.getPad().getName()).get(zone.getName()));
		}
		
		logicNames.addActionListener(this);
	}
	
	@Override
	public void updateContent() {
		super.updateContent();
		
		Logic logic = Logic.getLogic(logicEditor.getLogicMappings().get(zone.getPad().getName()).get(zone.getName()));
		logicOptionsPanel.removeAll();
		if (logic != null){
			List<LogicOption> logicOptions = logic.getLogicOptions();
			if (logicOptions != null){
				for (LogicOption logicOption : logicOptions) {
					final LogicOption logicOptionFinal = logicOption;
					JComponent component = null;

					//Boolean option
					if (logicOption.getLogicOptionType().equals(LogicOptionType.OPTION_BOOLEAN)){
						component = new JCheckBox(logicOption.getName());
						((JCheckBox) component).setSelected(Boolean.parseBoolean(logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOption.getName())));
						((JCheckBox) component).addChangeListener(new ChangeListener(){
							public void stateChanged(ChangeEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), ((JCheckBox) e.getSource()).isSelected() + "");
							}
						});
					}

					else if (logicOption.getLogicOptionType().equals(LogicOptionType.OPTION_INTEGER)){
						component = new JPanel(new FlowLayout(FlowLayout.LEFT));
						final MossDecimalField number = new MossDecimalField((long) logicOption.getDefaultValue(), false, 0);
						try {
							number.setValue(Long.parseLong(logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOption.getName())));
						}
						catch (NumberFormatException nfe){}
						component.add(number);
						component.add(new JLabel(logicOption.getName()));
						number.addFocusListener(new FocusAdapter(){
							@Override
							public void focusLost(FocusEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), number.getValue() + "");
							}
						});
					}
					
					//TODO add other option types here

					if (component != null)
						logicOptionsPanel.add(component);
				}
			}
		}
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(logicNames)){
			logicEditor.getLogicMappings().get(zone.getPad().getName()).put(zone.getName(), (String) logicNames.getSelectedItem());
			
			
		}
	}
}
