package controller;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.openflow.protocol.OFType;

/**
 * @author Nick
 *	
 *This is the interface that both the client and server MUST have either included in their source files or included in their build path.
 */

public interface OVSwitchAPI extends Remote {
	public String getSwitchName() throws RemoteException;
	public int getSwitchTimeout()throws RemoteException;
	public void setSwitchTimeout(int switchTimeout)throws RemoteException;
	public boolean register(String id, OFType type) throws RemoteException;
	public boolean register(String id, ArrayList<OFType> types) throws RemoteException;
	public boolean unregister(String id, OFType type) throws RemoteException;
	public boolean unregister(String id, ArrayList<OFType> types) throws RemoteException;
	public boolean isAlive() throws RemoteException;
	
}
