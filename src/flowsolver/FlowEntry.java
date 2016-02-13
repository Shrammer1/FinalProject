package flowsolver;

import java.util.ArrayList;
import org.openflow.protocol.OFFlowMod;
import controller.OFSwitch;

public class FlowEntry{
	
	private OFFlowMod fmod;
	private boolean active;
	private OFSwitch Switch;
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
	
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
	
	
}
