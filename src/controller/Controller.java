package controller;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.factory.BasicFactory;

import api.ControllerAPI;
import flowsolver.FlowSolver;
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
	private FlowSolver fsolv;
	private ArrayList<OFSwitch> switches = new ArrayList<OFSwitch>();
	private ArrayList<Application> apps = new ArrayList<Application>();
	
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
	
	public void run(){
		
		try{
			ServerSocketChannel listenSock = ServerSocketChannel.open();
			listenSock.configureBlocking(false);
		    listenSock.socket().bind(new java.net.InetSocketAddress(6001));
		    listenSock.socket().setReuseAddress(true);
		    
		    //Instantiating and Starting the switch handler
		    swhl = new SwitchHandler(threadName + "_SwitchHandler",this);
		    topo = new TopologyMapper("TopologyMapper",this);
		    fsolv = new FlowSolver(this);
		    reg.rebind(threadName, this);
		    swhl.start();
		    topo.start();
		    
		    OFMessageAsyncStream.defaultBufferSize = 655360;
		    BasicFactory factory = BasicFactory.getInstance();
		    //Always running and listening for tcp connections
		    SocketChannel sock = null;
		    long lastCheck = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			while(true){
				Thread.sleep(0,1);
		    	sock = listenSock.accept();
			    if(sock!=null){
			    	swhl.addSwitch(sock,new OFMessageAsyncStream(sock, factory));
			    	sock=null;
			    }
			    if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastCheck > 5){
			    	Iterator<Application> itr = apps.iterator();
			    	while(itr.hasNext()){
			    		Application app = itr.next();
			    		if(!(app.isAlive())){
			    			itr.remove();
			    			LOGGER.log(Level.INFO, "Application \'" + app.getApplicationName() + "\' has expired.");
			    		}
			    	}
			    	lastCheck = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			    }
						
			    
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
	
	public FlowSolver getFlowSolver(){
		return fsolv;
	}

	public TopologyMapper getTopologyMapper() {
		return topo;
	}
	public SwitchHandler getSwitchHandler(){
		return swhl;
	}

	
	public Object register(int priority, Remote remoteApp) throws RemoteException {
		Application newApp = new Application(priority,this,remoteApp);
		apps.add(newApp);
		return newApp;
	}
	
	@Override
	public Object register(int priority) throws RemoteException {
		Application newApp = new Application(priority,this);
		apps.add(newApp);
		return newApp;
	}
	
	

	@Override
	public String listApplications() throws RemoteException {
		String retVal = "";
		for(Application app:apps){
			retVal=retVal+app.getApplicationName()+"\n"; 
		}
		return retVal;
	}


	@Override
	public ArrayList<Remote> getCLIApplications() throws RemoteException {
		ArrayList<Remote> retVal = new ArrayList<Remote>();
		for(Application app:apps){
			retVal.add(app.getRemoteApp());
		}
		return retVal;
	}


	
}