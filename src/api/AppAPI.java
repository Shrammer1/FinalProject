package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import flowsolver.FlowRequest;

public interface AppAPI extends Remote{
	public String getSwitchFullName(String switchID) throws RemoteException;
	public String getSwitchNickName(String switchID) throws RemoteException;
	public void setSwitchNickName(String switchID,String name) throws RemoteException;
	public int getSwitchTimeout(String switchID)throws RemoteException;
	public boolean setSwitchTimeout(String switchID,int switchTimeout)throws RemoteException;
	public boolean isAlive(String switchID) throws RemoteException;
	public String listPorts(String switchID) throws RemoteException;
	public ArrayList<String> listSwitches() throws RemoteException;
	
	public int getID() throws RemoteException;
	
	public boolean requestAddFlow(FlowRequest request) throws RemoteException;
	public boolean requestDelFlow(FlowRequest request) throws RemoteException;
	
}
