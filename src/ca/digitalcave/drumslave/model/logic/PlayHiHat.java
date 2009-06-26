package ca.digitalcave.drumslave.model.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ca.digitalcave.drumslave.model.audio.Sample;
import ca.digitalcave.drumslave.model.hardware.Zone;
import ca.digitalcave.drumslave.model.mapping.GainMapping;
import ca.digitalcave.drumslave.model.mapping.SampleMapping;

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
	
	private final static String LOGICAL_TIGHT = " (Tight)";
	private final static String LOGICAL_CLOSED = " (Closed)";
	private final static String LOGICAL_LOOSE = " (Loose)";
	private final static String LOGICAL_OPEN = " (Open)";
	
	public PlayHiHat(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
		String padName = zone.getPad().getName();
		
		HiHatControllerAnalog analog = (HiHatControllerAnalog) Logic.getLogic(HiHatControllerAnalog.HIHAT_CONTROLLER_ANALOG_NAME);
		HiHatControllerDigital digital = (HiHatControllerDigital) Logic.getLogic(HiHatControllerDigital.HIHAT_CONTROLLER_DIGITAL_NAME);
		Float analogValue = analog.getAnalogValueByPad(padName);
		boolean isClosed = digital.isClosedByPad(padName);
		
		if (analogValue == null)
			return;
		
		Sample sample = null;
		if (isClosed){
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), padName, zone.getName() + LOGICAL_TIGHT));
		}
		else if (analogValue > 0.9f){
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), padName, zone.getName() + LOGICAL_OPEN));
		}
		else if (analogValue > 0.5){
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), padName, zone.getName() + LOGICAL_LOOSE));
		}
		else {
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), padName, zone.getName() + LOGICAL_CLOSED));
		}
		
		if (sample != null){
			logger.finer("Playing " + sample.getName() + " (hihat analog value is " + analogValue + ")");
			sample.play(rawValue, GainMapping.getPadGain(zone.getPad().getName()));
		}
	}

	@Override
	public List<String> getLogicalNames(Zone zone) {
		List<String> logicalNames = new ArrayList<String>();
		logicalNames.add(zone.getName() + LOGICAL_TIGHT);
		logicalNames.add(zone.getName() + LOGICAL_CLOSED);
		logicalNames.add(zone.getName() + LOGICAL_LOOSE);
		logicalNames.add(zone.getName() + LOGICAL_OPEN);
		return logicalNames;
	}
}
