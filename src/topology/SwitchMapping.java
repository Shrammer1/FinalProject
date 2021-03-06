package topology;

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
	private HostTable hostTable;
	
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
		hosts.add(new HostMapping(mac,ip,ttl,this));
	}
	
	public long getTimeAlive(){
		return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created;
	}
	
	public boolean isValid(){
		ArrayList<HostMapping> tmp = new ArrayList<HostMapping>(hosts);
		for(HostMapping h:tmp){
			if(!(h.isValid())){
				hosts.remove(h);
				h.updateSwitches();
			}
		}
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > ttl){
			//System.out.println("Mapping expired: " + this.toString());
			return false;
		}
		return true;
	}
	
	
	public boolean updateHosts(ArrayList<HostMapping> hosts){
		boolean flag = false;
		boolean retVal = false;
		ArrayList<HostMapping> hostsToAdd = new ArrayList<>();
		for(HostMapping h1: hosts){
			flag = false;
			for(HostMapping h2: this.hosts){
				if(h2.equals(h1)){
					created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
					if(h2.update(h1)){
						retVal = true;
					}
					flag = true;
				}
			}
			if(!(flag)){ //we didnt find any hosts to update so add the host as a new one.
				h1.setSwitchMap(this);
				hostsToAdd.add(h1);
				retVal = true;
			}
		}
		
		this.hosts.addAll(hostsToAdd);
		return retVal;
	}
	
	
	public void addHost(byte[] mac, int ip, long ttl){
		created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		HostMapping hostToAdd = new HostMapping(mac, ip,ttl,this);		
		for(HostMapping h:hosts){
			if(h.equals(hostToAdd)){
				h.update(hostToAdd); //theres already a host with this MAC, update it
				return;
			}
		}
		//No hosts found, add a new one
		this.hosts.add(hostToAdd);
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
		return sw.getSwitchFullName() + ":" + port;
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


	public HostTable getHostTable() {
		return hostTable;
	}


	public void setHostTable(HostTable hostTable) {
		this.hostTable = hostTable;
	}

	
	
}