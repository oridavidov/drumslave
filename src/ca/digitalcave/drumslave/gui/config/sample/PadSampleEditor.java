package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

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
			Collections.sort(zones);
			for (Zone zone : zones) {
				String logicName = LogicMapping.getLogicMapping(zone.getPad().getName(), zone.getName());
				if (logicName != null){
					Logic logic = Logic.getLogic(logicName);
					if (logic != null){
						for (String logicalName : logic.getLogicalNames(zone)) {
							this.add(new ZoneSampleEditor(zone.getPad(), logicalName, sampleEditor, padChooser, matchSampleNamesToPad));					
						}
					}
				}
			}
		}
	}
	
	public void setPad(Pad pad) {
		this.pad = pad;
		
		updateContent();
	}
}
