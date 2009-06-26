package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigGainMapping;
import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.hardware.Pad;

public class GainMappingConfigManager implements ConfigManager<ConfigGainMapping>{

	public void loadFromConfig(List<ConfigGainMapping> configElements) {
		LogicMapping.clearLogicMappings();

		if (configElements == null)
			return;

		for (ConfigGainMapping configGainMapping : configElements) {
			GainMapping.addGainMapping(configGainMapping.getSampleMappingGroupName(), configGainMapping.getPadName(), configGainMapping.getGain());
		}
	}
	public List<ConfigGainMapping> saveToConfig() {
		List<ConfigGainMapping> configGainMappings = new ArrayList<ConfigGainMapping>();

		//We only save gain mappings for which there are valid zones.  Is this right?
		for (String sampleMappingGroupName : SampleMapping.getSampleGroups()){
			for (Pad pad : Pad.getPads()) {
				Float gain = GainMapping.getPadGain(sampleMappingGroupName, pad.getName());
				if (gain != null){
					ConfigGainMapping configGainMapping = new ConfigGainMapping();
					configGainMapping.setSampleMappingGroupName(sampleMappingGroupName);
					configGainMapping.setPadName(pad.getName());
					configGainMapping.setGain(gain);
					configGainMappings.add(configGainMapping);
				}
			}
		}

		return configGainMappings;
	}
}
