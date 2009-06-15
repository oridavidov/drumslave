package ca.digitalcave.drumslave.model.hardware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.logic.Logic;
import ca.digitalcave.drumslave.model.mapping.LogicMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

/**
 * This class provides a hybrid Multiton-like access to the Zone object.  
 * Objects are initially created using the Zone constructor, but can be
 * accessed later using the Multiton static getZone(padName, zoneName) method.
 * 
 * This class (and others in DrumSlave) provide easy thread-safe access
 * to various configuration elements without needing to pass around a 
 * reference to them, and without needing to traverse a singleton instance
 * of the configuration.
 *  
 * @author wyatt
 *
 */
public class Zone implements Comparable<Zone> {

	private final static Logger logger = Logger.getLogger(Zone.class.getName());
	
	private final static Map<Integer, Zone> channels = new ConcurrentHashMap<Integer, Zone>();
	private final static Map<String, Map<String, Zone>> zones = new ConcurrentHashMap<String, Map<String, Zone>>();
	
	private final Pad pad;
	private final String name;
	private final int channel;

	protected static void clearZones(){
		zones.clear();
		channels.clear();
	}
	
	public static Zone getZone(String padName, String zoneName){
		if (zones.get(padName) != null)
			return zones.get(padName).get(zoneName);
		return null;
	}
	
	public static Zone getZone(int channel){
		return channels.get(channel);
	}
	
	protected Zone(int channel, String name, Pad pad) {
		if (pad == null || name == null)
			throw new RuntimeException("A zone's name and parent pad must not be null.");
		if (channel < 0 || channel >= 40)
			throw new RuntimeException("DrumMaster hardware only supports channels from 0 to 39 inclusive.");
		
		if (channels.get(channel) != null)
			throw new RuntimeException("Channel " + channel + " already in use for zone " + channels.get(channel).getPad().getName() + ":" + channels.get(channel).getName());
		if (zones.get(pad.getName()) != null){
			if (zones.get(pad.getName()).get(name) != null){
				throw new RuntimeException("Zone " + name + " belonging to pad " + pad + " already exists.  Use Zone.getZone(name) to retrieve an existing zone.");
			}
		}
		
		this.channel = channel;
		this.pad = pad;
		this.name = name;
		
		if (zones.get(pad.getName()) == null)
			zones.put(pad.getName(), new ConcurrentHashMap<String, Zone>());
		zones.get(pad.getName()).put(name, this);
		
		channels.put(channel, this);
		pad.addZone(this);
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
	
	public void play(float rawValue){
		String logicName = LogicMapping.getLogicMapping(getPad().getName(), getName());
		if (logicName == null){
			logger.info("No logic name is mapped to " + getPad().getName() + ":" + getName());
			return;
		}
		
		Logic logic = Logic.getLogic(logicName);
		if (logic == null)
			throw new RuntimeException("No logic class is mapped to name " + logicName);

		logic.execute(this, rawValue);
	}
	
	public float getLevel(){
		Sample sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), this.getPad().getName(), this.getName()));
		if (sample == null)
			return 0f;
		return sample.getLevel();
	}
	
	@Override
	public int hashCode() {
		return (getPad().getName() + ":" + getName() + ":" + getChannel()).hashCode();
	}
	
	public int compareTo(Zone o) {
		return this.getName().compareTo(o.getName());
	}
}
