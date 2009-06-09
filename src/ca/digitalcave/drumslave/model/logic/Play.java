package ca.digitalcave.drumslave.model.logic;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class Play extends Logic {

	public Play(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float value) {
		String sampleName = SampleMapping.getSampleMapping(zone.getPad().getName(), zone.getName());
		if (sampleName == null)
			throw new RuntimeException("No sample name is mapped to " + zone.getPad().getName() + ":" + zone.getName());
		
		Sample sample = Sample.getSample(sampleName);
		if (sample == null)
			throw new RuntimeException("No sample is mapped to name " + sampleName);

		//Adjust the value based on the GUI gain control
//		float adjustedValue = value * (float) Math.log10(zone.getPad().getGain() * 100);
		float adjustedValue = value * zone.getPad().getGain();
		sample.play(adjustedValue);
	}
}
