package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class LWJGLSample extends Sample {

	/** Buffers hold sound data. */
	IntBuffer buffer = BufferUtils.createIntBuffer(1);

	/** Sources are points emitting sound. */
	IntBuffer source = BufferUtils.createIntBuffer(1);

	/** Position of the source sound. */
	FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

	/** Velocity of the source sound. */
	FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

	/** Position of the listener. */
	FloatBuffer listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

	/** Velocity of the listener. */
	FloatBuffer listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

	/** Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
	FloatBuffer listenerOri =
		BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });

	/**
	 * boolean LoadALData()
	 *
	 *  This function will load our sample data from the disk using the Alut
	 *  utility and send the data into OpenAL as a buffer. A source is then
	 *  also created to play that buffer.
	 */
	int loadALData() {
		// Load wav data into a buffer.
		AL10.alGenBuffers(buffer);

		if(AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		InputStream is = null;
		try {
			is = new FileInputStream(new File("samples/Cymbal/Ride/Zildjian A Ping 20/Bow/00.wav"));
		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
		WaveData waveFile = WaveData.create(is);
		AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();

		// Bind the buffer with the source.
		AL10.alGenSources(source);

		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		AL10.alSourcei(source.get(0), AL10.AL_BUFFER,   buffer.get(0) );
		AL10.alSourcef(source.get(0), AL10.AL_PITCH,    1.0f          );
		AL10.alSourcef(source.get(0), AL10.AL_GAIN,     1.0f          );
		AL10.alSource(source.get(0), AL10.AL_POSITION, sourcePos     );
		AL10.alSource(source.get(0), AL10.AL_VELOCITY, sourceVel     );
		// Do another error check and return.
		if (AL10.alGetError() == AL10.AL_NO_ERROR)
			return AL10.AL_TRUE;

		return AL10.AL_FALSE;
	}

	/**
	 * void setListenerValues()
	 *
	 *  We already defined certain values for the Listener, but we need
	 *  to tell OpenAL to use that data. This function does just that.
	 */
	void setListenerValues() {
		AL10.alListener(AL10.AL_POSITION,    listenerPos);
		AL10.alListener(AL10.AL_VELOCITY,    listenerVel);
		AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
	}

	/**
	 * void killALData()
	 *
	 *  We have allocated memory for our buffers and sources which needs
	 *  to be returned to the system. This function frees that memory.
	 */
	void killALData() {
		AL10.alDeleteSources(source);
		AL10.alDeleteBuffers(buffer);
	}

	public LWJGLSample(String name) {
		super(name);
	}

	@Override
	public void play(float volume) {
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public float getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void main(String[] args) throws Exception {
		//		LWJGLSample sample = new LWJGLSample("Cymbal/Ride/Zildjian A Ping 20/Bow");
		//		sample.play(0.27f);
		//		Thread.sleep(1000);
		//		sample.play(1.0f);
		//		Thread.sleep(4000);
		//		System.exit(0);

		// Initialize OpenAL and clear the error bit.
		try{
			AL.create();
		} catch (LWJGLException le) {
			le.printStackTrace();
			return;
		}
		AL10.alGetError();

		LWJGLSample s = new LWJGLSample("Cymbal/Ride/Zildjian A Ping 20/Bow");

		// Load the wav data.
		if(s.loadALData() == AL10.AL_FALSE) {
			System.out.println("Error loading data.");
			return;
		}

		s.setListenerValues();

		AL10.alSourcePlay(s.source.get(0));

		Thread.sleep(5000);

		s.killALData();
	}
}
