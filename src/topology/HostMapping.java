package topology;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class HostMapping {
	private MacMapping mac;
	private ArrayList<IPMapping> ips = new ArrayList<IPMapping>();
	
	
	public byte[] getMac() {
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
				//System.out.println("Deleting old ip: " + intToIP(ip.getIP()));
			}
		}
		return mac.isValid();
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
	
	public void update(HostMapping map){
		this.mac.setMacAddress(map.getMac());
		for(IPMapping ipToAdd:map.ips){
			if(ipToAdd.getIP() != 0){
				if(!(this.ips.contains(ipToAdd))){
					this.ips.add(ipToAdd);
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
	}
	
	public HostMapping(byte[] mac, int ip,long ttl){
		this.mac = new MacMapping(mac,ttl);
		if(ip != 0){
			this.ips.add(new IPMapping(ip));
		}
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

	
	
	
	
	
}
