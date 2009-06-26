package ca.digitalcave.drumslave.model.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("sample-mapping")
public class ConfigSampleMapping {

	@XStreamAsAttribute
	@XStreamAlias("pad-name")
	private String padName;
	@XStreamAsAttribute
	@XStreamAlias("logical-name")
	private String logicalName;
	@XStreamAsAttribute
	@XStreamAlias("sample-name")
	private String sampleName;
	
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String logicName) {
		this.sampleName = logicName;
	}
	public String getPadName() {
		return padName;
	}
	public void setPadName(String padName) {
		this.padName = padName;
	}
	public String getLogicalName() {
		return logicalName;
	}
	public void setLogicalName(String zoneName) {
		this.logicalName = zoneName;
	}
	
	@Override
	public String toString() {
		return padName + ":" + logicalName + ":" + sampleName;
	}
}
