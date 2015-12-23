import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.factory.BasicFactory;

public class Server implements Runnable{
	private Thread t;
	private String threadName;
	
	public Server(String t){
		this.threadName = t;
	}
	
	@Override
	public void run(){
		try{
			ServerSocketChannel listenSock = ServerSocketChannel.open();
			listenSock.configureBlocking(false);
		    listenSock.socket().bind(new java.net.InetSocketAddress(6000));
		    listenSock.socket().setReuseAddress(true);
		    
		    
			while(true){
			    
				BasicFactory factory = new BasicFactory();
			    SocketChannel sock = null;
			    while(sock==null){
			    	Thread.sleep(0,1);
			    	sock = listenSock.accept();
			    }
		        OFMessageAsyncStream stream = new OFMessageAsyncStream(sock, factory);
		        
		        vSwitch sw = new vSwitch("Switch_" + sock.getRemoteAddress(),stream,sock);
		        sw.start();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void stop(){
		t.interrupt();
	}
	
	public void start (){
      System.out.println("Starting " +  threadName);
      if (t == null){
         t = new Thread (this, threadName);
         t.start();
      }
   }
	
	
	
	public static void main(String args[]){
		Server srv = new Server("Server");
		srv.start();
		
		
	}
	    
}
	
	/*
	public static void main(String args[]){
		
		
		try {
			SimpleController test = new SimpleController(6000);
			test.run();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			SocketChannel swSocket = SocketChannel.open();
			swSocket.connect(new InetSocketAddress("192.168.1.203",6653));
			BasicFactory factory = new BasicFactory();
			
			OFMessageAsyncStream stream = new OFMessageAsyncStream(swSocket, factory);
			
 
			
			//List<OFMessage> l = new ArrayList<OFMessage>();
	        //l.add(factory.getMessage(OFType.HELLO));
	        //l.add(factory.getMessage(OFType.FEATURES_REQUEST));
	        //stream.write(l);
			
			OFMessage msg = factory.getMessage(OFType.HELLO);			
			//msg.setType(OFType.HELLO);
			//msg.setVersion((byte) 1);
			//msg.setXid(1);
			
			stream.write(msg);
			stream.flush();
			
			List<OFMessage> msgs = stream.read();
			
			
			//msg = msgs.get(0);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	*/
	
	
	
	
	
	//OVSDB Example
	
	/*
	public static void main(String args[]){
		
		String db = "Open_vSwitch";
		
		JSONArray arr =new JSONArray();
		arr.put(db);
		
		JSONObject jrpc = new JSONObject();
		jrpc.put("method","get_schema");
		jrpc.put("params",arr);
		jrpc.put("id",1);
		
		System.out.println(jrpc.toString());
		
		try{
			
			Socket swSocket = new Socket("192.168.1.203",6653);
			//Socket swSocket = new Socket("134.117.89.179",6653);
			
			
			
			BufferedInputStream inStream = new BufferedInputStream(swSocket.getInputStream());
			BufferedOutputStream outStream = new BufferedOutputStream(swSocket.getOutputStream());
			//PrintWriter outStream = new PrintWriter(swSocket.getOutputStream());
			//BufferedReader inStream = new BufferedReader(new InputStreamReader(swSocket.getInputStream()));
			
			byte[] b = jrpc.toString().getBytes();
			
			outStream.write(b,0,b.length);
			
			outStream.flush();
			
			
			
			byte[] buffer = new byte[32768];
			int bytesRead=0;

			ByteArrayOutputStream readBuffer = new ByteArrayOutputStream();
			
    		while((bytesRead = inStream.read(buffer)) != -1){
    			readBuffer.write(buffer,0,bytesRead);
    			if(buffer[bytesRead]==0){
    				break;
    			}
    		}
    		//System.out.println(new String(input));
    		
    		JSONObject swInput = new JSONObject(new String(readBuffer.toByteArray()));
			//JSONArray swInput = new JSONArray(new String(input));
    		System.out.println(swInput.toString());
    		System.out.println("");
    		System.out.println("");
    		System.out.println("");
    		
    		
    		
    		JSONObject res = new JSONObject(swInput.get("result").toString());
    		res = new JSONObject(res.get("tables").toString());
    		JSONArray out = res.names();
    		System.out.println(out.toString(1));
    		
    		
			inStream.close();
			outStream.close();
			swSocket.close();
		 } catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/

