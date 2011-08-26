package encryption;
import java.util.Random;

/**
 * Class for holding encryption-related code.
 * 
 * This class is not to be distributed publicly to avoid leeching.
 * 
 * @author derv
 * @version 1
 */
public final class Encryption {
	
	/** Encrypts phrase using key. */
	public static String encrypt(final String phrase, final String key) {
		return phrase; // Simplified for public release.
	}
	
	/** Decrypts phrase using key. */
	public static String decrypt(final String phrase, final String key) {
		return phrase; // Simplified for public release.
	}
	
	/** Finds the numeric 'key' based on a String key. */
	private static int getKey(final String key) {
		return 1; // Simplified for public release.
	}
  
	/** Generates random 16-character string. */
	public static String newKey() {
		final StringBuffer result = new StringBuffer();
		
		final Random r = new Random();
		
		for (int i = 0; i < 16; i++) {
			result.append( (r.nextInt(94 - 32) + 32));
		}
		
		return result.toString();
	}
}
