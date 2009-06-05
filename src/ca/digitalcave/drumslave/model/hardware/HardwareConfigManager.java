package ca.digitalcave.drumslave.model.hardware;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigPad;
import ca.digitalcave.drumslave.model.config.ConfigZone;


public class HardwareConfigManager implements ConfigManager<ConfigPad>{

	public void loadFromConfig(List<ConfigPad> configElements){
		Pad.clearPads();
		Zone.clearZones();
		
		if (configElements == null)
			return;
		
		for (ConfigPad configPad : configElements) {
			Pad pad = new Pad(configPad.getName());
			for (ConfigZone configZone : configPad.getZones()) {
				new Zone(configZone.getChannel(), configZone.getName(), pad);
			}
		}
	}
	
	public List<ConfigPad> saveToConfig(){
		List<ConfigPad> configPads = new ArrayList<ConfigPad>();
		for (Pad pad : Pad.getPads()) {
			ConfigPad configPad = new ConfigPad();
			configPad.setName(pad.getName());
			List<ConfigZone> configZones = new ArrayList<ConfigZone>();
			for (Zone zone : pad.getZones()) {
				ConfigZone configZone = new ConfigZone();
				configZone.setChannel(zone.getChannel());
				configZone.setName(zone.getName());
				configZones.add(configZone);
			}
			configPad.setZones(configZones);
			configPads.add(configPad);
		}
		
		return configPads;
	}
}
