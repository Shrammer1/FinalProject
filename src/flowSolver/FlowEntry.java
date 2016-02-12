package flowSolver;

import java.util.ArrayList;
import org.openflow.protocol.OFFlowMod;
import controller.OFSwitch;

public class FlowEntry {
	private OFFlowMod fmod;
	private boolean active;
	private OFSwitch Switch;
	private ArrayList<FlowRequest> fRequest = new ArrayList<FlowRequest>();
}
