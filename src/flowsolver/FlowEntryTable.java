package flowsolver;

import java.util.HashMap;

public class FlowEntryTable extends HashMap<Integer, FlowEntry>{

	private static final long serialVersionUID = -6202524173607913862L;
	
	public FlowEntry put(FlowEntry flowentry){
		return super.put(flowentry.hashCode(),flowentry);
	}
	
	public FlowEntry remove(FlowEntry flowentry){
		return super.remove(flowentry.hashCode());
	}
	
	public boolean contains(FlowEntry flowentry){
		return super.containsKey(flowentry.hashCode());
	}
	
	
}
