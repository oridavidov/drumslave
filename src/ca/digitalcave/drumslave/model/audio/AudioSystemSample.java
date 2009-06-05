package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
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
 * of each sample if you want simultaneous playback, etc.
 * 
 * The largest limitation of this Sample implementation is that it can only open 
 * 32 clips simultaneously.  Since there is one clip opened for each sample * each
 * count, this can obviously be overloaded rather quickly; as such, this Sample class
 * is best used for testing or small (one or two pads) implementations, with low
 * sample level numbers and low counts / level.   
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

	public AudioSystemSample(String name, Map<String, String> params) {
		super(name, params);
		
		if (params.get(PARAM_COUNT) == null)
			throw new RuntimeException("AudioSystemSample requires a parameter named '" + PARAM_COUNT + "'");
		if (!params.get(PARAM_COUNT).matches("[0-9]+"))
			throw new RuntimeException("AudioSystemSample requires a parameter named '" + PARAM_COUNT + "' containing a postive integer");

		int count = Integer.parseInt(params.get(PARAM_COUNT));
		
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
	
	@Override
	public Map<String, String> getParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(PARAM_COUNT, getCount() + "");
		return params;
	}

	public int getCount() {
		return count;
	}

	public void play(float volume){
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

	public void stop(){
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
}
