package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Pad;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.GainMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

/**
 * HiHat Controller Digital keeps track of whether a hihat controller is open at
 * all or not, and will give this value to the HiHat Play logic.  This can be 
 * used in conjunction with a HiHat Controller Analog to give even more accurate 
 * (and responsive) details.
 * 
 * @author wyatt
 *
 */
public class HiHatControllerDigital extends Logic {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final Map<Pad, Long> lastStateChangeTimeByPad = new ConcurrentHashMap<Pad, Long>();
	private final Map<Pad, Boolean> currentStateByPad = new ConcurrentHashMap<Pad, Boolean>();
	private final Map<Pad, Long> lastChicPlayedTimeByPad = new ConcurrentHashMap<Pad, Long>();
	private final Map<Pad, Long> lastSplashPlayedTimeByPad = new ConcurrentHashMap<Pad, Long>();
	
//	private final static Map<Pad, HiHatControllerDigital> hiHatControllerDigitalLogicsByPad = new ConcurrentHashMap<Pad, HiHatControllerDigital>();
//
//	public static HiHatControllerDigital getHiControllerDigital(Pad pad){
//		if (pad == null)
//			return null;
//		return hiHatControllerDigitalLogicsByPad.get(pad);
//	}

	
	private final static String LOGICAL_CHIC = "Chic";
	private final static String LOGICAL_SPLASH = "Splash";
	 
	private static final long CHIC_SAMPLE_DEBOUNCE_PERIOD = 250; //in ms.
	
	private static final long SPLASH_SAMPLE_DEBOUNCE_PERIOD = 100; //If the open was less than this after a close, chances are it is from switch bouncing  
	private static final long SPLASH_SAMPLE_THRESHOLD = 300; //How soon after a close should it be open to play splash
	
	//This is initialized the first time the HiHat Controller is instantiated.
	//TODO This will only let there be one HiHatControllerDigital.  Is this right?
	static String HIHAT_CONTROLLER_DIGITAL_NAME = null;
	
	public HiHatControllerDigital(String name) {
		super(name);
		if (HIHAT_CONTROLLER_DIGITAL_NAME != null)
			throw new RuntimeException("You cannot have HiHatControllerDigital logic assigned to multiple zones.");
		HIHAT_CONTROLLER_DIGITAL_NAME = name;
//		hiHatControllerDigitalLogicsByPad.put(LogicMapping.getLogicMapping(padName, zoneName)key, value)
	}
	
	public void execute(Zone zone, float rawValue) {
		long currentTime = System.currentTimeMillis();
		currentStateByPad.put(zone.getPad(), rawValue > 0.5f);
		if (lastStateChangeTimeByPad.get(zone.getPad()) == null)
			lastStateChangeTimeByPad.put(zone.getPad(), 0l);
		if (lastChicPlayedTimeByPad.get(zone.getPad()) == null)
			lastChicPlayedTimeByPad.put(zone.getPad(), 0l);
		if (lastSplashPlayedTimeByPad.get(zone.getPad()) == null)
			lastSplashPlayedTimeByPad.put(zone.getPad(), 0l);
		
		//Since the values come in as floats, we do less / greater than 0.5 to avoid
		// exact comparison errors; of course, these will always be 0.0 or 1.0, since
		// this must be hooked up to a digital channel.
		if (rawValue < 0.5f){
			logger.finest(zone.getPad().getName() + " opened at " + currentTime);
			//If the HH has just been closed and is now open (far enough), we will
			// play the splash sample
			if (lastStateChangeTimeByPad.get(zone.getPad()) + SPLASH_SAMPLE_THRESHOLD > currentTime
					&& lastSplashPlayedTimeByPad.get(zone.getPad()) + SPLASH_SAMPLE_DEBOUNCE_PERIOD < currentTime){
				zone.getPad().stop(10, LOGICAL_SPLASH, LOGICAL_CHIC);
				Sample sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), LOGICAL_SPLASH));
				if (sample != null){
					HiHatControllerAnalog analog = (HiHatControllerAnalog) Logic.getLogic(HiHatControllerAnalog.HIHAT_CONTROLLER_ANALOG_NAME);
					//Technically the 'closed' argument to get the velocity should be false, as by the time
					// this is called, it will already be open.  However, we know that it was *just* closed,
					// and by assuming that it was, we avoid the situation where we mis-read the analog values
					// because they are moving too fast (i.e., pedal started at open, and was slammed shut
					// and re-opened before the controller could see the value of the closed state).
					//Adjust the final argument 'time' to change the sensitivity.  Perhaps sometime
					// we may abstract this to a config file value, but for now it is
					// just hard coded.
					Float volume = analog.getAnalogVelocityByPad(zone.getPad(), true, 70);
					Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Last known analog value: " + volume);
					if (volume != null && volume > 0.5f){
						logger.finer("Playing HiHat Splash at volume " + volume);
						sample.play(volume, GainMapping.getPadGain(zone.getPad().getName()));
					}
				}
			}
		}
		else {
			logger.finest(zone.getPad().getName() + " closed at " + currentTime);
			//If the HH is closed, we want to be sure that all non-closed sounds stop.
			zone.getPad().stop(10, LOGICAL_CHIC, LOGICAL_SPLASH, zone.getName() + PlayHiHat.LOGICAL_TIGHT, zone.getName() + PlayHiHat.LOGICAL_CLOSED);
			if (lastChicPlayedTimeByPad.get(zone.getPad()) + CHIC_SAMPLE_DEBOUNCE_PERIOD < currentTime){
				zone.getPad().stop(10, LOGICAL_CHIC);
			
				Sample sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), LOGICAL_CHIC));
				if (sample != null){
					HiHatControllerAnalog analog = (HiHatControllerAnalog) Logic.getLogic(HiHatControllerAnalog.HIHAT_CONTROLLER_ANALOG_NAME);
					Float volume = analog.getAnalogVelocityByPad(zone.getPad(), true, 100);
					if (volume != null){
						logger.finer("Playing HiHat Chic at volume " + volume);
						sample.play(volume, GainMapping.getPadGain(zone.getPad().getName()));
						lastChicPlayedTimeByPad.put(zone.getPad(), currentTime);
					}
				}
			}
		}
		
		//Keep track of the time
		lastStateChangeTimeByPad.put(zone.getPad(), currentTime);
	}
	
	public boolean isClosedByPad(Pad pad){
		if (currentStateByPad.get(pad) == null)
			return false;
		return currentStateByPad.get(pad);
	}
	
	@Override
	public List<String> getLogicalNames(Zone zone) {
		List<String> logicalNames = new ArrayList<String>();
		logicalNames.add(LOGICAL_CHIC);
		logicalNames.add(LOGICAL_SPLASH);
		return logicalNames;
	}
}
