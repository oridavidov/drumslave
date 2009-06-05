package ca.digitalcave.drumslave.model.config;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("sample")
public class ConfigSample {
	
	@XStreamAsAttribute
	private String name;
	@XStreamImplicit
	private List<ConfigSampleParam> params;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ConfigSampleParam> getParams() {
		return params;
	}
	public void setParams(List<ConfigSampleParam> params) {
		this.params = params;
	}
}
