package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
	// Position of the source sound.
	private static float[] sourcePos = { 0.0f, 0.0f, 0.0f };
	// Velocity of the source sound.
	private static float[] sourceVel = { 0.0f, 0.0f, 0.0f };


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
	
	private int loadALData(int sourceIndex, File sample) {

		int[] format = new int[1];
		int[] size = new int[1];
		ByteBuffer[] data = new ByteBuffer[1];
		int[] freq = new int[1];
		int[] loop = new int[1];

		// load wav data into buffers

		al.alGenBuffers(MAX_SIMULTANEOUS, buffers, 0);
		if (al.alGetError() != AL.AL_NO_ERROR) {
			return AL.AL_FALSE;
		}
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){			

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
//			ALut.alutUnloadWAV(format[0], data[0], size[0], freq[0]);
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

		return AL.AL_FALSE;
	}

	public JoalSample(String name) {
		super(name);

		for (int i = 0; i < sampleFiles.size(); i++){
			loadALData(i, sampleFiles.get(i));
			volumes[i] = 0f;
		}
	}

	@Override
	public void play(float volume) {
		al.alSourcef(sources[sourceCounter], AL.AL_GAIN, volume);
		al.alSourcePlay(sources[sourceCounter]);
		volumes[sourceCounter] = volume;
		sourceCounter = (sourceCounter + 1) % MAX_SIMULTANEOUS;
	}

	@Override
	public void stop() {
		//Fade out logarithmically over 10 iterations (approx. 200 ms)
		for (int i = 0; i < 10; i++){
			for (int j = 0; j < MAX_SIMULTANEOUS; j++){
				al.alSourcef(sources[j], AL.AL_GAIN, volumes[j]);
				volumes[j] = volumes[j] / 2;
			}
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException ie){}
		}
		for (int i = 0; i < MAX_SIMULTANEOUS; i++){
			al.alSourceStop(sources[i]);
		}
	}

	@Override
	public float getLevel() {
		return 0;
	}

	public static void main(String[] args) throws Exception {
		JoalSample sample = new JoalSample("Cymbal/Ride/Zildjian A Ping 20/Bow");
		sample.play(1.0f);
		System.out.println(sample.getLevel());
		Thread.sleep(500);
		sample.stop();
		Thread.sleep(4000);
		System.exit(0);
	}
}
