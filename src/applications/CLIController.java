package applications;

import java.rmi.Naming;
import java.util.ArrayList;

import api.ControllerAPI;
import views.CLI;

public class CLIController {

	private CLI cliFrame;
	private ControllerAPI controllerIntf;
	ArrayList<String> swLst;
	
	public CLIController(CLI cliFrame) {
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
		}else if(strings.length<2){
			println("Not enough arguments");
			return;
		}
		try {
			controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + strings[1] + "/controller");
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

	private void set(String[] input) {
		if(input.length<5){
			println("Not Enough Arguments");
			return;
		}
		if(input[1].equals("switch")){
			if(input[2].equals("timeout")){
				try{
					int newTimout = Integer.parseInt(input[4]);
					if(controllerIntf.setSwitchTimeout(input[3], newTimout)){
						println("Success");
					}else{
						println("Failure");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public CLI getCliFrame() {
		return cliFrame;
	}

	public void setCliFrame(CLI cliFrame) {
		this.cliFrame = cliFrame;
	}

	private void println(String s) {
		cliFrame.println(s);
	}

	private void exit() {
		System.exit(0);
	}

}
