
package applications;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.openflow.util.LRULinkedHashMap;
import org.openflow.util.U16;

import api.OVSwitchAPI;
import api.SwitchHandlerAPI;


public class TestApplication {
	
	public static void main(String[] args) {
			
		String serverURL = "rmi://127.0.0.1:1099/"; //this is the registry entry we want to use
		String controllerExt = "controller";
		//serverIntf is the interface we use to invoke methods on the server. Here we are retrieving the server object by looking it up in the registry
		try {
			SwitchHandlerAPI controllerIntf =(SwitchHandlerAPI)Naming.lookup(serverURL + controllerExt);
			ArrayList<String> swLst = controllerIntf.listSwitches();
			ArrayList<RemoteSwitch> switches = new ArrayList<RemoteSwitch>();
			
			
			for(String sw: swLst){
				OVSwitchAPI swAPI = (OVSwitchAPI) Naming.lookup(serverURL + sw);
				switches.add(new RemoteSwitch(swAPI,new PacketHandler(sw, sw + "sw_pkhl", swAPI)));
				
				//gB Testing
				System.out.println("SW: " + swAPI.getSwitchName() + " t_out: " + swAPI.getSwitchTimeout());
			}
			
			
			for(RemoteSwitch sw: switches){
				System.out.println(sw.swAPI.listPorts());
				sw.swAPI.register(sw.pkhl.getID(), OFType.PACKET_IN);
				sw.pkhl.start();
			}
			
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	private static class RemoteSwitch{
		public OVSwitchAPI swAPI;
		public PacketHandler pkhl;
		
		public RemoteSwitch(OVSwitchAPI swAPI, PacketHandler pkhl){
			this.swAPI = swAPI;
			this.pkhl = pkhl;
		}
		
	}
	
	/**
	 * 
	 * @author Nicholas Landriault
	 *
	 * PacketHandler Class (Runnable)
	 *
	 */
	private static class PacketHandler implements Runnable{
		
		/**************************************************
		 * PRIVATE VARIABLES
		 **************************************************/
		
		
		
		/*macTable variable only used if l2_learning (layer 2 learning) is enabled. 
		 * See other references on how to enable this feature/behavior. 
		 * If feature enabled, the Switch stays in Fail_Standalone mode where there 
		 * is no management from the controller, otherwise (feature disabled) the 
		 * Switch stays in Fail_Secure mode where it drops all until connected/managed
		 * by a controller.
		 */
		private Map<Integer, Short> macTable;
		
		
		private String threadName;
		private Thread t;
		private OVSwitchAPI swAPI;
		
		/*
		 * A basic OpenFlow factory that supports naive creation of both Messages 
		 * and Actions/Instructions.
		 */
		private BasicFactory factory = new BasicFactory();
		private List<OFMessage> l = new ArrayList<OFMessage>();
		private String id;

		public String getID(){
			return id;
		}
		
		/**************************************************
		 * CONSTRUCTORS
		 **************************************************/
		public PacketHandler(String threadname, String id, OVSwitchAPI swAPI)
		{
			this.threadName = threadname;
			this.id = id;
			this.swAPI = swAPI;
			this.macTable = new LRULinkedHashMap<Integer, Short>(64001, 64000);
		}
		
		/**************************************************
		 * PRIVATE METHODS
		 * @throws RemoteException 
		 **************************************************/
		/*
		 * 
		 */
		private void handle_PACKETIN(OFMessage msg) throws RemoteException{
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
	        Short outPort = null;
	        // if the destination is not multicast, look it up
	        if ((dlDst[0] & 0x1) == 0) {
	            outPort = macTable.get(dlDstKey);
	        }

	        //STEP_3_A: If the output port is known, create and push a FlowMod
	        if (outPort != null) {
	            /*
	             * Retrieves an OFMessage instance corresponding to the specified 
	             * OFType.
	             */
	        	OFFlowMod fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
	            fm.setBufferId(bufferId);
	            fm.setCommand((short) 0);
	            fm.setCookie(0);
	            fm.setFlags((short) 0);
	            fm.setHardTimeout((short) 0);
	            fm.setIdleTimeout((short) 5);
	            
	            match.setInputPort(pi.getInPort());
	            match.setWildcards(OFMatch.OFPFW_DL_TYPE + OFMatch.OFPFW_DL_VLAN_PCP + OFMatch.OFPFW_DL_VLAN);
	            match.setDataLayerDestination(pktIn.getDataLayerDestination());
	            match.setDataLayerSource(pktIn.getDataLayerSource());
	                  	                
	            fm.setMatch(match);
	            fm.setOutPort((short) OFPort.OFPP_NONE.getValue());
	            fm.setPriority((short) 0);
	            OFActionOutput action = new OFActionOutput();
	            action.setMaxLength((short) 0);
	            action.setPort(outPort);
	            
	            List<OFAction> actions = new ArrayList<OFAction>();
	            actions.add(action);
	            fm.setActions(actions);
	            //Setting header
	            fm.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
	            ByteBuffer toData =  ByteBuffer.allocate(fm.getLengthU());
	            fm.writeTo(toData);
				byte[] msgData = new byte[fm.getLengthU()];
				msgData = toData.array();
				swAPI.sendMsg(msgData);
	        }

	        //Sending packet out
	        if (outPort == null || pi.getBufferId() == 0xffffffff) {
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
	            ByteBuffer toData =  ByteBuffer.allocate(po.getLengthU());
	            po.writeTo(toData);
				byte[] msgData = new byte[po.getLengthU()];
				msgData = toData.array();
				swAPI.sendMsg(msgData);
	            
	        }
		}
		
		
		/**************************************************
		 * PUBLIC METHODS
		 * @throws RemoteException 
		 **************************************************/
		
		//Sends a flow mod that drops all packets
		public void dropAll() throws RemoteException{
		
			OFMatch match = new OFMatch();
			//Retrieves an OFMessage instance corresponding to the specified OFType
			OFFlowMod fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
	        fm.setCommand((short) 0);
	        fm.setCookie(0);
	        fm.setHardTimeout((short) 0);
	        
	        //Matching all coming in from that port
	        match.setInputPort(OFPort.OFPP_NONE.getValue());
	        	                
	        fm.setMatch(match);
	        fm.setOutPort((short) OFPort.OFPP_NONE.getValue());
	        fm.setPriority((short) 0);
	        OFActionOutput action = new OFActionOutput();
	        action.setMaxLength((short) 0);
	        List<OFAction> actions = new ArrayList<OFAction>();
	        actions.add(action);
	        fm.setActions(actions);
	        fm.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
	        
	        
			ByteBuffer toData =  ByteBuffer.allocate(fm.getLengthU());
			fm.writeTo(toData);
			byte[] msgData = new byte[fm.getLengthU()];
			msgData = toData.array();
			swAPI.sendMsg(msgData);
		}
		

		@Override
		public void run(){
			
		    BasicFactory factory = new BasicFactory();
		    while(t.isInterrupted()==false){
		    	ArrayList<OFMessage> lst = null;
		    	
		    	try {
		    		lst = (ArrayList<OFMessage>) factory.parseMessages(ByteBuffer.wrap(swAPI.getMessage(this.id)));
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
		    	
		    	for(OFMessage msg : lst){
		    		if(msg.getType() == OFType.PACKET_IN){
				    	try {
							handle_PACKETIN(msg);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
		    		}
		    	}
			    //Clearing the list of OFMessages
		    	l.clear();
		    }
		}	
		
		//Method to interrupt a PacketHandler Thread
		public void stop(){
			t.interrupt();
			System.out.println("Stopping " +  threadName);
		}
		
		//Method to allocate/instantiate a new PacketHandler Thread
		public void start (){
	      System.out.println("Starting " +  threadName);
	      if (t == null){
	         t = new Thread (this, threadName);
	         t.start ();
	      }
		}
	}
}