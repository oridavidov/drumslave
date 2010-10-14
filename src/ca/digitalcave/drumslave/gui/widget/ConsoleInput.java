package ca.digitalcave.drumslave.gui.widget;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.homeunix.thecave.moss.swing.MossPanel;

import ca.digitalcave.drumslave.gui.util.Formatter;
import ca.digitalcave.drumslave.serial.DrumSignal;

public class ConsoleInput extends MossPanel implements ActionListener {
	public static final long serialVersionUID = 0l;

	private final JTextField inputField;
	private final JButton submitButton;
	
	public ConsoleInput() {
		super(true);
		
		inputField = new JTextField();
		submitButton = new JButton("Submit");
		
		open();
	}
	
	@Override
	public void init() {
		super.init();
		
		submitButton.addActionListener(this);
		inputField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				
				if (e.getKeyCode() == KeyEvent.VK_ENTER){
					submitButton.doClick();
				}
			}
		});
		
		inputField.setPreferredSize(Formatter.getComponentSize(inputField, 200));
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(inputField);
		this.add(submitButton);
	}
	
	public void actionPerformed(ActionEvent e) {
		String text = inputField.getText();
		
		String[] split = text.split(":");
		int channel = Integer.parseInt(split[0]);
		int data = Math.min(1023, Integer.parseInt(split[1]));

		int[] command = new int[3];
		int checksum = 0x0;
		
		command[0] = (0xF0 | ((channel >> 2) & 0xF));
		checksum ^= command[0] >> 4;
		checksum ^= command[0] & 0xF;

		command[1] = (((channel & 0x3) << 6) | ((data >> 4) & 0x3F));
		checksum ^= command[1] >> 4;
		checksum ^= command[1] & 0xF;
		
		command[2] = ((data & 0xF) << 4);
		checksum ^= command[2] >> 4;
		command[2] |= checksum & 0xF;
		
		DrumSignal.threadPool.execute(new DrumSignal(command));
		inputField.setText("");
		updateContent();
	}
}
