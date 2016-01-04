import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;



/**
 * 
 * @author Nick
 *
 *This class is to be used for handling all the switches run on the controller. It will track all the switches and will be able to return the instance of a switch that has a requested switchID
 *
 *
 */
public class SwitchHandler extends UnicastRemoteObject implements Runnable, SwitchHandlerAPI {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3480557320840477486L;

	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");	
	
	private String threadName;
	private Thread t;
	private ArrayList<OVSwitch> switches = new ArrayList<OVSwitch>();
	private Registry reg;
	private String regName;
	
	
	
	//****************************************************************************
	//Remote available methods here
		
	public synchronized ArrayList<String> listSwitches(){
		ArrayList<String> res = new ArrayList<String>();
		synchronized (switches) {
			for(int i = 0; i<switches.size();i++){
				res.add(switches.get(i).getSwitchName());
			}
			switches.notifyAll();
		}
		return res;
	}
	
	public String[] listRegisteredObjects(){
		try {
			return reg.list();
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		return null;
	}
	
	
	
	
	//****************************************************************************
	
	public SwitchHandler(String name, String regName, Registry reg) throws RemoteException{
		threadName = name;
		this.reg = reg;
		this.regName = regName;
	}	
	
	
	private void abort(){
		stop();
	}
	
	public synchronized void addSwitch(SocketChannel sock, OFMessageAsyncStream stream){
		synchronized (switches) {
			try {
				new SwitchSetup(threadName + "_SetupSwitch_" + sock.getRemoteAddress(),threadName,sock, stream, this);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
	}
		
		
		
	public synchronized void addSwitch(OVSwitch sw){
		synchronized (switches) {
			switches.add(sw);
			switches.notifyAll();
			try {
				reg.rebind(sw.getSwitchName(), sw);
			} catch (RemoteException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
	}
	public synchronized OVSwitch getSwitch(String switchID){
		OVSwitch sw = null;
		synchronized (switches) {
			for(int i = 0; i<switches.size();i++){
				if((sw = switches.get(i)).getSwitchID().equals(switchID)) return sw;
			}
			switches.notifyAll();
		}
		return null;
	}
	
	
	
	@Override
	public void run(){
		while(!(t.isInterrupted())){
			try {
				Thread.sleep(0, 1);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
		}
        this.abort();
	}
	
	
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
	}
	
	public void start(){
      LOGGER.info("Starting " +  threadName);
      try {
		reg.rebind(regName, this);
	} catch (AccessException e) {
		LOGGER.log(Level.SEVERE, e.toString());
	} catch (RemoteException e) {
		LOGGER.log(Level.SEVERE, e.toString());
	}
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }
	
		
		
		
		
	/**
	 * 
	 * @author Nick
	 *
	 *The reason why this class exists is so that we can create multiple new switches rapidly and have each switch be created by a separate 
	 *thread as each time a switch is created the thread must wait for the switch to send the OFFeaturesReply message to get the switchID
	 *
	 */
	private class SwitchSetup implements Runnable{
		
		private String threadName;
		private Thread t;
		private OFMessageAsyncStream stream;
		private SocketChannel sock;
		private SwitchHandler swhl; 
		private String swName;
		
		public SwitchSetup(String name,String swName, SocketChannel sock, OFMessageAsyncStream stream,SwitchHandler swhl) {
			threadName = name;
			this.stream = stream;
			this.sock = sock;
			this.swhl = swhl;
			this.swName = swName;
			this.start();
		}	
		
		
		private void abort(){
			stop();
		}			
		
		private OFFeaturesReply getFeaturesReply() throws IOException{
			List<OFMessage> msgs = new ArrayList<OFMessage>();
			while(true){
				try {
					Thread.sleep(0,1);
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, e.toString());
				}
	        	msgs.addAll(stream.read());
	        	for(int i = 0; i<msgs.size();i++){
	        		if(msgs.get(i).getType() == OFType.FEATURES_REPLY){
	        			return (OFFeaturesReply) msgs.get(i);
	        		}
	        	}
	        	
	        }
		}
		
		@Override
		public void run(){
			OVSwitch sw = null;
			try {
				List<OFMessage> l = new ArrayList<OFMessage>();
				l.add(stream.getMessageFactory().getMessage(OFType.HELLO));
		        l.add(stream.getMessageFactory().getMessage(OFType.FEATURES_REQUEST));
		        stream.write(l);
		        while(stream.needsFlush()){
		        	stream.flush();
		        }
		        OFFeaturesReply fr = getFeaturesReply();
		        sw = swhl.getSwitch(Long.toHexString(fr.getDatapathId()));
		        if(sw==null){
		        	sw = new OVSwitch(swName + "_Switch_" + sock.getRemoteAddress(),"0000000000000000".substring(Long.toHexString(fr.getDatapathId()).length()) + Long.toHexString(fr.getDatapathId()),stream,sock,fr,30);
		        }
		        else{
		        	sw.restart(sock,stream,fr);
		        }
		        
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.toString());
			}
	        sw.start();
	        swhl.addSwitch(sw);
	        this.abort();
		}
		
		
		public void stop(){
			t.interrupt();
			LOGGER.info("Stopping " +  threadName);
		}
		
		public void start(){
	      LOGGER.info("Starting " +  threadName);
	      if (t == null){
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }
	}
		
		
}