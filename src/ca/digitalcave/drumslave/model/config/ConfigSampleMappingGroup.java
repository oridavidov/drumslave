package ca.digitalcave.drumslave.model.config;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("group")
public class ConfigSampleMappingGroup {

	@XStreamAsAttribute
	@XStreamAlias("name")
	private String name;
	@XStreamImplicit
	private List<ConfigSampleMapping> sampleMappings;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ConfigSampleMapping> getSampleMappings() {
		return sampleMappings;
	}
	public void setSampleMappings(List<ConfigSampleMapping> sampleMappings) {
		this.sampleMappings = sampleMappings;
	}
}
