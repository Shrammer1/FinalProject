package flowsolver;

import java.util.ArrayList;
import org.openflow.protocol.OFFlowMod;
import controller.OFSwitch;

public class FlowEntry{
	
	private OFFlowMod fmod;
	private boolean active;
	private OFSwitch ofswitch;
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
	
	//***CONSTRUCTORS
	public FlowEntry(OFFlowMod fmod, boolean active, OFSwitch ofswitch,
			ArrayList<FlowRequest> fRequest) {
		this.fmod = fmod;
		this.active = active;
		this.ofswitch = ofswitch;
		this.fRequest = fRequest;
	}
	
	public FlowEntry(){
		
	}
	
	//***SETTERS AND GETTERS
	public OFFlowMod getFmod() {
		return fmod;
	}
	
	public void setFmod(OFFlowMod fmod) {
		this.fmod = fmod;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public OFSwitch getSwitch() {
		return ofswitch;
	}
	public void setSwitch(OFSwitch switch1) {
		ofswitch = switch1;
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
		result = prime * result + ((fmod == null) ? 0 : fmod.hashCode());
		result = prime * result + ((ofswitch == null) ? 0 : ofswitch.hashCode());
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
		if (fmod == null) {
			if (other.fmod != null)
				return false;
		} else if (!fmod.equals(other.fmod))
			return false;
		if (ofswitch == null) {
			if (other.ofswitch != null)
				return false;
		} else if (!ofswitch.equals(other.ofswitch))
			return false;
		return true;
	}


	
	
	
}
