package controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.util.U16;

/**
 * 
 * @author Nicholas Landriault
 *
 * PacketHandler Class (Runnable)
 *
 */
public class PacketHandler implements Runnable{
	
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
	private Map<Integer, Integer> macTable;
	
	
	private String threadName;
	private ConcurrentLinkedQueue<OFMessage> q = new ConcurrentLinkedQueue<OFMessage>();
	private Thread t;
	
	private BasicFactory factory = BasicFactory.getInstance();
	private List<OFMessage> l = new ArrayList<OFMessage>();
	private StreamHandler sthl;
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public PacketHandler(String name, StreamHandler sthl)
	{
		threadName = name;
		this.macTable=new LinkedHashMap<Integer, Integer>(64001, 64000);
		this.sthl = sthl;
	}
	
	/**************************************************
	 * PRIVATE METHODS
	 **************************************************/
	
	private void handle_PACKETIN(OFMessage msg){
		OFPacketIn pi;
		OFMatch match = new OFMatch();
    	OFMatch pktIn = new OFMatch();
		pi = (OFPacketIn) msg;
		
		/*
		 * Initializes this OFMatch structure with the corresponding data from 
		 * the specified packet. Must specify the input port, to ensure that 
		 * this.in_port is set correctly. Specify OFPort.NONE or OFPort.ANY 
		 * if input port not applicable or available.
		 */
		pktIn.loadFromPacket(pi.getPacketData(), pi.getInPort()); //kinda cheating here, were using the OFMatch object to be an easy to access data structure for a packet
		
        byte[] dlDst = pktIn.getDataLayerDestination(); //returns an array of bytes
        Integer dlDstKey = Arrays.hashCode(dlDst);
        byte[] dlSrc = pktIn.getDataLayerSource();
        Integer dlSrcKey = Arrays.hashCode(dlSrc);
        int bufferId = pi.getBufferId();

        /*
        STEPS:
        1. Read the packet in, if source is not a multicast learn the 
           MAC (dlSrcKey) and associate the source port with it.
        2. Lookup destination MAC (dlDstKey), if found in macTable set the 
           output port, else output port is NULL.
        3_A. If the output port is known, create a FlowMod
        3_B. If the output port is unknown, flood the packet.
        */
        
        /*
         * STEP_1: Read the packet in, if source is not a multicast learn the 
           MAC (dlSrcKey) and associate the source port with it.
         */
        if ((dlSrc[0] & 0x1) == 0) {
            if (!macTable.containsKey(dlSrcKey) || !macTable.get(dlSrcKey).equals(pi.getInPort())) {
                macTable.put(dlSrcKey, pi.getInPort());
            }
        }
        
        /*
         *STEP_2: Lookup destination MAC (dlDstKey), if found in macTable set 
         *the output port, else output port is NULL. 
         */
        Integer outPort = null;
        // if the destination is not multicast, look it up
        if ((dlDst[0] & 0x1) == 0) {
            outPort = macTable.get(dlDstKey);
        }

        //STEP_3_A: If the output port is known, create and push a FlowMod
        //if (outPort != null && pktIn.getDataLayerType() == (short) 0x0800) {
        if (outPort != null) {
            //Retrieves an OFMessage instance corresponding to the specified OFType
        	OFFlowMod fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
            fm.setBufferId(bufferId);
            fm.setCommand(OFFlowMod.OFPFC_ADD);
            fm.setCookie(0);
            fm.setFlags((short) 0);
            fm.setIdleTimeout((short) 5);
            fm.setFlags((short) 0x0001);
            
            match.setInPort(pi.getInPort());
            match.setDataLayerDestination(pktIn.getDataLayerDestination());
            if(pktIn.getDataLayerType() == (short) 0x0800){
            	match.setDataLayerType((short) 0x0800);
                match.setNetworkSource(pktIn.getNetworkSource());
            }
            else{
            	match.setDataLayerType(pktIn.getDataLayerType());
            }
            match.setDataLayerSource(pktIn.getDataLayerSource());
            fm.setMatch(match);
            fm.setTableId((byte) 1);
            fm.setPriority((short) 1);
            OFActionOutput action = new OFActionOutput();
            action.setMaxLength((short) 0);
            action.setPort(outPort);
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(action);
            
            //OpenFlow 1.3 no longer sends instructions directly in the flow mod. Now instructions are used to specify what to do with a packet that matches a flow. One of the options is to follow a list of actions, which is what we're doing here.
            //Fow more see OpenFlow spec 1.3 page 47
            List<OFInstruction> instructions = new ArrayList<OFInstruction>();
            OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
            instructions.add(instruction);
            
            fm.setInstructions(instructions);

            try {
				sthl.sendMsg(fm);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
        }

        //Sending packet out
        //if (outPort == null || pi.getBufferId() == 0xffffffff || pktIn.getDataLayerType() != (short) 0x0800){
        if (outPort == null || pi.getBufferId() == 0xffffffff){
            OFPacketOut po = new OFPacketOut();
            po.setBufferId(bufferId);
            po.setInPort(pi.getInPort());

            //Setting actions
            OFActionOutput action = new OFActionOutput();
            action.setMaxLength((short) 0);
            //STEP_3_B: If the output port is unknown, flood the packet.
            action.setPort((short) ((outPort == null) ? OFPort.OFPP_FLOOD.getValue() : outPort));
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(action);
            po.setActions(actions);
            po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

            //Setting data if needed
            if (bufferId == 0xffffffff) {
                byte[] packetData = pi.getPacketData();
                //Setting header
                po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH + po.getActionsLength() + packetData.length));
                po.setPacketData(packetData);
            } else {
                po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH+ po.getActionsLength()));
            }
            try {
				sthl.sendMsg(po);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
        }
	}
	
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	//Adding a packet to the queue
	public void addPacket(OFMessage pk){
		q.add(pk);
	}
	
	//Waking up a single thread that is waiting on this object's monitor.
	public void wakeUp(){
		synchronized (t) {
			t.notify();
		}
	}
	
	//Sends a flow mod that drops all packets
	public void dropAll(){
	
		OFMatch match = new OFMatch();
		//Retrieves an OFMessage instance corresponding to the specified OFType
		OFFlowMod fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
        fm.setCommand((byte) 0);
        fm.setCookie(0);
        fm.setHardTimeout((short) 0);
        
        //Matching all coming in from that port
        match.setInPort(OFPort.OFPP_ANY.getValue());
        	                
        fm.setMatch(match);
        fm.setOutPort((short) OFPort.OFPP_ANY.getValue());
        fm.setPriority((short) 0);
        OFActionOutput action = new OFActionOutput();
        action.setMaxLength((short) 0);
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(action);
        
        List<OFInstruction> instructions = new ArrayList<OFInstruction>();
        OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
        instructions.add(instruction);
        
        fm.setInstructions(instructions);
        fm.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
        try {
			sthl.sendMsg(fm);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	

	@Override
	public void run(){
		
	    OFMessage msg = null;
	    while(t.isInterrupted()==false){
	    	
	    	//Retrieves and removes the head of this queue, or returns null if this queue is empty
	    	msg = q.poll();
	    	if(msg==null){
	    		try {
					synchronized (t) {
						//Maximum time to wait in milliseconds
						t.wait(10000);
					}
				} catch (InterruptedException e) {
					return;
				}
	    	}
	    	else{
	    		if(msg.getType() == OFType.PACKET_IN){
			    	handle_PACKETIN(msg);
	    		}    		
    		}
		    //Clearing the list of OFMessages
	    	l.clear();
	    }
	}	
	
	//Method to interrupt a PacketHandler Thread
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
	}
	
	//Method to allocate/instantiate a new PacketHandler Thread
	public void start (){
      LOGGER.info("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}
}
