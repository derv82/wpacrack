package range;

import java.util.List;

public class WordlistTest {
	
	public static void main(String[] args) {
		// Range r = new Range("0000", "9999", "0123456789");
		
		Wordlist w = new Wordlist("000", "999", "0123456789");
		w.addRange(new Range("aaa", "ccc", "abc"));
		
		System.out.println(w);
		
		List<Range> r1 = w.partition(500);
		System.out.println(w);
		
		List<Range> r2 = w.partition(504);
		System.out.println(w);
		
		List<Range> r3 = w.partition(500);
		System.out.println(w);
		
		w.consolidate(r2);
		System.out.println(w);
		
		w.consolidate(r3);
		System.out.println(w);
		
		w.consolidate(r1);
		System.out.println(w);
		
		
		/*
		Wordlist w = new Wordlist("00000000", "99999999", "0123456789");
		
		System.out.println("WORDLIST=" + w);
		
		List<Range> r1 = w.partition(500);
		List<Range> r2 = w.partition(500);
		List<Range> r3 = w.partition(500);
		List<Range> r4 = w.partition(500);
		List<Range> r5 = w.partition(500);
		
		print("");
		System.out.println(w);
		
		w.consolidate(r1);
		
		print("");
		System.out.println(w);
		
		print("");
		List<Range> part = w.partition(550);
		for (Range herp : part) {
			print("Partition: " + herp);
		}
		
		w.consolidate(r5);
		w.consolidate(r2);
		w.consolidate(r4);
		
		print("");
		System.out.println(w);
		
		w.consolidate(r3);
		
		print("");
		System.out.println(w);
		
		w.consolidate(part);
		
		print("");
		System.out.println(w);
		
		/* Testing wordlist generation.
		try {
			r.generateFile("C:\\test.txt");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}*/
		
	}
}
