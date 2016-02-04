package topology;

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
	
	public boolean equals(SwitchMapping map){
		if(map.port == this.port && map.sw.equals(this.sw)){
			return true;
		}
		return false;
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