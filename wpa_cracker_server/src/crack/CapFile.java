package crack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Object containing information about a CAP file to be cracked.
 * This information includes the SSID, client ID, and filepath to file (locally).
 * 
 * @author derv
 * @version 1
 */
public class CapFile {
	
	/** SSID of the access point. */
	private String ssid;
	
	/** Filename to the .cap file (local). */
	private String filename;
	
	/** ID # of the client which is requesting the crack. */
	private int id;
	
	/**
	 * Sets instance field variables.   
	 * Also writes 'bytes' to a (local) file and strips handshake (if applicable).
	 * 
	 * @param ssid
	 * @param bytes
	 * @param id
	 */
	public CapFile(String ssid, byte[] bytes, int id) {
		this.ssid = ssid;
		this.filename = generateFilename();
		
		try {
			final OutputStream out = new FileOutputStream(filename);
			out.write(bytes);
			out.close();
			
			stripHandshake();
		} catch (final IOException ioe) { ioe.printStackTrace(); }
		
	}
	
	/** @return SSID corresponding to this .cap file. */
	public String getSSID()     { return this.ssid; }
	/** @return ID corresponding to the client who uploaded this .cap file. */
	public int getID()          { return this.id; }
	/** @return Path & filename pointing to the .cap file (locally). */
	public String getFilename() { return this.filename; }
	
	/** 
	 * Helper method for creating a new filename for a .cap file that is not in use.
	 * @return The filename (with path removed) of this object's "filename". */
	private String generateFilename() {
		String dir = "cap_files" + System.getProperty("file.separator");
		new File(dir).mkdirs();
		
		int i = 0;
		while (new File(dir + ++i + ".cap").exists()) { }
		
		return dir + i + ".cap";
	}
	
	/** Removes all packets except the handshakes from this object's local file. */
	private void stripHandshake() {
		
		final Runtime rt = Runtime.getRuntime();
		try {
			// TODO Platform check (windows/linux)
			rt.exec("C:\\Program Files\\Wireshark\\tshark.exe -r " + filename + " -R " + 
					"\"eapol || wlan.fc.type subtype == 0x08\" -w " + filename + "_temp");
			
			if (new File(filename + "_temp").exists()) {
				
				// t-shark creatd a new cap file. replace the old one
				new File(filename).delete();
				new File(filename + "_temp").renameTo(new File(filename));
			} else {
				// Tshark did not generate an output file
			}
			
		} catch (IOException ioe) { ioe.printStackTrace(); return; }
	}
	
	/** @return String representation of this object. */
	public String toString() {
		return "SSID='" + ssid + "'; FILE='" + filename + "'; id=" + id + "";
	}
}
