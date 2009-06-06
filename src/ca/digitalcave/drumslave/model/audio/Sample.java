package ca.digitalcave.drumslave.model.audio;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a hybrid Multiton-like access to the Sample object.  
 * Objects are initially created using the Sample constructor, but can be
 * accessed later using the Multiton static getSample(name) method.
 * 
 * This class (and others in DrumSlave) provide easy thread-safe access
 * to various configuration elements without needing to pass around a 
 * reference to them, and without needing to traverse a singleton instance
 * of the configuration.
 * 
 * Recorded samples on the file system should be named in a certain way to.
 * allow them to be used by this class.  Samples must be in the format:
 * 		folder1/folder2/.../folderX/Y.wav
 * where:
 * 	-folder1, folder2, ..., folder[X-1] are the logical hierarchy of the sample
 *  -folderX is the name of the sample type
 *  -Y is a two digit number from 00 to 99, which tells the relative volume which 
 *      the sample was recorded at.  If there is any number X where X > 0, there
 *      must also exist a number [X-1].  These MUST start at 00 and increment by one
 *      for each volume level decrease (00 is loudest, 01 is next loudest, ...
 *      99 (if it exists) is softest).  These samples MUST be normalized such that
 *      all volumes for all samples for a given instrument are equal (Drum Slave will
 *      apply a volume reduction to samples, and if the raw samples alraedy have had
 *      volume adjustments, the volume will fall off too quickly).  These samples 
 *      SHOULD be mapped in a linear fashion, e.g if there are two samples, 00 is 
 *      recorded from the loudest hit, and 01 is recorded from a hit which is half as
 *      strong as 00). 
 *  
 * For instance, assume we have a ride cymbal sampled.  Possible sample names could include:
 * 
 *  Cymbal/Ride/Zildjian A Ping 20"/Bow/00.wav
 *  Cymbal/Ride/Zildjian A Ping 20"/Bow/01.wav
 *  Cymbal/Ride/Zildjian A Ping 20"/Bow/02.wav
 *  
 *  Cymbal/Ride/Zildjian A Ping 20"/Bell/00.wav
 *  
 *  Cymbal/Ride/Zildjian A Ping 20"/Edge/00.wav
 *  Cymbal/Ride/Zildjian A Ping 20"/Edge/01.wav
 *  
 * There is no special need to keep to a given hierarchy for samples, as the Drum Slave sample
 * mapping window will search through the samples folder and find all samples.  However, it 
 * does help to mantain large samples repositories if you maintain the hierarchy as indicated
 * above. 
 *  
 * @author wyatt
 *
 */
public abstract class Sample {

	//Static resources, related to the Multiton functionality of this class
	private final static Map<String, Sample> samples = new ConcurrentHashMap<String, Sample>();
	public static Sample getSample(String name){
		return samples.get(name);
	}
	public static Collection<Sample> getSamples(){
		return Collections.unmodifiableCollection(samples.values());
	}
	protected static void clearSamples(){
		samples.clear();
	}
	/**
	 * Returns the sample folder based on the given name.  Sample names must be in 
	 * the format:
	 * 		folder1/folder2/.../folderX
	 * 
	 * Basically, this will point to the folder which contains the 00.wav, 01.wav, etc
	 * sample files.
	 * 
	 * There is no error checking done in this method
	 * 
	 * @param name
	 * @return
	 */
	private static File getSampleFolder(String name){
		return new File("samples/" + name);
	}



	//Non-static resources
	private final String name;
	protected List<File> sampleFiles;

	public Sample(String name, Map<String, String> params) {
		if (name == null)
			throw new RuntimeException("Sample name cannot be null.");
		if (samples.get(name) != null)
			throw new RuntimeException("Sample " + name + " has already been loaded.  You cannot load a sample more than once.");

		//Find the sample folder based on the given name, and find all files within it
		// which match the regex "[0-9]{2}.wav".
		File sampleFolder = getSampleFolder(name);
		sampleFiles = Arrays.asList(sampleFolder.listFiles(new FileFilter(){
			public boolean accept(File file) {
				return (file.isFile() && file.getName().matches("[0-9]{2}.wav"));
			}
		}));
		if (sampleFiles.size() == 0)
			throw new RuntimeException("No samples were found in folder " + sampleFolder.getAbsolutePath());


		
		//Verify that the samples are in proper order, starting at 00.
		Collections.sort(sampleFiles, new Comparator<File>(){
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (int i = 0; i < sampleFiles.size(); i++){
			int sampleNumber = Integer.parseInt(sampleFiles.get(i).getName().replaceAll("[^0-9]", ""));
			if (i != sampleNumber)
				throw new RuntimeException("The sample " + sampleFiles.get(i).getAbsolutePath() + " does not follow the correct sequence; it should be named " + i);
		}

		//Finally all the sanity checks are done.  Save the constructor values, and continue.
		this.name = name;

		samples.put(name, this);
	}

	public String getName() {
		return name;
	}
	
	public abstract Map<String, String> getParams();

	public abstract void play(float volume);

	public abstract void stop();
	
	/**
	 * 	Defines a linear mapping between volume and sample number.  This is currently
	 *  implemented as a linear function, returning a number between 0 and total_samples - 1.
	 *  Volume is on the X axis from 0 to 1, and the sample number is on the Y axis from 0 to N-1.
	 *  
	 * @param sampleCount
	 * @param volume
	 * @return
	 */
	protected int getVolumeToSampleNumberMapping(int sampleCount, float volume){
		int sampleNumber = (int) ((-1f * volume * sampleCount) + sampleCount);
		if (sampleNumber >= sampleCount - 1)
			sampleNumber = sampleCount - 1;
		if (sampleNumber < 0)
			sampleNumber = 0;
		return sampleNumber;
	}
}