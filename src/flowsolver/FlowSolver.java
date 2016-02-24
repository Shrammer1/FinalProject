package flowsolver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoTable;

import controller.Application;
import controller.Controller;
import controller.OFSwitch;

public class FlowSolver {
	private Controller controller;
	
	private FlowEntryTable flows = new FlowEntryTable();
	
	public FlowSolver(Controller ctrl){
		this.controller = ctrl;
	}
	
	public boolean requestAddFlow(FlowRequest request, Application app){
		
		
		//Step 1: break the request down into all the sub components
		//Step 2: determine the flows needed to build all the FlowEntry objects
		//Step 3: add the FlowEntry objects to the FlowEntryTable, updating the FlowRequest field where appropriate and only adding new FlowEntry objects when there are no duplicates (done via HashMap)
		
		
		ArrayList<FlowEntry> flows = buildFlows(request, app.getPriority() + request.getPriority());
		
		//TODO: add code that properly updates the FlowEntryTable and sends out new flows
		
		//This magical code takes a sledge hammer to the controller and forces out flow mods, its for testing only
				/*
		if(flows == null) return false;
		for(FlowEntry e:flows){
			e.getSwitch().get(0).sendMsg(e.getFlowMod());
		}
		*/
		return true;
	}
	
	private ArrayList<FlowEntry> buildFlows(FlowRequest request, int priority){
		boolean wildcardDstFlag = false;
		byte portType;
		short srcPortNum = 0;
		short dstPortNum = 0;
		
		portType = request.getTrafficClass().getPortType();
		srcPortNum = request.getTrafficClass().getSrcPort();
		dstPortNum = request.getTrafficClass().getDstPort();
		
		
		
		//if the application asks for a priority that is out of the valid range reject the flow.
		
		
		//Step 1:
		
		ArrayList<DomainEntry> listOfDomainEntries = request.getSrc().toArray();
		ArrayList<byte[]> srcIPs = new ArrayList<byte[]>();
		ArrayList<byte[]> srcMACs = new ArrayList<byte[]>();
		ArrayList<byte[]> dstIPs = new ArrayList<byte[]>();
		ArrayList<byte[]> dstMACs = new ArrayList<byte[]>();
		
		for(DomainEntry entry:listOfDomainEntries){
			if(entry.getType() == DomainType.IP){
				srcIPs.addAll(entry.getValues());
			}
			else if(entry.getType() == DomainType.MAC){
				srcMACs.addAll(entry.getValues());
			}
		}
		
		if(request.getDst()!=null){
			listOfDomainEntries = request.getDst().toArray();
			//if there are 1 or more domain entries in the dst field load them, else set the wildcard flag
			if(listOfDomainEntries.size() != 0){
				for(DomainEntry entry:listOfDomainEntries){
					if(entry.getType() == DomainType.IP){
						dstIPs.addAll(entry.getValues());
					}
					else if(entry.getType() == DomainType.MAC){
						dstMACs.addAll(entry.getValues());
					}
				}
			}
			else{
				wildcardDstFlag=true;
			}
		}
		else{
			wildcardDstFlag=true;
		}
		
		
		
		//Step 2:
		
		//No source specified
		if(srcIPs.size()==0 && srcMACs.size()==0){
			return null;
		}
		
		ArrayList<FlowEntry> flows = new ArrayList<FlowEntry>();
		//first create flow entries for IPs then MACs
		if(wildcardDstFlag){
			if(srcIPs.size()!=0){
				flows.addAll(generateFlows(srcIPs,request, priority));
			}
			if(srcMACs.size() !=0){
				flows.addAll(generateFlows(srcMACs,request, priority));
			}
		}
		else{
			if(srcIPs.size()!=0){
				if(dstIPs.size()!=0){
					
				}
				if(dstMACs.size() !=0){
					
				}
			}
			if(srcMACs.size() !=0){
				if(dstIPs.size()!=0){
					
				}
				if(dstMACs.size() !=0){
					
				}
			}
		}
		
		if(portType!=0){
			for(FlowEntry entry:flows){
				OFMatch match = entry.getFlowMod().getMatch();
				match.setDataLayerType((short) 0x0800);
				if(srcPortNum!=0){
					match.setTransportSource(portType, srcPortNum);
				}
				if(dstPortNum!=0){
					match.setTransportDestination(portType, dstPortNum);
				}
				
			}
		}
		
		for(FlowEntry entry:flows){
			entry.addFlowRequest(request);
		}
		
		return flows;
	}
	
