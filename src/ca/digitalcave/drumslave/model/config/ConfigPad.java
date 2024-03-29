package ca.digitalcave.drumslave.model.config;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("pad")
public class ConfigPad {

	@XStreamAsAttribute
	private String name;
	@XStreamImplicit
	private List<ConfigZone> zones;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ConfigZone> getZones() {
		return zones;
	}
	public void setZones(List<ConfigZone> zones) {
		this.zones = zones;
	}
	public void addZone(ConfigZone zone){
		if (this.zones == null)
			this.zones = new ArrayList<ConfigZone>();
		zones.add(zone);
	}
	@Override
	public String toString() {
		return name + ":" + zones;
	}
}
