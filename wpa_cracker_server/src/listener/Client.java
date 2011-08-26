package listener;
import java.util.List;

import range.Range;


/**
 * Contains information about a "connected" client.
 * 
 * This class is so that the Server can keep track of how many clients are connected
 * and the last time they checked in (connected directly).
 * 
 * @author derv
 * @version 1
 */
public class Client implements Comparable<Client> {
	
	/** Unique identifier for this client. */
	private int id = -1;
	
	/** Range of passphrases for this client. */
	private List<Range> range;
	
	/** The last time (secs since epoch) that this client 'phoned home'. */
	private long last_updated;
	
	public Client(int id) {
		this.id = id;
		this.update();
	}
	
	/** @return The ID associated with this client. */
	public int getID() 	     { return id; }
	
	/** @return the time last updated (since 1970 epoch, in ms). */
	public long getUpdated() { return last_updated; }
	
	/** @return The list of wordlist "Range"s this client is set to crack. */
	public List<Range> getRange()    { return range; }
	
	/** @param i The ID to set this client as. */
	public void setID(final int i)       { this.id = i; }
	
	/** Refresh the client's last-updated time to *now*. */
	public void update() { this.last_updated = System.currentTimeMillis() / 1000; }
	
	/** @param list The list of wordlist "Ranges" this client is supposed to crack. */
	public void setRange(final List<Range> list) { this.range = list; }
	
	/** Compares based on ID number. 
	 * @return -1 if this Client's ID is smaller than the given Client's ID.
	 *          0 if the ID's are equal, 1 otherwise. */
	public int compareTo(final Client o) {
		if (this.id < o.getID())
			return -1;
		else if (this.id == o.getID())
			return 0;
		else
			return 1;
	}
	
}
