package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;


/**
 * The AudioSystemSample is a Sample implementation which uses the javax.sound.sampled
 * package for audio playback.  An implementation of this API should be included on all
 * major JVMs without any extra libraries neede.  However, it does have some limitations,
 * such as requiring that you manually fade out, that you must load multiple copies
 * of each sample if you want simultaneous playback, etc.  As well, some extra functionality
 * which JOAL samples include, such as EQ integration, etc is not available.  It is 
 * not recommended to use this source for anything other than initial experimentation.
 * 
 * The largest limitation of this Sample implementation is that it can only open 
 * 32 clips simultaneously.  Since there is one clip opened for each sample * each
 * count, this can obviously be overloaded rather quickly.
 * 
 * To configure this, you need to include a <param> item in the <sample> element in the
 * config file; this param must include an attribute name='count' and value='X', where
 * X is an integer between 1 and 32 inclusive.  This parameter determines how many instances
 * of the given sound should be loaded; this affects how many sounds can be simultaneously
 * played.  This number can be set based on a number of factors, including how long the 
 * sample is (the longer it is, the more samples you would need, up to a maximum of maybe 
 * 8), how frequently it is to be played, how many sample velocities are recorded (this 
 * number is applied to all of samples; if you have 16 samples and load each 8 times, you 
 * are looking at 128 samples being loaded in memory!), and how much available memory you 
 * have, etc.  For instance, a Ride cymbal with 16 levels may need 4 samples for each 
 * level; a ride cymbal with 2 levels may need 16 samples loaded for each level; a snare 
 * with 1 levels may need four samples loaded, while a snare with 32 levels may need only
 * one (or at most two) samples loaded for each.
 *  
 * @author wyatt
 *
 */
public class AudioSystemSample extends Sample {

	public final static String PARAM_COUNT = "count";
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	//Non-static resources
	private final Map<Integer, Queue<Clip>> clips = new ConcurrentHashMap<Integer, Queue<Clip>>();
	private final int count;

	public AudioSystemSample(String name) {
		super(name);
		
		int count = 2;
		
		if (count < 1)
			throw new RuntimeException("Count cannot be less than 1");
		if (count > 32)
			throw new RuntimeException("Count cannot be greater than 32");
		
		this.count = count;
		
		//Iterate through all the samples, and load a certain number of them into a circular queue.
		// This allows us to play that many samples at the same time.  This number can be set based on 
		// a number of factors, including how long the sample is (the longer it is, the more samples
		// you would need, up to a maximum of maybe 8), how frequently it is to be played,
		// how many sample velocities are recorded (this number is applied to all of samples; if you
		// have 16 samples and load each 8 times, you are looking at 128 samples being loaded
		// in memory!), and how much available memory you have, etc.  For instance, a Ride cymbal 
		// with 16 levels may need 4 samples for each level; a ride cymbal with 2 levels may need 
		// 16 samples loaded for each level; a snare with 1 levels may need four samples loaded,
		// while a snare with 32 levels may need only one (or at most two) samples loaded for each. 
		for (File sampleFile : sampleFiles) {
			//SampleNumber is the 00, 01, etc which is based on the sample's relative velocity.
			int sampleNumber = Integer.parseInt(sampleFile.getName().replaceAll("[^0-9]", ""));
			
			if (clips.get(sampleNumber) == null)
				clips.put(sampleNumber, new CircularQueue<Clip>());
			else
				logger.warning("The clips map for sample number " + sampleNumber + " was not null.  I don't think this is supposed to happen.");
			
			for (int i = 0; i < count; i++){
				try {
					AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(sampleFile);
					AudioFormat format = audioInputStream.getFormat();
					if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
						format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
								format.getSampleRate(),
								format.getSampleSizeInBits() * 2,
								format.getChannels(),
								format.getFrameSize() * 2,
								format.getFrameRate(),
								true);        // big endian
						audioInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
					}

					// Create the clip
					DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat(), ((int) audioInputStream.getFrameLength() * format.getFrameSize()));
					Clip clip = (Clip) AudioSystem.getLine(info);

					// This method does not return until the audio file is completely loaded
					clip.open(audioInputStream);

					clips.get(sampleNumber).add(clip);
				}
				catch (Exception e){
					logger.log(Level.SEVERE, "Could not load clip", e);
				}
			}
		}
	}

	public int getCount() {
		return count;
	}

	public void play(float volume, float gain){
		int sampleCount = clips.keySet().size();
		if (sampleCount == 0)
			throw new RuntimeException("No samples found for " + getName());
		
		//Figure out which sample to use based on volume
		int sampleNumber = getVolumeToSampleNumberMapping(sampleCount, volume);
		if (clips.get(sampleNumber).size() == 0)
			throw new RuntimeException("No sample loaded for sample number " + sampleNumber);
		
		Clip clip = clips.get(sampleNumber).poll();

		if (clip.isRunning())
			clip.stop();

		clip.setFramePosition(0);

		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
		gainControl.setValue(dB);
		clip.start();
	}
	
	@Override
	public void adjustLastVolume(float rawVolume, float gain) {
		logger.warning("adjustLastVolume() not implemented.");
	}

	public void stop(long fadeOutPeriod){
		final int fadeTimeMillis = 200;

		for (int i = 0; i < fadeTimeMillis / 10; i++){
			for (Integer sampleNumber : clips.keySet()) {
				for (int j = 0; j < count; j++){
					Clip clip = clips.get(sampleNumber).poll();
					FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
					gainControl.setValue(gainControl.getValue() - 5);
					System.out.println("Reducing volume to " + gainControl.getValue());

					try {
						Thread.sleep(10);
					}
					catch(InterruptedException ie){}
				}
			}
		}
	}
	
//	@Override
//	public void stopImmediately() {
//		throw new RuntimeException("Method not implemented.");
//	}
	
	@Override
	public void stopLastSample() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public float getLevel() {
//		clips.get(0).peek().
		return 0;
	}
	
	private class CircularQueue<E> extends ConcurrentLinkedQueue<E> {
		private static final long serialVersionUID = 1l;

		/**
		 * Returns the head element in the queue, and re-inserts it at the tail.
		 */
		public E poll() {
			E e = super.poll();
			this.offer(e);
			return e;
		}
	}
}
