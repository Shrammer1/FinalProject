package controller;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFError;
import org.openflow.protocol.OFError.OFErrorType;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFPhysicalPort.OFPortState;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.statistics.OFPortDescription;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;

import api.OVSwitchAPI;
import topology.LLDPMessage;
import topology.TopologyMapper;

//Runnable class
public class OVSwitch extends UnicastRemoteObject implements Runnable, OVSwitchAPI{
	
	private static final long serialVersionUID = -5333332671617251523L;


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
	

	
	//boolean variable to control Layer 2 behavior
	private boolean l2_learning = false;
	
	private TopologyMapper topo;
	private PacketHandler pkhl;
	private String threadName;
	private OFMessageAsyncStream stream;
	private BasicFactory factory = BasicFactory.getInstance();
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
	private ArrayList<OFPhysicalPort> ports = new ArrayList<OFPhysicalPort>();
	
	/**************************************************
	 * PUBLIC VARIABLES
	 **************************************************/
	public long lastHeard;
	
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public OVSwitch(String name, String switchID, OFMessageAsyncStream strm, 
			SocketChannel s, OFFeaturesReply fr, int swtime, TopologyMapper topo, boolean l2_learning) throws RemoteException
	{
		threadName = name;
		stream = strm;
		sock = s;
		this.switchID = switchID;
		this.featureReply = fr;
		this.switchTimeout = swtime;
		this.l2_learning = l2_learning;
		this.topo = topo;
	}
	
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	public boolean equals(OVSwitch sw){
		if(sw.switchID == this.switchID){
			return true;
		}
		return false;
	}
	
	
	public String listPorts(){
		String retval = "";
		for(OFPhysicalPort p : ports){
			String portState = OFPortState.valueOf(p.getState()).toString();
			retval = retval + "Port name: " + p.getName() + " - " + "ID:" + p.getPortNumber() + " - " + "State: " + portState + "\n";
		}
		return retval;
	}
	
	public int getSwitchTimeout() {
		return switchTimeout;
	}
	public OFFeaturesReply getFeatures(){
		return featureReply;
	}
	
	public void setSwitchTimeout(int switchTimeout) {
		this.switchTimeout = switchTimeout;
	}
	
	public void setSwitchID(String l){
		switchID = l;
	}
	
	public String getSwitchID() throws RemoteException{
		return switchID;
	}
	
	public String getSwitchFullName() throws RemoteException {
<<<<<<< Upstream, based on origin/master
		return nickname + "_" + switchID;
=======
		return switchID + "_" + nickname;
>>>>>>> 2853901 Added some API functionality to support giving switches nicknames.
	}
	
	public String getSwitchNickName() throws RemoteException{
		return nickname;
	}

