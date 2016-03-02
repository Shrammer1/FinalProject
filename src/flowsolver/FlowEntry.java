package flowsolver;

import java.util.ArrayList;
import org.openflow.protocol.OFFlowMod;
import controller.OFSwitch;

public class FlowEntry{
	
	private OFFlowMod FlowMod;
	private boolean active;
	private ArrayList<OFSwitch> ofswitch = new ArrayList<OFSwitch>();
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
	
	//***CONSTRUCTORS
	public FlowEntry(OFFlowMod FlowMod, boolean active, ArrayList<OFSwitch> ofswitch,
			ArrayList<FlowRequest> fRequest) {
		this.FlowMod = FlowMod;
		this.active = active;
		this.ofswitch = ofswitch;
		this.fRequest = fRequest;
	}
	
	public FlowEntry(){
		
	}
	
	//***SETTERS AND GETTERS
	public OFFlowMod getFlowMod() {
		return FlowMod;
	}
	
	public void setFlowMod(OFFlowMod FlowMod) {
		this.FlowMod = FlowMod;
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
	public void addSwitchs(OFSwitch switch1) {
		ofswitch.add(switch1);
	}
	public void updateSwitchs(ArrayList<OFSwitch> switch1) {
		for(OFSwitch sw_toAdd:switch1){
			for(OFSwitch sw_toCheck: ofswitch){
				if(!(sw_toAdd.equals(sw_toCheck))){
					ofswitch.add(sw_toAdd);
				}
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
		return fRequest.add(req);
	}
	public boolean addFlowRequest(ArrayList<FlowRequest> req){
		return fRequest.addAll(req);
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
		result = prime * result + ((FlowMod == null) ? 0 : FlowMod.hashCode());
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
		if (FlowMod == null) {
			if (other.FlowMod != null)
				return false;
		} else if (!FlowMod.equals(other.FlowMod))
			return false;
		return true;
	}
	
	


	


	
	
	
}
