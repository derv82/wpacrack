package crack;

import network.Client;

/**
 * Tests the Crack object as thoroughly as possible.
 * 
 * @author derv
 * @version 1
 */
public class CrackTest {
	/** Nothing. */
	public CrackTest() { }
	
	/**
	 * Simplified print.
	 * @param txt The text to print. */
	private void p(Object txt) { System.err.println(txt.toString()); }
	
	/**
	 * Method to test the Crack object. 
	 * Ensures the object will behave expectedly on the current.
	 * Prints status and error messages to stderr.
	 * 
	 * @return True if all tests pass, False otherwise.
	 */
	public boolean test() {
		p("    Crack: Instantiating 'Crack'...");
		Crack c = new Crack(new Client("127.0.0.1", 9001));
		
		
		p("    Crack: Initializing...");
		String init = c.init();
		if (!init.equals("")) {
			p("    Crack: Unable to initalize Crack: " + init);
			return false;
		}
		p("    Crack: Initialized. Benchmark: " + c.getBenchmark());
		
		
		p("    Crack: Cracking test .cap file...");
		if (!c.testCrack()) {
			p("    Unable to crack .cap file!");
			return false;
		}
		p("    Crack: Test successful.");
		
		
		p("    Crack: Verifying bechmark is accurate (10 sec)...");
		int real_bench = c.testBenchmark();
		if (real_bench > 2000) {
			p("    Crack: Benchmark is more than 1 second off: " + real_bench + "ms");
			return false;
		} else {
			p("    Crack: Benchmark is accurate! Off by " + real_bench + "ms");
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		CrackTest ct = new CrackTest();
		ct.test();
	}
	
}
