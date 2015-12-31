import java.net.MalformedURLException;
import java.rmi.*;


public class TestApplication {

	
	
	
	
	
	public static void main(String[] args) {
			
		String serverURL = "rmi://127.0.0.1:1099/controller"; //this is the registry entry we want to use
		//serverIntf is the interface we use to invoke methods on the server. Here we are retrieving the server object by looking it up in the registry
		try {
			OVSwitchAPI serverIntf =(OVSwitchAPI)Naming.lookup(serverURL);
			
			System.out.println(serverIntf.listSwitches());
			
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}