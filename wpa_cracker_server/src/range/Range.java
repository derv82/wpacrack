package range;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * Class for holding, manipulating, and grabbing info on a 'range' of words.
 * All ranges are from a starting String to an ending String.
 * The ending String is always 'greater than' the starting String.
 * 
 * @author derv
 * @version 19 July 2011
 */
public class Range implements Comparable<Range>, Serializable {
	/** Makes this class serializable. */
	private static final long serialVersionUID = 2L;
	
	/** Start of range. */
	private String start;
	
	/** End of range. */
	private String end;
	
	/** Character set used by this range.  (Useful for counting). */
	private String charset;
	
	/** Sets instance variables
	 * @param start The first element in the range.
	 * @param end   The last element in the range. */
	public Range(final String start, final String end, final String charset) {
		this.start   = start;
		this.end     = end;
		this.charset = charset;
	}
	
	/** @return Number of items in this range. */
	public long getCount()    { 
		long top    = 0;
		long bottom = 0;
		int exp     = charset.length();
		// calculates in logarithmic time! :D
		for (int i = 0; i < start.length(); i++) {
			top    += (charset.indexOf(end.charAt(i)) * (Math.pow(exp, end.length() - i - 1)));
			bottom += (charset.indexOf(start.charAt(i)) * (Math.pow(exp, start.length() - i - 1)));
		}
		return 1 + top - bottom;
	}
	
	
	// ACCESSORS & MUTATORS
	
	
	/** @return The first element in this range. */
	public String getStart() { return start; }
	
	/** @return The last element in this range. */
	public String getEnd() { return end;   }
	
	/** Set the beginning element of the range. SHOULD be "smaller" than the end. */
	public void setStart(final String s) { this.start = s; }
	
	/** Set the last element of the range. SHOULD be "greater" than the start. */
	public void setEnd(final String e) { this.end = e;   }
	
	/** @return String representation of this object. */
	public String toString() { return "{" + start + "-" + end + "}"; }
	
	/** Compares against another Range object. For sorting purposes.
	 * @param r The other Range object to compare to.
	 * @return 0 if they are equal, -1 if this is smaller than r, 1 otherwise.*/
	public int compareTo(final Range r) {
		if (!start.equals(r.getStart())) {
			return start.compareTo( r.getStart() );
		}
		return end.compareTo( r.getEnd() );
	}
	
	/** Compares this object against another Range. 
	 * @param r The other object
	 * @return true if the objects are identical, false otherwise */
	public boolean equals(final Range r) {
		return compareTo(r) == 0;
	}
	
	
	// "OTHER" METHODS
	
	
	/** Writes all words from 'start' to 'end' to a file.
	 * @param filename Location of file to write to. */
	public void generateFile(final String filename, final boolean append) throws IOException {
		// "index" holds the current position in the range.
		final int[] index = new int[start.length()];
		for (int i = 0; i < start.length(); i++) { 
			// Initialize each element of index to the values given in 'start'
			index[i] = charset.indexOf(start.charAt(i));
		}
		
		final FileWriter fw = new FileWriter(filename, append);
		
		fw.write(stringFromIntArray(index, true));
		final String last_line = end + "\n";
		
		int cur = index.length - 1;
		while (cur >= 0) {
			index[cur] += 1;
			if (index[cur] > charset.length() - 1) {
				for (int i = cur; i < index.length; i++) {
					index[i] = 0;
				}
				cur -= 1;
			} else {
				cur = index.length - 1;
				String line = stringFromIntArray(index, true);
				fw.write(line);
				
				if (line.equals(last_line)) 
					cur = -1;
			}
		}
		
		fw.close();
	}
	
	/**
	 * Helper method for getting the Sting from an array of integers.
	 * Used by generateFile() and split().
	 * @param array Array of ints corresponding to indices in the 'charset' string.
	 * @return Corresponding String based on the elements of the array.
	 */
	private String stringFromIntArray(final int[] array, final boolean append_newline) {
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			result.append(charset.charAt(array[i]));
		}
		if (append_newline) result.append('\n');
		return result.toString();
	}
	
	/** Returns a new range containing number_of_words from this range.
	 * Alters this range so that it contains the remaining words.
	 * 
	 * @param number_of_words The number of words to split from this Range
	 * @return Range containing words from this range.
	 */
	public Range split(final long number_of_words) {
		final String first = this.start;
		
		// "index" holds the current position in the range.
		final int[] index = new int[start.length()];
		for (int i = 0; i < start.length(); i++) { 
			// Initialize each element of index to the values given in 'start'
			index[i] = charset.indexOf(start.charAt(i));
		}
		
		int words = 0;
		
		int cur = index.length - 1;
		while (cur >= 0 && words < number_of_words) {
			index[cur] += 1;
			if (index[cur] > charset.indexOf(end.charAt(cur))) {
				for (int i = cur; i < index.length; i++) {
					index[i] = 0;
				}
				cur -= 1;
			} else {
				cur = index.length - 1;
				words++;
			}
		}
		
		final String last = stringFromIntArray(index, false);
		
		// At this point, "first" and "last" contain the first and last words for a range of size number_of_words
		
		// We need to update THIS object's start/end so as to not include the split words (first -> last, including last).
		
		this.start = last;
		
		return new Range(first, last, charset);
	}
	

}
