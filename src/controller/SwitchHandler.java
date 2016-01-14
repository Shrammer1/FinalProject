package controller;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

/**
 * 
 * @author Nicholas Landriault
 *
 *This class is to be used for handling all the switches run on the controller. 
 *It will track all the switches and will be able to return the instance of a 
 *switch that has a requested switchID
 *
 *
 */
public class SwitchHandler extends UnicastRemoteObject implements Runnable, SwitchHandlerAPI {
	
	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	/*final=its value cannot be changed once initialized
	 * static=variable shared among all the instances of this class as it
	 * belongs to the type and not to the actual objects themselves.
	 */
	
	private static final long serialVersionUID = 3480557320840477486L;

	/*
	 * LOGGER variable shared among all the instances of this class
	 * Built-in log levels available : SEVERE, WARNING, INFO, CONFIG, FINE, 
	 * FINER, FINEST
	 */	
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");	
	
	private String threadName;
	private Thread t;
	private ArrayList<OVSwitch> switches = new ArrayList<OVSwitch>();
	
	/*
	 * Remote interface to a simple remote object registry that 
	 * provides methods for storing and retrieving remote object references 
	 * bound with arbitrary string names. Used if RMI is implemented as the
	 * method for retrieving objects.
	 */
	private Registry reg;
	private String regName;
	
	/*
	 * boolean variable to control Layer 2 behavior
	 */
	private boolean l2_learning = false;
	
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public SwitchHandler(String name, String regName, Registry reg, 
			boolean l2_learning) throws RemoteException
	{
		threadName = name;
		this.reg = reg;
		this.regName = regName;
		this.l2_learning = l2_learning;
	}
	
	/**************************************************
	 * PRIVATE METHODS
	 **************************************************/
	private void abort(){
		stop();
	}
	
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	
	/*
	 * Method to obtain the list of all switches but allowing those waiting
	 * on the object's monitor to continue using it. 
	 */
	public synchronized ArrayList<String> listSwitches(){
		ArrayList<String> res = new ArrayList<String>();
		synchronized (switches) {
			for(int i = 0; i<switches.size();i++){
				res.add(switches.get(i).getSwitchName());
			}
			/*
			 * Allowing those waiting on the object's monitor to continue using
			 * it.
			 */
			switches.notifyAll();
		}
		return res;
	}
	
