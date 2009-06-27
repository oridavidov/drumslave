package ca.digitalcave.drumslave.gui.config.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import ca.digitalcave.drumslave.DrumSlave;

public class SamplesComboBoxModel extends DefaultComboBoxModel {
	public static final long serialVersionUID = 0l;

	private final JCheckBox matchSampleNamesToPad;
	private final JComboBox padChooser;
	
	public SamplesComboBoxModel(JComboBox padChooser, JCheckBox matchSampleNamesToPad) {
		this.matchSampleNamesToPad = matchSampleNamesToPad;
		this.padChooser = padChooser;
		
		updateModel();
	}
	
	public void updateModel(){
		this.removeAllElements();
		
		boolean matchSamples = matchSampleNamesToPad.isSelected();
		String padName = padChooser.getSelectedItem().toString();
		Pattern pattern = Pattern.compile(".*" + padName.replaceAll("[^a-zA-Z]", "") + ".*", Pattern.CASE_INSENSITIVE);
		
		File samplesFolder = DrumSlave.getSamplesFolder();
		List<String> samples = new ArrayList<String>(getSampleNamesRecursive(samplesFolder));

		Collections.sort(samples);

		this.addElement(null);
		for (String sample : samples) {
			//Only add samples which match the pad name somewhat.
			if (!matchSamples || pattern.matcher(sample.replaceAll("[^a-zA-Z]", "")).matches())
				this.addElement(sample);
		}
	}

	/**
	 * Recursively scans files underneath the given folder, and returns 
	 * any folders which contain files with extension .wav whose names are
	 * exactly two digits. 
	 * @param file
	 * @return
	 */
	private Set<String> getSampleNamesRecursive(File folder){
		Set<String> samples = new HashSet<String>();

		File[] children = folder.listFiles();
		if (children != null){
			for (File child : children) {
				if (child.isFile() 
						&& child.getName().matches("[0-9]{2}.wav"))
					samples.add(folder.getAbsolutePath().replaceAll("^" + new File("samples").getAbsolutePath() + "/", ""));
				else {
					samples.addAll(getSampleNamesRecursive(child));				
				}
			}
		}

		return samples;
	}
}