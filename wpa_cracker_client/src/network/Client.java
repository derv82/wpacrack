package network;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;

import javax.swing.Timer;

import range.Range;

/**
 * Model for Client.
 * 
 * @author derv
 * @version 1
 */
public class Client extends Observable {
	/** IP address of server. */
	private final String ip_address;
	
	/** Port number of the server. */
	private final int port_num;
	
	/** This client's unique ID. Used by Server. */
	private int id;
	
	/** Range of objects this client is supposed to crack. */
	private List<Range> range;
	
	/** Path to the word-list (containing contents of 'range'). */
	private String path_to_wordlist;
	
	/** Working directory for files. */
	public final String working_dir;
	
	/** Flag for whether this client is "connected" or "disconnected" from the server. */ 
	private boolean connected = false;
	
	/** Timer that checks a web server for new .cap files to crack. */
	private Timer web_timer;
	
	/** HTTP address that the updated .txt file will be uploaded to. */
	private String web_address;
	
	/** Timer that checks in with the Server every so often. */
	private Timer update_timer;
	
	/** Constructor, sets instance variables and calls superclass. */
	public Client(final String ip, final int port) { 
		super(); 
		this.ip_address = ip;
		this.port_num   = port;
		
		final String sep = System.getProperty("file.separator");
		this.working_dir = System.getProperty("user.home") + sep + "wpa_client" + sep;
	}
	
	// ACCESSORS
	
	/** @return The IP address of the server. */
	public String getIP()         { return ip_address; }
	
	/** @return The port number of the server. */
	public int getPort()          { return port_num; }
	
	/** @return This client's unique ID. */
	public int getID()            { return id; }
	
	/** @return True if we are "connected" to the server, false otherwise. */
	public boolean getConnected() { return connected; }
	
	/** @return Web address (http) to check for new .cap files to crack (along w/ other information). */
	public String getWebAddress() { return web_address; }
	
	/** @return Amount of time to wait (in ms) before checking web_address again. */
	public int getWebDelay()      { return web_timer.getDelay(); }
	
	/** @return The Timer which checks for new .cap files on the web server. */
	public Timer getWebTimer()    { return web_timer; }
	
	/** @return Amount of time to wait (in ms) before 'updating' with the Server again. */
	public int getUpdateDelay()   { return update_timer.getDelay(); }
	
	/** @return The list of password ranges this client will use to crack .cap files. */
	public List<Range> getRange() { return range; }
	
	/** @return Path to the word-list file that this client is supposed to run cracks against. */
	public String getWordlist()   { return this.path_to_wordlist; }
	
	/** @return String representation for this object; the values of this Client's instance variables. */
	public String toString() { 
		return "<Client, " + 
				  "ip="    + ip_address + 
				", port="  + port_num + 
				", id="    + id + 
				", range=" + range + 
				", connected=" + connected +
				", web_address=" + web_address +
				", web_delay=" + web_timer.getDelay()  +
				", update_delay=" + update_timer.getDelay() + ">";
	}
	
	
	// MUTATORS
	
	/** @param id This client's new unique ID. */
	public void setID(final int id) { this.id = id; }
	
	/** @param t Timer to change this Client object's web-update timer to. */
	public void setWebTimer(final Timer t) { this.web_timer = t; }
	
	public void setWebDelay(final int delay) { web_timer.setDelay(delay); }
	
	/** @param t Timer to change this Client object's Server-update timer to. */
	public void setUpdateTimer(final Timer t) { this.update_timer = t; }
	
	public void setUpdateDelay(final int delay) { update_timer.setDelay(delay); }
	
	/** @param connected true if we are "connected" to the server, false otherwise. */
	public void setConnected(final boolean connected) {
		this.connected = connected;
		
		// Appropriately start/stop the HTTP .cap file listener
		// Also, start/stop the "updater" that phones-home to the server every-so-often.
		
		if (connected) {
			web_timer.start();    
			update_timer.start();
		} else {
			web_timer.stop();
			update_timer.start();
		}
	}
	
	/** @param url The url to check frequently for the next .cap file to crack (and other information). */
	public void setWebAddress(final String url) { 
		this.web_address = url;
		WebListener wl = (WebListener) web_timer.getActionListeners()[0];
		wl.setURL(url);
	}
	
	/** @param r The list of ranges this client will use to crack future .cap files. */
	public void setRange(final List<Range> r) { 
		this.range = r;
		generateWordlist();
	}
	
	/** 
	 * Helper method for setRange().
	 * Creates a single file containing all possible words within the ranges of "range".
	 * Also updates the path to the word-list for this Client.
	 */
	private void generateWordlist() {
		final File f = new File(working_dir, "wordlist.txt");
		if (f.exists()) f.delete();
		
		try {
			for (final Range r : range) { 
				r.generateFile(f.toString(), true);
			}
		} catch (IOException ioe) { ioe.printStackTrace(); }
		this.path_to_wordlist = f.toString();
	}
	
	
	// SERVER COMMANDS
	
	/** Initial exchange with Server. Receive new ID, Range, etc. */
	public void connect(final int benchmark) throws IOException {
		final Packet p = new Packet(Packet.REQUEST_CONNECT);
		
		// On first connect, we only need to send this machine's benchmark.
		p.setBenchmark(benchmark);
		
		final ClientConnect sess = new ClientConnect(this, p, "");
		while (sess.isAlive()) { }
	}
	
	/** Update server so we don't get removed from list of clients. */
	public void update() throws IOException {
		final Packet p = new Packet(Packet.REQUEST_UPDATE);
		
		// Updating requires giving the server our current Range and ID (for verification purposes).
		p.setRange(this.range);
		p.setID(this.id);
		
		final ClientConnect sess = new ClientConnect(this, p, "");
		while (sess.isAlive()) { }
	}
	
	/** Send a .cap file to server, including SSID. 
	 * @param ssid SSID of the access point in the .cap file we are to crack.
	 * @param cap_file Path to .cap file to upload and crack. */
	public void crack(final String ssid, final String cap_file) throws IOException {
		final Packet p = new Packet(Packet.REQUEST_CRACK);
		
		// Requesting a crack requires the SSID, cap file, and our ID.
		p.setID(this.id);
		p.setRange(this.range);
		p.setSSID(ssid);
		
		final ClientConnect sess = new ClientConnect(this, p, cap_file);
		while (sess.isAlive()) { }
	}
	
	/**
	 * Reports a cracked password to the Server.
	 * @param id ID of the client requesting the crack.
	 * @param password Password of the client requesting the crack.
	 * @param ssid SSID of the client requesting the crack.
	 */
	public void cracked(final int id, final String password, final String ssid) {
		final Packet p = new Packet(Packet.REQUEST_CRACKED);
		p.setSSID(ssid);
		p.setCrackedID(id);
		p.setPassword(password);
		final ClientConnect sess = new ClientConnect(this, p, "");
		while (sess.isAlive()) { }
	}
	
	/**
	 * Tells the Server we are disconnecting.
	 * @throws IOException If we are unable to tell the server. */
	public void disconnect() throws IOException {
		final Packet p = new Packet(Packet.REQUEST_DISCONNECT);
		p.setID(this.id);
		p.setRange(this.range);
		final ClientConnect sess = new ClientConnect(this, p, "");
		while (sess.isAlive()) { }
	}
	
	/** Notify any observers of this object. */
	public void observerMessage(final String msg) {
		setChanged();
		this.notifyObservers(msg);
	}
	
}
