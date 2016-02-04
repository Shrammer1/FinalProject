package controller;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFHello;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.hello.OFHelloElement;
import org.openflow.protocol.hello.OFHelloElementVersionBitmap;

import topology.TopologyMapper;

/**
 * 
 * @author Nicholas Landriault
 *
 *The reason why this class exists is so that we can create multiple new 
 *switches rapidly and have each switch be created by a separate 
 *thread as each time a switch is created the thread must wait for the 
 *switch to send the OFFeaturesReply message to get the switchID
 *
 */
public class SwitchSetup implements Runnable{
	
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	private String threadName;
	private Thread t;
	private OFMessageAsyncStream stream;
	private SocketChannel sock;
	private SwitchHandler swhl; 
	private String swName;
	private TopologyMapper topo;
	
	//Constructor
	public SwitchSetup(String name,String swName, SocketChannel sock, 
			OFMessageAsyncStream stream, TopologyMapper topo, SwitchHandler swhl) 
	{
		threadName = name;
		this.stream = stream;
		this.sock = sock;
		this.swhl = swhl;
		this.swName = swName;
		this.topo = topo;
		this.start();
	}	
	
	//Method to abort a Thread of Switch Setup
	private void abort(){
		stop();
	}			
	
	/*
	 * Method to obtain an entire OFFeaturesReply message from the
	 * corresponding stream read.
	 */
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
		/*
		 * Obtains OFMessages and writes them onto the stream of 
		 * OFMessageAsync.
		 */
		try {
			List<OFMessage> l = new ArrayList<OFMessage>();
			OFHello helloMsg = (OFHello) stream.getMessageFactory().getMessage(OFType.HELLO);
			List<OFHelloElement> helloElements = new ArrayList<OFHelloElement>();
	        OFHelloElementVersionBitmap hevb = new OFHelloElementVersionBitmap();
	        List<Integer> bitmaps = new ArrayList<Integer>();
	        bitmaps.add(0x10);
	        hevb.setBitmaps(bitmaps);
	        helloElements.add(hevb);
	        helloMsg.setHelloElements(helloElements);
			
			l.add(helloMsg);
	        l.add(stream.getMessageFactory().getMessage(OFType.FEATURES_REQUEST));
	        stream.write(l);
	        
	        //If the stream was used for transfer messages, then clean it
	        while(stream.needsFlush()){
	        	stream.flush();
	        }
	        
	        /*
	         * Start switch Thread/Stream. If it does not exist or never
	         * registered or associated before it creates a new object
	         * and initializes it. If it existed before it gets restarted.
	         */
	        OFFeaturesReply fr = getFeaturesReply();
	        sw = swhl.getSwitch("0000000000000000".substring(Long.toHexString(fr.getDatapathId()).length())+ Long.toHexString(fr.getDatapathId()));
	        if(sw==null){
	        	sw = new OVSwitch(swName + "_Switch_" + sock.getRemoteAddress(),"0000000000000000".substring(Long.toHexString(fr.getDatapathId()).length())+ Long.toHexString(fr.getDatapathId()),stream,sock,fr,30,topo,swhl.getL2_Learning());
	        }
	        else{
	        	sw.restart(sock,stream,fr);
	        	return;
	        }
	        
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
        sw.start();
        swhl.addSwitch(sw);
        this.abort();
	}
	
	//Method to stop/interrupt a Thread of SwitchSetup
	public void stop(){
		t.interrupt();
		LOGGER.info("Stopping " +  threadName);
	}
	
	/*
	 * Method to start a Thread of SwitchSetup. If the Thread does not exist
	 * it creates a new one.
	 */
	public void start(){
      LOGGER.info("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}