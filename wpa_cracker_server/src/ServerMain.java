
import java.util.Scanner;
import listener.Clients;
import listener.Server;
import range.Range;
import range.Wordlist;
import crack.Crack;
import crack.CrackQueue;

/**
 * Where the magic happens!
 * 
 * @author derv
 * @version 1
 */
public class ServerMain {
	
	/** Time to wait, in milliseconds, for the clients to crack. */
	public static final int CRACK_TIME_MS      = 4 * 60 * 1000;
	
  // TODO Enter valid FTP credentials.
  
	/** User name to login to ftp. */
	public static final String FTP_LOGIN_USER   = "user@domain.com";
	/** Password to login to ftp. */
	public static final String FTP_LOGIN_PASS   = "password";
	
	/** Domain we are logging into (FTP). */
	public static final String FTP_SERVER       = "domain.com";
	
	/** Public directory (HTTP) where the files uploaded via FTP are accessible to the public. */
	public static final String HTTP_SERVER      = "http://www.domain.com/subdirectory";
	
	/** How often clients should check in with server. */
	public static final int CLIENT_UPDATE_DELAY = 60 * 60 * 1000;
	
	/** How often clients should check web server for .cap files. */
	public static final int CLIENT_WEB_DELAY    = 60 * 1000;
	
	/** Simplified print line. */
	private static void p(final Object txt) { System.out.println(txt.toString()); }
	
	/**
	 * Main method for running the WPA cracker server.
	 * @param args command-line arguments.
	 */
	public static void main(String[] args) {
		
		/** List of clients. */
		final Clients    clients     = new Clients();
		
		/** List of possible passwords. */
		final Wordlist   wordlist    = new Wordlist("00000000", "99999999", "0123456789");
		wordlist.addRange(new Range("aaaaaaaa", "zzzzzzzz", "abcdefghijklmnopqrstuvwxyz"));
		
		/** List of .CAP files to crack. */
		final CrackQueue crack_queue = new CrackQueue();
		
		/** Start a new server (thread), listening for clients. */
		final Server server = new Server(clients, wordlist, crack_queue, HTTP_SERVER + "update.txt", CLIENT_UPDATE_DELAY, CLIENT_WEB_DELAY);
		
		/** Repeatedly check crack_queue for new files (in it's own thread), 
		 * uploads to FTP and notifies clients as needed. */
		final Crack crack = new Crack(crack_queue, CRACK_TIME_MS, CLIENT_WEB_DELAY, 
				FTP_LOGIN_USER, FTP_LOGIN_PASS, FTP_SERVER, HTTP_SERVER);
		
		final Scanner scan = new Scanner(System.in);
		
		String response;
		
		// Loop until user selects to exit the server.
		do {
			p(" *** Menu ***");
			p("1) View server stats");
			p("9) exit");
			response = scan.next();
			if (response.equals("1")) {
				p("Clients: " + clients.size() + " connected");
				p("Wordlist: " + wordlist.getPasswordsTaken() + " passwords taken");
				p("Crack: " + crack.getLastUpdated());
				p("");
			}
		} while (!response.equals("9"));
		
		p("Closing Crack object..");
		crack.exit();
		p("Done");
		
		p("Closing Server object...");
		server.exit();
		p("Done");
	}
}
