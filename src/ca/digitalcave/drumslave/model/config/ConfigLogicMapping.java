package ca.digitalcave.drumslave.model.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

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
}
