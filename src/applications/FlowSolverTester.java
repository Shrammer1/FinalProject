package applications;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import api.AppAPI;
import api.ControllerAPI;
import flowsolver.Domain;
import flowsolver.FlowAction;
import flowsolver.FlowRequest;
import flowsolver.TrafficClass;

public class FlowSolverTester {

	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
		String connectTo = "127.0.0.1:1099";
		ControllerAPI controllerIntf = (ControllerAPI) Naming.lookup("rmi://" + connectTo + "/controller");
		AppAPI appIntf = (AppAPI) controllerIntf.register(0);
		
		Domain src = new Domain();
		ArrayList<byte[]> networks = new ArrayList<byte[]>();
		ByteBuffer n = ByteBuffer.allocate(4);
		/*
		n.put((byte) 172);
		n.put((byte) 16);
		n.put((byte) 1);
		n.put((byte) 101);
		networks.add(n.array());
		src.setNetworks(networks);
		*/
		ArrayList<byte[]> macs = new ArrayList<byte[]>();
		n = ByteBuffer.allocate(4);
		n.put((byte) 209);
		n.put((byte) 85);
		n.put((byte) 146);
		n.put((byte) 101);
		macs.add(n.array());
		src.setMacList(macs);
		
		
		networks = new ArrayList<byte[]>();
		n = ByteBuffer.allocate(5);
		n.put((byte) 172);
		n.put((byte) 16);
		n.put((byte) 1);
		n.put((byte) 0);
		n.put((byte) 24);
		networks.add(n.array());
		Domain dst = new Domain();
		dst.setNetworks(networks);
		TrafficClass tclass = new TrafficClass();
		
		FlowRequest fReq = new FlowRequest(src,dst,tclass , 3, FlowAction.DROP);
		
		//appIntf.requestAddFlow(fReq);
		
		appIntf.requestDelFlow(fReq);
		
		
	}

}
