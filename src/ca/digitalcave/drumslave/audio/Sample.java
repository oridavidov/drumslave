package ca.digitalcave.drumslave.audio;

import java.io.File;
import java.util.Queue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

public class Sample {

	private final Queue<Clip> clips = new CircularQueue<Clip>();
	private final int count;

	public Sample(String name, int count) {
		this.count = count;
		
		for (int i = 0; i < count; i++){
			try {
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(name));
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
//				FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
//				gainControl.setValue(Float.MIN_VALUE);
//				clip.setFramePosition(clip.getFrameLength());
//				clip.start();
				
				clips.add(clip);
			}
			catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	}
	
	public void play(float volume){
		Clip clip = clips.poll();
		
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
			for (int j = 0; j < count; j++){
				Clip clip = clips.poll();
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
