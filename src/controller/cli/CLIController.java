package controller.cli;

import java.rmi.Naming;
import java.util.ArrayList;

import api.SwitchHandlerAPI;
import views.CLIFrame;

public class CLIController {

	private CLIFrame cliFrame;
	private SwitchHandlerAPI controllerIntf;
	ArrayList<String> swLst;
	
	public CLIController(CLIFrame cliFrame) {
		this.setCliFrame(cliFrame);
	}

	public void handleCommand(String text) {
		if(text.toLowerCase().startsWith("connect ")){
			connect(text.split(" "));
		}else if(text.toLowerCase().startsWith("show ")){
			show(text.split(" "));
		}else if(text.toLowerCase().startsWith("set ")){
			set(text.split(" "));
		}else if(text.toLowerCase().startsWith("exit")){
			exit();
		}else{
			println("Invalid Command ...");
		}
	}

	private void connect(String[] strings) {
		if(controllerIntf!=null){
			println("--------------");
			println("Already Connected ...");
			return;
		}
		try {
			controllerIntf = (SwitchHandlerAPI) Naming.lookup("rmi://" + strings[1] + "/controller");
			println("Connected ...");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void show(String[] strings) {
		if(controllerIntf==null){
			println("Not Connected ...");
			return;
		}
		if(strings[1].toLowerCase().equals("switches")){
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

	private void set(String[] split) {
		
	}

	public CLIFrame getCliFrame() {
		return cliFrame;
	}

	public void setCliFrame(CLIFrame cliFrame) {
		this.cliFrame = cliFrame;
	}

	private void println(String s) {
		cliFrame.println(s);
	}

	private void exit() {
		System.exit(0);
	}

}
