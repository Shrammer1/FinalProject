import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import org.openflow.util.U16;


public class PacketHandler implements Runnable{
	private Map<Integer, Short> macTable;
	private String threadName;
	private ConcurrentLinkedQueue<OFMessage> q = new ConcurrentLinkedQueue<OFMessage>();
	private Thread t;
	private BasicFactory factory = new BasicFactory();
	private List<OFMessage> l = new ArrayList<OFMessage>();
	private StreamHandler sthl;
	
	
	public PacketHandler(String name, Map<Integer, Short> macTable, StreamHandler sthl){
		threadName = name;
		this.macTable=macTable;
		this.sthl = sthl;
	}
	
	public void addPacket(OFMessage pk){
		q.add(pk);
	}
	
	public void wakeUp(){
		synchronized (t) {
			t.notify();
		}
	}
	
	@Override
	public void run(){
	    OFMessage msg = null;
	    while(t.isInterrupted()==false){
	    	
	    	msg = q.poll();

	    	if(msg==null){
	    		try {
					synchronized (t) {
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
		    l.clear();
	    }
	}
	
	
	
	
	
	private void handle_PACKETIN(OFMessage msg){
		OFPacketIn pi;
		OFMatch match = new OFMatch();
    	OFMatch pktIn = new OFMatch();
		pi = (OFPacketIn) msg;
		pktIn.loadFromPacket(pi.getPacketData(), pi.getInPort()); //kinda cheating here, were using the OFMatch object to be an easy to access data structure for a packet
		
        byte[] dlDst = pktIn.getDataLayerDestination();
        Integer dlDstKey = Arrays.hashCode(dlDst);
        byte[] dlSrc = pktIn.getDataLayerSource();
        Integer dlSrcKey = Arrays.hashCode(dlSrc);
        int bufferId = pi.getBufferId();

        
        /*
        
        STEPS:
        
        1. Read the packet it, if its not a multicast learn the source MAC (dlSrcKey) and associate the source port with it
        2. Lookup destination MAC (dlDstKey), if we find it in the MAC table set the output port, else output port is null
        3. If we know the output port, create a FlowMod
        4. If we don't know the output port, flood the packet.
        
        
        */
        
        
        // if the src is not multicast, learn it
        if ((dlSrc[0] & 0x1) == 0) {
            if (!macTable.containsKey(dlSrcKey) || !macTable.get(dlSrcKey).equals(pi.getInPort())) {
                macTable.put(dlSrcKey, pi.getInPort());
            }
        }

        Short outPort = null;
        // if the destination is not multicast, look it up
        if ((dlDst[0] & 0x1) == 0) {
            outPort = macTable.get(dlDstKey);
        }

        // push a flow mod if we know where the packet should be going
        if (outPort != null) {
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
            fm.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
            sthl.sendMsg(fm);
        }

        // Send a packet out
        if (outPort == null || pi.getBufferId() == 0xffffffff) {
            OFPacketOut po = new OFPacketOut();
            po.setBufferId(bufferId);
            po.setInPort(pi.getInPort());

            // set actions
            OFActionOutput action = new OFActionOutput();
            action.setMaxLength((short) 0);
            action.setPort((short) ((outPort == null) ? OFPort.OFPP_FLOOD.getValue() : outPort)); //if we don't know the outport flood
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(action);
            po.setActions(actions);
            po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

            // set data if needed
            if (bufferId == 0xffffffff) {
                byte[] packetData = pi.getPacketData();
                po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH + po.getActionsLength() + packetData.length));
                po.setPacketData(packetData);
            } else {
                po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH+ po.getActionsLength()));
            }
            sthl.sendMsg(po);
        }
	}
	
	
	
	
	public void stop(){
		t.interrupt();
		System.out.println("Stopping " +  threadName);
	}
	
	public void start (){
      System.out.println("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}

}
