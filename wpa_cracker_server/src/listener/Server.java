package listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import range.Wordlist;

import crack.CrackQueue;

/**
 * Listens for clients to connect.
 * Passes connected clients onto ServerConnect() class.
 * 
 * Threaded so that we can listen continuously for new clients.
 * 
 * @author derv
 * @version 1
 */
public class Server extends Thread {
	/** Port to listen on. */
	public static final int PORT = 9001;
	
	/** Socket the server will listen on. */
	private ServerSocket socket;
	
	/** List of words we will partition off to clients as they connect. */
	private final Wordlist wordlist;
	/** List of clients we will add to as clients connect. */
	private final Clients clients;
	/** Crack queue we will add CapFiles to as clients upload them. */
	private final CrackQueue crack_queue;

	private final String web_server; 
	
	private final int update_delay;
	
	private final int web_delay;
	
	/** Simplified print. */
	public void p(Object txt) { System.err.println("SERVER ; " + txt); }
	
	/** Flag so we can stop the thread cleanly. */
	private boolean stop = false;
	
	/**
	 * Starts the server socket.
	 * @param clients List of clients.
	 * @param wordlist List of words to use for cracking.
	 * @param crack_queue Queue to store CapFiles in.
	 */
	public Server(final Clients clients, final Wordlist wordlist, final CrackQueue crack_queue, 
			final String web_server, final int update_delay, final int web_delay) { 
		super();
		
		this.clients  = clients;
		this.wordlist = wordlist;
		this.crack_queue = crack_queue;

		this.web_server   = web_server;
		this.update_delay = update_delay;
		this.web_delay    = web_delay;
		
		try {
			// Open a new socket.
			socket = new ServerSocket(PORT);
			p("Listening on " + PORT);
			this.start();
		} catch (final IOException ioe) {
			p("Unable to start new socket; Thread Server.start() has not be initialized.");
			ioe.printStackTrace();
		}
	}
	
	/** Thread which listens for new clients and 'handles' them using ServerConnect class as they connect. */
	public void run() {
		// Looping until it's time to stop.
		while (!this.stop) {
			p("Waiting for connections...");
			try {
				Socket client = socket.accept();
				p("Receiving client!");
				
				// Connected clients are to be handled in their own separate threads.
				new ServerConnect(client, clients, wordlist, crack_queue, web_server, update_delay, web_delay);
				
			} catch (final IOException ioe) { } // ioe.printStackTrace(); }
			
		}
		
		// Safely close the socket.
		try {
			socket.close();
		} catch (final IOException ioe) { ioe.printStackTrace(); }
		
		// Let exit() know we are done and the thread can end.
		this.stop = false;
	}
	
	/** Stops the server from listening. exit() will continue to execute until the thread has stopped. */
	public void exit() {
		this.stop = true;
		
		try {
			if (socket != null)
				socket.close();
		} catch (final IOException ioe) { } // We expect the IOException in this event.
		
	}
}
