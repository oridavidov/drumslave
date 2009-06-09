package ca.digitalcave.drumslave.model.audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Sample implementation, based on the OpenAL Java wrapper project 'JOAL' (https://joal.dev.java.net/).
 * JOAL allows many very interesting features for playing sounds, including positional
 * audio, moving sources, and even things like the doppler effect!  However, we don't use
 * even close to all of JOAL's features, but we do use its ability to play many audio
 * sources concurrently.
 * 
 * This class loads multiple JoalCircularSource objects, mapped to the sample number for a
 * given sample.  When a play request comes in, we calculate which sample number to play,
 * and play the JoalCircularSource mapped to it.  When a stop request comes, we stop all
 * JoalCircularSources for this sample.
 * 
 * @author wyatt
 *
 */
public class JoalSample extends Sample {

	private final Map<Integer, JoalCircularSource> joalSources = new ConcurrentHashMap<Integer, JoalCircularSource>();
	
	public JoalSample(String name) {
		super(name);

		for (int i = 0; i < sampleFiles.size(); i++){
			joalSources.put(i, new JoalCircularSource(sampleFiles.get(i)));
		}
	}

	@Override
	public void play(float volume) {
		int sampleCount = joalSources.keySet().size();
		if (sampleCount == 0)
			throw new RuntimeException("No samples found for " + getName());
		
		//Figure out which sample to use based on volume
		int sampleNumber = getVolumeToSampleNumberMapping(sampleCount, volume);
		if (joalSources.get(sampleNumber) == null)
			throw new RuntimeException("No sample loaded for sample number " + sampleNumber);
		
		joalSources.get(sampleNumber).play(volume);
	}

	@Override
	public void stop() {
		//Fade out logarithmically over 10 iterations (less than a second total, but since the
		// fading is logarithmic, the perceived end of playback is even faster)
		for (int i = 0; i < 10; i++){
			for (JoalCircularSource source : joalSources.values()) {
				source.setGain(0.5f);
			}
			try {
				Thread.sleep(40);
			}
			catch (InterruptedException ie){}
		}
		for (JoalCircularSource source : joalSources.values()) {
			source.stop();
		}
	}

	@Override
	public float getLevel() {
		int level = 0;
		for (JoalCircularSource source : joalSources.values()) {
			level = Math.max(level, source.getLevel());
		}
//		return (float) Math.log(level / 32768f + 1) * 6;
		return level / 32768f;
	}

	public static void main(String[] args) throws Exception {
		JoalSample sample = new JoalSample("Cymbal/Ride/Zildjian A Ping 20/Bow");
//		JoalSample sample = new JoalSample("Drum/Snare/Garage Band/Head");
		sample.play(1.0f);
		for (int i = 0; i < 20; i++){
			System.out.println(sample.getLevel());
			Thread.sleep(100);
		}
		Thread.sleep(2000);
		sample.stop();
		Thread.sleep(4000);
		System.exit(0);
	}
}
