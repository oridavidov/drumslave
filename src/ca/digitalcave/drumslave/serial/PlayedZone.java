package ca.digitalcave.drumslave.serial;

import ca.digitalcave.drumslave.model.hardware.Zone;

/**
 * Object which is passed from Serial listener to logic delegate,
 * which encapsulates a Zone with an associated velocity.
 * @author wyatt
 *
 */
public class PlayedZone {
	
	private final Zone zone;
	private final float velocity;
	private float primaryVelocity; //What is the velocity of the primary zone?  Set in LogicDelegate
	
	public PlayedZone(Zone zone, float velocity) {
		this.zone = zone;
		this.velocity = velocity;
		this.primaryVelocity = 0;
	}
	
	public float getVelocity() {
		return velocity;
	}
	
	public Zone getZone() {
		return zone;
	}
	
	public void setPrimaryVelocity(float primaryVelocity) {
		this.primaryVelocity = primaryVelocity;
	}
	
	public Float getSecondaryVelocityRatio() {
		return velocity / primaryVelocity;
	}
}
