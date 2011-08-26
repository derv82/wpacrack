package network;

import java.io.Serializable;

/**
 * Object containing all info needed by a client to crack a .cap file.
 * Also includes other information (constants) needed by the client.
 * 
 * All instance fields of this object are PUBLIC.
 * They can be accessed and altered by any object!
 * 
 * @author derv
 * @version 1
 */
public class Update implements Serializable {
	
	/** Default version UID so this class can be serializable. */
	private static final long serialVersionUID = 1L;
	
	/** Whether or not this update object contains info on a .cap file. */
	public boolean contains_cap;
	
	/** Location of the cap file on the server. */
	public String cap_file_url;
	
	/** SSID of the .cap file (needed for cracking. */
	public String ssid;
	
	/** ID of the client requesting the crack. */
	public int id;
	
	/** Amount of time (in ms) to wait between checking for new .cap files. */
	public int web_delay;
	
	/** Amount of time (in ms) to wait before reconnecting to the server. */
	public int update_delay;
	
	/** Default constructor, initializes .cap file information (ssid, id) to 'nulls'). */
	public Update() {
		cap_file_url = null;
		ssid = null;
		id = -1;
	}
	
	/**
	 * Specialized constructor. Initializes .cap file information.
	 * @param cap_url Location of the .cap file on the server.
	 * @param ssid SSID of the .cap file
	 * @param id ID of the client requesting the crack.
	 */
	public Update(final String cap_url, final String ssid, final int id) {
		cap_file_url = cap_url;
		this.ssid    = ssid;
		this.id      = id;
		contains_cap = true;
	}
	
}
