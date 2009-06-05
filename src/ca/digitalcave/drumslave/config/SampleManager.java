package ca.digitalcave.drumslave.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.audio.Sample;
import ca.digitalcave.drumslave.hardware.Zone;

public class SampleManager {

	private static Map<Zone, Sample> samples = new ConcurrentHashMap<Zone, Sample>();
	
	public static Sample getSample(Zone zone){
		return samples.get(zone);
	}
	
	public static void loadSampleMappings(){
		//TODO Load from file or something
		samples.put(Zone.getZone("Bass", "Head"), new Sample("samples/snare.wav", 2));
		samples.put(Zone.getZone("Ride", "Bow"), new Sample("samples/ride.wav", 2));
	}
}
