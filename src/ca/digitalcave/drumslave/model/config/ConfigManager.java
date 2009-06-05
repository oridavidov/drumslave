package ca.digitalcave.drumslave.model.config;

import java.util.List;

public interface ConfigManager<E> {

	/**
	 * Given a collection of config elements, load the matching elements into memory.  Clear
	 * existing elements from memory before loading. 
	 * @param configElements
	 */
	public void loadFromConfig(List<E> configElements);
	
	/**
	 * Return a collection of config elements matching elements currently in memory. 
	 * @return
	 */
	public List<E> saveToConfig();
}
