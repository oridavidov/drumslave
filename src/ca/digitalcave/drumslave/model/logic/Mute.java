package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;

import ca.digitalcave.drumslave.model.hardware.Zone;

public class Mute extends Logic {

	public Mute(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float value) {
		zone.getPad().stop(200);
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		return new ArrayList<String>();
	}
}
