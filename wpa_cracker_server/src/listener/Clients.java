package listener;

import java.util.ArrayList;
import java.util.List;

import range.Range;


/**
 * Wrapper object. Stores list of Client objects.
 * 
 * @author derv
 * @version 1
 */
@SuppressWarnings("serial")
public class Clients extends ArrayList<Client> {
	private List<Client> list;
	
	private int currentID = 1;
	
	/** Initializes a new list of Clients. */
	public Clients() {
		list = new ArrayList<Client>();
	}
	
	/** Add a new client to the list. 
	 * @param client The client to add. */
	synchronized public boolean add(final Client client) {
		// int index = binarySearch(client);
		list.add(client);
		
		return true;
	}
	
	/** @param client The client to remove from the list. */
	synchronized public boolean remove(final Client client) {
		list.remove(client);
		return true;
	}
	
	/**
	 * Removes client based on the client's ID. 
	 * @param id The unique ID of the client to remove.
	 * @return The Client objec, if found. A null object otherwise
	 */
	synchronized public Client remove(final int id) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getID() == id) {
				return list.remove(i);
			}
		}
		return null;
	}
	
	/** Resets the last_updated time of a client to *now*. 
	 * @param client the client to "update". */
	synchronized public boolean update(final int id) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getID() == id) {
				list.get(i).update();
				return true;
			}
				
		}
		return false;
	}
	
	/** @param id The ID of the client to find. */
	synchronized public int find(final int id) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getID() == id)
				return i;
		}
		return -1;
	}
	
	/** 
	 * Verifies that a client with ID "id" and list of ranges "range" is in the list.
	 * @param id The ID of the client to verify
	 * @param range The list of Range objects this client is supposed to have.
	 * @return True if there is a client with the same ID and ranges as given.
	 */
	synchronized public boolean verify(final int id, final List<Range> range) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getID() == id && 
					equalRanges(list.get(i).getRange(), range)) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * Compares two lists of Range objects.
	 * @param r1 The first list of Range objects
	 * @param r2 The second list of Range objects
	 * @return True if the lists are identical, false otherwise
	 */
	synchronized private boolean equalRanges(final List<Range> r1, final List<Range> r2) {
		if (r1.size() != r2.size()) 
			return false;
		
		for (int i = 0; i < r1.size(); i++) {
			if (!r1.get(i).equals(r2))
				return false;
		}
		
		return true;
	}
	
	/** @return a new, unused client id. */
	synchronized public int getNewID() { return ++currentID; }
	
}
