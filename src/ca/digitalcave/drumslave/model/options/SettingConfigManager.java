package ca.digitalcave.drumslave.model.options;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.config.ConfigManager;
import ca.digitalcave.drumslave.model.config.ConfigOption;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

public class SettingConfigManager implements ConfigManager<ConfigOption>{

	public static final String SELECTED_SAMPLE_GROUP = "selected-sample-group";
	
	public void loadFromConfig(List<ConfigOption> configElements) {
		for (ConfigOption configOption : configElements) {
			//Load the selected sample group
			if (configOption.getName().trim().equals(SELECTED_SAMPLE_GROUP)){
				SampleMapping.setSelectedSampleGroup(configOption.getValue());
			}
			
		}
	}
	public List<ConfigOption> saveToConfig() {
		List<ConfigOption> configOptions = new ArrayList<ConfigOption>();

		ConfigOption c;
		
		//Save the selected sample group 
		c = new ConfigOption();
		c.setName(SELECTED_SAMPLE_GROUP);
		c.setValue(SampleMapping.getSelectedSampleGroup());
		configOptions.add(c);
		
		return configOptions;
	}
}
