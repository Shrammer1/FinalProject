package cli;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import api.CLIModule;
import api.ControllerAPI;

public class CLI {
	
	
	public static void main(String[] args){
		String connectTo = "127.0.0.1:1099"; //TODO: replace this with a 'connect' command
		try {
			ControllerAPI controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + connectTo + "/controller");
			ArrayList<CLIModule> apps = new ArrayList<CLIModule>(); 
			for(Remote remoteApp:controllerIntf.getCLIApplications()){
				apps.add((CLIModule) remoteApp);
			}
			System.out.println(apps.get(0).getApplicationContextName() + "> " + apps.get(0).executeCommand("show domain-list"));
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	
}
