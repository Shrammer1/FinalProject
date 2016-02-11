package controller;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFType;
import org.openflow.protocol.factory.BasicFactory;

import api.ControllerAPI;
import topology.TopologyMapper;

//Controller runnable class
public class Controller extends UnicastRemoteObject implements Runnable, ControllerAPI{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7634450767607143395L;

	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	 /* Built-in log levels available : SEVERE, WARNING, INFO, CONFIG, FINE, 
	 * FINER, FINEST
	 */
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	//For RMI implementation
	private Registry reg;
	private int port;
	private Thread t;
	private String threadName;
	private TopologyMapper topo;
	private SwitchHandler swhl;
	private ArrayList<OFSwitch> switches = new ArrayList<OFSwitch>();
	
	//boolean variable to control Layer 2 behavior
	private boolean l2_learning;
	
	public Registry getReg() {
		return reg;
	}
	
	
	public ArrayList<OFSwitch> getSwitches() {
		return switches;
	}   
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public Controller(String t, int prt) throws RemoteException{
		this.threadName = t;
		this.port = prt;
		this.l2_learning=false;
	}
	public Controller(String t, int prt,boolean l2_learning) throws RemoteException{
		this.threadName = t;
		this.port = prt;
		this.l2_learning=l2_learning;
	}
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	
	//Controller listens on port 6001
	@Override
	public void run(){
		
		try{
			ServerSocketChannel listenSock = ServerSocketChannel.open();
			listenSock.configureBlocking(false);
		    listenSock.socket().bind(new java.net.InetSocketAddress(6001));
		    listenSock.socket().setReuseAddress(true);
		    
		    //Instantiating and Starting the switch handler
		    swhl = new SwitchHandler(threadName + "_SwitchHandler",this);
		    topo = new TopologyMapper("TopogoyMapper",this);
		    reg.rebind(threadName, this);
		    swhl.start();
		    topo.start();
		    
		    //Always running and listening for tcp connections
			while(true){
				BasicFactory factory = BasicFactory.getInstance();
			    SocketChannel sock = null;
			    while(sock==null){
			    	Thread.sleep(0,1);
			    	sock = listenSock.accept();
			    }
		        swhl.addSwitch(sock,new OFMessageAsyncStream(sock, factory));
			}
		}
		catch(Exception e){
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	
	
	//Method to stop a Thread of a Controller
	public void stop(){
		t.interrupt();
	}
	
	//Method to start a Thread of a Controller
	public void start (){
		LOGGER.setLevel(Level.FINEST);
		try {
			//Creating a file to store logs
			FileHandler fh = new FileHandler("Logs/OVS-Controller.log");
			//SimpleFormatter sf = new SimpleFormatter();
			//fh.setFormatter(sf);
			
			fh.setFormatter(new LogFormatter());
			LOGGER.addHandler(fh);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		//Informing that Thread has started. Controller Thread ready and listening
		LOGGER.info("Starting " +  threadName);
		//Part of the RMI implementation on the server
		try {
			reg = LocateRegistry.createRegistry(port);
			LOGGER.info("RMI Registry created on port: " + port);
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		//If the Thread does not exist, then create one
		if (t == null){
			t = new Thread (this, threadName);
			t.start();
		}
   }
	
	
	/**************************************************
	 * PRIVATE CLASS
	 **************************************************/
	
	//Entire private class to be used for logs formatting.
	private class LogFormatter extends Formatter
	{
	    SimpleDateFormat dateFormatter = new SimpleDateFormat ("MM/dd/yy '-' HH:mm:ss a");

	    public String format(LogRecord record)
	    {
	        StringBuffer buffer = new StringBuffer();
	        String timeString = dateFormatter.format(new Date(record.getMillis()));
	        buffer.append("Level: " + record.getLevel().toString() + " - ");
	        buffer.append("Time: " + timeString  + " | ");
	        buffer.append(record.getMessage() + "\n");
	        return buffer.toString();
	    }
	}


	public boolean getL2Learning() {
		return l2_learning;
	}


	public TopologyMapper getTopologyMapper() {
		return topo;
	}


	@Override
	public String getSwitchFullName(String switchID) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getSwitchFullName();
			}
		}
		return null;
	}


	@Override
	public String getSwitchNickName(String switchID) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getSwitchNickName();
			}
		}
		return null;
	}


	@Override
	public void setSwitchNickName(String switchID, String name) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				sw.setSwitchNickName(name);
				return;
			}
		}
	}


	@Override
	public int getSwitchTimeout(String switchID) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getSwitchTimeout();
			}
		}
		return -1;
	}


	@Override
	public void setSwitchTimeout(String switchID, int switchTimeout) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				sw.setSwitchTimeout(switchTimeout);
				return;
			}
		}
	}


	@Override
	public boolean register(String switchID, String id, OFType type) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.register(id, type);
			}
		}
		return false;
	}


	@Override
	public boolean register(String switchID, String id, ArrayList<OFType> types) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.register(id, types);
			}
		}
		return false;
	}


	@Override
	public boolean unregister(String switchID, String id, OFType type) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.unregister(id, type);
			}
		}
		return false;
	}


	@Override
	public boolean unregister(String switchID, String id, ArrayList<OFType> types) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.unregister(id, types);
			}
		}
		return false;
	}


	@Override
	public boolean isAlive(String switchID) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.isAlive();
			}
		}
		return false;
	}


	@Override
	public void sendMsg(String switchID, byte[] msg) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				sw.sendMsg(msg);
				return;
			}
		}
	}


	@Override
	public Queue<byte[]> getMessages(String switchID, String id) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getMessages(id);
			}
		}
		return null;
	}


	@Override
	public byte[] getMessage(String switchID, String id) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.getMessage(id);
			}
		}
		return null;
	}


	@Override
	public String listPorts(String switchID) throws RemoteException {
		for(OFSwitch sw:switches){
			if(sw.getSwitchID().equals(switchID)){
				return sw.listPorts();
			}
		}
		return "";
	}


	@Override
	public ArrayList<String> listSwitches() throws RemoteException {
		return swhl.listSwitches();
	}

}