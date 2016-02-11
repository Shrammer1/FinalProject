package topology;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import controller.OFSwitch;

/**
 * Used to represent a switch on a link with a specific port
 * @author Nicholas Landriault
 */
public class SwitchMapping{
	private int port;
	private OFSwitch sw;
	private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	private long ttl;
	private ArrayList<HostMapping> hosts = new ArrayList<HostMapping>();
	
	public SwitchMapping(int port, OFSwitch sw){
		this.port = port;
		this.sw = sw;
	}
	
	
	public SwitchMapping(int port, OFSwitch sw, long ttl){
		this.port = port;
		this.sw = sw;
		this.ttl = ttl;
	}
	public SwitchMapping(int port, OFSwitch sw,byte[] mac, int ip, long ttl){
		this.port = port;
		this.sw = sw;
		this.ttl = ttl * 3;
		hosts.add(new HostMapping(mac,ip,ttl));
	}
	
	public long getTimeAlive(){
		return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created;
	}
	
	public boolean isValid(){
		ArrayList<HostMapping> tmp = new ArrayList<HostMapping>(hosts);
		for(HostMapping h:tmp){
			if(!(h.isValid())){
				hosts.remove(h);
			}
		}
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > ttl){
			//System.out.println("Mapping expired: " + this.toString());
			return false;
		}
		return true;
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
	
	public void updateHosts(ArrayList<HostMapping> hosts){
		boolean flag = false;
		ArrayList<HostMapping> hostsToAdd = new ArrayList<>();
		for(HostMapping h1: hosts){
			flag = false;
			for(HostMapping h2: this.hosts){
				if(h2.equals(h1)){
					if(h1.ip == 0 || h2.ip ==0){
						int i =1;
					}
					
					created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
					h2.update(h1);
					flag = true;
				}
			}
			if(!(flag)){ //we didnt find any hosts to update so add the host as a new one.
				hostsToAdd.add(h1);
			}
		}
		this.hosts.addAll(hostsToAdd);
	}
	
	
	public void addHost(byte[] mac, int ip, long ttl){
		created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		hosts.add(new HostMapping(mac, ip,ttl));
	}
	
	public void delHost(byte[] mac, int ip){
		HostMapping newHost = new HostMapping(mac, ip,99999);
		ArrayList<HostMapping> tmp = new ArrayList<>(hosts);
		for(HostMapping h: tmp){
			if(h.equals(newHost)){
				hosts.remove(newHost);
			}
		}
	}
	
	public ArrayList<HostMapping> getHosts(){
		return hosts;
	}
	
	
	@Override
	public String toString() {
		try {
			return sw.getSwitchFullName() + ":" + port;
		} catch (RemoteException e) {
			//can never occur
		}
		return null;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public OFSwitch getSw() {
		return sw;
	}

	public void setSw(OFSwitch sw) {
		this.sw = sw;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + port;
		result = prime * result + ((sw == null) ? 0 : sw.hashCode());
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
		SwitchMapping other = (SwitchMapping) obj;
		if (port != other.port)
			return false;
		if (sw == null) {
			if (other.sw != null)
				return false;
		} else if (!sw.equals(other.sw))
			return false;
		return true;
	}

	
	
}