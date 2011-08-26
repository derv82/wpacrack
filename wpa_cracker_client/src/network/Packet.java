package network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import range.Range;

/**
 * Object representing a packet sent to/from the client and server.
 * All information needed of the client and the server are contained in this class.
 * 
 * @author derv
 * @version 1
 */
public class Packet implements Serializable {
	/** Needs to be serializable so that we can send it via sockets. */
	private static final long serialVersionUID = 1L;
	
	/** Request types. */
	public static final int REQUEST_SERVER     = 0; // Only the server can send these
	public static final int REQUEST_CONNECT    = 1; // 1-5 are used by clients
	public static final int REQUEST_UPDATE     = 2;
	public static final int REQUEST_CRACK      = 3;
	public static final int REQUEST_CRACKED    = 4;
	public static final int REQUEST_DISCONNECT = 5;
	
	private int requestType; // will be one of the above request types
	
	/** Used for encryption/verification. */
	private String msg = "";
	private String key = null;
	
	/** Unique id for this client. */
	private int id = -1;
	
	/** Benchmark of client (number of crack attempts per minute). */
	private int benchmark;
	
	/** Range of pass phrases specific to this client. */
	private List<Range> range = null;
	
	/** SSID of access point we are cracking.
	 * Could also be the SSID of the *cracked* network. 
	 * Every wifi AP has an SSID (linksys, NETGEAR). */
	private String ssid = null;
	
	/** Password of a cracked .cap file. */
	private String cracked_password;
	
	/** Unique client ID corresponding to a cracked .cap file. */
	private int cracked_id;
	
	/** Web address to check for new .cap files to crack (and other relevant information). */
	private String web_address;
	
	/** Amount of time (ms) before checking web_address again. */
	private int web_delay;
	
	/** Amount of time (ms) before reconnecting to the Server to "update". */
	private int update_delay;
	
	/** Create new packet of type requestType
	 * @param requestType The type of packet to create (from Packet.SESSION_* constants). */
	public Packet(final int requestType) {
		this.requestType = requestType;
	}
	
	/** Create new packet of type requestType and a specific message.
	 * @param requestType The type of packet to create (from Packet.SESSION_* constants).
	 * @param msg The String message to set. */
	public Packet(final int requestType, final String msg) {
		this.requestType = requestType;
		this.msg = msg;
	}
	
	
	// ACCESSORS
	
	public String toString()      { return this.msg; }
	public String getKey()        { return this.key; }
	public String getSSID()       { return this.ssid; }
	public int getRequest()       { return this.requestType; }
	public int getID()            { return this.id; }
	public int getBenchmark()     { return this.benchmark; }
	public int getCrackedID()     { return this.cracked_id; }
	public int getUpdateDelay()   { return this.update_delay; }
	public int getWebDelay()      { return this.web_delay; }
	public String getPassword()   { return this.cracked_password; }
	public String getWebAddress() { return this.web_address; }
	public List<Range> getRange() { return this.range; }
	
	
	// MUTATORS
	
	public void setMessage(final String m)    { this.msg  = m; }
	public void setKey(final String k)        { this.key  = k; }
	public void setSSID(final String s)       { this.ssid = s; }
	public void setRequest(final int req)     { this.requestType = req; }
	public void setID(final int id)           { this.id   = id; }
	public void setBenchmark(final int b)     { this.benchmark = b; }
	public void setCrackedID(final int id)    { this.cracked_id = id; }
	public void setUpdateDelay(final int d)   { this.update_delay = d; }
	public void setWebDelay(final int d)      { this.web_delay = d; }
	public void setPassword(final String pw)  { this.cracked_password = pw; }
	public void setWebAddress(final String w) { this.web_address = w; }
	public void setRange(final List<Range> r) { this.range = r; }
	
	
	// OTHER METHODS
	
	/** @return All of the bytes contained in 'file' */
	public static byte[] setBytes(final String file) throws IOException {
		final File f = new File(file);
		if (f.length() > Integer.MAX_VALUE) 
			throw new IOException("File size too large:\n\n" + file + " is of size " + f.length());
		
		byte[] result = new byte[(int) f.length()];
		
		InputStream is = new FileInputStream(file);
		
		int offset = 0;
	    int numRead = 0;
	    while (offset < result.length
	           && (numRead=is.read(result, offset, result.length - offset)) >= 0) {
	        offset += numRead;
	    }
	    
	    if (offset < result.length) {
	        throw new IOException("Could not completely read file " + f.getName() + 
	        		"\n\nExpected " + result.length + " bytes, found " + offset);
	    }
	    
	    return result;
	}
	
	/** Saves contents of specified 'bytes' array to specified file. */
	public static void writeBytesToFile(final String file, final byte[] bytes) {
		try {
			final FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
}
