package topology;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class HostMapping {
	private MacMapping mac;
	private ArrayList<IPMapping> ips = new ArrayList<IPMapping>();
	private SwitchMapping switchMap;
	
	public byte[] getMac() {
		if(mac==null) return null;
		return mac.getMacAddress();
	}

	public void setMac(byte[] mac) {
		this.mac.setMacAddress(mac);
	}

	public ArrayList<IPMapping> getIPs() {
		return ips;
	}

	public void setIPs(ArrayList<IPMapping> ips) {
		this.ips = ips;
	}

	public boolean isValid(){
		Iterator<IPMapping> i = this.ips.iterator();
		while(i.hasNext()){
			IPMapping ip = i.next();
			if(!(ip.isValid())){
				i.remove();
				ip.updateSwitches();
				//System.out.println("Deleting old ip: " + intToIP(ip.getIP()));
			}
		}
		return mac.isValid();
	}
	
	public String toString(){
		String ips = "";
		for(IPMapping ip:this.ips){
			ips = ips + intToIP(ip.getIP()) + " | ";
		}
		return bytesToString(mac.getMacAddress()) + " : " + ips + "\n";
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
	
	public boolean update(HostMapping map){
		this.mac.setMacAddress(map.getMac());
		this.mac.setHost(this);
		boolean retVal = false;
		for(IPMapping ipToAdd:map.ips){
			if(ipToAdd.getIP() != 0){
				if(!(this.ips.contains(ipToAdd))){
					ipToAdd.setHost(this);
					this.ips.add(ipToAdd); // new IP added
					retVal = true;
				}
				else{
					//we already know about the hosts IP mapping, we need to find it and refresh it so it doesnt time out.
					for(IPMapping ip:this.ips){
						if(ip.getIP() == ipToAdd.getIP()){
							ip.refresh();
						}
					}
				}
			}
		}
		return retVal;
	}
	
	public HostMapping(byte[] mac, int ip,long ttl){
		this.mac = new MacMapping(mac,ttl,this);
		if(ip != 0){
			this.ips.add(new IPMapping(ip,this));
		}
	}

	public HostMapping(byte[] mac, int ip,long ttl,SwitchMapping sw){
		this.mac = new MacMapping(mac,ttl,this);
		this.setSwitchMap(sw);
		if(ip != 0){
			this.ips.add(new IPMapping(ip,this));
		}
	}
	
	public HostMapping(){
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
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
		HostMapping other = (HostMapping) obj;
		if (mac == null) {
			if (other.mac != null)
				return false;
		} else if (!mac.equals(other.mac))
			return false;
		return true;
	}

	public MacMapping getMacMapping() {
		return this.mac;
	}

	public ArrayList<Integer> getIPArray() {
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for(IPMapping ip:ips){
			retVal.add(ip.getIP());
		}
		return retVal;
	}

	public SwitchMapping getSwitchMap() {
		return switchMap;
	}

	public void setSwitchMap(SwitchMapping switchMap) {
		this.switchMap = switchMap;
	}

	public void updateSwitches() {
		mac.updateSwitches();
	}

	
	
	
	
	
}
