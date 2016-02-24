package controller.cli;

import java.rmi.Naming;
import java.util.ArrayList;
import api.AppAPI;
import api.ControllerAPI;
import views.CLIFrame;

public class CLIController {

	private CLIFrame cliFrame;
	private AppAPI appIntf;
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
		if(appIntf==null){
			println("Not Connected ...");
			return;
		}
		if(text.toLowerCase().equals("switches")){
			try {
				swLst = appIntf.listSwitches();
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
		if(appIntf!=null){
			println("--------------");
			println("Already Connected ...");
			return;
		}
		try {
			ControllerAPI controller = (ControllerAPI) Naming.lookup("rmi://" + text + "/controller");
			appIntf = (AppAPI) controller.register(0);
			
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
