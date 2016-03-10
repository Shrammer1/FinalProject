package flowsolver;

import java.util.ArrayList;
import java.util.HashMap;

import controller.OFSwitch;
import topology.HostMapping;

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
				entry.newSwitchSet(new ArrayList<OFSwitch>()); //update the switches on the flow entry with nothing causing all switches to be removed and all flows to be retracted
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

	/**
	 * Finds all FlowEntry objects that are relevant to the provided HostMapping
	 * @param host Host to search for
	 * @return ArrayList of relevant FlowEnty objects
	 */
	public ArrayList<FlowEntry> getRelevantFlows(HostMapping host) {
		ArrayList<FlowEntry> retVal = new ArrayList<FlowEntry>();
		for(FlowEntry entry:this.values()){
			if(entry.isRelevant(host)) retVal.add(entry);
		}
		return retVal;
	}
	
	public boolean isAllowed(HostMapping host) {
		ArrayList<FlowEntry> relevantFlows = new ArrayList<FlowEntry>(getRelevantFlows(host));
		short priority = 0;
		FlowEntry entry = null;
		
		for(FlowEntry toCheck:relevantFlows){
			if(toCheck.getFlowMod().getPriority() > priority){
				entry = toCheck;
			}
		}
		if(entry == null) return true;
		
		return entry.getAction();
		
	}
	
	

}
