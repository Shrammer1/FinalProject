package applications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import api.AppAPI;
import api.CLIModule;
import api.ControllerAPI;
import flowsolver.Domain;
import flowsolver.FlowAction;
import flowsolver.FlowRequest;
import flowsolver.TrafficClass;

/**
 * The firewall application's responsibility is to provide functionality for defining Domains
 * and FlowRequests referencing those Domains, and to pass those FlowRequests to the controller to
 * be implemented in the network. The firewall application also provides an implementation of a 
 * command-line interface to allow network administrators to perform configuration of these Domains
 * and FlowRequests.
 * @author Wes
 *
 */
public class FirewallApp extends UnicastRemoteObject implements CLIModule {

	private static final long serialVersionUID = 3038609942924199599L;
//	private ControllerAPI controller;
	private AppAPI api;
	private HashMap<String, Domain> domains = new HashMap<String, Domain>(); // tracks all configured domains, each with a name
	private HashMap<String, FlowRequest> flowReqs = new HashMap<String, FlowRequest>(); // tracks all configured flow requests
	
	/**
	 * This application is to be invoked with the arguments: <controllerip:port> <priority>
	 * For example: java applications FirewallApp 127.0.0.1:1099 100
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		// TODO verification of command-line arguments
		String connectTo = args[0];
		int priority = Integer.parseInt(args[1]);
		
		ControllerAPI controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + connectTo + "/controller");
		FirewallApp firewall = new FirewallApp(controllerIntf, priority);
		
		firewall.test();
		firewall.testCLI();
	}
	
	public FirewallApp(ControllerAPI controller, int priority) throws RemoteException {
//		this.controller = controller;
		this.api = (AppAPI) controller.register(priority);
	}
	
	public void test() throws RemoteException {
		// define two domains A and B
		Domain domA = new Domain();
		Domain domB = new Domain();
		
		domA.getNetworks().add(new byte[] {(byte) 192,(byte) 168,10,0,24}); // domA = 192.168.10.0/24
		domB.getNetworks().add(new byte[] {(byte) 192,(byte) 168,20,0,24}); // domA = 192.168.20.0/24
		
		// push a flow request that blocks all traffic from A to B
		FlowRequest req = new FlowRequest(domA, domB, new TrafficClass(), 0, FlowAction.DROP);
		api.requestAddFlow(req);
	}
	
	public void testCLI() {
		/*
		 * This method is just to test the interactive CLI functionality
		 */
		try {
		String appName = getApplicationContextName();
		System.out.print("cli/" + appName + "> ");
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input = "";
			try {
				input = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String output = executeCommand(input);
			System.out.println(output);
			System.out.println();
			System.out.print("cli/" + appName + "> ");
		}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, Domain> getDomains() {
		return domains;
	}
	
	public HashMap<String, FlowRequest> getFlowReqs() {
		return flowReqs;
	}
	
	// CLI interface below ////////////////////////////////////////////////////
	
	@Override
	public String getApplicationContextName() throws RemoteException {
		return "firewall";
	}

	@Override
	public String executeCommand(String command) throws RemoteException {
		if (command == null || command.trim().length() == 0)
			return ""; // we all love to mash the enter key
		
		try {
			String[] args = command.split(" ");
			switch (args[0]) {
			case "show":
				switch (args[1]) {
				case "domain-list":
					return getDomains().toString();
				case "policy-list":
					return getFlowReqs().toString();
				}
				break;
			case "delete":
				break;
			case "domain": // domain <name> ...
				if (args.length < 5)
					break; // require a useful command after the name
				String name = args[1];
				Domain dom = domains.get(name);
				if (dom == null) {
					// Create the domain if not already present.
					dom = new Domain();
					domains.put(name, dom);
				}
				switch (args[2]) {
				case "add":
					switch (args[3]) {
					case "ip": // domain X add ip <list>
						for (int i = 4; i < args.length; i++) {
							// Parse the ip network argument, which is in CIDR notation with an optional mask.
							// If the mask is not present, assume the mask is /32. 
							byte[] prefix = cidrToBytes(args[i]);
							if (prefix == null) {
								return "Bad prefix: " + args[i];
							}
							dom.getNetworks().add(prefix);
						}
						return "";
					case "mac": // domain X add mac <list>
						break;
					case "domain": // domain X add domain <list>
						break;
					}
					break;
				}
				break;
			case "policy":
				break;
			default:
			}
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// someone fed me garbage
		}
		return "Unrecognized command: " + command;
	}
	
	/**
	 * 
	 * @param network IPv4 network prefix in CIDR notation
	 * @return Byte array with 5 values denoting IPv4 address and network mask, or null if the string is malformed.
	 */
	private byte[] cidrToBytes(String network) {
		return null;
	}
}
