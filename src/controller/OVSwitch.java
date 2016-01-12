package controller;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.util.LRULinkedHashMap;

/*
 * Runnable class
 */
public class OVSwitch implements Runnable, OVSwitchAPI{
	
	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	/*final=its value cannot be changed once initialized
	 * static=variable shared among all the instances of this class as it
	 * belongs to the type and not to the actual objects themselves.
	 * LOGGER variable shared among all the instances of this class
	 * Built-in log levels available : SEVERE, WARNING, INFO, CONFIG, FINE, 
	 * FINER, FINEST
	 */
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	
	/*macTable variable only used if l2_learning (layer 2 learning) is enabled. 
	 * See other references on how to enable this feature/behavior. 
	 * If feature enabled, the Switch stays in Fail_Standalone mode where there 
	 * is no management from the controller, otherwise (feature disabled) the 
	 * Switch stays in Fail_Secure mode where it drops all until connected/managed
	 * by a controller.
	 */
	private Map<Integer, Short> macTable;
	
	/*
	 * boolean variable to control Layer 2 behavior
	 */
	private boolean l2_learning = false;
	
	
	private PacketHandler pkhl;
	private String threadName;
	private OFMessageAsyncStream stream;
	private BasicFactory factory = new BasicFactory();
	private List<OFMessage> l = new ArrayList<OFMessage>();
	private List<OFMessage> msgIn = new ArrayList<OFMessage>();
	private StreamHandler sthl;
	private SocketChannel sock;
	private Thread t;
	private OFFeaturesReply featureReply;
	private String switchID;
	private String nickname = "";
	private Map<String,Registration> registrations = new HashMap<String,Registration>();
	private int switchTimeout;
	
	/**************************************************
	 * PUBLIC VARIABLES
	 **************************************************/
	public long lastHeard;
	
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public OVSwitch(String name, String switchID, OFMessageAsyncStream strm, 
			SocketChannel s, OFFeaturesReply fr, int swtime, boolean l2_learning) 
	{
		threadName = name;
		stream = strm;
		sock = s;
		this.switchID = switchID;
		this.featureReply = fr;
		this.switchTimeout = swtime;
		this.l2_learning = l2_learning;
		if(l2_learning) macTable = new LRULinkedHashMap<Integer, Short>(64001, 64000);
	}
	
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	
	public int getSwitchTimeout() {
		return switchTimeout;
	}
	
	public String getSwitchName(){
		return nickname + "_" + switchID;
	}
	
	public void setSwitchTimeout(int switchTimeout) {
		this.switchTimeout = switchTimeout;
	}

	public OFFeaturesReply getFeatures(){
		return featureReply;
	}
	
	public String getSwitchID(){
		return switchID;
	}
	
	public String getSwitchNickName(){
		return nickname;
	}
	
	public void setSwitchNickName(String s){
		nickname = s;
	}
	
	public void setSwitchID(String l){
		switchID = l;
	}
	
	
	/*
	 * Method to add an OFType into registrations HashMap (of Registration)
	 * based on given id (if id exists already). True is returned if OFType
	 * did not exist previously; False otherwise.
	 * If id does not exist, is then created and added to the registrations
	 * Registration HashMap and true is returned.
	 */
	public boolean register(String id, OFType type){
		if(registrations.containsKey(id)){
			return registrations.get(id).register(type);
		}
		else{
			registrations.putIfAbsent(id, new Registration(id,type));
			return true;
		}
	}
	
	/*
	 * Method to add several OFTypes into registrations HashMap (of Registration)
	 * based on given id (if id exists already). True is returned if OFTypes
	 * did not exist previously; False otherwise.
	 * If id does not exist, is then created and added to the registrations
	 * Registration HashMap and true is returned.
	 */
	public boolean register(String id, ArrayList<OFType> types){
		if(registrations.containsKey(id)){
			return registrations.get(id).register(types);
		}
		else{
			registrations.putIfAbsent(id, new Registration(id,types));
			return true;
		}
	}
	
