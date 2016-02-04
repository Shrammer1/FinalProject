package topology;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import controller.OVSwitch;

public class TopologyMapper implements Runnable{
	
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	private Thread t;
	private String threadName;
	private ArrayList<OVSwitch> switches = new ArrayList<OVSwitch>();
	private ArrayList<MacEntry> macTable = new ArrayList<MacEntry>();
	private LinkTable links = new LinkTable();
	
	public TopologyMapper(String name,ArrayList<OVSwitch> switches) {
		this.switches = switches;
		this.threadName = name;
	}
	
	
	@Override
	public void run() {
		
		long lastSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		while(!(t.isInterrupted())){
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastSent > 4){
				lastSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
				for(OVSwitch sw : switches){
					sw.discover();
				}
			}
		}
		
	}
	

	
	public void stop(){
		t.interrupt();
	}

	public void start (){
      LOGGER.info("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }	
	
	public synchronized void learn(byte[] macAddr, int inPort, OVSwitch sw){
		MacEntry me = new MacEntry(macAddr,inPort,sw);
		if(!(macTable.contains(me))){
			macTable.add(me);
		}
	}

	public synchronized void learn(LLDPMessage lldpMessage, OVSwitch sw, int inPort) {
		
		System.out.println(links.toString());
		
		OVSwitch farEnd = null;

		for(OVSwitch s:switches){
			try {
				if(s.getSwitchID().equals(lldpMessage.getSwitchID())){
					farEnd = s;
					break;
				}
			} catch (RemoteException e) {
				//can never occur
			}
		}
		
		Link lnk = links.getLink(lldpMessage.getPort(), farEnd);
		
		if(lnk==null){
			//the originating switch doesn't have a link object associated to it. Lets see if the recieving switch has one
			lnk = links.getLink(inPort, sw);
			if(lnk == null){
				//neither the current switch nor the originating switch has a link, create a new link and add both of them to it
				lnk = new Link();
				lnk.addSwitch(lldpMessage.getPort(), farEnd);
				lnk.addSwitch(inPort, sw);
				links.add(lnk);
			}
			
			
		}
		else{
			//the far end has a link. Lets see if the local end has one
			Link lnk2 = links.getLink(inPort, sw);
			if(lnk2==null){
				//we dont know about either switch - port mapping. learn them
				lnk = new Link();
				lnk.addSwitch(lldpMessage.getPort(), farEnd);
				lnk.addSwitch(inPort, sw);
				links.add(lnk);
			}
			else{
				//if lnk and lnk2 are the same object we already know about this link.
				if(lnk.equals(lnk2)){
					return;
				}
				else{
					/*
					//if lnk and lnk2 are different objects it means that something has changed in the topology from the last time we learned everything
					links.remove(lnk);
					links.remove(lnk2);
					for(SwitchMapping map:lnk){
						map.getSw().discover();
					}
					for(SwitchMapping map:lnk2){
						map.getSw().discover();
					}
					
					*/
				}
			}
		}
		
	}
	
			
	
	
	private class MacEntry{
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private byte[] mac;
		private int port;
		private OVSwitch sw;
		
		
		
		public MacEntry(byte[] mac, int inPort, OVSwitch sw){
			this.mac = mac;
			this.port = inPort;
			this.sw = sw;
			
		}


		private TopologyMapper getOuterType() {
			return TopologyMapper.this;
		}
	}


}
