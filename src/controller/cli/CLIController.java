package controller.cli;

import java.rmi.Naming;
import java.util.ArrayList;
import api.ControllerAPI;
import views.CLIFrame;

public class CLIController {

	private CLIFrame cliFrame;
	private ControllerAPI controllerIntf;
	ArrayList<String> swLst;
	
	public CLIController(CLIFrame cliFrame) {
		this.setCliFrame(cliFrame);
	}

	public void handleCommand(String text) {
		if(text.toLowerCase().startsWith("connect ")){
			connect(text.substring(text.indexOf(" ")+1, text.length()));
			
		}else if(text.toLowerCase().startsWith("show ")){
			show(text.substring(text.indexOf(" ")+1, text.length()));
		}else if(text.toLowerCase().startsWith("exit")){
			exit();
		}else{
			println("Invalid Command ...");
		}
	}

	private void exit() {
		System.exit(0);
	}

	private void show(String text) {
		if(controllerIntf==null){
			println("Not Connected ...");
			return;
		}
		if(text.toLowerCase().equals("switches")){
			try {
				swLst = controllerIntf.listSwitches();
				println("Switch List Contains: " + swLst.size() + " Entries ...");
				for(String s : swLst){
					println(s);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}else{
			println("Invalid Parameter ...");
		}
	}

	private void println(String s) {
		cliFrame.println(s);
	}

	private void connect(String text) {
		if(controllerIntf!=null){
			println("--------------");
			println("Already Connected ...");
			return;
		}
		try {
			controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + text + "/controller");
			println("Connected ...");
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public CLIFrame getCliFrame() {
		return cliFrame;
	}

	public void setCliFrame(CLIFrame cliFrame) {
		this.cliFrame = cliFrame;
	}

}
