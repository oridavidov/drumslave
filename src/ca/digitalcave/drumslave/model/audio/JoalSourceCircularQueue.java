package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.joal.AL;
import net.java.games.joal.ALFactory;
import net.java.games.joal.util.ALut;

/**
 * This is a wrapper around JOAL functions to play a single sample.  This wrapper 
 * will do the following:
 * 
 *   1) Load the sound multiple times
 *   2) When a play request comes through, play the next sound in the circular queue
 *   3) When a stop request comes through, stop all sounds from playing.
 *   
 * This lets us play the same sound multiple times in a row at different volumes. The
 * size of the circular queue determines how many you can play in a row; once you 
 * loop back to the first source, any volume adjustments will affect the currently playing
 * soure as well.  From my experiments, 8 seems to be a decent size for the circular queue,
 * but this can be adjusted as you see fit; there is not too much of a memory hit to increase
 * this number.  Note however that there is one Joal Source created for each entry in the
 * queue, and some platforms may not be able to create large numbers of sources.  If this
 * becomes a problem, we may need to change the logic of this class to allocate a source
 * only when needed (perhaps keeping one ready at all times, and using that for the next
 * request).
 * 
 * All samples which are read from this class MUST be 16bit integer / sample, 44100Hz, 
 * uncompressed .wav files.  Even if OpenAL will read other formats, the getLevel() method
 * will not work properly for other file formats.
 * 
 * The methods in this class don't map directly to the Source interface; to allow some
 * functions to work in a more efficient manner, some things (like fading out over a short
 * interval to simulate a muted cymbal) must be handled from the JoalSample instead of this
 * class.  In these cases, we simply make methods available as required.
 * 
 * The playback methods (play, setGain, etc) in this class MUST NOT block for any length
 * of time.  DrumSlave perceived latency can be directly affected by this.
 * 
 * TODO: Instead of loading a source for each sound here, we may want to have a pool of 
 * sources, set to the maximum which the system allows (apparently this differs from system
 * to system), and load up the data in buffers instead.  When a sound is played, load the
 * buffer to the next available source in the pool.  Depending on how long this takes, it
 * may introduce unacceptable overhead; however, according to some articles I have read,
 * some systems only allow a small maximum number of sources to be loaded at the same time
 * (for reference, on the Mac this appears to be 1000, which is more than enough for our
 * needs; however, some hardware based systems appear to be much lower than that).  Regardless,
 * this is not a high priority modification, as it works fine for now, but it is something
 * to think about. 
 *  
 * @author wyatt
 *
 */
public class JoalSourceCircularQueue {

	private static final AL al;

	//TODO Allow users to customize the layout of their drum set, and change the position
	// of each source.
	// Position of the source sound.
	private static final float[] sourcePos = { 0.0f, 0.0f, 0.0f };
	// Velocity of the source sound.
	private static final float[] sourceVel = { 0.0f, 0.0f, 0.0f };

//	private static int loadedSources = 0;

	/**
	 * This should only happen once per program run.  Running ALut.alutInit() multiple
	 * times will cause an error; setting the listener position doesn't matter, but
	 * there is no point in doing it multiple times, as there is just one listener anyway.
	 */
	static {
		ALut.alutInit();
		al = ALFactory.getAL();
		al.alGetError();

		al.alListenerfv(AL.AL_POSITION, new float[]{0.0f, 0.0f, 0.0f}, 0);
		al.alListenerfv(AL.AL_VELOCITY, new float[]{0.0f, 0.0f, 0.0f} , 0);
		al.alListenerfv(AL.AL_ORIENTATION, new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }, 0);
	}

	private final int MAX_SIMULTANEOUS = 4; //How many Sources to include in the circular buffer
	private int sourceCounter = 0; //Internal counter to find the next source
	private int[] sources = new int[MAX_SIMULTANEOUS]; //First index is from 0 - (MAX_SIMULTANEOUS - 1), and will loop based on sourceCounter
	private int[] buffers = new int[1]; //We share the same buffer with all sources in a given JoalCircularSource instance.
	private final ByteBuffer bufferData; //We need to store the buffer data so that we can find the value at a given position, for VU meter.
	
