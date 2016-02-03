package topology;

import java.util.ArrayList;

import controller.OVSwitch;

public class Link {
	private ArrayList<SwitchMapping> switches = new ArrayList<SwitchMapping>();
	
	public void addSwitch(short port, OVSwitch sw){
		switches.add(new SwitchMapping(port, sw));
	}
	
	public short getPort(OVSwitch sw){
		for(SwitchMapping map : switches){
			if(map.getSw() == sw){
				return map.port;
			}
		}
		return -1;
	}
	
	
	/**
	 * Used to represent a switch on a link with a specific port
	 * @author Nicholas Landriault
	 */
	private class SwitchMapping{
		private short port;
		private OVSwitch sw;
		
		public SwitchMapping(short port, OVSwitch sw){
			this.port = port;
			this.sw = sw;
		}

		public short getPort() {
			return port;
		}

		public void setPort(short port) {
			this.port = port;
		}

		public OVSwitch getSw() {
			return sw;
		}

		public void setSw(OVSwitch sw) {
			this.sw = sw;
		}
	}
}
