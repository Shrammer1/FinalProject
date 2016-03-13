package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is to be implemented by controller applications that want to be accessed via the 
 * controller's command-line interface.
 * @author Wes
 *
 */
public interface CLIModule extends Remote {
	/**
	 * Provides the caller an application context name to be used at the CLI prompt and to uniquely identify this CLI module in
	 * the command-line namespace.
	 * @return The name of the application context.
	 * @throws RemoteException
	 */
	public String getApplicationContextName() throws RemoteException;
	/**
	 * Instructs the module to execute the given command and return a String as output.
	 * @param command Command to execute, NOT INCLUDING THE APPLICATION CONTEXT NAME. So, if the user entered "firewall show domain A" and
	 * 		the object implementing this has a context name of "firewall" then this method should be called with the string "show domain A".
	 * @return Results of the command execution.
	 * @throws RemoteException
	 */
	public String executeCommand(String command) throws RemoteException;
}