//	private static int count = 0;

	public JoalSourceCircularQueue(File sample) {
		Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Starting to load buffer and sources for sample " + sample.getAbsolutePath());
		
		int[] format = new int[1];
		int[] size = new int[1];
		ByteBuffer[] data = new ByteBuffer[1];
		int[] freq = new int[1];
		int[] loop = new int[1];

		//Generate buffer and load PCM data
		al.alGenBuffers(1, buffers, 0);
		InputStream is;
		try {
			is = new FileInputStream(sample);
		}
		catch (IOException ioe){
			throw new RuntimeException("Cannot load file", ioe);
		}
		ALut.alutLoadWAVFile(is, format, data, size, freq, loop);
		al.alBufferData(buffers[0], format[0], data[0], size[0], freq[0]);
		
		bufferData = data[0].asReadOnlyBuffer();

		//Check for error after buffer loading
		int error = al.alGetError();
		if (error != AL.AL_NO_ERROR) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while loading buffer for " + sample.getAbsolutePath() + "; error #" + error);
		}

		//Generate sources and link to buffer, set defaults, etc
		al.alGenSources(MAX_SIMULTANEOUS, sources, 0);
		for (int i = 0; i < MAX_SIMULTANEOUS; i++) {
			al.alSourcei(sources[i], AL.AL_BUFFER, buffers[0]);
			error = al.alGetError();
			if (error != AL.AL_NO_ERROR) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while generating source for " + sample.getAbsolutePath() + "; error #" + error);
			}
			
			al.alSourcef(sources[i], AL.AL_PITCH, 1.0f);
			error = al.alGetError();
			if (error != AL.AL_NO_ERROR) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while setting pitch for " + sample.getAbsolutePath() + "; error #" + error);
			}
			
			al.alSourcef(sources[i], AL.AL_GAIN, 1.0f);
			al.alSourcef(sources[i], AL.AL_MIN_GAIN, 0.0f);
			al.alSourcef(sources[i], AL.AL_MAX_GAIN, 1.0f);
			error = al.alGetError();
			if (error != AL.AL_NO_ERROR) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while setting current, min, and max gain for " + sample.getAbsolutePath() + "; error #" + error);
			}			
			
			error = al.alGetError(); al.alSourcefv(sources[i], AL.AL_POSITION, sourcePos, 0);
			error = al.alGetError();
			if (error != AL.AL_NO_ERROR) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while setting playback position for " + sample.getAbsolutePath() + "; error #" + error);
			}			
			
			al.alSourcefv(sources[i], AL.AL_POSITION, sourceVel, 0);
			error = al.alGetError();
			if (error != AL.AL_NO_ERROR) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while setting source velocity for " + sample.getAbsolutePath() + "; error #" + error);
			}			
			
			al.alSourcei(sources[i], AL.AL_LOOPING, AL.AL_FALSE);
			error = al.alGetError();
			if (error != AL.AL_NO_ERROR) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error encountered while disabling looping for " + sample.getAbsolutePath() + "; error #" + error);
			}			
		}
		
		Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Finished loading buffer and sources for sample " + sample.getAbsolutePath());
	}

	/**
	 * Start playing the next source in the circular queue immediately,
	 * at the given volume.  This method maps directly to Source.play().
	 * @param volume
	 */
	public void play(float volume) {
		//We increment the source counter before, not after; that way we have access to
		// the most recently played sample up until the time a sample is played.
		sourceCounter = (sourceCounter + 1) % MAX_SIMULTANEOUS;
		
		al.alSourcef(sources[sourceCounter], AL.AL_GAIN, volume);
		al.alSourcePlay(sources[sourceCounter]);
	}
	
	public void adjustLastVolume(float volume) {
		al.alSourcef(sources[sourceCounter], AL.AL_GAIN, volume);
	}

	/**
	 * Sets the gain for all sources based on the given gain adjustment.
	 * This sets the gain to be the current gain * the adjustment value.
	 * To cut the gain in half, for instance, use the gainAdjustment value 0.5.
	 * 
	 * This function will apply immediately to all source, whether they are playing or not.
	 * 
	 * @param gainAdjustment
	 */
	public void setGain(float gainAdjustment) {
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){
			float[] data = new float[1];
			al.alGetSourcef(sources[i], AL.AL_GAIN, data, 0);
			float volume = data[0] * gainAdjustment;
			al.alSourcef(sources[i], AL.AL_GAIN, volume);
		}
	}
	
	/**
	 * Stops the playback of all sources in the circular queue immediately.
	 */
	public void stop() {
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){
			al.alSourceStop(sources[i]);
		}
	}

	/**
	 * Looks at the position of all playing sources, and notes the raw level value in the
	 * buffer (looks about 100ms forward in each buffer).  Returns the highest value in
	 * all the sources.  
	 * @return
	 */
	public int getLevel() {
		int maxLevel = 0;
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){
			int[] data = new int[1];
			al.alGetSourcei(sources[i], AL.AL_BYTE_OFFSET, data, 0);
			int position = data[0] / 2 * 2;  //Start on even index to be sure we get the byte order correct.
			if (position > 0){ //Don't show this for non-playing sources
				int j = 0;
				int rawMax = 0;
				//The more frames we iterate over, the more accurate the level will be.
				// 8820 will get us 100ms of sample data (4410 for a single channel, * 2 
				// for stereo); this should be about right, as our VU meters should update 
				// about that frequently. 
				for (; j < 4410 && position + j + 1 < bufferData.capacity(); j+=2){
					int highByte = bufferData.get(position + j + 1) << 8; //MSB is signed and shifted
					int lowByte = bufferData.get(position + j) & 0xFF; //LSB is unsigned
					int sample = Math.abs(highByte + lowByte);
					rawMax = Math.max(rawMax, sample);
				}
				
				//Factor in the gain for the given sample 
				float[] gainData = new float[1];
				al.alGetSourcef(sources[i], AL.AL_GAIN, gainData, 0);
				
				maxLevel = Math.max(maxLevel, (int) (gainData[0] * rawMax));
			}
		}
		return maxLevel;
	}
}
