package listener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import network.Packet;
import range.Wordlist;
import crack.CapFile;
import crack.CrackQueue;
import encryption.Encryption;

/**
 * This class handles the "conversation" between the Server and the Client.
 * Created and initiated by class "Server".
 * 
 * @author derv
 * @version 1
 */
public class ServerConnect extends Thread {
	/** The connection with the client. */
	private Socket socket;
	
	/** List of clients. */
	private final Clients clients;
	/** List of password ranges that have not been used yet. */
	private final Wordlist wordlist;
	/** Queue for storing CapFiles uploaded by clients. */
	private final CrackQueue crack_queue;
	
	/** Input stream. */
	private ObjectInputStream ois;
	/** Output stream. */
	private ObjectOutputStream oos;
	
	/** Location to look for new .cap files. */
	private final String web_server;
	
	/** Time to tell the clients to wait before checking the web server for new .cap files. */
	private final int web_delay;
	/** Time for clients to wait before checking back in with us. */
	private final int update_delay;
	
	/** Simplified print print. */
	public void p(Object txt) { System.err.println("SERVERCONNECT ; " + txt); }
	
	/** Sets instance variables, opens connection to client, opens object streams, starts thread. */
	public ServerConnect(final Socket socket, final Clients clients, 
					final Wordlist wordlist, final CrackQueue cq,
					final String web_server, final int update_delay, final int web_delay) {
		super();
		
		this.socket      = socket;
		this.clients     = clients;
		this.wordlist    = wordlist;
		this.crack_queue = cq;
		
		this.web_server  = web_server;
		this.update_delay= update_delay;
		this.web_delay   = web_delay;
		
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException ioe) { ioe.printStackTrace(); }
		
		p("***CONNECTED*** to Client.");
		
		this.start();
	}
	
	/** Entire conversation with client happens in this method. */
	public void run() {
		final String encryption_key = Encryption.newKey();
		
		try {
			// Send initial message, encryption challenge.
			Packet request = new Packet(0, "HELO");
			
			request.setKey(encryption_key);
			
			oos.writeObject(request);
			oos.flush();
			
			Packet response = (Packet) ois.readObject();
			String text = Encryption.decrypt(response.toString(), encryption_key);
			
			// First 4 letters of decrypted message MUST be "HELO".
			if (!text.startsWith("HELO")) {
				p("    HELO; Challenge failed, disconnecting.");
				request = new Packet(0, "Encryption challenge failed. Disconnecting.");
				oos.writeObject(request);
				oos.flush();
				disconnect();
				return;
			}
			
			// At this point, the client is connecting for 1 of 5 reasons...
			switch(response.getRequest()) {
			case Packet.REQUEST_CONNECT:
				// Client wants to connect to the server for the first time.
				
				// Prepare a new id and word list range for the client.
				Client client = new Client(clients.getNewID());
				
				p("    REQUEST_CONNECT; ID=" + client.getID() + ", benchmark=" + response.getBenchmark());
				client.setRange(wordlist.partition(response.getBenchmark()));
				clients.add(client);
				
				request = new Packet(0, getServerStatus()); // Include server stats
				request.setID(client.getID());       // Give them their new ID
				request.setRange(client.getRange()); // and their range of passwords
				
				request.setWebAddress(web_server);    // Location on web server which will be updated with info on .cap files to crack.
				request.setWebDelay(web_delay);       // How frequently they should check for updates on the web server.
				request.setUpdateDelay(update_delay); // How frequently they should phone home to us.
				
				oos.writeObject(request);
				oos.flush();
				
				break;
				
			case Packet.REQUEST_UPDATE:
				// Client is "checking in" with the server for the first time.
				
				if (clients.update(response.getID())) {
					// Client updated successfully.
					p("    REQUEST_UPDATE; Success.");
					request = new Packet(0, getServerStatus());
					
					oos.writeObject(request);
					oos.flush();
					
				} else {
					// Client # is not in the list. Create a new client
					p("    REQUEST_UPDATE; Failure! Client id '" + response.getID() + "' not found!");
					
					client = new Client(clients.getNewID());
					client.setRange(wordlist.partition(response.getBenchmark()));
					
					clients.add(client);
					
					request = new Packet(0, "No");
					request.setID(client.getID());
					request.setRange(client.getRange());
					
					oos.writeObject(request);
					oos.flush();
					
				}
				
				break;
				
			case Packet.REQUEST_CRACK:
				// Client wants to upload a .CAP file to be cracked.
				// Next packet will be the bytes of the .CAP file
				byte[] bytes = (byte[]) ois.readObject();
				
				p("    REQUEST_CRACK");
				CapFile tc = new CapFile(response.getSSID(), bytes, response.getID());
				crack_queue.add(tc);
				
				request = new Packet(0, ".CAP file received. Number of .cap files in queue: " + crack_queue.size());
				oos.writeObject(request);
				oos.flush();
				
				break;
				
			case Packet.REQUEST_CRACKED:
				// Client has found the password to a .CAP file.
				p("    REQUEST_CRACKED");
				
				break;
				
			case Packet.REQUEST_DISCONNECT:
				// Remove client from list (if client is already in the list).
				p("    REQUEST_DISCO");
				if (clients.verify(response.getID(), response.getRange())) {
					clients.remove(response.getID());
				}
				break;
				
			}
		} catch (final IOException ioe) { ioe.printStackTrace();
		} catch (final ClassNotFoundException cnfe) { cnfe.printStackTrace(); }
		
		disconnect();
		p("***DISCONNECTED***");
	}
	
	/** Force disconnect from client as cleanly as possible. */
	public void disconnect() {
		try {
			ois.close();
			oos.close();
			socket.close();
		} catch (final IOException ioe) { ioe.printStackTrace(); }
	}
	
	/** @return String representation of the Server stats. */
	public String getServerStatus() {
		return "Clients online: " + clients.size() + ", Passwords taken: " + wordlist.getPasswordsTaken();
	}
}
