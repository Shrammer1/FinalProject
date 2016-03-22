package cli;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import api.CLIModule;
import api.ControllerAPI;

public class CLI {
	
	ArrayList<String> swLst;
	ControllerAPI controllerIntf;
	ArrayList<CLIModule> apps = new ArrayList<CLIModule>(); 
	private String context = "";
	private CLIModule currentApp = null;
	
	public void parseCommands(){
		init();
	}

	private void init() {
		Scanner s = new Scanner(System.in);
		while(true){
	        System.out.print("CLI " + context + "> ");
	        String sentence = s.nextLine();
	        if(!(handleCommand(sentence))){
	        	break;
	        }
		}
		s.close();
	}

	public void println(String s) {
		System.out.println(s);
	}
	
	private void executeCommand(String text){
		try {
			System.out.println(currentApp.executeCommand(text));
		} catch (RemoteException e) {
			System.err.println("Error connecting to app, please retry.");
			currentApp=null;
			context="Controller";
		}
	}
	
	
	public boolean handleCommand(String text) {
		
		if(currentApp!=null){
			if(text.equals("exit")){
				currentApp=null;
				context="Controller";
				return true;
			}else{
				executeCommand(text);
				return true;
			}
		}
		
		if(text.toLowerCase().startsWith("connect ")){
			connect(text.split(" "));
		}else if(text.toLowerCase().startsWith("show ")){
			show(text.split(" "));
		}else if(text.toLowerCase().startsWith("setcontext ")){
			setcontext(text.split(" "));
		}else if(text.toLowerCase().startsWith("exit")){
			System.out.println("Bye.");
			return false;
		}else{
			println("Invalid Command ...");
		}
		return true;
	}

	private void setcontext(String[] strings) {
		refreshApps();
		for(CLIModule app:apps){
			try {
				if(strings[1].equals(app.getApplicationContextName())){
					context = app.getApplicationContextName();
					currentApp = app;
					System.out.println("Switched to context: " + context);
				}
			} catch (RemoteException e) {
				System.err.println("Error connecting to app, please retry.");
			}
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
			controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + strings[1]);
			context = "Controller";
			currentApp = null;
			println("Connected ...");
			refreshApps();
		} catch (MalformedURLException e) {
			System.err.println("Error connecting to controller at: " + strings[1]);
			currentApp = null;
			context = "";
		} catch (NotBoundException e) {
			System.err.println("Error connecting to controller at: " + strings[1]);
			currentApp = null;
			context = "";
		} catch (RemoteException e) {
			System.err.println("Error connecting to controller at: " + strings[1]);
			currentApp = null;
			context = "";
		}
		
		
	}
	
	private void show(String[] strings) {
		if(controllerIntf==null){
			println("Not Connected ...");
			return;
		}
		if(strings[1].toLowerCase().equals("apps")){
			refreshApps();
			System.out.println("");
			for(CLIModule app:apps){
				try {
					System.out.println(app.getApplicationContextName() + "\n");
				} catch (RemoteException e) {
					System.err.println("Error connecting to app, please retry.");
				}
			}
		}else{
			println("Invalid Parameter ...");
		}
	}
	
	private void refreshApps(){
		apps = new ArrayList<>();
		try {
			for(Remote remoteApp:controllerIntf.getCLIApplications()){
				apps.add((CLIModule) remoteApp);
			}
		} catch (RemoteException e) {
			System.err.println("Error connecting to controller, please reconnect.");
			controllerIntf=null;
			currentApp = null;
			context = "";
		}
	}
	
	
	public static void main(String[] args){
		CLI cli = new CLI();
		cli.parseCommands();
	}
	
	
	
}