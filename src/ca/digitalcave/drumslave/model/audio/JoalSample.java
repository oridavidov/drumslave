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

	private final Map<Integer, JoalSourceCircularQueue> joalSources = new ConcurrentHashMap<Integer, JoalSourceCircularQueue>();
	private int lastSampleNumber; //Last number which was played; this is used for adjustLastVolume. 
	private final Object lastSampleNumberMutex = new Object();
	
	public JoalSample(String name) {
		super(name);

		for (int i = 0; i < sampleFiles.size(); i++){
			joalSources.put(i, new JoalSourceCircularQueue(sampleFiles.get(i)));
		}
	}

	@Override
	public void play(float rawVolume, float gain) {
		int sampleCount = joalSources.keySet().size();
		if (sampleCount == 0)
			throw new RuntimeException("No samples found for " + getName());
		
		//Figure out which sample to use based on volume
		int sampleNumber = getVolumeToSampleNumberMapping(sampleCount, rawVolume);
		if (joalSources.get(sampleNumber) == null)
			throw new RuntimeException("No sample loaded for sample number " + sampleNumber);
		
		joalSources.get(sampleNumber).play(rawVolume * gain);
		synchronized (lastSampleNumberMutex) {
			lastSampleNumber = sampleNumber;			
		}
	}
	
	@Override
	public void adjustLastVolume(float rawVolume, float gain) {
		synchronized (lastSampleNumberMutex) {
			joalSources.get(lastSampleNumber).adjustLastVolume(rawVolume * gain);
		}
	}

	@Override
	public void stop() {
		//Fade out logarithmically over 10 iterations (less than a second total, but since the
		// fading is logarithmic, the perceived end of playback is even faster)
		for (int i = 0; i < 10; i++){
			for (JoalSourceCircularQueue source : joalSources.values()) {
				source.setGain(0.5f);
			}
			try {
				Thread.sleep(40);
			}
			catch (InterruptedException ie){}
		}
		for (JoalSourceCircularQueue source : joalSources.values()) {
			source.stop();
		}
	}

	@Override
	public float getLevel() {
		int level = 0;
		for (JoalSourceCircularQueue source : joalSources.values()) {
			level = Math.max(level, source.getLevel());
		}
		//Linear display - decent initial response, but falls off too fast
		//return (float) level / 32768f; 

		//y = log10((x + 1000) / 1000) / log10(33)
		//Low levels may get a bit clipped, but overall this maps pretty well to
		// what sounds / looks correct.  This formula found by trial an error,
		// not by using any proper scientific approaches.
		return (float) Math.log10((level + 1000) / 1000) / 1.5f; 
	}
	
	public static void main(String[] args) throws Exception {
		Sample s = new JoalSample("Cymbal/Ride/Zildjian A Ping 20/Bow");
		s.play(1f, 1f);
		Thread.sleep(1000);
		s.adjustLastVolume(1f, 0.5f);
		Thread.sleep(5000);
	}
}
