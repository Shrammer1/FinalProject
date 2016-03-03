package flowsolver;

import java.util.ArrayList;
import java.util.HashMap;

import org.openflow.protocol.OFFlowMod;

import controller.OFSwitch;

public class FlowEntryTable extends HashMap<Integer, FlowEntry>{

	private static final long serialVersionUID = -6202524173607913862L;
	
	public FlowEntry put(FlowEntry flowentry){
		//System.out.println(flowentry.hashCode());
		return this.put(flowentry.hashCode(),flowentry);
	}
	
	public FlowEntry remove(FlowEntry flowToRemove){
		FlowEntry entry = this.get(flowToRemove.hashCode());
		//System.out.println(entry.hashCode());
		if(entry == null){
			return null;
		}
		else{
			entry.removeFlowRequest(flowToRemove.getFlowRequest());
			if(entry.getFlowRequest().size()==0){
				for(OFSwitch sw:entry.getSwitchs()){
					OFFlowMod mod = entry.getFlowMod().clone();
					mod.setCommand((byte) 0x03);
					if(sw != null)sw.sendMsg(mod);
				}
				//System.out.println(entry.hashCode());
				return this.remove(entry.hashCode());
			}
			return entry;
		}
	}
	
	public boolean contains(FlowEntry flowentry){
		return super.containsKey(flowentry.hashCode());
	}

	public FlowEntry add(FlowEntry flowToAdd) {
		FlowEntry entry = null;
		entry = this.get(flowToAdd.hashCode());
		if(entry == null){
			//no mapping, add a new one then send flow
			for(OFSwitch sw:flowToAdd.getSwitchs()){
				if(sw != null)sw.sendMsg(flowToAdd.getFlowMod());
			}
			return put(flowToAdd);
		}
		else{
			//we found an entry
			//Steps:
			//Step 1: update requests
			//Step 2: update switches
			
			entry.addFlowRequest(flowToAdd.getFlowRequest());
			entry.updateSwitchs(flowToAdd.getSwitchs());
				
		}
		
		return entry;
	}
	
	
}
