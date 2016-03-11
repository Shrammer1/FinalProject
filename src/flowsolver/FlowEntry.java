package flowsolver;

import java.util.ArrayList;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionType;

import controller.OFSwitch;
import topology.HostMapping;

public class FlowEntry{
	
	private OFFlowMod flowMod;
	private ArrayList<OFSwitch> switches = new ArrayList<OFSwitch>();
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
	
	//***CONSTRUCTORS
	public FlowEntry(OFFlowMod FlowMod, ArrayList<OFSwitch> ofswitch,
			ArrayList<FlowRequest> fRequest) {
		this.flowMod = FlowMod;
		this.switches = ofswitch;
		this.fRequest = fRequest;
	}
	
	public FlowEntry(){
		
	}
	
	//***SETTERS AND GETTERS
	public OFFlowMod getFlowMod() {
		return flowMod;
	}
	
	public void setFlowMod(OFFlowMod FlowMod) {
		this.flowMod = FlowMod;
	}
	public boolean isActive() {
		if(switches.size() > 0) return true; else return false;
	}
	public ArrayList<OFSwitch> getSwitchs() {
		return switches;
	}
	public void setSwitchs(ArrayList<OFSwitch> switch1) {
		switches = switch1;
	}
	public void addSwitch(OFSwitch sw_toAdd) {
		if(!(switches.contains(sw_toAdd)) && sw_toAdd != null){
			switches.add(sw_toAdd);
		}
	}
	public void updateSwitchs(ArrayList<OFSwitch> switchesToAdd) {
		for(OFSwitch sw_toAdd:switchesToAdd){
			if(!(switches.contains(sw_toAdd))){
				switches.add(sw_toAdd);
				sw_toAdd.sendMsg(flowMod);
			}
		}
	}
	
	public void removeSwitch(OFSwitch sw){
		switches.remove(sw);
		OFFlowMod mod = flowMod.clone();
		mod.setCommand((byte) 0x3);
		sw.sendMsg(mod);
	}
	
	
	public boolean isRelevant(HostMapping host) {
		OFMatch match = flowMod.getMatch();
		int ipaddr = match.getNetworkSource();
		int bits = match.getNetworkSourceMask();
		byte[] mac = match.getDataLayerSource();
		ArrayList<Integer> ipsToCheck = host.getIPArray();
		
		//System.out.println("Check");
		
		if(ipsToCheck.contains(ipaddr)){
			return true;
		}
		if(host.getMac()!=null && mac !=null){
			if(host.getMac().equals(mac)){
				return true;
			}
		}
		if(bits!=0){
			for(int ip:ipsToCheck){
				int mask = -1 << (32 - bits);
				if ((ip & mask) == (ipaddr & mask)) {
				    return true;
				}	
			}
		}
		return false;
	}
	
		
	public void newSwitchSet(ArrayList<OFSwitch> newSwitches){
		ArrayList<OFSwitch> toRemove = new ArrayList<OFSwitch>(switches);
		toRemove.removeAll(newSwitches);
		//remove all the no longer desired entries
		for(OFSwitch sw:toRemove){
			OFFlowMod mod = flowMod.clone();
			mod.setCommand((byte) 0x3);
			sw.sendMsg(mod);
		}
		//send the new entries that aren't already there
		for(OFSwitch sw:newSwitches){
			if(!(switches.contains(sw))){
				sw.sendMsg(flowMod);
			}
		}
		switches = new ArrayList<OFSwitch>(newSwitches);
	}
	
	public ArrayList<FlowRequest> getFlowRequest() {
		return fRequest;
	}
	public void setFlowRequest(ArrayList<FlowRequest> fRequest) {
		this.fRequest = fRequest;
	}
	
	public boolean addFlowRequest(FlowRequest req){
		if(fRequest.contains(req)) return false;
		return fRequest.add(req);
	}
	public boolean addFlowRequest(ArrayList<FlowRequest> req){
		boolean ret = false;
		for(FlowRequest req_toAdd:req){
			if(!(fRequest.contains(req_toAdd))){
				fRequest.add(req_toAdd);
				ret = true;
			}
		}
		return ret;
	}
	public boolean removeFlowRequest(FlowRequest req){
		return fRequest.remove(req);
	}
	public boolean removeFlowRequest(ArrayList<FlowRequest> req){
		return fRequest.removeAll(req);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((flowMod == null) ? 0 : flowMod.hashCode());
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
		FlowEntry other = (FlowEntry) obj;
		if (flowMod == null) {
			if (other.flowMod != null)
				return false;
		} else if (!flowMod.equals(other.flowMod))
			return false;
		return true;
	}

	/**
	 * Checks if the FlowEntry is to forward traffic or drop traffic
	 * @return True if traffic is forwarded, False if traffic is dropped by this entry
	 */
	public boolean getAction() {
		
		ArrayList<OFInstruction> instructions = new ArrayList<OFInstruction>(flowMod.getInstructions());
		OFInstruction instruction = null;
		for(OFInstruction ins:instructions){
			if(ins.getType() == OFInstructionType.APPLY_ACTIONS){
				instruction = ins;
			}
		}
		if(instruction==null) return true;
		ArrayList<OFAction> actions = new ArrayList<OFAction>(((OFInstructionApplyActions)instruction).getActions());
		
		for(OFAction act:actions){
			if(act.getType() == OFActionType.OUTPUT){
				if(((OFActionOutput) act).getLengthU() == 0 ){
					return false;
				}
			}
		}
		
		
		return false;
	}

}
