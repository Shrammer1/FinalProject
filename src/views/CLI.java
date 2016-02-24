package views;

import java.util.Scanner;

import applications.CLIController;

public class CLI {
	
	private CLIController controller;
	
	public CLI(){
		init();
	}

	@SuppressWarnings("resource")
	private void init() {
		controller = new CLIController(this);
		while(true){
			Scanner s = new Scanner(System.in);
	        System.out.print("SDN CLI > ");
	        String sentence = s.nextLine();
	        controller.handleCommand(sentence);
		}
	}

	public void println(String s) {
		System.out.println(s);
	}
}