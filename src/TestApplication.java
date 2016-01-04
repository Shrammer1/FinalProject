import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;


public class TestApplication {
	
	public static void main(String[] args) {
			
		String serverURL = "rmi://127.0.0.1:1099/"; //this is the registry entry we want to use
		String controllerExt = "controller";
		//serverIntf is the interface we use to invoke methods on the server. Here we are retrieving the server object by looking it up in the registry
		try {
			SwitchHandlerAPI controllerIntf =(SwitchHandlerAPI)Naming.lookup(serverURL + controllerExt);
			
			ArrayList<String> arrl = controllerIntf.listSwitches();
			System.out.println(arrl.toString());
			
			
			
			
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}