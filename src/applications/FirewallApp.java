package applications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
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
		if (args.length != 2) {
			System.out.println("Usage: FirewallApp <controller_ipv4_addr:port> <priority>");
			return;
		}
		String connectTo = args[0];
		int priority = Integer.parseInt(args[1]);
		
		ControllerAPI controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + connectTo + "/controller");
		FirewallApp firewall = new FirewallApp(controllerIntf, priority);
		
		firewall.test();
		firewall.testCLI();
	}
	
	public FirewallApp(ControllerAPI controller, int priority) throws RemoteException {
//		this.controller = controller;
		this.api = (AppAPI) controller.register(priority, RemoteObject.toStub(this));
	}
	
	public void test() throws RemoteException {
		// define two domains A and B
		Domain domA = new Domain("TestDomain1");
		Domain domB = new Domain("TestDomain2");
		
		domA.getNetworks().add(new byte[] {(byte) 192,(byte) 168,10,0,24}); // domA = 192.168.10.0/24
		domB.getNetworks().add(new byte[] {(byte) 192,(byte) 168,20,0,24}); // domA = 192.168.20.0/24
		
		domains.put(domA.getName(), domA);
		domains.put(domB.getName(), domB);
		
		// push a flow request that blocks all traffic from A to B
		FlowRequest req = new FlowRequest("testPolicy", domA, domB, new TrafficClass(), 0, FlowAction.DROP);
		flowReqs.put(req.getName(), req);
		api.requestAddFlow(req);
	}
	
	public void testCLI() {
		/*
		 * This method is just to test the interactive CLI functionality
		 */
		try {
			String appName = getApplicationContextName();
			System.out.print(appName + "> ");
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
				System.out.print(appName + "> ");
			}
		} catch (RemoteException e) {
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
					return printDomains();
				case "policy-list":
					return printFlowReqs();
				}
				break;
			case "delete":
				switch (args[1]) {
				case "domain": // delete domain <name>
					String domainName = args[2];
					// make sure the domain is not in use before deleting it
					for (FlowRequest req : flowReqs.values()) {
						if (req.getSrc().getName().equals(domainName) ||
								req.getDst().getName().equals(domainName)) {
							return "Error: domain in use by policy \"" + req.getName() + "\"";
						}
					}
					domains.remove(domainName);
					return "";
				case "policy": // delete policy <name>
					String flowReqName = args[2];
					FlowRequest request = flowReqs.get(flowReqName);
					api.requestDelFlow(request); // when a flow request is deleted, we need to inform the controller
					flowReqs.remove(flowReqName);
					break;
				}
				break;
			case "domain": // domain <name> ...
			{
				if (args.length < 5)
					break; // require a useful command after the name
				String name = args[1];
				Domain dom = domains.get(name);
				if (dom == null) {
					// Create the domain if not already present.
					dom = new Domain(name);
					domains.put(name, dom);
				}
				switch (args[2]) {
				case "add":
					switch (args[3]) {
					case "ip": // domain <name> add ip <list>
						for (int i = 4; i < args.length; i++) {							
							// Parse the ip network argument, which is in CIDR notation with an optional mask.
							// If the mask is not present, assume the mask is /32. 
							byte[] prefix = cidrToBytes(args[i]);
							if (prefix == null) {
								return "Bad IPv4 prefix: " + args[i];
							}
							// Don't add the prefix if we already contain it
							// have to iterate the networks array and run Arrays.equals() on each element
							for (byte[] existingPrefix : dom.getNetworks()) {
								if (Arrays.equals(existingPrefix, prefix)) {
									return ""; // it's a duplicate, do nothing and report no error
								}
							}
							
							dom.getNetworks().add(prefix);
						}
						return "";
					case "mac": // domain <name> add mac <list>
						for (int i = 4; i < args.length; i++) {
							// format should be "00:00:01:23:45:67"
							byte[] mac = macToBytes(args[i]);
							if (mac == null) {
								return "Bad MAC address: " + args[i];
							}
							// Don't add the mac if we already contain it
							// have to iterate the mac array and run Arrays.equals() on each element
							for (byte[] existingMac : dom.getMacList()) {
								if (Arrays.equals(existingMac, mac)) {
									return ""; // it's a duplicate, do nothing and report no error
								}
							}
							
							dom.getMacList().add(mac);
						}
						return "";
					case "domain": // domain <name> add domain <list>
						for (int i = 4; i < args.length; i++) {
							// Search for the domain given by name and make sure it exists 
							String domainName = args[i];
							Domain subDomain = domains.get(domainName);
							if (subDomain == null)
								return "Domain does not exist: " + args[i];
							else
								dom.getSubDomains().add(domains.get(domainName));
						}
						return "";
					}
					break;
				}
				break;
			}
			case "policy": // policy <name> ...
			{
				if (args.length < 8) 
					break; // require minimum set of parameters (to/from, and action)
				
				String policyName = args[1];
				Domain src = null, dst = null;
				TrafficClass tClass = new TrafficClass();
				int priority = 0;
				FlowAction action = FlowAction.DROP;
				
				// these flags track whether the required syntax for the the command has been met
				//	That is, policy must specify an action and at least one domain.
				boolean hasDomain = false;
				boolean hasAction = false;
				boolean hasPriority = false;
				
				for (int i = 2; i < args.length; i++) { // interpret the rest of the parameters
					switch (args[i]) {
					case "from":
						hasDomain = true;
						src = domains.get(args[++i]);
						if (src == null)
							return "Error: source domain does not exist";
						break;
					case "to":
						hasDomain = true;
						dst = domains.get(args[++i]);
						if (dst == null)
							return "Error: destination domain does not exist";
						break;
					case "srcport":
						tClass.setSrcPort(Short.parseShort(args[++i]));
						break;
					case "dstport":
						tClass.setDstPort(Short.parseShort(args[++i]));
						break;
					case "priority":
						hasPriority = true;
						priority = Integer.parseInt(args[++i]);
						break;
					case "action":
						hasAction = true;
						switch (args[++i]) {
						case "drop":
							action = FlowAction.DROP;
							break;
						case "allow":
							action = FlowAction.ALLOW;
							break;
						}
						break;
					}
				}
				if (!hasDomain || !hasAction || !hasPriority) {
					return "Error: required parameters missing.\nUsage: policy <name> <from <domain-name> | to <domain-name>> [srcport <srcport>] [dstport <dstport>] priority <priority> action <action>";
				}
				
				// if the policy already exists, we first delete the existing policy in order to overwrite it
				if (flowReqs.containsKey(policyName)) {
					api.requestDelFlow(flowReqs.get(policyName));
					flowReqs.remove(policyName);
				}
				
				// install the new policy
				FlowRequest policy = new FlowRequest(policyName, src, dst, tClass, priority, action);
				api.requestAddFlow(policy);
				flowReqs.put(policyName, policy);
				
				return "";
			}
			default:
			}
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// someone fed me garbage
		}
		return "Unrecognized command: " + command;
	}
	
	/**
	 * Generates console output that describes the contents of all configured Domains.
	 * @return
	 */
	private String printDomains() {
		StringBuilder sb = new StringBuilder();
		for (String name : domains.keySet()) {
			Domain d = domains.get(name);
			sb.append("\nDomain: ").append(name).append("\n");
			sb.append("----------------------------------------\n");
			if (!d.getNetworks().isEmpty()) {
				sb.append("IPv4 networks:\n");
				for (byte[] prefix : d.getNetworks()) {
					sb.append("    ");
					sb.append(prefix[0] & 0xFF);
					sb.append(".");
					sb.append(prefix[1] & 0xFF);
					sb.append(".");
					sb.append(prefix[2] & 0xFF);
					sb.append(".");
					sb.append(prefix[3] & 0xFF);
					sb.append("/");
					sb.append(prefix[4] & 0xFF);
					sb.append("\n");
				}
			}
			if (!d.getMacList().isEmpty()) {
				sb.append("MAC addresses:\n");
				for (byte[] mac : d.getMacList()) {
					sb.append("    ");
					for (byte b : mac) {
						sb.append(byteToHex(b));
						if (b != mac[mac.length-1])
							sb.append(":");
					}
					sb.append("\n");
				}
			}
			if (!d.getSubDomains().isEmpty()) {
				sb.append("Subdomains:\n");
				for (Domain subdomain : d.getSubDomains()) {
					sb.append("    ");
					sb.append(subdomain.getName());
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Generates console output that describes the contents of all configured FlowRequests.
	 * @return
	 */
	private String printFlowReqs() {
		StringBuilder sb =  new StringBuilder();
		for (String name : flowReqs.keySet()) {
			FlowRequest req = flowReqs.get(name);
			sb.append("\nPolicy: " ).append(name).append("\n");
			sb.append("----------------------------------------\n");
			sb.append("Priority ").append(req.getPriority()).append("\n");
			sb.append("  From: ").append(req.getSrc() != null ? req.getSrc().getName() : "").append("\n");
			sb.append("    To: ").append(req.getDst() != null ? req.getDst().getName() : "").append("\n");
			
			sb.append(" Class: ");
			TrafficClass tc = req.getTrafficClass();
			StringBuilder classField = new StringBuilder();
			short srcPort = tc.getSrcPort();
			short dstPort = tc.getDstPort();
			if (srcPort > 0) classField.append("srcPort(").append(srcPort).append(") ");
			if (dstPort > 0) classField.append("dstPort(").append(dstPort).append(") ");
			if (classField.length() == 0) classField.append("Any");
			sb.append(classField);
			sb.append("\n");
			
			sb.append("Action: ");
			switch (req.getFlowAction()) {
			case ALLOW:
				sb.append("Allow");
				break;
			case DROP:
				sb.append("Drop");
				break;
			default:
				break;
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param network IPv4 network prefix in CIDR notation
	 * @return Byte array with 5 values denoting IPv4 address and network mask, or null if the string is malformed.
	 */
	private byte[] cidrToBytes(String network) {
		// format should be "0.0.0.0/0" or "0.0.0.0"
		String[] tokens = network.split("\\."); // need to escape "any character" character from regex
		if (tokens.length != 4)
			return null;
		byte[] bytes = new byte[5];
		bytes[0] = (byte) Integer.parseInt(tokens[0]);
		bytes[1] = (byte) Integer.parseInt(tokens[1]);
		bytes[2] = (byte) Integer.parseInt(tokens[2]);
		tokens = tokens[3].split("/");
		bytes[3] = (byte) Integer.parseInt(tokens[0]);
		bytes[4] = (byte) 32;
		if (tokens.length == 2)
			bytes[4] = (byte) Integer.parseInt(tokens[1]);
		return bytes;
	}
	
	/**
	 * 
	 * @param mac 
	 * @return
	 */
	private byte[] macToBytes(String mac) {
		// format should be "00:00:01:23:45:67"
		String[] tokens = mac.split(":");
		if (tokens.length != 6)
			return null;
		byte[] bytes = new byte[6];
		for (int i = 0; i < 6; i++) {
			bytes[i] = Integer.decode("0x" + tokens[i]).byteValue(); // convert from hex string
		}
		return bytes;
	}
	
	/*
	 * This code from http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	 */
	final protected static char[] hexArray = "0123456789abcdef".toCharArray(); // changed to lowercase - Wes
//	public static String bytesToHex(byte[] bytes) {
//	    char[] hexChars = new char[bytes.length * 2];
//	    for ( int j = 0; j < bytes.length; j++ ) {
//	        int v = bytes[j] & 0xFF;
//	        hexChars[j * 2] = hexArray[v >>> 4];
//	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//	    }
//	    return new String(hexChars);
//	}
	
	// modified by Wes to do a single byte at a time
	public static String byteToHex(byte b) {
		char[] hexChars = new char[2];
		int v = b & 0xFF;
		hexChars[0] = hexArray[v >>> 4];
		hexChars[1] = hexArray[v & 0x0F];
		return new String(hexChars);
	}
}
