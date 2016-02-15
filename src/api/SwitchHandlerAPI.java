package api;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import controller.OVSwitch;

	/**
	 * @author Nicholas Landriault
	 *	
	 *This is the interface that both the client and server MUST have either 
	 *included in their source files or included in their build path.
	 *
	 *Think of an interface like a prototype for all methods in the server
	 */
public interface SwitchHandlerAPI extends Remote {
	public ArrayList<String> listSwitches() throws RemoteException;
	public String[] listRegisteredObjects() throws RemoteException;
	public boolean setSwitchTimeout(String switchID, int newTimout) throws RemoteException ;
}