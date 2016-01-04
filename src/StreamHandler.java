import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFMessage;


public class StreamHandler implements Runnable{
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	private String threadName;
	private OFMessageAsyncStream stream;
	private Thread t;
	
	
	public StreamHandler(String name, OFMessageAsyncStream strm){
		threadName = name;
		stream = strm;
	}
	
	protected synchronized void sendMsg(OFMessage msg){
		try {
			synchronized (stream) {
				stream.write(msg);
				stream.notifyAll();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}	
	}
	
	protected synchronized void sendMsg(List<OFMessage> l){
		try {
			synchronized (stream) {
				stream.write(l);
				stream.notifyAll();
			}
				
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}	
	}
	
	@Override
	public void run(){
		try {
		    while(!(t.isInterrupted())){
		    	Thread.sleep(0, 1);
		    	synchronized (stream) {
		    		if(stream.needsFlush()) stream.flush();
		    		stream.notifyAll();
				}
		    }
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}

	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
	}
	
	public void start (){
      LOGGER.info("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}

}
