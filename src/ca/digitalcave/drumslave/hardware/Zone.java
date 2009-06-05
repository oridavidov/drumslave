package ca.digitalcave.drumslave.hardware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.digitalcave.drumslave.audio.Sample;
import ca.digitalcave.drumslave.config.SampleManager;

/**
 * After the Channel, this is the next highest logical mapping.  Consists of a pad
 * and a zone name; the pad equates to a physical drum pad, and various zones map
 * to different sensors on that pad (for instance, a Ride cymbal may have three 
 * zones, 'Bow', 'Bell', 'Edge').
 * @author wyatt
 *
 */
public class Zone {

	private final static Map<Integer, Zone> channels = new ConcurrentHashMap<Integer, Zone>();
	private final static Map<String, Map<String, Zone>> zones = new ConcurrentHashMap<String, Map<String, Zone>>();
	
	private final Pad pad;
	private final String name;
	private final int channel;
	
	public static Zone getZone(String padName, String zoneName){
		if (zones.get(padName) != null)
			return zones.get(padName).get(zoneName);
		return null;
	}
	
	public static Zone getZone(int channel){
		return channels.get(channel);
	}
	
	public Zone(int channel, String name, Pad pad) {
		if (pad == null || name == null)
			throw new RuntimeException("A zone's name and parent pad must not be null.");
		if (channel < 0 || channel >= 40)
			throw new RuntimeException("DrumMaster hardware only supports channels from 0 to 39 inclusive.");
		
		if (channels.get(channel) != null)
			throw new RuntimeException("Channel " + channel + " already in use for zone " + channels.get(channels));
		if (zones.get(pad.getName()) != null){
			if (zones.get(pad.getName()).get(name) != null){
				throw new RuntimeException("Zone " + name + " belonging to pad " + pad + " already exists.  Use Zone.getZone() to retrieve an existing zone.");
			}
		}
		
		this.channel = channel;
		this.pad = pad;
		this.name = name;
		
		if (zones.get(pad.getName()) == null)
			zones.put(pad.getName(), new ConcurrentHashMap<String, Zone>());
		zones.get(pad.getName()).put(name, this);
		
		channels.put(channel, this);
	}
	
	public Pad getPad() {
		return pad;
	}
	
	public String getName() {
		return name;
	}
	
	public int getChannel() {
		return channel;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getChannel() + ")";
	}
	
	public void play(float volume){
		Sample sample = SampleManager.getSample(this);
		if (sample != null){
			sample.play(volume);
		}
	}
}
