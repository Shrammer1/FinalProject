import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Nick
 *	
 *This is the interface that both the client and server MUST have either included in their source files or included in their build path.
 */

public interface OVSwitchAPI extends Remote {
	public String getSwitchName() throws RemoteException;
	public int getSwitchTimeout()throws RemoteException;
	public void setSwitchTimeout(int switchTimeout)throws RemoteException;
}
