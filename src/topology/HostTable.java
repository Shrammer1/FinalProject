package topology;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import controller.OFSwitch;

public class HostTable extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 853289804357492274L;
	
	public SwitchMapping getMapping(int inPort, OFSwitch sw){
		SwitchMapping testMap = new SwitchMapping(inPort, sw);
		for(SwitchMapping map:this){
			if(map.equals(testMap)) return map;
		}
		
		return null;
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
					ips = ips + intToIP(ip.getIP()) + " | ";
				}
				//retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + Integer.toHexString(ByteBuffer.wrap(mac).getInt()) + "\n";
				retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + bytesToString(h.getMac()) + " : " + ips + "\n";
			}
		}
		return retval;
	}
	
	private String intToIP(int ip){
	  byte[] addr = new byte[] {
	    (byte)((ip >>> 24) & 0xff),
	    (byte)((ip >>> 16) & 0xff),
	    (byte)((ip >>>  8) & 0xff),
	    (byte)((ip       ) & 0xff)};

	  try {
		return InetAddress.getByAddress(addr).getHostAddress();
	  } catch (UnknownHostException e) {
		e.printStackTrace();
	  }
	  return null;
	}
	
	
	//TODO: make this a public static class/method
	private String bytesToString(byte[] mac){
		StringBuilder sb = new StringBuilder(18);
	    for (byte b : mac) {
	        if (sb.length() > 0)
	            sb.append(':');
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
	
	
	public synchronized boolean add(SwitchMapping map){
		for(SwitchMapping m:this){
			if(m.equals(map)){
				//we found a mapping for this switch - port
				return m.updateHosts(map.getHosts());
			}
		}
		//we didnt find a mapping, add a new one
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
	
	public void ageMapping(int ip){
		for(SwitchMapping swMap:this){
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
