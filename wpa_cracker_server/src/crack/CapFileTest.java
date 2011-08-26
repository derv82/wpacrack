package crack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CapFileTest {
	public CapFileTest() { }
	
	private void p(Object txt) { System.err.println(txt.toString()); }
	
	public void test() {
		p("CapFileTest: Beginning test of CapFile...");
		
		byte[] bytes = new byte[10];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) (i + 1);
		
		CapFile cp = new CapFile("SSID", bytes, 1);
		
		File f = new File(cp.getFilename());
		
		// Ensure constructor saved the capfile locally
		if (!f.exists())
			p("    CapFileTest: getFilename() does not exist.");
		
		// Ensure the constructor saved the cap file appropriately.
		if (f.length() != bytes.length)
			p("    CapFileTest: Length of getFilename() is not equal to # of bytes received.\n      Although stripHandshake() may have shortened the file length.");
		
		try {
			FileReader fr = new FileReader(cp.getFilename());
			
			char buff[] = new char[10];
			fr.read(buff);
			for (int i = 0; i < 10; i++) {
				// p("    CapFileTest: Comparing " + (byte) buff[i] + " to " + bytes[i]+ ".");
				if ((char) buff[i] != bytes[i])
					p("    CapFileTest: Bytes written to file do not match byte array.");
			}
		} catch (FileNotFoundException fnfe) { p("    CapFileTest: File not found on '" + cp.getFilename() + "'");
		} catch (IOException ioe)            { p("    CapFileTest: IOException while reading."); }
		
		f.delete();
		
		p("CapFileTest: Test of CapFile complete.");
	}
}
