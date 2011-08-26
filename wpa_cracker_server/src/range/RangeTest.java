package range;

import java.io.File;
import java.io.IOException;

public class RangeTest {
	public RangeTest() { }
	
	private void p(Object txt) { System.err.println(txt.toString()); }
	
	public void test() {
		Range r = new Range("20000000", "20113977", "0123456789");
		try {
			r.generateFile("test.txt", false);
		} catch (IOException ioe) { ioe.printStackTrace(); }
		
		long size = new File("test.txt").length();
		
		if (size != 2000 * 6) { 
			p("    Range.generateFile() did not generate the file correctly.");
			p("    Size: " + size);
		}
		
	}
	
	public static void main(String[] args) {
		RangeTest rt = new RangeTest();
		rt.test();
	}
}
