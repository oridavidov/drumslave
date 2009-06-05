package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigSampleMapping;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class SampleMappingConfigManager implements ConfigManager<ConfigSampleMapping>{

	public void loadFromConfig(List<ConfigSampleMapping> configElements) {
		SampleMapping.clearSampleMappings();
		
		if (configElements == null)
			return;
		
		for (ConfigSampleMapping configSampleMapping : configElements) {
			SampleMapping.addSampleMapping(configSampleMapping.getPadName(), configSampleMapping.getZoneName(), configSampleMapping.getSampleName());
		}
	}
	public List<ConfigSampleMapping> saveToConfig() {
		List<ConfigSampleMapping> configSampleMappings = new ArrayList<ConfigSampleMapping>();
		
		//We only save logic mappings for which there are valid zones.  Is this right?
		for (Pad pad : Pad.getPads()) {
			for (Zone zone : pad.getZones()) {
				String sample = SampleMapping.getSampleMapping(pad.getName(), zone.getName());
				if (sample != null){
					ConfigSampleMapping configSampleMapping = new ConfigSampleMapping();
					configSampleMapping.setPadName(pad.getName());
					configSampleMapping.setZoneName(zone.getName());
					configSampleMapping.setSampleName(sample);
					configSampleMappings.add(configSampleMapping);
				}
			}
		}
		
		return configSampleMappings;
	}
}
