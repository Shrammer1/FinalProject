package controller;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;

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
public class SwitchHandler implements Runnable {
	
	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	

	/*
	 * LOGGER variable shared among all the instances of this class
	 * Built-in log levels available : SEVERE, WARNING, INFO, CONFIG, FINE, 
	 * FINER, FINEST
	 */	
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");	
	
	private String threadName;
	private Thread t;
	private Controller controller;
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public SwitchHandler(String name, Controller controller)
	{
		threadName = name;
		this.controller = controller;
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
	
	public boolean getL2_Learning(){
		return controller.getL2Learning();
	}


	public boolean setSwitchTimeout(String switchID, int newTimeout) throws RemoteException {
		for(OFSwitch ovs : controller.getSwitches()){
			if(ovs.getSwitchID().equals(switchID)){
				ovs.setSwitchTimeout(newTimeout);
				return true;
			}
		}
		return false;
	}	
	/*
	 * Method to obtain the list of all switches but allowing those waiting
	 * on the object's monitor to continue using it. 
	 */
	public synchronized ArrayList<String> listSwitches(){
		ArrayList<String> res = new ArrayList<String>();
		synchronized (controller.getSwitches()) {
			for(int i = 0; i<controller.getSwitches().size();i++){
				res.add(controller.getSwitches().get(i).getSwitchID());
			}
			//Allowing those waiting on the object's monitor to continue using it
			controller.getSwitches().notifyAll();
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
			return controller.getReg().list();
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
		synchronized (controller.getSwitches()) {
			try {
				new SwitchSetup(threadName + "_SetupSwitch_" + sock.getRemoteAddress(),sock,stream,this);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
	}
		
	
	/*
	 * Method for adding a Thread for an OFSwitch and allow those waiting on 
	 * the object's monitor to continue/start using it. If RMI used, then it
	 * binds the corresponding objects to the registry.
	 */
	public synchronized void addSwitch(OFSwitch sw){
		ArrayList<OFSwitch> switches = controller.getSwitches();
		synchronized (switches) {
			switches.add(sw);
			for(OFSwitch s: switches){
				s.discover();
			}
			switches.notifyAll();
		}
	}
	
	
	/*
	 * Method to get the corresponding switch (argument sent in) from the
	 * list of switches. Also allows those waiting on the object's monitor to 
	 * continue using it.
	 */
	public synchronized OFSwitch getSwitch(String switchID){
		OFSwitch sw = null;
		ArrayList<OFSwitch> switches= controller.getSwitches();
		synchronized (switches) {
			for(int i = 0; i<switches.size();i++){
				if((sw = switches.get(i)).getSwitchID().equals(switchID)) {
					switches.notifyAll();
					return sw;
				}
			}
			switches.notifyAll();
		}
		return null;
	}
	
	
	@Override
	public void run(){
		while(!(t.isInterrupted())){
			//TODO: USE TIMER TO CLEAN UP SWITCHES!!!!!!
			try {
				Thread.sleep(1000);
				Iterator<OFSwitch> i = controller.getSwitches().iterator();
				while(i.hasNext()){
					OFSwitch sw = (OFSwitch) i.next();
					if(!(sw.isAlive())){
						if(sw.hasTimmedOut()){
							i.remove();
							LOGGER.log(Level.INFO, "Switch: " + sw.getSwitchFullName() + " has timmed out");
						}
					}
				}
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
        this.abort();
	}
	

	//Method to stop a Thread of a SwitchHandler
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
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}

	public Controller getController() {
		return controller;
	}
}