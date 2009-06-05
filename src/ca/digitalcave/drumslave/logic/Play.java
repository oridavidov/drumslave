package ca.digitalcave.drumslave.logic;

import ca.digitalcave.drumslave.audio.Sample;
import ca.digitalcave.drumslave.config.SampleManager;
import ca.digitalcave.drumslave.hardware.Zone;

public class Play implements Logic {

	public void play(Zone zone, float volume) {
		Sample sample = SampleManager.getSample(zone);
		if (sample != null){
			sample.play(volume);
		}		
	}
}
