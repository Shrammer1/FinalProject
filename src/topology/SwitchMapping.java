package topology;

import java.rmi.RemoteException;

import controller.OVSwitch;

/**
 * Used to represent a switch on a link with a specific port
 * @author Nicholas Landriault
 */
public class SwitchMapping{
	private int port;
	private OVSwitch sw;
	
	public SwitchMapping(int port, OVSwitch sw){
		this.port = port;
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
}