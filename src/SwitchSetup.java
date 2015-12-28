import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

/**
	 * 
	 * @author Nick
	 *
	 *The reason why this class exists is so that we can create multiple new switches rapidly and have each switch be created by a separate 
	 *thread as each time a switch is created the thread must wait for the switch to send the OFFeaturesReply message to get the switchID
	 *
	 */
public class SwitchSetup implements Runnable{
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	private String threadName;
	private Thread t;
	private OFMessageAsyncStream stream;
	private SocketChannel sock;
	private SwitchHandler swhl; 
	
	public SwitchSetup(String name, SocketChannel sock, OFMessageAsyncStream stream,SwitchHandler swhl) {
		threadName = name;
		this.stream = stream;
		this.sock = sock;
		this.swhl = swhl;
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
				e.printStackTrace();
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
	        sw = swhl.getSwitch(fr.getDatapathId());
	        if(sw==null){
	        	sw = new OVSwitch("Switch_" + sock.getRemoteAddress(),fr.getDatapathId(),stream,sock);
	        }
	        else{
	        	sw.restart(sock,stream);
	        }
	        
		} catch (IOException e) {
			e.printStackTrace();
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