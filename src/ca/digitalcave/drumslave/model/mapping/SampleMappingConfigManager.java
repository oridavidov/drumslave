package ca.digitalcave.drumslave.model.mapping;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigOption;
import ca.digitalcave.drumslave.model.config.ConfigSampleMapping;
import ca.digitalcave.drumslave.model.config.ConfigSampleMappingGroup;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.options.Settings;
import ca.digitalcave.drumslave.model.options.SettingsConfigManager;

public class SampleMappingConfigManager implements ConfigManager<ConfigSampleMappingGroup>{

	public void loadFromConfig(List<ConfigSampleMappingGroup> configElements) {
		SampleMapping.clearSampleMappings();
		
		if (configElements == null)
			return;

		//We need the currently selected sample group so that we can figure out which pad
		// gain settings to apply
		String selectedSampleGroup = Settings.getSetting(SettingsConfigManager.SELECTED_SAMPLE_GROUP);
		if (selectedSampleGroup == null)
			selectedSampleGroup = "";
		for (ConfigSampleMappingGroup configSampleMappingGroup : configElements) {
			if (configSampleMappingGroup.getName() != null)
				SampleMapping.addSampleGroup(configSampleMappingGroup.getName());
			
			if (configSampleMappingGroup.getSampleMappings() != null){
				for (ConfigSampleMapping configSampleMapping : configSampleMappingGroup.getSampleMappings()) {
					SampleMapping.addSampleMapping(configSampleMappingGroup.getName(), configSampleMapping.getPadName(), configSampleMapping.getZoneName(), configSampleMapping.getSampleName());				
				}
				
				if (selectedSampleGroup.equals(configSampleMappingGroup.getName())){
					for (ConfigOption padGainOption : configSampleMappingGroup.getPadGainAdjustments()) {
						Pad pad = Pad.getPad(padGainOption.getName());
						if (pad != null)
							pad.setGain(Float.parseFloat(padGainOption.getValue()));
					}
				}
			}
		}
	}
	public List<ConfigSampleMappingGroup> saveToConfig() {
		List<ConfigSampleMappingGroup> configSampleMappingGroups = new ArrayList<ConfigSampleMappingGroup>();
		
		//We only save logic mappings for which there are valid zones.  Is this right?
		for (String sampleConfigName : SampleMapping.getSampleGroups()) {
			ConfigSampleMappingGroup configSampleMappingGroup = new ConfigSampleMappingGroup();
			configSampleMappingGroup.setName(sampleConfigName);
			List<ConfigSampleMapping> configSampleMappings = new ArrayList<ConfigSampleMapping>();
			List<ConfigOption> padGainOptions = new ArrayList<ConfigOption>();
			for (Pad pad : Pad.getPads()) {
				for (Zone zone : pad.getZones()) {
					String sample = SampleMapping.getSampleMapping(sampleConfigName, pad.getName(), zone.getName());
					if (sample != null){
						ConfigSampleMapping configSampleMapping = new ConfigSampleMapping();
						configSampleMapping.setPadName(pad.getName());
						configSampleMapping.setZoneName(zone.getName());
						configSampleMapping.setSampleName(sample);
						configSampleMappings.add(configSampleMapping);
					}
				}
				for (ConfigOption configOption : padGainOptions) {
					
				}
				ConfigOption padGainOption = new ConfigOption();
				padGainOption.setName(pad.getName());
				padGainOption.setValue(pad.getGain() + "");
				padGainOptions.add(padGainOption);
			}
			configSampleMappingGroup.setSampleMappings(configSampleMappings);
			configSampleMappingGroups.add(configSampleMappingGroup);
		}
		
		return configSampleMappingGroups;
	}
}
