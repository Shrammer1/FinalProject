package controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Queue;

import org.openflow.protocol.OFType;

import api.AppAPI;
import flowsolver.FlowRequest;

public class Application extends UnicastRemoteObject implements AppAPI{
	
	//TODO: should applications ever expire? if yes then find some way to have applications expire so that objects may be cleaned up. 
	
	private static final long serialVersionUID = 5697031628246495923L;

	private static int nextID = 1;
	
	private String name;
	private int priority;
	private int id;
	private Controller controller;
	
	public Application(int priority, Controller controller) throws RemoteException{
		this.priority = priority;
		this.controller = controller;
		this.id = Application.getNextID();
	}
	
	public String getApplicationName() throws RemoteException{
		return name;
	}

	public void setApplicationName(String name) throws RemoteException{
		this.name = name;
	}

	public String getSwitchFullName(String switchID) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getSwitchFullName();
			}
		}
		return null;
	}


	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getID() throws RemoteException{
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getSwitchNickName(String switchID) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getSwitchNickName();
			}
		}
		return null;
	}


	
	public void setSwitchNickName(String switchID, String name) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				sw.setSwitchNickName(name);
				return;
			}
		}
	}


	
	public int getSwitchTimeout(String switchID) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getSwitchTimeout();
			}
		}
		return -1;
	}


	
	public boolean setSwitchTimeout(String switchID, int switchTimeout) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				sw.setSwitchTimeout(switchTimeout);
				return true;
			}
		}
		return false;
	}


	
	public boolean register(String switchID, String id, OFType type) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.register(id, type);
			}
		}
		return false;
	}


	
	public boolean register(String switchID, String id, ArrayList<OFType> types) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.register(id, types);
			}
		}
		return false;
	}


	
	public boolean unregister(String switchID, String id, OFType type) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.unregister(id, type);
			}
		}
		return false;
	}


	
	public boolean unregister(String switchID, String id, ArrayList<OFType> types) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.unregister(id, types);
			}
		}
		return false;
	}


	
	public boolean isAlive(String switchID) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.isAlive();
			}
		}
		return false;
	}


	
	public void sendMsg(String switchID, byte[] msg) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				sw.sendMsg(msg);
				return;
			}
		}
	}


	
	public Queue<byte[]> getMessages(String switchID, String id) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getMessages(id);
			}
		}
		return null;
	}


	
	public byte[] getMessage(String switchID, String id) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getMessage(id);
			}
		}
		return null;
	}


	
	public String listPorts(String switchID) throws RemoteException {
		for(OFSwitch sw:controller.getSwitches()){
			if(sw.getSwitchID().equals(switchID)){
				return sw.listPorts();
			}
		}
		return "";
	}


	
	public ArrayList<String> listSwitches() throws RemoteException {
		return controller.getSwitchHandler().listSwitches();
	}
	
	
	@Override
	public boolean requestAddFlow(FlowRequest request) throws RemoteException {
		return controller.getFlowSolver().requestAddFlow(request, this);
	}
	
	@Override
	public boolean requestDelFlow(FlowRequest request) throws RemoteException {
		return controller.getFlowSolver().requestDelFlow(request, this);
	}
	
	
	
	
	
	//***********************************************************************************
	//STATIC METHOD FOR GETTING UNIQUE IDs
	private static int getNextID(){
		return nextID++;
	}

	
	
}
