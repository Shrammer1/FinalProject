package controller;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFMessage;

//StreamHandler Class (Runnable)
public class StreamHandler implements Runnable{
	
	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	/*final=its value cannot be changed once initialized
	 * static=variable shared among all the instances of this class as it
	 * belongs to the type and not to the actual objects themselves.
	 * LOGGER variable shared among all the instances of this class
	 * Built-in log levels available : SEVERE, WARNING, INFO, CONFIG, FINE, 
	 * FINER, FINEST
	 */
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	private String threadName;
	private OFMessageAsyncStream stream;
	private Thread t;
	
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/	
	public StreamHandler(String name, OFMessageAsyncStream strm){
		threadName = name;
		stream = strm;
	}
	
	//Method to send a single OFMessage
	protected synchronized void sendMsg(OFMessage msg) throws IOException {
		synchronized (stream) 
		{
			//Buffers a single OFMessage
			msg.computeLength();
			stream.write(msg);
			//Wakes up all threads that are waiting on this object's monitor.
			stream.notifyAll(); 
		}
	}
	
	//Method to send a group of OFMessages
	protected synchronized void sendMsg(List<OFMessage> l) throws IOException{
		
		synchronized (stream) 
		{
			for(OFMessage msg:l){
				msg.computeLength();
			}
			//Buffers a list of OFMessages
			stream.write(l);
			//Wakes up all threads that are waiting on this object's monitor.
			stream.notifyAll();
		}
			
	}
	
	public boolean isAlive(){
		return t.isAlive();
	}
	
	
	@Override
	public void run(){
		try {
		    /*Tests whether this thread has been interrupted. A thread 
		     * interruption ignored because a thread was not alive at the time 
		     * of the interrupt will be reflected by this method returning false. 
		     */
			while(!(t.isInterrupted())){
		    	Thread.sleep(0, 1); //(ms,ns); ownership not lost
		    	synchronized (stream) 
		    	{
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

	//Method to interrupt a StreamHandler Thread
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
	}
	
	//Method to allocate/instantiate a new StreamHandler Thread
	public void start (){
      LOGGER.info("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}
}