	public void setSwitchNickName(String name) throws RemoteException {
		this.nickname = name;
	}
	
	
	public void sendMsg(byte[] msgs){
		List<OFMessage> l = factory.parseMessages(ByteBuffer.wrap(msgs));
		try {
			sthl.sendMsg(l);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	
	public Queue<byte[]> getMessages(String id){
		Queue<byte[]> ret = new ConcurrentLinkedQueue<byte[]>();
		while(registrations.get(id).msgsAvailable()){
			OFMessage msg = registrations.get(id).take();
			ByteBuffer toData = ByteBuffer.allocate(msg.getLengthU());
			registrations.get(id).take().writeTo(toData);
			ret.add(toData.array());
		}
		return ret;
	}
	

	public byte[] getMessage(String id){
		OFMessage msg = registrations.get(id).take();
		ByteBuffer toData =  ByteBuffer.allocate(msg.getLengthU());
		msg.writeTo(toData);
		byte[] ret = new byte[msg.getLengthU()];
		ret = toData.array();
		return ret;
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
			//registrations.putIfAbsent(id, new Registration(id,type));
			registrations.put(id, new Registration(id,type));
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
			//registrations.putIfAbsent(id, new Registration(id,types));
			registrations.put(id, new Registration(id,types));
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
	
	/**
	 * Sends a PacketOut to the switch containing an LLDP message to each of the switch ports in an attempt to alert directly connected switches of its presence 
	 */
	
	public synchronized void discover(){
		if(!(sthl.isAlive())){return;}
		for(OFPhysicalPort ofp : ports){
			LLDPMessage msg = new LLDPMessage(switchID, ofp.getPortNumber());
			OFActionOutput action = new OFActionOutput();
            action.setMaxLength((short) 0);
            action.setPort(ofp.getPortNumber());
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(action);
            OFPacketOut pkOut = new OFPacketOut(msg.getMessage(), actions , 0xffffffff);
			try {
				sthl.sendMsg(pkOut);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
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
		
		
		//Creating/Instantiating a new StreamHandler Object
		sthl = new StreamHandler(threadName + "_StreamHandler", stream);
		
		
		//As of OpenFlow 1.3 a default flow must be sent to the switches to direct non-matching traffic to the controller:
		//Clear all existing rules
		OFFlowMod fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
        fm.setCommand(OFFlowMod.OFPFC_DELETE);
        try {
			sthl.sendMsg(fm);
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.toString());
		}
        //Install default rule required by OF1.3
        fm.setCommand(OFFlowMod.OFPFC_ADD);
        fm.setPriority((short) 0);
        OFActionOutput action = new OFActionOutput().setPort(OFPort.OFPP_CONTROLLER); 
        fm.setInstructions(Collections.singletonList((OFInstruction)new OFInstructionApplyActions().setActions(Collections.singletonList((OFAction)action))));
        try {
			sthl.sendMsg(fm);
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.toString());
		}
        
        
        //Since OpenFlow 1.3 doesnt give a list of ports in the features reply (why would they do this...?) we have to query the switch for the ports
        OFStatisticsRequest omr = (OFStatisticsRequest) factory.getMessage(OFType.STATS_REQUEST);
        omr.setStatisticsType(OFStatisticsType.DESC);
        try {
			sthl.sendMsg(omr);
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.toString());
		}
        omr = (OFStatisticsRequest) factory.getMessage(OFType.STATS_REQUEST);
        omr.setStatisticsType(OFStatisticsType.PORT_DESC);
        try {
			sthl.sendMsg(omr);
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.toString());
		}
        
        
		
		
		/*
		 * Evaluating if Layer 2 functionality should be used and sending its
		 * corresponding arguments.
		 */
		if(l2_learning){
			
			//Creating/Instantiating a new PacketHandler Object
			pkhl = new PacketHandler(threadName + "_PacketHandler",sthl);
			
			//Starting a PacketHandler Thread
			pkhl.start();
		}
		//Starting a StreamHandler Thread
		sthl.start();
        try {
        	lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        	
        	//Sending/Buffering the list of OFMessages for processing
        	sthl.sendMsg(l);
        	
        	//Clearing the list of OFMessages
        	l.clear();
        	
        	boolean waitForReply = false;
        	
        	//Processing of messages available in stream
        	OFMessage msg = null;
            while(t.isInterrupted()==false){
            	
            	try{
            		msgIn.addAll(stream.read());
            		Thread.sleep(0, 1); //(ms,ns); ownership not lost
            	}
            	//Action taken upon NULL stream
            	catch(NullPointerException e){
            		abort();
            		return; //Return to previous try/catch section after abort
            	}
            	
            	if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) 
            			- lastHeard > 4 && waitForReply==false)
            	{
            		//Create an OFMessage of type ECHO_REQUEST and send it for processing
            		l.add(factory.getMessage(OFType.ECHO_REQUEST));
            		sthl.sendMsg(l);
            		//Clear the list of OFMessages after previous operation
				    l.clear();
				    //Update boolean flag for replies to inform of the change
				    waitForReply = true;
            	}
            	
            	//Processing of time out. Forcing to abort and start from scratch
            	if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) 
            			- lastHeard > 10)
            	{
            		abort();
            		return;
            	}
            	
