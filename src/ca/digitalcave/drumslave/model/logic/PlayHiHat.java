package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.hardware.Zone;

/**
 * HiHat Play logic is used for playing the HiHat pad.  It will work in conjunction
 * with the hi hat controller logics to determine which sound to play, and go ahead
 * and play it.
 * 
 * @author wyatt
 *
 */
public class PlayHiHat extends Play {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	protected final static Map<String, Long> lastPlayedTime = new ConcurrentHashMap<String, Long>();
	
	public PlayHiHat(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {

	}

	@Override
	public List<String> getLogicalNames(Zone zone) {
		List<String> logicalNames = new ArrayList<String>();
		logicalNames.add(zone.getName() + " (0 - Tight)");
		logicalNames.add(zone.getName() + " (1 - Closed)");
		logicalNames.add(zone.getName() + " (2 - Loose)");
		logicalNames.add(zone.getName() + " (3 - Open)");
		return logicalNames;
	}
}
