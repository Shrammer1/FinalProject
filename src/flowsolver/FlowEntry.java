package flowsolver;

import java.util.ArrayList;
import org.openflow.protocol.OFFlowMod;
import controller.OFSwitch;

public class FlowEntry{
	
	private OFFlowMod fmod;
	private boolean active;
	private OFSwitch Switch;
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
	
	//***CONSTRUCTORS
	public FlowEntry(OFFlowMod fmod, boolean active, OFSwitch switch1,
			ArrayList<FlowRequest> fRequest) {
		this.fmod = fmod;
		this.active = active;
		Switch = switch1;
		this.fRequest = fRequest;
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
		return Switch;
	}
	public void setSwitch(OFSwitch switch1) {
		Switch = switch1;
	}
	public ArrayList<FlowRequest> getfRequest() {
		return fRequest;
	}
	public void setfRequest(ArrayList<FlowRequest> fRequest) {
		this.fRequest = fRequest;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fmod == null) ? 0 : fmod.hashCode());
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
		return true;
	}
	
	
	
}
