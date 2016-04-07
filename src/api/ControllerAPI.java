package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ControllerAPI extends Remote{
	public Object register(int priority, String name, Remote remoteApp) throws RemoteException;
	public Object register(int priority, String name) throws RemoteException;
	public String listApplications() throws RemoteException;
	public ArrayList<Remote> getCLIApplications() throws RemoteException;
}
