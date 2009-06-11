package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigSampleMapping;
import ca.digitalcave.drumslave.model.config.ConfigSampleMappingGroup;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class SampleMappingConfigManager implements ConfigManager<ConfigSampleMappingGroup>{

	public void loadFromConfig(List<ConfigSampleMappingGroup> configElements) {
		SampleMapping.clearSampleMappings();
		
		if (configElements == null)
			return;
		
		for (ConfigSampleMappingGroup configSampleMappingGroup : configElements) {
			for (ConfigSampleMapping configSampleMapping : configSampleMappingGroup.getSampleMappings()) {
				SampleMapping.addSampleMapping(configSampleMappingGroup.getName(), configSampleMapping.getPadName(), configSampleMapping.getZoneName(), configSampleMapping.getSampleName());				
			}
		}
	}
	public List<ConfigSampleMappingGroup> saveToConfig() {
		List<ConfigSampleMappingGroup> configSampleMappingGroups = new ArrayList<ConfigSampleMappingGroup>();
		
		//We only save logic mappings for which there are valid zones.  Is this right?
		for (String sampleConfigName : SampleMapping.getSampleConfigNames()) {
			ConfigSampleMappingGroup configSampleMappingGroup = new ConfigSampleMappingGroup();
			configSampleMappingGroup.setName(sampleConfigName);
			List<ConfigSampleMapping> configSampleMappings = new ArrayList<ConfigSampleMapping>();
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
			configSampleMappingGroup.setSampleMappings(configSampleMappings);
			configSampleMappingGroups.add(configSampleMappingGroup);
		}
		
		return configSampleMappingGroups;
	}
}
