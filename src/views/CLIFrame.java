package views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import controller.cli.CLIController;

public class CLIFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1730331711605799254L;
	private GridBagLayout layout = new GridBagLayout();
	private GridBagConstraints lContraints = new GridBagConstraints();
	private Toolkit tools = Toolkit.getDefaultToolkit();
	
	private CLIController cliController;
	
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu;
	private JMenuItem exitAction;
	
	private CLIPanel cliPanel;
	public CLIFrame(){
		init();
		initComponents();
		initListeners();
		update();
	}

	private void init() {
		cliController = new CLIController(this);
		setLocation(((int)tools.getScreenSize().getWidth()/2)-300,((int)tools.getScreenSize().getHeight()/2)-300);
		setLayout(layout);
		setTitle("CLI");
		setJMenuBar(menuBar);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600,600);
		setVisible(true);
	}

	private void initComponents() {
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		exitAction = new JMenuItem("Exit");
		fileMenu.add(exitAction);

		cliPanel = new CLIPanel(this);
		addToGrid(cliPanel,0,0,1,1);
	}

	private void initListeners() {
		exitAction.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg){
				exit();
			}
		});
	}
	
	private void exit() {
		dispose();
		System.exit(0);
	}
	
	private void update(){
		revalidate();
		repaint();
	}

	private void addToGrid(JPanel theComponent, int i, int j, int k, int l) {
		lContraints.gridx = i;
		lContraints.gridy = j;
		lContraints.gridwidth  = k;
		lContraints.gridheight = l;
		lContraints.fill = GridBagConstraints.BOTH;
		lContraints.insets = new Insets(10,5,10,5);
		lContraints.anchor = GridBagConstraints.NORTHWEST;
		lContraints.weightx = 1.0;
		lContraints.weighty = 1.0;
		layout.setConstraints(theComponent,lContraints);
		add(theComponent);
	}

	public void send(String text) {
		cliController.handleCommand(text);
	}

	public void println(String s) {
		cliPanel.println(s);
	}

}
