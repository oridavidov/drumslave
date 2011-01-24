package ca.digitalcave.drumslave.model.logic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.LogicMapping;
import ca.digitalcave.drumslave.serial.PlayedZone;

public class LogicDelegate {
	
	public static void chooseZoneToPlay(List<PlayedZone> zones){		
		
		float primaryVelocity = 0;
		Zone primaryZone = null;
		
		//First we need to find which (if any) of the zones is primary.  We also check if 
		// there is any mute logic; if there is, that takes highest precedence.
		for (PlayedZone playedZone : zones) {
			Zone z = playedZone.getZone();
			Logic l = Logic.getLogic(LogicMapping.getLogicMapping(z.getPad().getName(), z.getName()));

			if (l instanceof Mute){
				l.execute(z, 0); //Value doesn't matter
				return;
			}
			
			if (l instanceof Play && ((Play) l).getSecondaryVelocityThreshold(z) == 0){
				primaryVelocity = playedZone.getVelocity();
				primaryZone = z;
			}
		}
		
		//We then add the primary velocity to all non-primary zones.
		for (PlayedZone playedZone : zones) {
			if (!playedZone.getZone().equals(primaryZone))
				playedZone.setPrimaryVelocity(primaryVelocity);
		}
		
		//We now sort by the secondary velocity ratio, and pick the first
		// secondary zone which is over the threshold for that zone.
		Collections.sort(zones, new Comparator<PlayedZone>() {
			public int compare(PlayedZone o1, PlayedZone o2) {
				return o1.getSecondaryVelocityRatio().compareTo(o2.getSecondaryVelocityRatio());
			}
		});
		for (PlayedZone playedZone : zones) {
			Zone z = playedZone.getZone();
			Logic l = Logic.getLogic(LogicMapping.getLogicMapping(z.getPad().getName(), z.getName()));
			
			if (l instanceof Play
					&& !z.equals(primaryZone)
					&& playedZone.getSecondaryVelocityRatio() > ((Play) l).getSecondaryVelocityThreshold(z)){
				Logger.getLogger(LogicDelegate.class.getName()).log(Level.INFO, "Playing " + z.getName() + " at ratio " + ((Play) l).getSecondaryVelocityThreshold(z));
				z.play(playedZone.getVelocity());
				return;
			}
		}
		
		//If none of the secondary zones were over the secondary threshold
		// ratio, then we will just play the primary zone (if any).
		if (primaryZone != null){
			primaryZone.play(primaryVelocity);
			return;
		}
	
		//If there were no playable zones, we assume that this is a supporting
		// zone, such as hihat analog or something.  Just pass all non-play and 
		// non-mute based zones through to their individual logics.
		for (PlayedZone playedZone : zones) {
			Zone z = playedZone.getZone();
			Logic l = Logic.getLogic(LogicMapping.getLogicMapping(z.getPad().getName(), z.getName()));
			
			if (!(l instanceof Play || l instanceof Mute) && l != null && playedZone != null){
				l.execute(z, playedZone.getVelocity());
			}
		}
	}	
}
