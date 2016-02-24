package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControllerAPI extends Remote{
	public Object register(int priority) throws RemoteException;
	public String listApplications() throws RemoteException;
}
