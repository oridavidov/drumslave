package ca.digitalcave.drumslave.model.config;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("logic-mapping")
public class ConfigLogicMapping {

	@XStreamAsAttribute
	@XStreamAlias("pad-name")
	private String padName;
	@XStreamAsAttribute
	@XStreamAlias("zone-name")
	private String zoneName;
	@XStreamAsAttribute
	@XStreamAlias("logic-name")
	private String logicName;
	@XStreamImplicit
	private List<ConfigOption> logicOptions;
	
	public String getLogicName() {
		return logicName;
	}
	public void setLogicName(String logicName) {
		this.logicName = logicName;
	}
	public String getPadName() {
		return padName;
	}
	public void setPadName(String padName) {
		this.padName = padName;
	}
	public String getZoneName() {
		return zoneName;
	}
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	public List<ConfigOption> getLogicOptions() {
		return logicOptions;
	}
	public void setLogicOptions(List<ConfigOption> logicOptions) {
		this.logicOptions = logicOptions;
	}
}
