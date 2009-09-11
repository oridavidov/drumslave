package ca.digitalcave.drumslave.gui.config.logic;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.homeunix.thecave.moss.swing.MossDecimalField;
import org.homeunix.thecave.moss.swing.MossHintTextField;
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
						JCheckBox checkBox = new JCheckBox(logicOption.getShortName());
						checkBox.setToolTipText(logicOption.getName());
						checkBox.setSelected(Boolean.parseBoolean(logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOption.getName())));
						checkBox.addChangeListener(new ChangeListener(){
							public void stateChanged(ChangeEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), ((JCheckBox) e.getSource()).isSelected() + "");
							}
						});
						component = checkBox;
					}

					else if (logicOption.getLogicOptionType().equals(LogicOptionType.OPTION_INTEGER)){
						component = new JPanel(new FlowLayout(FlowLayout.LEFT));
						final MossDecimalField number = new MossDecimalField((long) logicOption.getDefaultValue(), false, 0);
						number.setToolTipText(logicOption.getName());
						try {
							number.setValue(Long.parseLong(logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOption.getName())));
						}
						catch (NumberFormatException nfe){}
						component.add(number);
						JLabel label = new JLabel(logicOption.getShortName());
						label.setToolTipText(logicOption.getName());
						component.add(label);
						number.addKeyListener(new KeyAdapter(){
							@Override
							public void keyReleased(KeyEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), number.getValue() + "");
							}
						});
					}
					
					else if (logicOption.getLogicOptionType().equals(LogicOptionType.OPTION_FLOAT)){
						component = new JPanel(new FlowLayout(FlowLayout.LEFT));
						final MossDecimalField number = new MossDecimalField((long) logicOption.getDefaultValue(), false, 2);
						number.setToolTipText(logicOption.getName());
						try {
							String logicOptionString = logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOption.getName());
							if (logicOptionString == null)
								logicOptionString = "0";
							float value = Float.parseFloat(logicOptionString);
							number.setValue((long) (value * 100f));
						}
						catch (NumberFormatException nfe){}
						component.add(number);
						JLabel label = new JLabel(logicOption.getShortName());
						label.setToolTipText(logicOption.getName());
						component.add(label);
						number.addKeyListener(new KeyAdapter(){
							@Override
							public void keyReleased(KeyEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), (float) (number.getValue() / 100f) + "");
							}
						});
					}
					
					else if (logicOption.getLogicOptionType().equals(LogicOptionType.OPTION_STRING)){
						component = new MossHintTextField(logicOption.getShortName());
						((MossHintTextField) component).setText(logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName()));
						component.setToolTipText(logicOption.getName());
						component.addKeyListener(new KeyAdapter(){
							@Override
							public void keyReleased(KeyEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), ((MossHintTextField) e.getSource()).getText());
							}
						});
					}
					
					else if (logicOption.getLogicOptionType().equals(LogicOptionType.OPTION_RANGE)){
						final JSlider slider = new JSlider(
								(int) (logicOption.getMinValue() * logicOption.getRangeSteps()), 
								(int) (logicOption.getMaxValue() * logicOption.getRangeSteps()));
						try {
							slider.setValue((int) (logicOption.getRangeSteps() * Float.parseFloat(logicEditor.getLogicOption(zone.getPad().getName(), zone.getName(), logicOption.getName()))));
						}
						catch (RuntimeException re){
							slider.setValue((int) (logicOption.getDefaultValue() * logicOption.getRangeSteps()));
						}
						JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
						panel.add(slider);
						panel.add(new JLabel(logicOption.getShortName()));
						component = panel;
						slider.addChangeListener(new ChangeListener(){
							public void stateChanged(ChangeEvent e) {
								logicEditor.setLogicOption(zone.getPad().getName(), zone.getName(), logicOptionFinal.getName(), (slider.getValue() / (float) logicOptionFinal.getRangeSteps()) + "");
							}
						});
						
					}
					//TODO add other option types here

					if (component != null)
						logicOptionsPanel.add(component);
				}
			}
		}
		
		this.validate();
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(logicNames)){
			logicEditor.getLogicMappings().get(zone.getPad().getName()).put(zone.getName(), (String) logicNames.getSelectedItem());
			
			updateContent();
		}
	}
}
