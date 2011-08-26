package encryption;

public class EncryptionTest {
	public EncryptionTest() { }

	private void p(Object txt) { System.err.println(txt.toString()); }
	
	public void test() {
		p("EncryptionTest: Begnning Encryption class test...");
		final String alpha   = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final String numeric = "0123456789";
		final String symbols = "~!@#$%^&*()_+`-=[]\\;',./{}|:\"<>?";
		final String all     = alpha + numeric + symbols;
		
		String temp = Encryption.encrypt(alpha, all);
		if (!Encryption.decrypt(temp, all).equals(alpha))
			p("    Encryption: decrypted text does not match encrypted text.");
		
		temp = Encryption.encrypt(alpha, all);
		if (!Encryption.decrypt(temp, all).equals(alpha))
			p("    Encryption: decrypted text does not match encrypted text.");
		
		temp = Encryption.encrypt(numeric, all);
		if (!Encryption.decrypt(temp, all).equals(numeric))
			p("    Encryption: decrypted text does not match encrypted text.");
		
		temp = Encryption.encrypt(symbols, all);
		if (!Encryption.decrypt(temp, all).equals(symbols))
			p("    Encryption: decrypted text does not match encrypted text.");
		
		temp = Encryption.encrypt(all, all);
		if (!Encryption.decrypt(temp, all).equals(all))
			p("    Encryption: decrypted text does not match encrypted text.");
		
		p("EncryptionTest: End of Encryption class test.");
	}
	
}
