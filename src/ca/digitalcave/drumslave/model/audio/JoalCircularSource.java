package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.java.games.joal.AL;
import net.java.games.joal.ALFactory;
import net.java.games.joal.util.ALut;

/**
 * This is a wrapper around JOAL functions to play a sound.  This wrapper 
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
 * but this can be adjusted as you see fit.
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
 * needs; however, some hardware based systems appear to be much lower than that).  Regardles,
 * this is not a high priority modification, as it works fine for now, but it is something
 * to think about. 
 *  
 * @author wyatt
 *
 */
public class JoalCircularSource {

	private static final AL al;

	// Position of the source sound.
	private static final float[] sourcePos = { 0.0f, 0.0f, 0.0f };
	// Velocity of the source sound.
	private static final float[] sourceVel = { 0.0f, 0.0f, 0.0f };

	private static int loadedSources = 0;

	static {
		System.out.println("Calling ALut.alutInit() on thread " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ")");
		ALut.alutInit();
		al = ALFactory.getAL();
		al.alGetError();

		al.alListenerfv(AL.AL_POSITION, new float[]{0.0f, 0.0f, 0.0f}, 0);
		al.alListenerfv(AL.AL_VELOCITY, new float[]{0.0f, 0.0f, 0.0f} , 0);
		al.alListenerfv(AL.AL_ORIENTATION, new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }, 0);
	}

	private final int MAX_SIMULTANEOUS = 8;
	private int sourceCounter = 0;
	private int[] sources = new int[MAX_SIMULTANEOUS]; //First index is from 0 - (MAX_SIMULTANEOUS - 1), and will loop based on sourceCounter
	private int[] buffers = new int[1]; //We share the same buffer with all sources in a given JoalCircularSource instance.
	private final ByteBuffer bufferData; //We need to store the buffer data so that we can find the value at a given position, for VU meter.

	private final File sample;

	public JoalCircularSource(File sample) {
		
		this.sample = sample;

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
			throw new RuntimeException("Error encountered while loading source #" + loadedSources + " for " + sample.getAbsolutePath() + " error #" + error);
		}

		//Generate sources and link to buffer, set defaults, etc
		al.alGenSources(MAX_SIMULTANEOUS, sources, 0);
		for (int i = 0; i < MAX_SIMULTANEOUS; i++) {
			al.alSourcei(sources[i], AL.AL_BUFFER, buffers[0]);
			al.alSourcef(sources[i], AL.AL_PITCH, 1.0f);
			al.alSourcef(sources[i], AL.AL_GAIN, 1.0f);
			al.alSourcefv(sources[i], AL.AL_POSITION, sourcePos, 0);
			al.alSourcefv(sources[i], AL.AL_POSITION, sourceVel, 0);
			al.alSourcei(sources[i], AL.AL_LOOPING, AL.AL_FALSE);
		}
	}

	/**
	 * Start playing the next source in the circular queue immediately,
	 * at the given volume.  This method maps directly to Source.play().
	 * @param volume
	 */
	public void play(float volume) {
		System.out.println("Playing " + sample.getAbsolutePath());
		al.alSourcef(sources[sourceCounter], AL.AL_GAIN, volume);
		al.alSourcePlay(sources[sourceCounter]);
		sourceCounter = (sourceCounter + 1) % MAX_SIMULTANEOUS;
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
//		return 0;
	}
}
