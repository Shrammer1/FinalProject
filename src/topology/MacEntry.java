package topology;

import java.util.Arrays;
import controller.OVSwitch;

public class MacEntry{
	private byte[] mac;
	private int port;
	private OVSwitch sw;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mac);
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
		MacEntry other = (MacEntry) obj;
		if (!Arrays.equals(mac, other.mac))
			return false;
		if (port != other.port)
			return false;
		if (sw == null) {
			if (other.sw != null)
				return false;
		} else if (!sw.equals(other.sw))
			return false;
		return true;
	}
	
	
	public MacEntry(byte[] mac, int inPort, OVSwitch sw){
		this.mac = mac;
		this.port = inPort;
		this.sw = sw;
		
	}

}