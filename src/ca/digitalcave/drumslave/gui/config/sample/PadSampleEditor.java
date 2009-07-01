package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.logic.Logic;
import ca.digitalcave.drumslave.model.mapping.LogicMapping;

public class PadSampleEditor extends MossPanel {
	public static final long serialVersionUID = 0l;
	
	private Pad pad;
	private final SampleEditor sampleEditor;

	private final JCheckBox matchSampleNamesToPad;
	private final JComboBox padChooser;
	

	public PadSampleEditor(SampleEditor logicEditor, JComboBox padChooser, JCheckBox matchSampleNamesToPad) {
		super(true);
		this.sampleEditor = logicEditor;
		this.matchSampleNamesToPad = matchSampleNamesToPad;
		this.padChooser = padChooser;

		open();
	}

	@Override
	public void init() {
		super.init();

		this.setLayout(new GridLayout(0, 1));
	}
	
	@Override
	public void updateContent() {
		super.updateContent();
		this.removeAll();
		
		if (pad != null){
			List<Zone> zones = new ArrayList<Zone>(pad.getZones());
			Set<String> logicalNames = new TreeSet<String>();
			Collections.sort(zones);
			for (Zone zone : zones) {
				String logicName = LogicMapping.getLogicMapping(zone.getPad().getName(), zone.getName());
				if (logicName != null){
					Logic logic = Logic.getLogic(logicName);
					if (logic != null){
						for (String logicalName : logic.getLogicalNames(zone)) {
							if (logicalName != null){
								logicalNames.add(logicalName);
							}
						}
					}
				}
			}
			
			for (String logicalName : logicalNames) {
				this.add(new ZoneSampleEditor(pad, logicalName, sampleEditor, padChooser, matchSampleNamesToPad));								
			}
			
			if (logicalNames.size() == 0){
				JLabel noLogicalSamplesMessage = new JLabel("<html>There were no logical sample names defined; perhaps you have not configured any<br>Play-style logic for the pad? (Mute and other silent logics will not include logical<br>sample mappings in this list)</html>");
				this.add(noLogicalSamplesMessage);
			}
		}
		
	}
	
	public void setPad(Pad pad) {
		this.pad = pad;
		
		updateContent();
	}
}