	/*
	 * Returns an array of the names bound in this registry. The array will 
	 * contain a snapshot of the names bound in this registry at the time of 
	 * the given invocation of this method. If RMI implemented.
	 */
	public String[] listRegisteredObjects(){
		try {
			return reg.list();
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		return null;
	}
	
	
	/*
	 * Alternate method for adding a Thread and Stream for a Switch. This uses
	 * an entire Private runnable Class which initializes all the required fields 
	 * for a switch object and also starts its corresponding Threads 
	 */
	public synchronized void addSwitch(SocketChannel sock, OFMessageAsyncStream stream){
		synchronized (switches) {
			try {
				new SwitchSetup(threadName + "_SetupSwitch_" + sock.getRemoteAddress(),threadName,sock, stream, this);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
	}
		
	
	/*
	 * Method for adding a Thread for an OVSwitch and allow those waiting on 
	 * the object's monitor to continue/start using it. If RMI used, then it
	 * binds the corresponding objects to the registry.
	 */
	public synchronized void addSwitch(OVSwitch sw){
		synchronized (switches) {
			switches.add(sw);
			switches.notifyAll();
			try {
				reg.rebind(sw.getSwitchName(), sw);
			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
	}
	
	
	/*
	 * Method to get the corresponding switch (argument sent in) from the
	 * list of switches. Also allows those waiting on the object's monitor to 
	 * continue using it.
	 */
	public synchronized OVSwitch getSwitch(String switchID){
		OVSwitch sw = null;
		synchronized (switches) {
			for(int i = 0; i<switches.size();i++){
				if((sw = switches.get(i)).getSwitchID().equals(switchID)) return sw;
			}
			switches.notifyAll();
		}
		return null;
	}
	
	
	
	@Override
	public void run(){
		while(!(t.isInterrupted())){
			try {
				Thread.sleep(0, 1);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
        this.abort();
	}
	
	
	/*
	 * Method to stop a Thread of a SwitchHandler
	 */
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
	}
	
	/*
	 * Method to start a Thread of SwitchHandler. If RMI used it also generates
	 * the corresponding bindings to the registry. If a Thread does not exist
	 * at the moment of the call, then it is created.
	 */
	public void start(){
      LOGGER.info("Starting " +  threadName);
      try {
		reg.rebind(regName, this);
		} 
      catch (AccessException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		} 
      catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}
	
	
	/**************************************************
	 * PRIVATE CLASS
	 **************************************************/
	/**
	 * 
	 * @author Nicholas Landriault
	 *
	 *The reason why this class exists is so that we can create multiple new 
	 *switches rapidly and have each switch be created by a separate 
	 *thread as each time a switch is created the thread must wait for the 
	 *switch to send the OFFeaturesReply message to get the switchID
	 *
	 */
	private class SwitchSetup implements Runnable{
		
		private String threadName;
		private Thread t;
		private OFMessageAsyncStream stream;
		private SocketChannel sock;
		private SwitchHandler swhl; 
		private String swName;
		
		
		/*
		 * Constructor
		 */
		public SwitchSetup(String name,String swName, SocketChannel sock, 
				OFMessageAsyncStream stream,SwitchHandler swhl) 
		{
			threadName = name;
			this.stream = stream;
			this.sock = sock;
			this.swhl = swhl;
			this.swName = swName;
			this.start();
		}	
		
		/*
		 * Method to abort a Thread of Switch Setup
		 */
		private void abort(){
			stop();
		}			
		
		/*
		 * Method to obtain an entire OFFeaturesReply message from the
		 * corresponding stream read.
		 */
		private OFFeaturesReply getFeaturesReply() throws IOException{
			List<OFMessage> msgs = new ArrayList<OFMessage>();
			while(true){
				try {
					Thread.sleep(0,1);
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, e.toString());
				}
	        	msgs.addAll(stream.read());
	        	for(int i = 0; i<msgs.size();i++){
	        		if(msgs.get(i).getType() == OFType.FEATURES_REPLY){
	        			return (OFFeaturesReply) msgs.get(i);
	        		}
	        	}
	        }
		}
		
		
		@Override
		public void run(){
			OVSwitch sw = null;
			
			/*
			 * Obtains OFMessages and writes them onto the stream of 
			 * OFMessageAsync.
			 */
			try {
				List<OFMessage> l = new ArrayList<OFMessage>();
				l.add(stream.getMessageFactory().getMessage(OFType.HELLO));
		        l.add(stream.getMessageFactory().getMessage(OFType.FEATURES_REQUEST));
		        stream.write(l);
		        
		        /*
		         * If the stream was used for transfer messages, then clean it
		         */
		        while(stream.needsFlush()){
		        	stream.flush();
		        }
		        
		        /*
		         * Start switch Thread/Stream. If it does not exist or never
		         * registered or associated before it creates a new object
		         * and initializes it. If it existed before it gets restarted.
		         */
		        OFFeaturesReply fr = getFeaturesReply();
		        sw = swhl.getSwitch(Long.toHexString(fr.getDatapathId()));
		        if(sw==null){
		        	sw = new OVSwitch(swName + "_Switch_" + sock.getRemoteAddress(),
		        			"0000000000000000".substring(Long.toHexString(fr.getDatapathId()).length()) 
		        			+ Long.toHexString(fr.getDatapathId()),stream,sock,fr,30,swhl.l2_learning);
		        }
		        else{
		        	sw.restart(sock,stream,fr);
		        }
		        
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
	        sw.start();
	        swhl.addSwitch(sw);
	        this.abort();
		}
		
		/*
		 * Method to stop/interrupt a Thread of SwitchSetup
		 */
		public void stop(){
			t.interrupt();
			LOGGER.info("Stopping " +  threadName);
		}
		
		/*
		 * Method to start a Thread of SwitchSetup. If the Thread does not exist
		 * it creates a new one.
		 */
		public void start(){
	      LOGGER.info("Starting " +  threadName);
	      if (t == null){
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }
	}	
}