package controller;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;

import flowsolver.Domain;
import flowsolver.DomainEntry;
import flowsolver.DomainType;
import flowsolver.FlowAction;
import flowsolver.FlowRequest;
import flowsolver.TrafficClass;

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
		
		//Port 1099 for RMI registry functionality
		Controller controller = null;
		try {
			controller = new Controller(args[0], 1099,l2_learning);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//Starting a Controller Thread
		controller.start();
		
		
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
		Domain src = new Domain();
		ArrayList<byte[]> networks = new ArrayList<byte[]>();
		ByteBuffer n = ByteBuffer.allocate(4);
		n.put((byte) 172);
		n.put((byte) 16);
		n.put((byte) 1);
		n.put((byte) 101);
		networks.add(n.array());
		src.setNetworks(networks);
		
		networks = new ArrayList<byte[]>();
		n = ByteBuffer.allocate(4);
		n.put((byte) 172);
		n.put((byte) 16);
		n.put((byte) 1);
		n.put((byte) 2);
		networks.add(n.array());
		Domain dst = new Domain();
		dst.setNetworks(networks);
		TrafficClass tclass = new TrafficClass();
		
		FlowRequest fReq = new FlowRequest(src,dst,tclass , 1, FlowAction.ALLOW);
		
		Application app = null;
		try {
			app = new Application(100, controller);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		controller.getFlowSolver().requestAddFlow(fReq, app);
		*/
		//END OF TEST CODE **************************************************************************************
		
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
