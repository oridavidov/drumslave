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
	
	public final static String LOGICAL_TIGHT = " (Tight)";
	public final static String LOGICAL_CLOSED = " (Closed)";
	public final static String LOGICAL_LOOSE = " (Loose)";
	public final static String LOGICAL_OPEN = " (Open)";
	
	public PlayHiHat(String name) {
		super(name);
	}
	
	public void execute(Zone zone, float rawValue) {
		HiHatControllerAnalog analog = (HiHatControllerAnalog) Logic.getLogic(HiHatControllerAnalog.HIHAT_CONTROLLER_ANALOG_NAME);
		HiHatControllerDigital digital = (HiHatControllerDigital) Logic.getLogic(HiHatControllerDigital.HIHAT_CONTROLLER_DIGITAL_NAME);
		Float analogValue = analog.getAnalogValueByPad(zone.getPad());
		boolean isClosed = digital.isClosedByPad(zone.getPad());
		
		if (analogValue == null)
			return;
		
		Sample sample = null;
		if (isClosed){
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), zone.getName() + LOGICAL_TIGHT));
		}
		else if (analogValue > 0.95f){
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), zone.getName() + LOGICAL_OPEN));
		}
		else if (analogValue > 0.3f){
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), zone.getName() + LOGICAL_LOOSE));
		}
		else {
			sample = Sample.getSample(SampleMapping.getSampleMapping(SampleMapping.getSelectedSampleGroup(), zone.getPad().getName(), zone.getName() + LOGICAL_CLOSED));
		}
		
		if (sample != null){
			logger.finer("Playing " + sample.getName() + " (hihat analog value is " + analogValue + ")");
			executor.execute(new HiHatPlayThread(sample, rawValue, GainMapping.getPadGain(zone.getPad().getName())));
//			new HiHatPlayThread(sample, rawValue, GainMapping.getPadGain(zone.getPad().getName()));
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
	
	protected class HiHatPlayThread implements Runnable  { 
		public static final long serialVersionUID = 0l;
		
		private final Sample sample;
		private final float rawValue;
		private final float gain;
		
		public HiHatPlayThread(Sample sample, float rawValue, float gain) {
			this.sample = sample;
			this.rawValue = rawValue;
			this.gain = gain;
		}

		public void run() {
			sample.play(rawValue, gain);
		}
	}
}
