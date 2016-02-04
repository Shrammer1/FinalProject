package topology;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import controller.OVSwitch;

/**
 * Used to represent a switch on a link with a specific port
 * @author Nicholas Landriault
 */
public class SwitchMapping{
	private int port;
	private OVSwitch sw;
	private ArrayList<byte[]> macs = new ArrayList<byte[]>();
	private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());;
	private long ttl;
	
	public SwitchMapping(int port, OVSwitch sw){
		this.port = port;
		this.sw = sw;
	}
	
	
	public SwitchMapping(int port, OVSwitch sw, long ttl){
		this.port = port;
		this.sw = sw;
		this.ttl = ttl;
	}
	public SwitchMapping(int port, OVSwitch sw,byte[] mac, long ttl){
		this.port = port;
		this.sw = sw;
		this.macs.add(mac);
		this.ttl = ttl;
	}
	
	public long getTimeAlive(){
		return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created;
	}
	
	public boolean isValid(){
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > ttl){
			return false;
		}
		return true;
	}
	
	
	
	public void addMac(byte[] mac){
		macs.add(mac);
	}
	
	public void delMac(byte mac[]){
		macs.remove(mac);
	}
	
	public ArrayList<byte[]> getMacs(){
		return macs;
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

	public OVSwitch getSw() {
		return sw;
	}

	public void setSw(OVSwitch sw) {
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