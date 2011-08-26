package network;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import crack.Crack;

/**
 * This class constantly checks a file on a given website for a change.
 * 
 * @author derv
 * @version 1
 */
public class WebListener implements ActionListener {
	/** Crack object to use for cracking .cap files. */
	final Crack crack;
	
	/** URL to check for new .cap files to crack. */
	String url;
	
	/** Path to wordlist to use for cracking. */
	final String wordlist;
	
	/** Constructor
	 * 
	 * @param crack Crack object to use for cracking .cap files when needed.
	 * @param url Web address to check frequently for new.cap files to crack. */
	public WebListener(final Crack crack, final String url, final String wordlist) {
		this.crack    = crack;
		this.url      = url;
		this.wordlist = wordlist;
	}
	
	/** Change the location on the web server to check for new files. */
	public void setURL(final String url) { this.url = url; }
	
	/** While 'connected' to Server, this method executes in intervals.
	 * Checks this object's URL for new information (new .cap file to crack, cracked password, etc). */
	public void actionPerformed(final ActionEvent e) {
		final String cap_file = crack.working_dir + "cap.cap";
		try {
			final Update u = getUpdate();
			if (u.contains_cap) {
				// There is a .cap file to crack.
				
				// Download the .cap file
				if (download(u.cap_file_url, cap_file)) {
					crack.crack(cap_file, u.ssid, wordlist);
					new File(cap_file).delete();
				}
				
			}
		} catch (final IOException ioe) { 
			// File could not be retrieved.
			System.out.println("File could not be retrieved.");
		}
		
	}
	
	/** Retrieves the Update object from the web server.
	 * @return The 'update' object from the web server.
	 * @throws IOException If the object could not be retrieved.
	 */
	private Update getUpdate() throws IOException {
		final File update_file = new File(crack.working_dir + "update.txt");
		
		// First, download the update file locally
		if (!download(url, update_file.toString()) || update_file.length() == 0) {
			throw new IOException("Unable to retrieve new update.");
		}
		
		try {
			// De-serialize the object.
			System.out.println("LENGTH OF UPDATE FILE = " + update_file.length());
			final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(update_file));
			
			final Update result = (Update) ois.readObject();
			
			ois.close();
			
			// update_file.delete();
			
			return result;
			
		} catch (final MalformedURLException mue)   { mue.printStackTrace(); 
		} catch (final ClassNotFoundException cnfe) { cnfe.printStackTrace(); 
		} catch (final IOException ioe)             { ioe.printStackTrace(); }
		
		throw new IOException("Unable to retrieve new update.");
	}
	
	/** 
	 * Saves file from URL to a file (locally).
	 * 
	 * @param the_url Location of the file to download
	 * @param file Path and filename of where to save the file to.
	 * @return True if the download was successful, false otherwise.
	 */
	private boolean download(final String the_url, final String file) {
		
		try {
			final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			
			final URL url = new URL(the_url);
			final URLConnection urlc = url.openConnection();
			
			final InputStream is = urlc.getInputStream();
			
			byte[] buffer = new byte[1024];
			int bytes_read, bytes_written = 0;
			
			while ((bytes_read = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytes_read);
				bytes_written += bytes_read;
			}
			
			is.close(); os.close();
			return true;
			
		} catch (final MalformedURLException mue) { mue.printStackTrace(); 
		} catch (final FileNotFoundException fnfe) { fnfe.printStackTrace(); 
		} catch (final IOException ioe) { ioe.printStackTrace(); }
		
		return false;
	}

}
