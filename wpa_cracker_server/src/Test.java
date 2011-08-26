import crack.CapFileTest;
import encryption.EncryptionTest;


/**
 * Test all of the test classes at once.
 * 
 * @author derv
 * @version 1
 */
public class Test {
	
	/**
	 * Main method to test all test classes within this project.
	 * Results are printed to standard error stream (System.err).
	 * @param args command-line arguments.
	 */
	public static void main(String[] args) {
		CapFileTest cft = new CapFileTest();
		cft.test();
		
		EncryptionTest e = new EncryptionTest();
		e.test();
		
	}
}
