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
	@XStreamAlias("sample-mappings")
	@XStreamImplicit
	private List<ConfigSampleMapping> sampleMappings;
	
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
	public List<ConfigSampleMapping> getSampleMappings() {
		return sampleMappings;
	}
	public void setSampleMappings(List<ConfigSampleMapping> sampleMappings) {
		this.sampleMappings = sampleMappings;
	}
}
