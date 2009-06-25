package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigSampleMapping;
import ca.digitalcave.drumslave.model.config.ConfigSampleMappingGroup;

public class SampleMappingConfigManager implements ConfigManager<ConfigSampleMappingGroup>{

	public void loadFromConfig(List<ConfigSampleMappingGroup> configElements) {
		SampleMapping.clearSampleMappings();
		
		if (configElements == null)
			return;

		for (ConfigSampleMappingGroup configSampleMappingGroup : configElements) {
			if (configSampleMappingGroup.getName() != null)
				SampleMapping.addSampleGroup(configSampleMappingGroup.getName());
			
			if (configSampleMappingGroup.getSampleMappings() != null){
				for (ConfigSampleMapping configSampleMapping : configSampleMappingGroup.getSampleMappings()) {
					SampleMapping.addSampleMapping(configSampleMappingGroup.getName(), configSampleMapping.getPadName(), configSampleMapping.getLogicalName(), configSampleMapping.getSampleName());				
				}
			}
		}
	}
	public List<ConfigSampleMappingGroup> saveToConfig() {
		List<ConfigSampleMappingGroup> configSampleMappingGroups = new ArrayList<ConfigSampleMappingGroup>();
		
		for (String sampleConfigName : SampleMapping.getSampleGroups()) {
			ConfigSampleMappingGroup configSampleMappingGroup = new ConfigSampleMappingGroup();
			configSampleMappingGroup.setName(sampleConfigName);
			List<ConfigSampleMapping> configSampleMappings = new ArrayList<ConfigSampleMapping>();
			
			Map<String, Map<String, String>> sampleMappingsByGroup = SampleMapping.getSampleMappingsByGroup(sampleConfigName);
			if (sampleMappingsByGroup != null){
				for (String padName : sampleMappingsByGroup.keySet()) {
					for (String logicalName : sampleMappingsByGroup.get(padName).keySet()) {
						String sample = SampleMapping.getSampleMapping(sampleConfigName, padName, logicalName);
						if (sample != null){
							ConfigSampleMapping configSampleMapping = new ConfigSampleMapping();
							configSampleMapping.setPadName(padName);
							configSampleMapping.setLogicalName(logicalName);
							configSampleMapping.setSampleName(sample);
							configSampleMappings.add(configSampleMapping);
						}						
					}
				}
			}
			
			configSampleMappingGroup.setSampleMappings(configSampleMappings);
			configSampleMappingGroups.add(configSampleMappingGroup);
		}
		
		return configSampleMappingGroups;
	}
}
