package controller;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.openflow.io.OFMessageAsyncStream;
import org.openflow.protocol.factory.BasicFactory;

/*
 * Server runnable class
 */
public class Server implements Runnable{

	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	 /* Built-in log levels available : SEVERE, WARNING, INFO, CONFIG, FINE, 
	 * FINER, FINEST
	 */
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");
	
	/*
	 * For RMI implementation
	 */
	private Registry reg;
	private int port;
	private Thread t;
	private String threadName;
	
	/*
	 * boolean variable to control Layer 2 behavior
	 */
	private boolean l2_learning;
	
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	public Server(String t, int prt){
		this.threadName = t;
		this.port = prt;
		this.l2_learning=false;
	}
	public Server(String t, int prt,boolean l2_learning){
		this.threadName = t;
		this.port = prt;
		this.l2_learning=l2_learning;
	}
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	
	/*
	 * Server listens on port 6001
	 */
	@Override
	public void run(){
		
		try{
			ServerSocketChannel listenSock = ServerSocketChannel.open();
			listenSock.configureBlocking(false);
		    listenSock.socket().bind(new java.net.InetSocketAddress(6001));
		    listenSock.socket().setReuseAddress(true);
		    
		    /*
		     * Instantiating and Starting the switch handler
		     */
		    SwitchHandler swhl = new SwitchHandler(threadName + "_Main_SwitchHandler",threadName,reg,l2_learning);
		    swhl.start();
		    
		    /*
		     * Always running and listening for incoming Asynchronous OFMessages
		     */
			while(true){
				BasicFactory factory = new BasicFactory();
			    SocketChannel sock = null;
			    while(sock==null){
			    	Thread.sleep(0,1);
			    	sock = listenSock.accept();
			    }
		        swhl.addSwitch(sock,new OFMessageAsyncStream(sock, factory));
		        
		        
			}
		}
		catch(Exception e){
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	
	
	/*
	 * Method to stop a Thread of a Server
	 */
	public void stop(){
		t.interrupt();
	}
	
	/*
	 * Method to start a Thread of a Server
	 */
	public void start (){
		LOGGER.setLevel(Level.FINEST);
		try {
			/*
			 * Creating a file to store logs
			 */
			FileHandler fh = new FileHandler("Logs/OVS-Controller.log");
			//SimpleFormatter sf = new SimpleFormatter();
			//fh.setFormatter(sf);
			
			fh.setFormatter(new LogFormatter());
			LOGGER.addHandler(fh);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		/*
		 * Informing that Thread has started. Server Thread ready and listening
		 */
		LOGGER.info("Starting " +  threadName);
		/*
		 * Part of the RMI implementation on the server
		 */
		try {
			reg = LocateRegistry.createRegistry(port);
			LOGGER.info("RMI Registry created on port: " + port);
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
		/*
		 * If the Thread does not exist, then create one
		 */
		if (t == null){
			t = new Thread (this, threadName);
			t.start();
		}
   }
	
	
	/*
	 * Main method to be used on CLI with additional arguments for extra
	 * functionality. L2 behavior argument option written by Wesley Cooper.
	 */
	public static void main(String args[]){
		boolean l2_learning=false;
		/*
		 * Parsing CLI arguments 
		 */
		if(args.length<1){
			System.err.println(printUsage());
			System.exit(1); //0=OK; 1=ERROR; -1=EXCEPTION
		}
		//this is going to have to be changed before the final version to incorporate all options
		if(args.length==2){
			if(args[1].equals("-l2")){
				l2_learning = true;
			}
			else{
				System.err.println(printUsage());
				System.exit(1); //0=OK; 1=ERROR; -1=EXCEPTION
			}
		}
		/*
		 * Port 1099 for RMI registry functionality
		 */
		Server srv = new Server(args[0], 1099,l2_learning);
		
		/*
		 * Starting a Server Thread
		 */
		srv.start();
		
		
	}
	
	/**************************************************
	 * PRIVATE METHODS
	 **************************************************/
	private static String printUsage(){
		return "Usage: THISFILENAME <Server_Name>\nOptions:\n\n-l2\tBuilt in standard layer 2 learning";
	}
	
	
	/**************************************************
	 * PRIVATE CLASS
	 **************************************************/
	/*
	 * Entire private class to be used for logs formatting. 
	 */
	private class LogFormatter extends Formatter
	{
	    SimpleDateFormat dateFormatter = new SimpleDateFormat ("MM/dd/yy '-' HH:mm:ss a");

	    public String format(LogRecord record)
	    {
	        StringBuffer buffer = new StringBuffer();
	        String timeString = dateFormatter.format(new Date(record.getMillis()));
	        buffer.append("Level: " + record.getLevel().toString() + " - ");
	        buffer.append("Time: " + timeString  + " | ");
	        buffer.append(record.getMessage() + "\n");
	        return buffer.toString();
	    }
	}    	
}
	
	/**************************************************
	 * UNUSED CODE ---TESTING---
	 **************************************************/

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

