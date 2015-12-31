import java.rmi.Remote;
import java.rmi.RemoteException;

	/**
	 * @author Nick
	 *	
	 *This is the interface that both the client and server MUST have either included in their source files or included in their build path.
	 */
public interface OVSwitchAPI extends Remote { //Think of an interface like a prototype for all methods in the server
	public String listSwitches() throws RemoteException;
}