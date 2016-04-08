package topology;

import java.math.BigInteger;
import java.util.ArrayList;

import controller.OFSwitch;
import utils.NetworkUtils;

public class HostTable extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 853289804357492274L;
	private TopologyMapper topo;
	
	public HostTable(TopologyMapper topo){
		this.setTopology(topo);
	}
	
	public SwitchMapping getMapping(int inPort, OFSwitch sw){
		SwitchMapping testMap = new SwitchMapping(inPort, sw);
		for(SwitchMapping map:this){
			if(map.equals(testMap)) return map;
		}
		
		return null;
	}
	
	public ArrayList<HostMapping> getMappings(OFSwitch sw){
		ArrayList<HostMapping> retVal = new ArrayList<HostMapping>();
		for(SwitchMapping map:this){
			if(map.getSw().equals(sw)) retVal.addAll(map.getHosts());
		}
		return retVal;
	}
	
	
	public OFSwitch getHost(int ipAddress){
		for(SwitchMapping swMap:this){
			for(HostMapping h: swMap.getHosts()){
				for(IPMapping ip:h.getIPs()){
					if(ip.getIP() == ipAddress) return swMap.getSw();
				}
			}
		}
		return null;
	}
	public OFSwitch getHost(byte[] macAddress){
		for(SwitchMapping swMap:this){
			for(HostMapping h: swMap.getHosts()){
				if(new BigInteger(h.getMac()).intValue() == new BigInteger(macAddress).intValue()) return swMap.getSw();
			}
		}
		return null;
	}
	
	public ArrayList<OFSwitch> getHosts(int ipAddress, int bits){
		ArrayList<OFSwitch> retVal = new ArrayList<OFSwitch>();
		for(SwitchMapping swMap:this){
			loop1:
			for(HostMapping h: swMap.getHosts()){
				int mask = -1 << (32 - bits);
				for(IPMapping ip:h.getIPs()){
					if ((ipAddress & mask) == (ip.getIP() & mask)) {
					    retVal.add(swMap.getSw());
					    break loop1; //we've found at LEAST 1 host in the subnet on the switch so we don't need to check the rest.
					}
				}
			}
		}
		return retVal;
	}
	
	
	
	
	public boolean remove(Object o){
		//System.out.println("Removing host mapping: " + ((SwitchMapping) o).toString());
		return super.remove(o);
	}
	
	public String toString(){
		String retval = "";
		for(SwitchMapping map:this){
			for(HostMapping h:map.getHosts()){
				String ips = "";
				for(IPMapping ip: h.getIPs()){
					ips = ips + NetworkUtils.intToIP(ip.getIP()) + " | ";
				}
				//retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + Integer.toHexString(ByteBuffer.wrap(mac).getInt()) + "\n";
				retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + NetworkUtils.bytesToMac(h.getMac()) + " : " + ips + "\n";
			}
		}
		return retval;
	}
	
	
	public synchronized boolean add(SwitchMapping map){
		for(SwitchMapping m:this){
			if(m.equals(map)){
				//we found a mapping for this switch - port
				return m.updateHosts(map.getHosts());
			}
		}
		//we didnt find a mapping, add a new one
		map.setHostTable(this);
		return super.add(map);
	}

	public void cleanDead() {
				
		ArrayList<SwitchMapping> tmp = new ArrayList<SwitchMapping>(this);
		for(SwitchMapping map:tmp){
			if(!(map.isValid())){
				this.remove(map);
			}
		}
	}
	
	public synchronized void ageIPMapping(int ip, OFSwitch ofSwitch){
		for(SwitchMapping swMap:this){
			if(swMap.getSw().equals(ofSwitch)){
				for(HostMapping hostMap:swMap.getHosts()){
					for(IPMapping ipMap:hostMap.getIPs()){
						if(ipMap.getIP() == ip){
							//we've found the mapping for this IP
							ipMap.startTimingOut();
						}
					}		
				}
			}
		}
	}

	public void updateMappings(MacMapping map) {
		for(SwitchMapping swMap:this){
			for(HostMapping hostMap:swMap.getHosts()){
				hostMap.getMacMapping().addMatchs(map.getMatches());					
			}
		}
	}

	public TopologyMapper getTopology() {
		return topo;
	}

	public void setTopology(TopologyMapper topo) {
		this.topo = topo;
	}
	
}
