package topology;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class HostMapping {
	private byte[] mac;
	private ArrayList<IPMapping> ips = new ArrayList<IPMapping>();
	
	
	public byte[] getMac() {
		return mac;
	}

	public void setMac(byte[] mac) {
		this.mac = mac;
	}

	public ArrayList<IPMapping> getIPs() {
		return ips;
	}

	public void setIPs(ArrayList<IPMapping> ips) {
		this.ips = ips;
	}

	private long ttl;
	private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	
	public boolean isValid(){
		Iterator<IPMapping> i = this.ips.iterator();
		while(i.hasNext()){
			IPMapping ip = i.next();
			if(!(ip.isValid())){
				i.remove();
				//System.out.println("Deleting old ip: " + intToIP(ip.getIP()));
			}
		}
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > ttl){
			return false;
		}
		return true;
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
		created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	}
	
	public HostMapping(byte[] mac, int ip,long ttl){
		this.mac = mac;
		if(ip != 0){
			this.ips.add(new IPMapping(ip));
		}
		this.ttl = ttl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mac);
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
		if (Arrays.equals(mac, other.mac)){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	
	
	
}
