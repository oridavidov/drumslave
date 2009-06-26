package ca.digitalcave.drumslave.gui.config.hardware;

import java.awt.FlowLayout;

import javax.swing.JLabel;

import org.homeunix.thecave.moss.swing.MossHintTextField;
import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.model.hardware.Zone;

public class ZoneEditor extends MossPanel {
	public static final long serialVersionUID = 0l;

	private MossHintTextField padName;
	private MossHintTextField zoneName;
	private JLabel channelLabel;
	
	private final int channel; 
	
	public ZoneEditor(int channel) {
		super(true);
		this.channel = channel;
		open();
	}
	
	@Override
	public void init() {
		padName = new MossHintTextField("<Pad>");
		zoneName = new MossHintTextField("<Zone>");
		channelLabel = new JLabel("99");
		
		this.setLayout(new FlowLayout());
		
		channelLabel.setPreferredSize(Formatter.getComponentSize(channelLabel, 20));
		channelLabel.setHorizontalAlignment(JLabel.RIGHT);
		padName.setPreferredSize(Formatter.getComponentSize(padName, 70));
		zoneName.setPreferredSize(Formatter.getComponentSize(zoneName, 70));
		
		this.add(channelLabel);
		this.add(padName);
		this.add(new JLabel(":"));
		this.add(zoneName);
		
		super.init();
	}
	
	@Override
	public void open() {
		super.open();
	}
	
	@Override
	public void updateContent() {
		super.updateContent();

		//Load the channel number
		channelLabel.setText((channel < 32 ? channel : channel - 32) + "");
		
		//Load the zone data from the currently 
		Zone zone = Zone.getZone(channel);
		if (zone != null){
			this.padName.setText(zone.getPad().getName());
			this.zoneName.setText(zone.getName());
		}
	}
	
	public String getPadName(){
		return padName.getText().trim();
	}
	
	public String getZoneName(){
		return zoneName.getText().trim();
	}
	
	public int getChannel() {
		return channel;
	}
}
