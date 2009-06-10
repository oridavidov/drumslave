package ca.digitalcave.drumslave.model.options;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigOption;
import ca.digitalcave.drumslave.model.config.ConfigOptionMapping;


public class OptionMappingConfigManager implements ConfigManager<ConfigOptionMapping>{

	public void loadFromConfig(List<ConfigOptionMapping> configElements){
		OptionMapping.clearOptionMappings();
		
		if (configElements == null)
			return;
		
		for (ConfigOptionMapping configOptionMapping : configElements) {
			for (ConfigOption configOption : configOptionMapping.getOptions()) {
				OptionMapping.addOptionMapping(configOptionMapping.getPadName(), 
						configOptionMapping.getZoneName(),
						configOption.getName(), 
						configOption.getValue());
			}
		}
		
		System.out.println(OptionMapping.getOptionMappings());
	}
	
	public List<ConfigOptionMapping> saveToConfig(){
		List<ConfigOptionMapping> configOptionMappings = new ArrayList<ConfigOptionMapping>();
		for (OptionMapping optionMapping : OptionMapping.getOptionMappings()) {
			ConfigOptionMapping configOptionMapping = new ConfigOptionMapping();
			configOptionMapping.setPadName(optionMapping.getPadName());
			configOptionMapping.setZoneName(optionMapping.getZoneName());
			
			List<ConfigOption> configOptions = new ArrayList<ConfigOption>();
			for (String optionName : optionMapping.getOptions().keySet()) {
				ConfigOption configOption = new ConfigOption();
				configOption.setName(optionName);
				configOption.setValue(optionMapping.getOptions().get(optionName));
				configOptions.add(configOption);
			}
			configOptionMapping.setOptions(configOptions);
			configOptionMappings.add(configOptionMapping);
		}
		
		return configOptionMappings;
	}
}
