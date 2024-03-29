package ca.digitalcave.drumslave.model.hardware;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

/**
 * This class provides a hybrid Multiton-like access to the Pad object.  
 * Objects are initially created using the Sample constructor, but can be
 * accessed later using the Multiton static getPad(name) method.
 * 
 * This class (and others in DrumSlave) provide easy thread-safe access
 * to various configuration elements without needing to pass around a 
 * reference to them, and without needing to traverse a singleton instance
 * of the configuration.
 *  
 * @author wyatt
 *
 */
public class Pad implements Comparable<Pad> {

	private final static Map<String, Pad> pads = new ConcurrentHashMap<String, Pad>();

	private final String name;
	private Map<String, Zone> zones;
	
	/**
	 * Clears all the pads from the static map.  Used before loading a new config file.
	 */
	protected static void clearPads(){
		pads.clear();
	}

	/**
	 * Returns a collection of all pads currently defined. 
	 * @return
	 */
	public static Collection<Pad> getPads(){
		return Collections.unmodifiableCollection(pads.values());
	}
	
	/**
	 * Returns the Pad object associate with the given name.  If no such Pad
	 * object exists, returns null. 
	 * @param name
	 * @return
	 */
	public static Pad getPad(String name){
		return pads.get(name);
	}
		
	/**
	 * Creates a new Pad object, and adds it to the static pads mapping.  You cannot
	 * create more than one Pad with any given name.
	 * @param name
	 */
	protected Pad(String name) {
		if (name == null)
			throw new RuntimeException("A pad's name must not be null.");
		
		if (pads.get(name) != null)
			throw new RuntimeException("Pad " + name + " already exists.  Use Pad.getPad(name) to retrieve an existing pad.");
		
		this.name = name;
		this.zones = new ConcurrentHashMap<String, Zone>();
		
		pads.put(name, this);
	}
	
	/**
	 * Returns the name associated with this Pad object.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	protected void addZone(Zone zone) {
		if (zone == null)
			throw new RuntimeException("Zone cannot be null");
		if (zones.get(zone.getName()) != null)
			throw new RuntimeException("Zone " + zone.getName() + " has already been added to pad " + getName());
		if (!this.equals(zone.getPad()))
			throw new RuntimeException("You can only add a zone to a pad if zone.getPad() equals the pad you are adding the zone to.");
		
		zones.put(zone.getName(), zone);
	}
	
	/**
	 * Returns all the zones associated with this Pad.
	 * @return
	 */
	public Collection<Zone> getZones() {
		return Collections.unmodifiableCollection(zones.values());
	}
	
	@Override
	public String toString() {
		return getName() + ":" + getZones();
	}
	
	/**
	 * Returns the highest level from all child zones
	 * @return
	 */
	public float getLevel(){
		float max = 0f;
		if (SampleMapping.getSampleMappingsByPad(SampleMapping.getSelectedSampleGroup(), this.getName()) == null)
			return max;
		
		Collection<String> sampleNames = SampleMapping.getSampleMappingsByPad(SampleMapping.getSelectedSampleGroup(), this.getName()).values();
		for (String sampleName : sampleNames) {
			Sample sample = Sample.getSample(sampleName);
			if (sample != null)
				max = Math.max(max, sample.getLevel());
		}
		return max;
	}
	
	/**
	 * Stops playback from all samples playing on this pad, except those listed in 
	 * the exemptedLogics set, fading out over the given fadeOutPeriod.
	 * @param exemptedLogicals
	 * @param fadeOutPeriod
	 */
	public void stop(long fadeOutPeriod, String... exemptedLogicals){
		Map<String, String> sampleMappings = SampleMapping.getSampleMappingsByPad(SampleMapping.getSelectedSampleGroup(), this.getName()); 
		if (sampleMappings != null){
			Collection<String> sampleNames = sampleMappings.values();
			Collection<String> exemptedSamples = new HashSet<String>();

			//Figure out which samples are excluded, based on logical names
			for (String exemptedLogical : exemptedLogicals) {
				exemptedSamples.add(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), this.getName(), exemptedLogical));
			}

			if (exemptedLogicals != null){
				Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Removing " + exemptedSamples + " from " + sampleNames);
				sampleNames.removeAll(exemptedSamples);
			}


			for (String sampleName : sampleNames) {
				Sample sample = Sample.getSample(sampleName);
				if (sample != null){
					sample.stop(fadeOutPeriod);
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Stopping " + sampleName);
				}
			}		
		}
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public int compareTo(Pad o) {
		return this.getName().compareTo(o.getName());
	}
}
