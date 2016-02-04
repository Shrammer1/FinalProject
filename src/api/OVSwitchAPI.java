package api;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Queue;
import org.openflow.protocol.OFType;

/**
 * @author Nicholas Landriault
 *	
 *This is the interface that both the client and server MUST have either 
 *included in their source files or included in their build path.
 */

public interface OVSwitchAPI extends Remote {
	public String getSwitchID() throws RemoteException;
	public String getSwitchFullName() throws RemoteException;
	public String getSwitchNickName() throws RemoteException;
	public void setSwitchNickName(String name) throws RemoteException;
	public int getSwitchTimeout()throws RemoteException;
	public void setSwitchTimeout(int switchTimeout)throws RemoteException;
	public boolean register(String id, OFType type) throws RemoteException;
	public boolean register(String id, ArrayList<OFType> types) throws RemoteException;
	public boolean unregister(String id, OFType type) throws RemoteException;
	public boolean unregister(String id, ArrayList<OFType> types) throws RemoteException;
	public boolean isAlive() throws RemoteException;
	public void sendMsg(byte[] msg) throws RemoteException;
	public Queue<byte[]> getMessages(String id) throws RemoteException;
	
	/**
	 * Blocks until there is a message for the registered remote application
	 * 
	 * @param id String id for the registered OFMessage types
	 * @return OFMessage at the front of the queue 
	 */
	public byte[] getMessage(String id)throws RemoteException;
	
	public String listPorts() throws RemoteException;
	
}
