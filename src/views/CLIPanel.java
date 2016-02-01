package views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class CLIPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -996223874552211923L;
	private CLIFrame cliFrame;
	private JScrollPane consoleScroller;
	private JTextArea consoleArea;
	private JTextField commandField;
	
	private GridBagLayout layout = new GridBagLayout();
	private GridBagConstraints lConstraints = new GridBagConstraints();
	@SuppressWarnings("unused")
	private Toolkit tools = Toolkit.getDefaultToolkit();
	
	public CLIPanel(CLIFrame cliFrame) {
		this.cliFrame = cliFrame;
		init();
		initComponents();
		initListeners();
		update();
	}

	private void init() {
		setVisible(true);
		setLayout(layout);
	}

	private void initComponents() {
		consoleArea = new JTextArea();
		consoleScroller = new JScrollPane(consoleArea);
		commandField = new JTextField();
		
		consoleArea.setEditable(false);
		
		lConstraints.anchor = GridBagConstraints.NORTH;
		lConstraints.weighty = 1.0;
		addToPanel(consoleScroller,0,0,1,1);
		lConstraints.anchor = GridBagConstraints.SOUTH;
		lConstraints.weighty = 0.0;
		addToPanel(commandField,0,1,1,1);
	}

	private void initListeners() {
		commandField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg){
				send();
			}
		});
	}

	private void send() {
		cliFrame.send(commandField.getText());
		commandField.setText("");
		update();
	}

	private void update() {
		revalidate();
		repaint();
	}
	
	private void addToPanel(JComponent theComponent, int i, int j, int k, int l) {
		lConstraints.gridx = i;
		lConstraints.gridy = j;
		lConstraints.gridwidth  = k;
		lConstraints.gridheight = l;
		lConstraints.fill = GridBagConstraints.BOTH;
		lConstraints.insets = new Insets(10,5,10,5);
		lConstraints.weightx = 1.0;
		layout.setConstraints(theComponent,lConstraints);
		add(theComponent);
	}

	public void println(String s) {
		consoleArea.append(s + "\n");
	}

}
