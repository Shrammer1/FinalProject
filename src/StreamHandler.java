import java.io.IOException;
import java.util.List;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFMessage;


public class StreamHandler implements Runnable{
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
			e.printStackTrace();
		}	
	}
	
	protected synchronized void sendMsg(List<OFMessage> l){
		try {
			synchronized (stream) {
				stream.write(l);
				stream.notifyAll();
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void run(){
		try {
		    while(!(t.isInterrupted())){	
		    	synchronized (stream) {
		    		if(stream.needsFlush()) stream.flush();
		    		stream.notifyAll();
				}
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop(){
		t.interrupt();
		System.out.println("Stopping " +  threadName);
	}
	
	public void start (){
      System.out.println("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
	}

}
