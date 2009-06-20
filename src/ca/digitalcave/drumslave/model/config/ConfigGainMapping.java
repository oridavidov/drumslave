package ca.digitalcave.drumslave.model.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("gain-mapping")
public class ConfigGainMapping {

	@XStreamAsAttribute
	@XStreamAlias("sample-mapping-group-name")
	private String sampleMappingGroupName;
	@XStreamAsAttribute
	@XStreamAlias("pad-name")
	private String padName;
	@XStreamAsAttribute
	@XStreamAlias("gain")
	private Float gain;

	public Float getGain() {
		return gain;
	}
	public void setGain(Float gain) {
		this.gain = gain;
	}
	
	public String getPadName() {
		return padName;
	}
	public void setPadName(String padName) {
		this.padName = padName;
	}
	
	public String getSampleMappingGroupName() {
		return sampleMappingGroupName;
	}
	public void setSampleMappingGroupName(String sampleMappingGroupName) {
		this.sampleMappingGroupName = sampleMappingGroupName;
	}
}
