package utils;

import controller.OFSwitch;

public class Tuple {
	
	public OFSwitch sw;
	public int ip;
	
	public Tuple(int ip, OFSwitch sw){
		this.ip = ip;
		this.sw=sw;
	}
	
}
