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
import org.openflow.protocol.OFFlowRemoved;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFPortMod;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFStatisticsReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFPhysicalPort.OFPortState;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoTable;
import org.openflow.protocol.statistics.OFPortDescription;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import topology.LLDPMessage;
import topology.TopologyMapper;

//Runnable class
public class OFSwitch implements Runnable{
	
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
	private Controller controller;
	private TopologyMapper topo;
	
	/**************************************************
	 * PUBLIC VARIABLES
	 **************************************************/
	public long lastHeard;
	
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public OFSwitch(String name, String switchID, OFMessageAsyncStream strm,SocketChannel sock , OFFeaturesReply fr, int swtime, SwitchHandler swhl)
	{
		this.sock = sock;
		this.controller = swhl.getController();
		this.topo = controller.getTopologyMapper();
		threadName = name;
		stream = strm;
		this.switchID = switchID;
		this.featureReply = fr;
		this.switchTimeout = swtime;
	}
	
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	
	public boolean hasTimmedOut(){
		long now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		if(now - lastHeard > switchTimeout){
			return true;
		}
		return false;
	}
	
	public boolean equals(OFSwitch sw){
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
	
	public String getSwitchID() {
		return switchID;
	}
	
	public String getSwitchFullName()  {
		return nickname + "_" + switchID;
	}
	
	public String getSwitchNickName() {
		return nickname;
	}

	public void setSwitchNickName(String name)  {
		this.nickname = name;
	}
	
	public void sendMsg(OFMessage msg){
		try {
			sthl.sendMsg(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		if(sthl == null){return;}
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
		if(t == null) return false;
		return t.isAlive();
	}
			
	
	@Override
	public void run(){
		
		boolean flag = false;
		
		//Creating/Instantiating a new StreamHandler Object
		sthl = new StreamHandler(threadName + "_StreamHandler", stream);
		sthl.start();
		
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
        fm.setTableId((byte)1);
        OFActionOutput action = new OFActionOutput().setPort(OFPort.OFPP_CONTROLLER); 
        fm.setInstructions(Collections.singletonList((OFInstruction)new OFInstructionApplyActions().setActions(Collections.singletonList((OFAction)action))));
        try {
			sthl.sendMsg(fm);
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.toString());
		}
        
        fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
        fm.setCommand(OFFlowMod.OFPFC_ADD);
        fm.setPriority((short) 0);
        fm.setTableId((byte)0);
        fm.setInstructions(Collections.singletonList((OFInstruction)new OFInstructionGotoTable((byte) 1)));
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
		if(controller.getL2Learning()){
			
			//Creating/Instantiating a new PacketHandler Object
			pkhl = new PacketHandler(threadName + "_PacketHandler",this);
			
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
            		stop();
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
            		stop();
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
	    						flag = true;
	    						topo.learn(new LLDPMessage(((OFPacketIn) msg).getPacketData()),this,((OFPacketIn) msg).getInPort());
	    					}
	    					else{
	    						//learn a host
	    						boolean newHost = topo.learn(new OFMatch().loadFromPacket((((OFPacketIn)msg).getPacketData()),((OFPacketIn)msg).getInPort()).getDataLayerSource(), new OFMatch().loadFromPacket((((OFPacketIn)msg).getPacketData()),((OFPacketIn)msg).getInPort()).getNetworkSource(),((OFPacketIn)msg).getInPort(), this);
	    						if(newHost){
	    							int i =1;
	    						}
	    					}
	    					
	    				}
	    				else if(msg.getType() == OFType.FLOW_REMOVED){
	    					OFFlowRemoved flowRem = (OFFlowRemoved) msg;
	    					if(flowRem.getCookie() == 0){
	    						//Its one of our l2 learning flows that got removed
	    						OFMatch match = flowRem.getMatch();
	    						topo.ageIP(match.getNetworkSource());
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
	    				else if(msg.getType() == OFType.PORT_STATUS){
	    					topo.updateLinks(((OFPortStatus) msg).getDesc().getPortNumber(), this);	    					
	    				}
	    				else if(msg.getType() == OFType.ERROR){
							OFError err = ((OFError) msg);
							//System.out.println(err.getErrorCodeName(OFErrorType.values()[err.getErrorType()], (int) err.getErrorCode()));
	    				}
	    				
	    				if(flag == false){ //flag is used to skip l2_learning when the packetin is an LLDP message
		    				//Evaluate if Layer 2 functionality is enabled and act upon it
		    				if(controller.getL2Learning()){
		    					//Add the message to the packet handler and activate a Thread for processing
		    					pkhl.addPacket(msg);
		    					pkhl.wakeUp();
		    				}
		    				//TODO: come up with a way to let apps register and hear about PacketINs containing LLDP messages (should we even let this happen?)
		    				for(Map.Entry<String,Registration> r: registrations.entrySet()){
	    						r.getValue().addMsg(msg);
	    					}
	    				}
	    				else{
	    					flag = false;
	    				}
	    			}
    	        }
            }
        	
        	
		} catch (Exception e) {
			stop();
			LOGGER.log(Level.SEVERE, e.toString());
			return;
		}
        
        this.stop();
	}
	
	//Method for Starting an OFSwitch Thread
	public void start (){
      LOGGER.info("Starting " +  threadName + "\t" + "Switch ID: " + switchID);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }
	
	//Method for Stopping an OFSwitch Thread
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
		if(controller.getL2Learning()){
			pkhl.stop();
		}
		//If Layer 2 functionality is enabled, stop packet handling
		if(controller.getL2Learning()){
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
	
	public Controller getController(){
		return this.controller;
	}
	
	
	//Method for hot restart
	public void restart(SocketChannel sock, OFMessageAsyncStream stream, 
			OFFeaturesReply fr)
	{
		//If there is a Thread alive, stop it
		try{if(t.isAlive()) stop();}
		
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((featureReply == null) ? 0 : featureReply.hashCode());
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result + ((ports == null) ? 0 : ports.hashCode());
		result = prime * result + ((registrations == null) ? 0 : registrations.hashCode());
		result = prime * result + ((switchID == null) ? 0 : switchID.hashCode());
		result = prime * result + switchTimeout;
		result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OFSwitch other = (OFSwitch) obj;
		if (featureReply == null) {
			if (other.featureReply != null)
				return false;
		} else if (!featureReply.equals(other.featureReply))
			return false;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		if (nickname == null) {
			if (other.nickname != null)
				return false;
		} else if (!nickname.equals(other.nickname))
			return false;
		if (ports == null) {
			if (other.ports != null)
				return false;
		} else if (!ports.equals(other.ports))
			return false;
		if (registrations == null) {
			if (other.registrations != null)
				return false;
		} else if (!registrations.equals(other.registrations))
			return false;
		if (switchID == null) {
			if (other.switchID != null)
				return false;
		} else if (!switchID.equals(other.switchID))
			return false;
		if (switchTimeout != other.switchTimeout)
			return false;
		if (threadName == null) {
			if (other.threadName != null)
				return false;
		} else if (!threadName.equals(other.threadName))
			return false;
		return true;
	}
	
}
