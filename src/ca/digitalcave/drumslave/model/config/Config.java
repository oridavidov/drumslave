package ca.digitalcave.drumslave.model.config;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("drum-slave")
public class Config {

	@XStreamAlias("pads")
	@XStreamImplicit
	private List<ConfigPad> pads;
	
	@XStreamAlias("samples")
	@XStreamImplicit
	private List<ConfigSample> samples;
	
	@XStreamAlias("logics")
	@XStreamImplicit
	private List<ConfigLogic> logics;
	
	@XStreamAlias("logic-mappings")
	@XStreamImplicit
	private List<ConfigLogicMapping> logicMappings;
	
	@XStreamAlias("sample-mapping-groups")
	@XStreamImplicit
	private List<ConfigSampleMappingGroup> sampleMappingGroups;
	
	@XStreamAlias("option-mappings")
	@XStreamImplicit
	private List<ConfigOptionMapping> optionMappings;

	@XStreamAlias("gain-mappings")
	@XStreamImplicit
	private List<ConfigGainMapping> gainMappings;
	
	@XStreamAlias("settings")
	@XStreamImplicit
	private List<ConfigOption> settings;
	
	public List<ConfigPad> getPads() {
		return pads;
	}
	public void setPads(List<ConfigPad> pads) {
		this.pads = pads;
	}
	public List<ConfigSample> getSamples() {
		return samples;
	}
	public void setSamples(List<ConfigSample> samples) {
		this.samples = samples;
	}
	public List<ConfigLogic> getLogics() {
		return logics;
	}
	public void setLogics(List<ConfigLogic> logics) {
		this.logics = logics;
	}
	public List<ConfigLogicMapping> getLogicMappings() {
		return logicMappings;
	}
	public void setLogicMappings(List<ConfigLogicMapping> logicMappings) {
		this.logicMappings = logicMappings;
	}
	public List<ConfigOptionMapping> getOptionMappings() {
		return optionMappings;
	}
	public void setOptionMappings(List<ConfigOptionMapping> optionMappings) {
		this.optionMappings = optionMappings;
	}
	public List<ConfigSampleMappingGroup> getSampleMappingGroups() {
		return sampleMappingGroups;
	}
	public void setSampleMappingGroups(List<ConfigSampleMappingGroup> sampleMappingGroups) {
		this.sampleMappingGroups = sampleMappingGroups;
	}
	public List<ConfigOption> getSettings() {
		return settings;
	}
	public void setSettings(List<ConfigOption> settings) {
		this.settings = settings;
	}
	public List<ConfigGainMapping> getGainMappings() {
		return gainMappings;
	}
	public void setGainMappings(List<ConfigGainMapping> gainMappings) {
		this.gainMappings = gainMappings;
	}
}
