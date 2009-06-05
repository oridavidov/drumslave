package ca.digitalcave.drumslave.hardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HardwareManager {

	public static List<Pad> loadHardware(){
		List<Pad> pads = Collections.synchronizedList(new ArrayList<Pad>());
		
		Pad bass = Pad.getPad("Bass");
		bass.addZone(new Zone(32, "Head", bass));
		
		Pad ride = Pad.getPad("Ride");
		ride.addZone(new Zone(29, "Bow", ride));
		ride.addZone(new Zone(23, "Bell", ride));
		ride.addZone(new Zone(22, "Edge", ride));
		
		return pads;
	}
}
