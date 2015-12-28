import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * 
 * @author Nick
 *
 *This class is to be used for handling all the switches run on the controller. It will track all the switches and will be able to return the instance of a switch that has a requested switchID
 *
 *
 */
public class SwitchHandler implements Runnable{
	private final static Logger LOGGER = Logger.getLogger("Controller_LOGGER");	
	
	private String threadName;
		private Thread t;
		private ArrayList<OVSwitch> switches = new ArrayList<OVSwitch>();
		
		public SwitchHandler(String name) {
			threadName = name;
		}	
		
		
		private void abort(){
			stop();
		}			
		
		public synchronized void addSwitch(OVSwitch sw){
			synchronized (switches) {
				switches.add(sw);
				switches.notifyAll();
			}
		}
		public synchronized OVSwitch getSwitch(long switchID){
			OVSwitch sw = null;
			synchronized (switches) {
				for(int i = 0; i<switches.size();i++){
					if((sw = switches.get(i)).switchID == switchID) return sw;
				}
				switches.notifyAll();
			}
			return null;
		}
		
		
		
		@Override
		public void run(){
			while(!(t.isInterrupted())){
				
			}
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