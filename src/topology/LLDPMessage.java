package topology;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class LLDPMessage {
	
	private byte[] fcs = new byte[4];
	private byte[] dstMac;
	private byte[] srcMac;
	private byte[] ethertype;
	private byte[] chasisID;
	private byte[] portID;
	private byte[] ttl;
	private byte[] eof;
	private byte[] preamble;
	private int port;
	private String switchID;
	
	public LLDPMessage(String switchID, int port){
		this.port = port;
		this.switchID = switchID;
	}
	
	/**
	 * 
	 * @return Returns the length of the LLDP message in bytes
	 */
	public int length(){
		int retval = 0;
		retval +=preamble.length;
		retval +=dstMac.length;
		retval +=srcMac.length;
		retval +=ethertype.length;
		retval +=chasisID.length;
		retval +=portID.length;
		retval +=ttl.length;
		retval +=eof.length;
		return retval;
	}
	
	public byte[] getMessage(){
		ByteBuffer preamble = ByteBuffer.allocate(7);
		preamble.put((byte) 0x55);
		preamble.put((byte) 0x55);
		preamble.put((byte) 0x55);
		preamble.put((byte) 0x55);
		preamble.put((byte) 0x55);
		preamble.put((byte) 0x55);
		preamble.put((byte) 0x5D);
		preamble.rewind();
		this.preamble = preamble.array();
		
		
		
		ByteBuffer dstMac = ByteBuffer.allocate(6);
		dstMac.put((byte) 0x01);
		dstMac.put((byte) 0x80);
		dstMac.put((byte) 0xc2);
		dstMac.put((byte) 0x00);
		dstMac.put((byte) 0x00);
		dstMac.put((byte) 0x00);
		dstMac.rewind();
		this.dstMac = dstMac.array();
		
		
		
		//16-0A-64-1A-95-B9 TEST MAC ADDR
		
		ByteBuffer srcMac = ByteBuffer.allocate(6);
		srcMac.put((byte) 0x16);
		srcMac.put((byte) 0x0a);
		srcMac.put((byte) 0x64);
		srcMac.put((byte) 0x1a);
		srcMac.put((byte) 0x95);
		srcMac.put((byte) 0xb9);
		srcMac.rewind();
		this.srcMac = srcMac.array();
		
		//Ethertype = 0x88CC
		
		ByteBuffer ethertype = ByteBuffer.allocate(2);
		ethertype.put((byte) 0x88);
		ethertype.put((byte) 0xcc);
		ethertype.rewind();
		this.ethertype = ethertype.array();
		
		
		TLV chasisID = new TLV((byte) 1,switchID.getBytes());
		TLV portID = new TLV((byte) 2, ByteBuffer.allocate(4).putInt(port).array());
		TLV ttl = new TLV((byte) 3,new byte[]{(byte) 120});
		
		this.chasisID = chasisID.toArray();
		this.portID = portID.toArray();
		this.ttl = ttl.toArray();
		
		TLV eof = new TLV((byte) 0);
		this.eof = eof.toArray();
		
		
		//make sure to allocate as much space as needed and then put all the other messages into a single buffer
		ByteBuffer outMsg = null;
		if(this.length()<60){
			outMsg = ByteBuffer.allocate(60);
		}
		else{
			outMsg = ByteBuffer.allocate(this.length());
		}
		outMsg.put(preamble);
		outMsg.put(dstMac);
		outMsg.put(srcMac);
		outMsg.put(ethertype);
		outMsg.put(this.chasisID);
		outMsg.put(this.portID);
		outMsg.put(this.ttl);
		outMsg.put(this.eof);
		
		
		
		//rewind the buffer
		outMsg.rewind();
		
		
		
		//create new CRC32 object
		CRC32 crc = new CRC32();
		crc.reset();
		//update the CRC object to calculate the crc code
		crc.update(outMsg.array(),0,outMsg.array().length);
		//get the value of the crc code for the fcs
		
		
		
		//int crc = calculateFCS(outMsg.array());
		
		
		//fcs is the reverse of the crc cause thats how ethernet works for some reason...
		//fcs = ByteBuffer.allocate(4).putInt(Integer.reverse((int) crc.getValue())).array();
		
		//ByteBuffer tst = ByteBuffer.allocate(4).putInt(Integer.reverse((int) crc.getValue()));
		//tst.order(ByteOrder.LITTLE_ENDIAN);
		fcs = ByteBuffer.allocate(4).putInt((int) crc.getValue()).array();
		
		ByteBuffer finalMsg = ByteBuffer.allocate(outMsg.array().length + fcs.length);
		finalMsg.put(outMsg.array());
		finalMsg.put(fcs);
		
		
		return finalMsg.array();
	}	
	
	private class TLV{
		private byte[] header = new byte[2];
		private byte[] data;
		private int len;
		
		public TLV(byte type, byte[] data){
			this.data = data;
			len = data.length;
			if(len>511 || type > 127 || type < 0 ){
				throw new IllegalArgumentException();
				
			}
			if(len > 255){
				header[0] = (byte) ((byte) (type << 1) | (byte) (1 & 0x01));
				header[1] = (byte) (((byte)len) & 0xff);
			}
			else{
				header[0] = (byte) ((byte) (type << 1));
				header[1] = (byte) (((byte)len) & 0xff);
			}
		}
		
		public TLV(byte type) {
			len = 0;
			if(len>511 || type > 127 || type < 0 ){
				throw new IllegalArgumentException();
				
			}
			if(len > 255){
				header[0] = (byte) ((byte) (type << 1) | (byte) (1 & 0x01));
				header[1] = (byte) (((byte)len) & 0xff);
			}
			else{
				header[0] = (byte) ((byte) (type << 1));
				header[1] = (byte) (((byte)len) & 0xff);
			}
		}

		public byte[] toArray(){
			ByteBuffer res = null;
			if(data==null){
				res = ByteBuffer.allocate(header.length);
				res.put(header);
			}
			else{
				res = ByteBuffer.allocate(header.length + data.length);
				res.put(header);
				res.put(data);
			}
			return res.array();
		}
		
	}
	
	
}
