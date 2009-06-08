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
 * @author wyatt
 *
 */
public class JoalCircularSource {

	private static AL al = ALFactory.getAL();

	// Position of the listener.
	private static final float[] listenerPos = { 0.0f, 0.0f, 0.0f };
	// Velocity of the listener.
	private static final float[] listenerVel = { 0.0f, 0.0f, 0.0f };
	// Orientation of the listener. (first 3 elems are "at", second 3 are "up")
	private static final float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
	// Position of the source sound.
	private static final float[] sourcePos = { 0.0f, 0.0f, 0.0f };
	// Velocity of the source sound.
	private static final float[] sourceVel = { 0.0f, 0.0f, 0.0f };


	static {
		ALut.alutInit();
		al.alGetError();

		al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
		al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
		al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
	}

	private final int MAX_SIMULTANEOUS = 8;
	private int sourceCounter = 0;
	private int[] sources = new int[MAX_SIMULTANEOUS]; //First index is from 0 - (MAX_SIMULTANEOUS - 1), and will loop based on sourceCounter
	private int[] buffers = new int[MAX_SIMULTANEOUS];
	private float[] volumes = new float[MAX_SIMULTANEOUS]; //We need to keep track of the last used velocty (gain).  This lets us fade down from that when stop() is called.

	private final File sample;

	public JoalCircularSource(File sample) {
		this.sample = sample;

		int[] format = new int[1];
		int[] size = new int[1];
		ByteBuffer[] data = new ByteBuffer[1];
		int[] freq = new int[1];
		int[] loop = new int[1];

		// load wav data into buffers

		al.alGenBuffers(MAX_SIMULTANEOUS, buffers, 0);
		if (al.alGetError() != AL.AL_NO_ERROR) {
			throw new RuntimeException("Error encountered while loading sources for " + sample.getAbsolutePath());
		}
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){			
			volumes[i] = 0f;

			InputStream is;
			try {
				is = new FileInputStream(sample);
			}
			catch (IOException ioe){
				throw new RuntimeException("Cannot load file", ioe);
			}
			if (is == null)
				throw new RuntimeException("Cannot load file");
			ALut.alutLoadWAVFile(
					is,
					format,
					data,
					size,
					freq,
					loop);

			al.alBufferData(
					buffers[i],
					format[0],
					data[0],
					size[0],
					freq[0]);
		}

		al.alGenSources(MAX_SIMULTANEOUS, sources, 0);

		for (int i = 0; i < MAX_SIMULTANEOUS; i++) {
			al.alSourcei(sources[i], AL.AL_BUFFER, buffers[i]);
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
		volumes[sourceCounter] = volume;
		sourceCounter = (sourceCounter + 1) % MAX_SIMULTANEOUS;
	}

	/**
	 * Sets the gain for all sources based on the given gain adjustment.
	 * This sets the gain to be the current gain * the adjustment value.
	 * To cut the gain in half, for instance, use the gainAdjustment value 0.5. 
	 * @param gainAdjustment
	 */
	public void setGain(float gainAdjustment) {
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){
			volumes[i] = volumes[i] * gainAdjustment;
			al.alSourcef(sources[i], AL.AL_GAIN, volumes[i]);
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

	public float getLevel() {
		return 0;
	}
}
