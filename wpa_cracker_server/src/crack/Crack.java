package crack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ftp.FTP;

/**
 * Thread which checks for new .cap files in the given CrackQueue.
 * When new .cap files are found, they are uploaded to the server via FTP,
 * This class then generates 'update.txt' which tells the clients 1) the location of the cap file,
 * 2) the SSID of the .cap file, and 3) the ID of the client requesting the crack.
 * 
 * @author derv
 * @version 1
 */
public class Crack extends Thread {
	
	/** Name of the file to upload to the FTP server. */
	private static final String update_file = "update.txt";
	
	/** List of .cap files we are to 'crack'. */
	private final CrackQueue crack_queue;
	
	/** FTP user name. */
	private String ftp_user;
	/** FTP password. */
	private String ftp_pass;
	/** FTP server (domain). */
	private String ftp_server;
	/** HTTP server (where files end up after being uploaded via FTP. */
	private String web_server;
	
	/** Amount of time to wait (in milliseconds) between each .cap file crack. */
	private int crack_wait_time = 1000 * 60 * 4;
	
	/** How frequently clients check the web server for a new file. */
	private int cap_wait_time = 60 * 1000;
	
	/** The last time (in epoch milliseconds) since the clients were notified of a new file. */
	private long last_updated = 0L;
	
	/** Flag to tell the thread to cleanly exit. */
	private boolean stop = false;
	
	/**
	 * Starts new thread which constantly checks CrackQueue cq for new CapFile objects.
	 * When files are found, removes files from CrackQueue, uploads the cap file to the ftp site,
	 * and updates the 'update.txt' file to point to the new cap file (including cap file information).
	 * 
	 * @param cq CrackQueue list we will be removing CapFile objects from.
	 * @param wait_time Amount of time to wait for clients to finish (in milliseconds).
	 * @param client_web_delay Amount of time the clients wait before checking for new update file.
	 * 
	 * @param ftp_user FTP user login
	 * @param ftp_pass FTP login password
	 * @param ftp_server FTP server (domain name) to log into
	 * @param web_server FULL PATH to where files will be stored (http://domain.name/more/paths/if/necessary/)
	 */
	public Crack(final CrackQueue cq, final int wait_time, final int client_web_delay,
					final String ftp_user, final String ftp_pass, 
					final String ftp_server, final String web_server) { 
		super();
		
		this.crack_queue     = cq;
		this.crack_wait_time = wait_time;
		this.cap_wait_time   = client_web_delay;
		
		this.ftp_user   = ftp_user;
		this.ftp_pass   = ftp_pass;
		this.ftp_server = ftp_server;
		this.web_server = web_server;
		
		remove_update(new FTP(ftp_user, ftp_pass, ftp_server));
		
		this.start(); // Start the thread!
	}
	
	/** This method stops the Crack thread.
	 * The method will not return until the thread is ready to close. */
	public void exit() { 
		this.stop = true;

		remove_update(new FTP(ftp_user, ftp_pass, ftp_server));
		
		while (this.stop) { } // wait for run() loop to finish
	}
	
	/** Checks crack_queue for new CapFile objects. 
	 * When found, uploads to cap file to server, updates update.txt to point to the new file (and include information).
	 * Waits specified wait_time until moving onto the next CapFile in crack_queue.
	 */
	public void run() {
		// Loops until the method exit() is called.
		while (!stop) {
			
			// Loop until there is something in the queue to crack AND we are past the wait_time limit.
			if (!crack_queue.isEmpty() && System.currentTimeMillis() - last_updated > crack_wait_time) {
				CapFile cp = crack_queue.remove();
				System.out.println("There's... SOMETHING ON THE WING\n" + cp);
				
				// upload current file.
				final FTP ftp = new FTP(ftp_user, ftp_pass, ftp_server);
				if (!ftp.upload_v2(cp.getFilename(), "cap.cap")) {
					System.err.println("Upload failed! File '" + cp.getFilename() + "' was not uploaded properly.");
					continue;
				}
				
				// change update.txt to include the file location, SSID, id, and other .cap file info.
				if (update(cp, ftp)) {
					last_updated = System.currentTimeMillis();
				}
				
				// Wait for 5 seconds longer than the client's refresh time (To account for straggler's).
				try {
					Thread.sleep(cap_wait_time + (5 * 1000));
				} catch (final InterruptedException ie) { ie.printStackTrace(); this.stop = true; }
				
				// Remove the update file (since every client who will read it has already read it)
				remove_update(ftp);
				System.out.println("Removed update.txt");
			}
		}
		this.stop = false;
	}
	
	/** 
	 * Generates 'update.txt' and uploads it to the server.
	 * @param cp The cap file to upload to the server.
	 * @return True if upload is successful, false otherwise.
	 */
	private boolean update(final CapFile cp, final FTP ftp_session) {
		
		// Delete the update file if it already exists
		File f = new File(update_file);
		if (f.exists())
			f.delete();
		
		// Write a new update file
		try {
			final FileWriter fw = new FileWriter(f);
			
			// Line 1: Location of cap file on server.
			fw.write(web_server + "cap.cap\n");
			// Line 2: SSID of the access point in the cap file.
			fw.write(cp.getSSID() + "\n");
			// Line 3: ID of the client who requested the crack.
			fw.write(cp.getID());
			
			fw.flush();
			fw.close();
			
			// Upload the file
			ftp_session.upload_v2(update_file, update_file);
			
		} catch (final IOException ioe) { ioe.printStackTrace(); return false; }
		
		return true;
	}
	
	/** Writes a blank file (null) to the location of the update file on the web server.
	 * @param ftp_session Current FTP session to 'remove' the update file from.
	 */ 
	public void remove_update(final FTP ftp_session) {
		// Delete the update file if it already exists
		final File f = new File(update_file);
		if (f.exists())
			f.delete();
		
		try {
			final FileWriter fw = new FileWriter(f, false);
			fw.write("");
			fw.flush();
			fw.close();
			
			ftp_session.upload_v2(update_file, update_file);
		} catch (IOException ioe) { ioe.printStackTrace(); return; }
	}
	
	/** Returns the last time this object tried to crack a CapFile. */
	public long getLastUpdated() { return last_updated; }
}
