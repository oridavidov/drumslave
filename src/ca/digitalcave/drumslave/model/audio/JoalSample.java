package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import net.java.games.joal.AL;
import net.java.games.joal.ALFactory;
import net.java.games.joal.util.ALut;

public class JoalSample extends Sample {

	private static AL al = ALFactory.getAL();

	// Position of the listener.
	private static float[] listenerPos = { 0.0f, 0.0f, 0.0f };
	// Velocity of the listener.
	private static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
	// Orientation of the listener. (first 3 elems are "at", second 3 are "up")
	private static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };


	static {
		ALut.alutInit();
		al.alGetError();

        al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
        al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
        al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
	}

	int[] source;
	private int loadALData(int sourceIndex, File sample) {
		
		// Buffers hold sound data.
		int[] buffer = new int[1];
		// Sources are points emitting sound.
//		int[] source = new int[1];
		
		// Position of the source sound.
		float[] sourcePos = { 0.0f, 0.0f, 0.0f };
		// Velocity of the source sound.
		float[] sourceVel = { 0.0f, 0.0f, 0.0f };


		// variables to load into

		int[] format = new int[1];
		int[] size = new int[1];
		ByteBuffer[] data = new ByteBuffer[1];
		int[] freq = new int[1];
		int[] loop = new int[1];

		// Load wav data into a buffer.
		al.alGenBuffers(1, buffer, 0);
		if (al.alGetError() != AL.AL_NO_ERROR)
			return AL.AL_FALSE;

		ALut.alutLoadWAVFile(sample.getAbsolutePath(),
				format,
				data,
				size,
				freq,
				loop);
		al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);

		// Bind buffer with a source.
		al.alGenSources(1, source, 0);

		if (al.alGetError() != AL.AL_NO_ERROR)
			return AL.AL_FALSE;

		al.alSourcei(source[0], AL.AL_BUFFER, buffer[0]);
		al.alSourcef(source[0], AL.AL_PITCH, 1.0f);
		al.alSourcef(source[0], AL.AL_GAIN, 1.0f);
		al.alSourcefv(source[0], AL.AL_POSITION, sourcePos, 0);
		al.alSourcefv(source[0], AL.AL_VELOCITY, sourceVel, 0);
		al.alSourcei(source[0], AL.AL_LOOPING, loop[0]);

		// Do another error check and return.
		if (al.alGetError() == AL.AL_NO_ERROR)
			return AL.AL_TRUE;

		return AL.AL_FALSE;
	}

	public JoalSample(String name, Map<String, String> params) {
		super(name, params);

		source = new int[sampleFiles.size()];
		for (int i = 0; i < sampleFiles.size(); i++){
			loadALData(i, sampleFiles.get(i));			
		}
	}

	@Override
	public void play(float volume) {
		al.alSourcePlay(source[0]);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static void main(String[] args) throws Exception {
		JoalSample sample = new JoalSample("Cymbal/Ride/Zildjian A Ping 20/Bow", new HashMap<String, String>());
		sample.play(0.27f);
		Thread.sleep(1000);
		sample.play(1.0f);
		Thread.sleep(4000);
		System.exit(0);
	}
}
