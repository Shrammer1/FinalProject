package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public interface ControllerAPI extends Remote{
	public Object register(int priority, Remote remoteApp) throws RemoteException;
	public Object register(int priority) throws RemoteException;
	public String listApplications() throws RemoteException;
	public ArrayList<Remote> getCLIApplications() throws RemoteException;
}
