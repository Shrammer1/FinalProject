package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Queue;

import org.openflow.protocol.OFType;

public interface ControllerAPI extends Remote{
	public String getSwitchFullName(String switchID) throws RemoteException;
	public String getSwitchNickName(String switchID) throws RemoteException;
	public void setSwitchNickName(String switchID,String name) throws RemoteException;
	public int getSwitchTimeout(String switchID)throws RemoteException;
	public boolean setSwitchTimeout(String switchID,int switchTimeout)throws RemoteException;
	public boolean register(String switchID,String id, OFType type) throws RemoteException;
	public boolean register(String switchID,String id, ArrayList<OFType> types) throws RemoteException;
	public boolean unregister(String switchID,String id, OFType type) throws RemoteException;
	public boolean unregister(String switchID,String id, ArrayList<OFType> types) throws RemoteException;
	public boolean isAlive(String switchID) throws RemoteException;
	public void sendMsg(String switchID,byte[] msg) throws RemoteException;
	public Queue<byte[]> getMessages(String switchID,String id) throws RemoteException;
	/**
	 * Blocks until there is a message for the registered remote application
	 * 
	 * @param id String id for the registered OFMessage types
	 * @return OFMessage at the front of the queue 
	 */
	public byte[] getMessage(String switchID,String id)throws RemoteException;
	
	public String listPorts(String switchID) throws RemoteException;
	
	
	
	public ArrayList<String> listSwitches() throws RemoteException;
	
	
	
}
