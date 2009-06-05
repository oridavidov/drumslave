package ca.digitalcave.drumslave.hardware;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.audio.Sample;
import ca.digitalcave.drumslave.config.SampleManager;

public class Pad {

	private final static Map<String, Pad> pads = new ConcurrentHashMap<String, Pad>();
	
	private final String name;
	private final Map<String, Zone> zones;
	
	public static Pad getPad(String name){
		if (pads.get(name) == null)
			pads.put(name, new Pad(name));
		return pads.get(name);
	}
	
	private Pad(String name) {
		this.name = name;
		this.zones = new ConcurrentHashMap<String, Zone>();
	}
	
	public void addZone(Zone zone){
		if (!this.equals(zone.getPad()))
			throw new RuntimeException("Attempted to add a zone to pad " + getName() + " when the zone is associated with pad " + zone.getPad().getName());
		zones.put(zone.getName(), zone);
	}
	
	public String getName() {
		return name;
	}
	
	public Collection<Zone> getZones() {
		return Collections.unmodifiableCollection(zones.values());
	}
	
	@Override
	public String toString() {
		return getName() + ":" + getZones();
	}
	
	/**
	 * Stops playback from all child zones.
	 */
	public void stop(){
		for (Zone zone : getZones()) {
			Sample sample = SampleManager.getSample(zone);
			if (sample != null)
				sample.stop();
		}
	}
}
