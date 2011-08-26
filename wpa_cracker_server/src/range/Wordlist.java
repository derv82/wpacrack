package range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds list of Range objects.
 * 
 * Has methods which can 'remove' portions of the Ranges, 
 * and can also add portions back into the list of ranges.
 * 
 * @author derv
 * @version 1
 */
public class Wordlist {
	/** List to keep track of what remains of the wordlist. */
	private List<Range> list = new ArrayList<Range>();
	
	/** Number of passwords currently taken. */
	private long total_passwords_taken = 0;
	
	public Wordlist() { }
	
	/** 
	 * Creates the wordlist. 
	 * End must be 'larger' than start. 
	 * All characters of start and end must be using letters in charset.
	 * 
	 * @param start First element in the wordlist.
	 * @param end   Last element in the wordlist. */
	public Wordlist(final String start, final String end, final String charset) {
		list.add(new Range(start, end, charset));
	}
	
	/** @return Number of passwords assigned to other clients. */
	public long getPasswordsTaken() { return this.total_passwords_taken; }
	
	/** Adds a range to the wordlist. */
	public void addRange(final Range r) {
		list.add(r);
		Collections.sort(list);
	}
	
	/**
	 * Partition off 'number' amount of words from ranges.
	 * Returns the partitioned list. This list is immediately removed from the list of ranges.
	 * 
	 * @param number_of_words Number of words to 'remove' from the wordlist.
	 * @return List of Range objects holding number_of_words from the Wordlist
	 */
	synchronized public List<Range> partition(final int number_of_words) {
		final List<Range> result = new ArrayList<Range>();
		
		int remaining = number_of_words;
		
		while (remaining > 0 && list.size() > 0) {
			long c = list.get(0).getCount();
			
			if (c <= remaining) {
				result.add(list.remove(0));
				remaining -= c;
			} else {
				result.add(list.get(0).split(remaining));
				remaining = 0;
			}
		}
		
		if (list.size() == 0) {
			System.out.println("\n NO MORE WORDLISTS");
		} else {
			total_passwords_taken += number_of_words;
		}
		return result;
	}
	
	/**
	 * Opposite of 'partition'; places previously-partitioned ranges back into wordlist.
	 * This prevents "list" from becoming too large by combining adjacent ranges.
	 * 
	 * @param ranges List of Range objects to put back into the list.
	 */
	synchronized public void consolidate(final List<Range> ranges) {
		for (final Range r : ranges) {
			boolean found_a_place = false;
			
			total_passwords_taken -= r.getCount();
			
			for (int i = 0; i < list.size(); i++ ) {
				if (list.get(i).getStart().equals(r.getEnd())) {
					list.get(i).setStart(r.getStart());
					
					if (i > 0) {
						if (list.get(i - 1).getEnd().equals(r.getStart())) {
							list.get(i).setStart(list.get(i-1).getStart());
							list.remove(i-1);
						}
					}
					found_a_place = true;
					break;
				}
				if (list.get(i).getEnd().equals(r.getStart())) {
					list.get(i).setEnd(r.getEnd());
					
					if (i < list.size() - 1) {
						if (list.get(i + 1).getStart().equals(r.getEnd())) {
							list.get(i).setEnd(list.get(i+1).getEnd());
							list.remove(i+1);
						}
					}
					found_a_place = true;
					break;
				}
			}
			
			if (!found_a_place) {
				list.add(r);
				Collections.sort(list);
			}
		}
	}
	
	/** Textual depiction of the state of the wordlists, including all of the ranges (start-end). */
	public String toString() {
		final StringBuffer result = new StringBuffer();
		
		for (Range r : list) {
			result.append(r.toString() + ", ");
		}
		
		if (result.equals("")) {
			result.append("{EMPTY}");
		}
		
		result.append(" (Passwords Taken: " + total_passwords_taken + ")");
		
		return result.toString();
	}
	
}
