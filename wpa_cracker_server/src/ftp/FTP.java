package ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import network.Update;

import org.apache.commons.net.ftp.FTPClient;

/** 
 * FTP file uploader class.
 * 
 * Only supports uploading individual files at a single time.
 * 
 * @author derv
 * @version 1
 */
public class FTP {
	
	/** Login string for logging into the ftp site. */ 
	private final String ftp_login;
	
	private final String user;
	private final String pass;
	private final String url;
	
	/** Constructor.
	 * Receives and stores login information.
	 * @param user FTP login Username.
	 * @param pass FTP login password.
	 * @param url Domain we are logging into.
	 */
	public FTP(String user, final String pass, final String url) {
		if (user.indexOf("@") != -1)
			user = user.replace("@", "%40");
		ftp_login = "ftp://" + user + ":" + pass + "@" + url;
		this.user = user.replace("%40", "@");
		this.pass = pass;
		this.url = url;
	}
	
	public boolean upload_v2(final String file, final String save_as) {
		if (! new File(file).exists()) {
			System.err.println("File not found: " + file + "!");
			return false;
		}
		
		final FTPClient ftp = new FTPClient();
		try {
			ftp.connect(url);
			if (!ftp.login(user, pass))
				return false;
			
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			
			ftp.deleteFile(save_as);
			
			final InputStream is = new FileInputStream(file);
			ftp.storeFile(save_as, is);
			is.close();
			
			ftp.logout();
			
			return true;
		} catch (IOException ioe) { ioe.printStackTrace(); }
		
		try {
			if (ftp.isConnected())  ftp.disconnect();
		} catch (final IOException ioe) { }
		
		return false;
	}
	
	public boolean upload_v2(final Update update, final String save_as) {
		// Save file locally
		
		File f = new File("update.txt");
		if (f.exists()) f.delete();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(update); oos.flush(); oos.close();
		} catch (IOException ioe) { }
		
		final FTPClient ftp = new FTPClient();
		try {
			ftp.connect(url);
			if (!ftp.login(user, pass))
				return false;
			
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			
			ftp.deleteFile(save_as);
			
			final InputStream is = new FileInputStream(f);
			ftp.storeFile(save_as, is);
			is.close();
			
			ftp.logout();
			
			return true;
		} catch (IOException ioe) { ioe.printStackTrace(); }
		
		try {
			if (ftp.isConnected())  ftp.disconnect();
		} catch (final IOException ioe) { }
		
		return false;
	}
	
	/**
	 * Uploads file to server.
	 * @param file The path/filename (local) of the file to upload.
	 * @param save_as The filename to give this file on the server.
	 * @return true if upload succeeds, false otherwise.
	 */
	public boolean upload(final String file, final String save_as) {
		if (! new File(file).exists()) {
			System.err.println("File not found: " + file + "!");
			return false;
		}
		
		try {
			final URL url = new URL(ftp_login + "/" + save_as + ";type=i");
			final URLConnection urlconn = url.openConnection();
			
			final InputStream is = new FileInputStream(file);
			final BufferedInputStream bis = new BufferedInputStream(is);
			
			final OutputStream os = urlconn.getOutputStream();
			final BufferedOutputStream bos = new BufferedOutputStream(os);
			
			final byte[] buffer = new byte[1024];
			int readCount;
			
			while( (readCount = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, readCount);
			}
			bos.flush();
			
			bos.close(); os.close();
			bis.close(); is.close();
			
		} catch (final MalformedURLException mue) { mue.printStackTrace(); return false;
		} catch (final IOException ioe) {           ioe.printStackTrace(); return false; }
		
		return true;
	}
	

	/**
	 * Uploads an "Update" object to the server.
	 * @param update Update object to upload to the FTP server.
	 * @param save_as The filename to give this file on the server.
	 * @return true if upload succeeds, false otherwise.
	 */
	public boolean upload(final Update update, final String save_as) {
		
		try {
			final URL url = new URL(ftp_login + "/" + save_as + ";type=i");
			final URLConnection urlconn = url.openConnection();
			
			final OutputStream os = urlconn.getOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(os);
			
			oos.writeObject(update);
			oos.flush();
			
			oos.close(); os.close();
			
		} catch (final MalformedURLException mue) { mue.printStackTrace(); return false;
		} catch (final IOException ioe) { ioe.printStackTrace(); return false; }
		
		return true;
	}
	
	
}
