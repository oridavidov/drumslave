package ca.digitalcave.drumslave.gui.config.sample;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class PadSampleEditor extends MossPanel {
	public static final long serialVersionUID = 0l;
	
	private Pad pad;
	private final SampleEditor sampleEditor;
	
	public PadSampleEditor(SampleEditor logicEditor) {
		super(true);
		this.sampleEditor = logicEditor;
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
				this.add(new ZoneSampleEditor(zone, sampleEditor));
			}
		}
	}
	
	public void setPad(Pad pad) {
		this.pad = pad;
		
		updateContent();
	}
}