	/**
	 * Generates an ArrayList of FlowEntry objects for a given source with a specified priority 
	 * @param src Source byte arrays
	 * @param priority Priority of the FlowEntry
	 * @return The resulting ArayList of FlowEntry objects
	 */
	private ArrayList<FlowEntry> generateFlows(ArrayList<byte[]> srcList,FlowRequest request, int priority){
		ArrayList<FlowEntry> retVal = new ArrayList<FlowEntry>(); 
		boolean srcIsMAC = false;
		if(srcList.get(0).length == 6){
			srcIsMAC = true;
		}
		if(srcIsMAC){
			for(byte[] src:srcList){
				FlowEntry entry = new FlowEntry();
				entry.setActive(false);
				entry.addSwitch(controller.getTopologyMapper().getMapping(src));
				
				OFFlowMod mod = new OFFlowMod();
				OFMatch match = new OFMatch();
				mod.setCommand((byte) 0);
				mod.setPriority((short) priority);
				mod.setTableId((byte) 0);
	            List<OFInstruction> instructions = new ArrayList<OFInstruction>();
	            
	            if(request.getFlowAction()==FlowAction.ALLOW){
	            	OFInstructionGotoTable instruction = new OFInstructionGotoTable();
		            instruction.setTableId((byte) 1);
	            }
	            else if(request.getFlowAction()==FlowAction.DROP){
	            	OFActionOutput action = new OFActionOutput();
	                action.setMaxLength((short) 0);
	                action.setPort(OFPort.OFPP_ALL);
	            	List<OFAction> actions = new ArrayList<OFAction>();
	                actions.add(action);
	                OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
	                instructions.add(instruction);
	            }
	            mod.setInstructions(instructions);
	            match.setDataLayerSource(src);
	            mod.setMatch(match);
	            entry.setFlowMod(mod);
	            retVal.add(entry);
			}
		}
		else{
			for(byte[] src:srcList){
				if(src.length == 4){
					FlowEntry entry = new FlowEntry();
					entry.setActive(false);
					//src is a single IP
					entry.addSwitch(controller.getTopologyMapper().getMapping(ByteBuffer.wrap(src).getInt()));
					OFFlowMod mod = new OFFlowMod();
					OFMatch match = new OFMatch();
					mod.setCommand((byte) 0);
					mod.setPriority((short) priority);
					mod.setTableId((byte) 0);
		            List<OFInstruction> instructions = new ArrayList<OFInstruction>();
		            
		            if(request.getFlowAction()==FlowAction.ALLOW){
		            	OFInstructionGotoTable instruction = new OFInstructionGotoTable();
			            instruction.setTableId((byte) 1);
		            }
		            else if(request.getFlowAction()==FlowAction.DROP){
		            	OFActionOutput action = new OFActionOutput();
		                action.setMaxLength((short) 0);
		                action.setPort(OFPort.OFPP_ALL);
		            	List<OFAction> actions = new ArrayList<OFAction>();
		                actions.add(action);
		                OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
		                instructions.add(instruction);
		            }
		            mod.setInstructions(instructions);
		            match.setDataLayerType((short) 0x0800);
		            match.setNetworkSource(ByteBuffer.wrap(src).getInt());
		            mod.setMatch(match);
		            entry.setFlowMod(mod);
		            retVal.add(entry);
				}
				else if(src.length == 5){
					ArrayList<OFSwitch> switches = controller.getTopologyMapper().getMappings(ByteBuffer.wrap(src).getInt(), (int) src[4]);
					for(OFSwitch sw:switches){
						FlowEntry entry = new FlowEntry();
						entry.setActive(false);
						//src is a single IP
						entry.addSwitch(sw);
						OFFlowMod mod = new OFFlowMod();
						OFMatch match = new OFMatch();
						mod.setCommand((byte) 0);
						mod.setPriority((short) priority);
						mod.setTableId((byte) 0);
			            List<OFInstruction> instructions = new ArrayList<OFInstruction>();
			            
			            if(request.getFlowAction()==FlowAction.ALLOW){
			            	OFInstructionGotoTable instruction = new OFInstructionGotoTable();
				            instruction.setTableId((byte) 1);
			            }
			            else if(request.getFlowAction()==FlowAction.DROP){
			            	OFActionOutput action = new OFActionOutput();
			                action.setMaxLength((short) 0);
			                action.setPort(OFPort.OFPP_ALL);
			            	List<OFAction> actions = new ArrayList<OFAction>();
			                actions.add(action);
			                OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
			                instructions.add(instruction);
			            }
			            mod.setInstructions(instructions);
			            match.setDataLayerType((short) 0x0800);
			            match.setNetworkSourceMask(ByteBuffer.wrap(src).getInt(), src[4]);			            
			            mod.setMatch(match);
			            entry.setFlowMod(mod);
			            retVal.add(entry);
					}
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Generates an ArrayList of FlowEntry objects for a given source and destination with a specified priority 
	 * @param src Source byte arrays
	 * @param dst Destination byte arrays
	 * @param priority Priority of the FlowEntry
	 * @return The resulting ArayList of FlowEntry objects
	 */
	private ArrayList<FlowEntry> generateFlows(ArrayList<byte[]> srcList,ArrayList<byte[]> dstList,FlowRequest request, int priority){
		boolean srcIsMAC = false;
		boolean dstIsMAC = false;
		if(srcList.get(0).length == 6){
			srcIsMAC = true;
		}
		if(dstList.get(0).length == 6){
			srcIsMAC = true;
		}
		
		
		return null;
	}
	
	
	
	
	
	
	public boolean requestDelFlow(FlowRequest request, int id){
		return false;
	}

}
