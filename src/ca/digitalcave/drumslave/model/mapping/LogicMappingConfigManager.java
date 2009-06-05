package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigLogicMapping;
import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class LogicMappingConfigManager implements ConfigManager<ConfigLogicMapping>{

	public void loadFromConfig(List<ConfigLogicMapping> configElements) {
		LogicMapping.clearLogicMappings();
		
		if (configElements == null)
			return;
		
		for (ConfigLogicMapping configLogicMapping : configElements) {
			LogicMapping.addLogicMapping(configLogicMapping.getPadName(), configLogicMapping.getZoneName(), configLogicMapping.getLogicName());
		}
	}
	public List<ConfigLogicMapping> saveToConfig() {
		List<ConfigLogicMapping> configLogicMappings = new ArrayList<ConfigLogicMapping>();
		
		//We only save logic mappings for which there are valid zones.  Is this right?
		for (Pad pad : Pad.getPads()) {
			for (Zone zone : pad.getZones()) {
				String logic = LogicMapping.getLogicMapping(pad.getName(), zone.getName());
				if (logic != null){
					ConfigLogicMapping configLogicMapping = new ConfigLogicMapping();
					configLogicMapping.setPadName(pad.getName());
					configLogicMapping.setZoneName(zone.getName());
					configLogicMapping.setLogicName(logic);
					configLogicMappings.add(configLogicMapping);
				}
			}
		}
		
		return configLogicMappings;
	}
}
