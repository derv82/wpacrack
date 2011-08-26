package crack;

import java.util.ArrayList;

/**
 * Stores list of CapFile objects. Thread-safe (hopefully!).
 * 
 * @author derv
 * @version 1
 */
public class CrackQueue {
	private final ArrayList<CapFile> queue;
	
	/** Constructor, does nothing. */
	public CrackQueue() { 
		queue = new ArrayList<CapFile>();
	}
	
	/** @return True if the queue is empty, false otherwise. */
	synchronized public boolean isEmpty() { return (queue.size() == 0); }
	
	/** Add cap file to the queue.
	 * @param tc The CapFile object to add. */
	synchronized public void add(CapFile tc) { queue.add(tc); }
	
	/** @return The next CapFile in the queue, but does not remove it. */
	synchronized public CapFile peek()       { return queue.get(0); }
	
	/** @return Removes and returns the next CapFile in the queue. */
	synchronized public CapFile remove() { 
		if (queue.size() == 0) {
			return null;
		}
		return queue.remove(0);
	}
	
	/** @return String representation of this object. */
	synchronized public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{ ");
		for (int i = 0; i < queue.size(); i++) {
			sb.append(queue.get(i).toString() + "  |  ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	/** @return The number of CapFile objects in the queue. */
	synchronized public int size() { return queue.size(); }
}
