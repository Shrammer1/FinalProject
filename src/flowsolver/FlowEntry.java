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
	public ArrayList<OFSwitch> getSwitch() {
		return ofswitch;
	}
	public void setSwitch(ArrayList<OFSwitch> switch1) {
		ofswitch = switch1;
	}
	public void addSwitch(OFSwitch switch1) {
		ofswitch.add(switch1);
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
	public boolean delFlowRequest(FlowRequest req){
		return fRequest.remove(req);
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
