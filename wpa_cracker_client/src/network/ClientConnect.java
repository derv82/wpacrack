package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import encryption.Encryption;

public class ClientConnect extends Thread {
	
	/** Simplified print. */
	public final void p(Object txt) { System.out.println("CLIENT ; " + txt); }
	
	/** The client object we will update as needed. */
	final private Client client;
	
	/** Packet we are going to send to the client. */
	final private Packet packet;
	
	/** Location of the .cap file (locally) that we will upload to the server. */
	final private String cap_file;
	
	Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	/** Start a new network session with the Server.
	 * 
	 * @param client The Client object to set instance fields as needed.
	 * @param ip_address IP address to connect to 
	 * @param session_type Type of session (from the constants Packet.SESSION_*)
	 * @param packet The Packet object to send to the server.
	 * @throws IOException
	 */
	public ClientConnect(final Client client, final Packet packet, final String capfile) {
		this.client   = client;
		this.packet   = packet;
		this.cap_file = capfile;
		
		// Start the multi-threading
		this.start();
	}
	
	/** Connects to server and does 1 of 5 exchanges (connect, update, etc..). */
	public void run() {
		try {
			// Connect to server
			p("Connecting...");
			socket = new Socket(client.getIP(), client.getPort());
			p("Connected.");
			// Open object streams
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			
			switch(this.packet.getRequest()) {
				case Packet.REQUEST_CONNECT:
					connect();
					break;
					
				case Packet.REQUEST_UPDATE:
					update();
					break;
					
				case Packet.REQUEST_CRACK:
					crack();
					break;
					
				case Packet.REQUEST_CRACKED:
					cracked();
					break;
					
				case Packet.REQUEST_DISCONNECT:
					disconnect();
					break;
			}
		} catch (final ConnectException ce) { ce.printStackTrace();   // Unable to connect
		} catch (final IOException ioe) { ioe.printStackTrace(); }    // Some other error
	}
	
	/** Helper method to assist in sending objects.
	 * @param o The object to send to the server.
	 * @throws IOException If the object is not sent. */
	private void send(final Object o) throws IOException {
		oos.writeObject(o);
		oos.flush();
	}
	
	/** Helper method to assist in receiving objects.
	 * @return The object sent by the server.
	 * @throws IOException If the object could not be sent. */
	private Object receive() throws IOException  {
		try {
			return ois.readObject();
		} catch (ClassNotFoundException cnfe) { cnfe.printStackTrace(); return null; }
	}
	
	/** 
	 * Helper method. Handles the initial exchange between server and this client.
	 * Sends back expected encryption string.
	 * @throws IOException If packets are unable to be sent/received.
	 */
	private void Helo() throws IOException {
		final Packet rec = (Packet) receive();
		
		// Respond with the response to the encryption challenge.
		packet.setMessage(Encryption.encrypt(rec.toString(), rec.getKey()));
		packet.setKey(rec.getKey());
		packet.setID(client.getID());
		
		send(packet);
	}
	
	/** Receives client ID and word-list range from Server. */
	public void connect() throws IOException {
		Helo();
		final Packet rec = (Packet) receive();
		if ("No".equals(rec.toString())) {
			// throw new IOException("Server responded with: " + rec.toString());
			client.setConnected(false);
			client.observerMessage("Unable to connect to server.");
		} else {
			client.setRange(rec.getRange());
			client.setID(rec.getID());
			
			client.setUpdateDelay(rec.getUpdateDelay());
			client.setWebDelay(rec.getWebDelay());
			client.setWebAddress(rec.getWebAddress());
			
			client.setConnected(true);
			client.observerMessage(rec.toString());
		}
	}
	
	/** Lets server know we are still active. */
	public void update() throws IOException {
		Helo();
		final Packet rec = (Packet) receive();
		if (!"No".equals(rec.toString())) {
			// Update failed! We need to change our ID/Range now!
			//client.setConnected(true);
			client.observerMessage(rec.toString());
		} else {
			client.setConnected(false);
			client.observerMessage("Unable to update with Server; Disconnected");
		}
	}
	
	/** Sends SSID, client ID, and cap file to server. */
	public void crack() throws IOException {
		Helo();
		
		final byte[] bytes = Packet.setBytes(this.cap_file);
		send(bytes);
		
		final Packet rec = (Packet) receive();
		if ("No".equals(rec.toString()))
			client.observerMessage("Server did not accept cap file.");
		else
			client.observerMessage("" + rec.toString());
	}
	
	/** Sends cracked SSID, client cracked client's ID, and cracked password to server. */
	public void cracked() {
		try {
			Helo();
			client.observerMessage("Cracked password sent to Server.");
		} catch (IOException ioe) { ioe.printStackTrace(); }
	}
	
	/** Tells the server we are disconnecting, removes us from the list of clients. */
	public void disconnect() throws IOException {
		Helo();
	}
	
}
