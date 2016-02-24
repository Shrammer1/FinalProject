package controller;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;

import flowsolver.Domain;
import flowsolver.DomainEntry;
import flowsolver.DomainType;

public class Server {

	/*
	 * Main method to be used on CLI with additional arguments for extra
	 * functionality. L2 behavior argument option written by Wesley Cooper.
	 */
	public static void main(String args[]){
		boolean l2_learning=false;
		//Parsing CLI arguments
		if(args.length<1){
			System.err.println(printUsage());
			System.exit(1); //0=OK; 1=ERROR; -1=EXCEPTION
		}
		
		l2_learning = true; //change this line and the if statement to add back in l2 learning app functionaility
		
		/*
		//this is going to have to be changed before the final version to incorporate all options
		if(args.length==2){
			if(args[1].equals("-l2")){
				l2_learning = true;
			}
			else{
				System.err.println(printUsage());
				System.exit(1); //0=OK; 1=ERROR; -1=EXCEPTION
			}
		}
		*/
		
		//TEST CODE HERE ****************************************************************************************
		
		/*
		ArrayList<byte[]> networks = new ArrayList<byte[]>();
		ByteBuffer n = ByteBuffer.allocate(5);
		n.put((byte) 127);
		n.put((byte) 0);
		n.put((byte) 0);
		n.put((byte) 1);
		n.put((byte) 32);
		networks.add(n.array());
		
		n = ByteBuffer.allocate(5);
		n.put((byte) 127);
		n.put((byte) 0);
		n.put((byte) 0);
		n.put((byte) 2);
		n.put((byte) 32);
		networks.add(n.array());
		
		n = ByteBuffer.allocate(5);
		n.put((byte) 127);
		n.put((byte) 0);
		n.put((byte) 0);
		n.put((byte) 129);
		n.put((byte) 25);
		networks.add(n.array());
		
		ArrayList<byte[]> macList = new ArrayList<byte[]>();
		ByteBuffer mac = ByteBuffer.allocate(6);
		mac.put((byte) 0);
		mac.put((byte) 0);
		mac.put((byte) 0);
		mac.put((byte) 12);
		mac.put((byte) 15);
		mac.put((byte) 23);
		macList.add(n.array());
		
		mac = ByteBuffer.allocate(6);
		mac.put((byte) 0);
		mac.put((byte) 0);
		mac.put((byte) 0);
		mac.put((byte) 15);
		mac.put((byte) 15);
		mac.put((byte) 1);
		macList.add(n.array());
		
		Domain top = new Domain();
		top.setNetworks(networks);
		top.setMacList(macList);
		
		Domain sub = new Domain();
		
		macList = new ArrayList<byte[]>();
		mac = ByteBuffer.allocate(6);
		mac.put((byte) 0);
		mac.put((byte) 0);
		mac.put((byte) 0);
		mac.put((byte) 12);
		mac.put((byte) 15);
		mac.put((byte) 23);
		macList.add(n.array());
		
		networks = new ArrayList<byte[]>();
		n = ByteBuffer.allocate(5);
		n.put((byte) 127);
		n.put((byte) 0);
		n.put((byte) 0);
		n.put((byte) 1);
		n.put((byte) 32);
		networks.add(n.array());
		
		sub.setMacList(macList);
		sub.setNetworks(networks);
		
		ArrayList<Domain> l = new ArrayList<Domain>();
		l.add(sub);
		
		top.setSubDomains(l);
		
		ArrayList<DomainEntry> result = top.toArray();
		ArrayList<byte[]> ips = new ArrayList<byte[]>();
		ArrayList<byte[]> macs = new ArrayList<byte[]>();
		
		for(DomainEntry entry:result){
			if(entry.getType() == DomainType.IP){
				ips.addAll(entry.getValues());
			}
			else if(entry.getType() == DomainType.Mac){
				macs.addAll(entry.getValues());
			}
		}
		
		
		*/
		//END OF TEST CODE **************************************************************************************
		
		//Port 1099 for RMI registry functionality
		Controller controller = null;
		try {
			controller = new Controller(args[0], 1099,l2_learning);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//Starting a Controller Thread
		controller.start();
		
	}
	
	/**************************************************
	 * PRIVATE METHODS
	 **************************************************/
	
	private static long ipToLong(String ipAddress) {

		String[] ipAddressInArray = ipAddress.split("\\.");

		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {

			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);

		}

		return result;
	  }
	
	
	private static String printUsage(){
		return "Usage: THISFILENAME <Server_Name>\nOptions:\n\n-l2\tBuilt in standard layer 2 learning";
	}
}
