import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.util.LRULinkedHashMap;


public class OVSwitch implements Runnable{
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	private Map<Integer, Short> macTable = new LRULinkedHashMap<Integer, Short>(64001, 64000);
	private String threadName;
	private OFMessageAsyncStream stream;
	private BasicFactory factory = new BasicFactory();
	private List<OFMessage> l = new ArrayList<OFMessage>();
	private List<OFMessage> msgIn = new ArrayList<OFMessage>();
	private PacketHandler pkhl;
	private StreamHandler sthl;
	private SocketChannel sock;
	private Thread t;
	private OFFeaturesReply featureReply;
	public long switchID;
	public String nickname;
	
	public OFFeaturesReply getFeatures(){
		return featureReply;
	}
	
	
	public OVSwitch(String name, long switchID, OFMessageAsyncStream strm, SocketChannel s, OFFeaturesReply fr) {
		threadName = name;
		stream = strm;
		sock = s;
		this.switchID = switchID;
		this.featureReply = fr;
	}	
	
	public boolean isAlive(){
		if(t.isAlive()) return true;
		return false;
	}
	
	
	private void abort(){
		stop();
		pkhl.stop();
		pkhl=null;
		sthl.stop();
		sthl=null;
		t=null;
		try {
			sock.close();
		} catch (IOException e) {

		}
	}
	
	public void restart(SocketChannel sock, OFMessageAsyncStream stream, OFFeaturesReply fr){
		try{
			if(t.isAlive()) abort();
		}catch(NullPointerException e){
			//perfectly normal, just means that the thread is already stopped
		}
		macTable = new LRULinkedHashMap<Integer, Short>(64001, 64000);
		this.featureReply = fr;
		this.stream = stream;
		this.sock = sock;
		LOGGER.info("RE-Starting " +  threadName + "\t" + "Switch ID: " + switchID);
	      if (t == null){
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	}
	
	
	@Override
	public void run(){
		sthl = new StreamHandler(threadName + "_StreamHandler", stream);
		pkhl = new PacketHandler(threadName + "_PacketHandler",macTable,sthl); 
		sthl.start();
		pkhl.start();
		
        try {
        	long lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        	sthl.sendMsg(l);
        	l.clear();
        	
        	boolean waitForReply = false;
        	
        	OFMessage msg = null;
            while(t.isInterrupted()==false){
            	
            	try{
            		msgIn.addAll(stream.read());
            		Thread.sleep(0, 1);
            	}catch(NullPointerException e){
            		abort();
            		return;
            	}
            	
            	if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastHeard > 4 && waitForReply==false){
            		l.add(factory.getMessage(OFType.ECHO_REQUEST));
            		sthl.sendMsg(l);
				    l.clear();
				    waitForReply = true;
            	}
            	
            	if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastHeard > 10){ //switch timed out. delete the connection and make it start from scratch
            		abort();
            		return;
            	}
            	
    	        if(!(msgIn.size()==0)){
	    			msg = msgIn.remove(0);
	    			if(msg.getType() == OFType.ECHO_REQUEST){
	    				lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    		    		l.add(factory.getMessage(OFType.ECHO_REPLY));
    		    		sthl.sendMsg(l);
    				    l.clear();
    				    waitForReply = false;
    		    	}
	    			else if(msg.getType() == OFType.ECHO_REPLY){
	    				lastHeard = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	    				waitForReply = false;
    		    	}
	    			else {
	    				pkhl.addPacket(msg);
	    				pkhl.wakeUp();
	    			}
    	        }
            }
        	
        	
        	
		} catch (Exception e) {
			abort();
			e.printStackTrace();
			return;
		}
        this.abort();
	}
	
	
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
		pkhl.stop();
	}
	
	public void start (){
      LOGGER.info("Starting " +  threadName + "\t" + "Switch ID: " + switchID);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }
	
		
}
