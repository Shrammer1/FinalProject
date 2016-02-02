package topology;

import java.nio.ByteBuffer;

public class TLV{
	private byte[] header = new byte[2];
	private byte[] data;
	private int len;
	private int type;
	
	/**
	 * Attempts to load the next TLV from a ByteBuffer. If successful the ByteBuffers position will be updated to the end of the TLV. If not the ByteBuffer will remain unchanged. 
	 * @param buffer The ByteBuffer to load from
	 * @return The TLV object
	 */
	public TLV(ByteBuffer buffer){
		
		byte type = 0;
		type = buffer.get();
		this.header[0] = type;
		type = (byte) (type & 0xFE);
		byte lenIn = 0;
		lenIn = buffer.get();
		this.header[1] = lenIn;
		this.len = (((type & 0x1)<<8) | lenIn & 0xff);
		this.type = type;
		this.data = new byte[this.len];
		buffer.get(this.data, 0, this.len);
		
	}
	
	
	
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

	public TLV(TLV nextTLV) {
		this.data = nextTLV.data;
		this.header = nextTLV.header;
		this.len = nextTLV.len;
		this.type = nextTLV.type;
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


	public int getLength() {
		return len;
	}

	public int getType() {
		return type;
	}
	
	public byte[] getData(){
		return this.data;
		}
		
}
	
	