	/*
	 * Method to remove an OFType from registrations HashMap (of Registration)
	 * based on given id (if id exists already). True is returned if OFType
	 * exists; False otherwise.
	 */
	public boolean unregister(String id, OFType type){
		if(registrations.containsKey(id)){
			return registrations.get(id).unregister(type);
		}
		else{
			return false;
		}
	}
	
	/*
	 * Method to remove several OFType from registrations HashMap (of Registration)
	 * based on given id (if id exists already). True is returned if OFTypes
	 * exists; False otherwise.
	 */
	public boolean unregister(String id, ArrayList<OFType> types){
		if(registrations.containsKey(id)){
			return registrations.get(id).unregister(types);
		}
		else{
			return false;
		}
	}
	
	/*
	 * Method to identify if the corresponding thread is alive, in other words,
	 * if the thread has been started but has not died yet.
	 */
	public boolean isAlive(){
		return t.isAlive();
	}
			
	
	@Override
	public void run(){
		sthl = new StreamHandler(threadName + "_StreamHandler", stream);
		
		if(l2_learning){
			pkhl = new PacketHandler(threadName + "_PacketHandler",macTable,sthl); 
			pkhl.start();
		}
		sthl.start();
		
        try {
        	lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        	
        	sthl.sendMsg(l);
        	l.clear();
        	
        	boolean waitForReply = false;
        	
        	OFMessage msg = null;
            while(t.isInterrupted()==false){
            	
            	try{
            		msgIn.addAll(stream.read());
            		Thread.sleep(0, 1);
            	}catch(NullPointerException e){
            		abort();
            		return;
            	}
            	
            	if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastHeard > 4 && waitForReply==false){
            		l.add(factory.getMessage(OFType.ECHO_REQUEST));
            		sthl.sendMsg(l);
				    l.clear();
				    waitForReply = true;
            	}
            	
            	if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastHeard > 10){ //switch timed out. delete the connection and make it start from scratch
            		abort();
            		return;
            	}
            	
    	        if(!(msgIn.size()==0)){
	    			msg = msgIn.remove(0);
	    			if(msg.getType() == OFType.ECHO_REQUEST){
	    				lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    		    		l.add(factory.getMessage(OFType.ECHO_REPLY));
    		    		sthl.sendMsg(l);
    				    l.clear();
    				    waitForReply = false;
    		    	}
	    			else if(msg.getType() == OFType.ECHO_REPLY){
	    				lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	    				waitForReply = false;
    		    	}
	    			else {
	    				if(l2_learning){
	    					pkhl.addPacket(msg);
	    					pkhl.wakeUp();
	    				}
	    				else{
	    					
	    				}
	    				
	    			}
    	        }
            }
        	
        	
		} catch (Exception e) {
			abort();
			LOGGER.log(Level.SEVERE, e.toString());
			return;
		}
        this.abort();
	}
	
	
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
		if(l2_learning) pkhl.stop();
	}
	
	public void start (){
      LOGGER.info("Starting " +  threadName + "\t" + "Switch ID: " + switchID);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }
	
	private void abort(){
		stop();
		if(l2_learning){
			pkhl.stop();
			pkhl=null;
		}
		sthl.stop();
		sthl=null;
		t=null;
		try {
			sock.close();
		} catch (IOException e) {

		}
	}
	
	public void restart(SocketChannel sock, OFMessageAsyncStream stream, OFFeaturesReply fr){
		try{
			if(t.isAlive()) abort();
		}catch(NullPointerException e){
			//perfectly normal, just means that the thread is already stopped
		}
		if(l2_learning) macTable = new LRULinkedHashMap<Integer, Short>(64001, 64000);
		this.featureReply = fr;
		this.stream = stream;
		this.sock = sock;
		LOGGER.info("RE-Starting " +  threadName + "\t" + "Switch ID: " + switchID);
	      if (t == null){
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	}

}
