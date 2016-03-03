package flowsolver;

import java.util.ArrayList;
import org.openflow.protocol.OFFlowMod;
import controller.OFSwitch;

public class FlowEntry{
	
	private OFFlowMod flowMod;
	private boolean active;
	private ArrayList<OFSwitch> ofswitch = new ArrayList<OFSwitch>();
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
	
	//***CONSTRUCTORS
	public FlowEntry(OFFlowMod FlowMod, boolean active, ArrayList<OFSwitch> ofswitch,
			ArrayList<FlowRequest> fRequest) {
		this.flowMod = FlowMod;
		this.active = active;
		this.ofswitch = ofswitch;
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
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public ArrayList<OFSwitch> getSwitchs() {
		return ofswitch;
	}
	public void setSwitchs(ArrayList<OFSwitch> switch1) {
		ofswitch = switch1;
	}
	public void addSwitchs(OFSwitch sw_toAdd) {
		if(!(ofswitch.contains(sw_toAdd))){
			ofswitch.add(sw_toAdd);
		}
	}
	public void updateSwitchs(ArrayList<OFSwitch> switch1) {
		for(OFSwitch sw_toAdd:switch1){
			if(!(ofswitch.contains(sw_toAdd))){
				ofswitch.add(sw_toAdd);
			}
		}
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
	
	


	


	
	
	
}