            	//Process messages if they exist
    	        if(!(msgIn.size()==0)){
	    			msg = msgIn.remove(0);
	    			//Case of an ECHO_REQUEST
	    			if(msg.getType() == OFType.ECHO_REQUEST){
	    				
	    				//Update the timer/time stamp
	    				lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	    				
	    				//Create and send and ECHO_REPLY message for processing
    		    		l.add(factory.getMessage(OFType.ECHO_REPLY));
    		    		sthl.sendMsg(l);
    		    		//Clear the list of OFMessages after previous operation
    				    l.clear();
    				    //Update boolean flag for replies to inform of the change
    				    waitForReply = false;
    		    	}
	    			//Case of an ECHO_REPLY
	    			else if(msg.getType() == OFType.ECHO_REPLY){
	    				
	    				//Update the timer/time stamp
	    				lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	    				//Update boolean flag for replies to inform of the change
	    				waitForReply = false;
    		    	}
	    			//Any other case
	    			else {
	    				
	    				
	    				
	    				if(msg.getType() == OFType.PACKET_IN){
	    					
	    					//really long way to ask if the nested packet inside the packet in is an LLDP messages
	    					
	    					if(((new OFMatch()).loadFromPacket(((OFPacketIn) msg).getPacketData(), ((OFPacketIn) msg).getInPort())).getDataLayerType() == (short)0x88CC){
	    						topo.learn(new LLDPMessage(((OFPacketIn) msg).getPacketData()),this,((OFPacketIn) msg).getInPort());
	    					}
	    					else{
	    						topo.learn(new OFMatch().loadFromPacket((((OFPacketIn)msg).getPacketData()),((OFPacketIn)msg).getInPort()).getDataLayerSource(),((OFPacketIn)msg).getInPort(), this);
	    					}
	    					
	    				}
	    				else if(msg.getType() == OFType.STATS_REPLY){
	    					if(((OFStatisticsReply) msg).getStatisticsType() == OFStatisticsType.PORT_DESC){
	    						List<? extends OFStatistics> stats = ((OFStatisticsReply) msg).getStatistics();
	    						for(OFStatistics pStat: stats){
	    							ports.add(((OFPortDescription) pStat).getPort());
	    						}
	    					}
	    				}
	    				else if(msg.getType() == OFType.ERROR){
							OFError err = ((OFError) msg);
							System.out.println(err.getErrorCodeName(OFErrorType.values()[err.getErrorType()], (int) err.getErrorCode()));
	    				}
	    				
	    				
	    				//Evaluate if Layer 2 functionality is enabled and act upon it
	    				if(l2_learning){
	    					//Add the message to the packet handler and activate a Thread for processing
	    					pkhl.addPacket(msg);
	    					pkhl.wakeUp();
	    				}
	    				else{
	    					for(Map.Entry<String,Registration> r: registrations.entrySet()){
	    						r.getValue().addMsg(msg);
	    					}
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
	
	//Method for Stopping an OVSwitch Thread
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
		if(l2_learning) pkhl.stop();
	}
	
	//Method for Starting an OVSwitch Thread
	public void start (){
      LOGGER.info("Starting " +  threadName + "\t" + "Switch ID: " + switchID);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }
	
	//Method for hot operation abort an OVSwitch Thread
	private void abort(){
		stop();
		//If Layer 2 functionality is enabled, stop packet handling
		if(l2_learning){
			pkhl.stop();
			pkhl=null;
		}
		//Stop Stream Handler Thread
		sthl.stop();
		sthl=null;
		t=null;
		try {
			//Close the socket
			sock.close();
		} catch (IOException e) {

		}
	}
	
	//Method for hot restart
	public void restart(SocketChannel sock, OFMessageAsyncStream stream, 
			OFFeaturesReply fr)
	{
		//If there is a Thread alive, abort it
		try{if(t.isAlive()) abort();}
		
		//perfectly normal, just means that the thread is already stopped
		catch(NullPointerException e){}
		
		//If Layer 2 functionality is enabled, reinitialize macTable
					
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
