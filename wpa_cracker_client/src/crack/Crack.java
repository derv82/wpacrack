package crack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import network.Client;
import range.Range;

public class Crack extends Thread {
	/** Client object we report cracked passwords to. */
	private final Client client;
	
	/** Path to aircrack-ng executable. */
	private String aircrack;
	
	/** Location of .cap file for when cracking while threaded. */
	private String thread_cap_file;
	/** Name of access point for when cracking while threaded. */
	private String thread_ssid;
	/** Location of wordlist for when cracking while threaded. */
	private String thread_wordlist;
	/** ID# of client we are cracking for (while cracking while threaded). */
	private int thread_id;
	
	/** Working directory for this program. */
	public final String working_dir;
	
	/** Number of passwords this CPU can try in 1 minute. */
	private int benchmark;
	
	/** Initializes instance variables, 
	 * creates working directory.
	 * @param client The Client object we will report cracked passwords to. */
	public Crack(final Client client) {
		this.client = client;
		
		this.working_dir = client.working_dir;
		
		File d = new File(this.working_dir);
		if (! d.isDirectory())
			d.mkdirs();
	}
	
	/** 
	 * Extracts required files (if on windows), 
	 * runs benchmark. 
	 * @return "" if initialization was successful, explanation if it was unable to initialize. */
	public String init() {
		// if we're on windows
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			// we need lots of cygwin dll's as well as the aircrack-ng.exe file
			extractFile("cygcrypto-0.9.8.dll");
			extractFile("cyggcc_s-1.dll");
			extractFile("cygwin1.dll");
			aircrack = extractFile("aircrack-ng.exe").toString();
			
		} else {
			// if we're on linux, the aircrack string is simply 'aircrack-ng'
			// we should have a check to ensure aircrack is installed and works.
			aircrack = "aircrack-ng";
		}
		
		this.benchmark = calculateBenchmark();
		if (this.benchmark <= 0)
			return "Error: Unable to calculate WPA/PSK cracking benchmark.";
		
		if (!testCrack())
			return "Error: Unable to crack test .cap file.";
		
		return "";
	}
	
	/** Tests the speed of aircrack-ng to see how long it takes to crack. */
	private int calculateBenchmark() {
		try {
			new Range("00000000", "00000009", "0123456789").generateFile(working_dir + "test_range0.txt", false);
			new Range("00000000", "00000099", "0123456789").generateFile(working_dir + "test_range1.txt", false);
			new Range("00000000", "00000999", "0123456789").generateFile(working_dir + "test_range2.txt", false);
			new Range("10000000", "10002999", "0123456789").generateFile(working_dir + "test_range3.txt", false);
			// new Range("20000000", "20131193", "0123456789").generateFile(working_dir + "test_range4.txt", false);
		} catch (IOException ioe) { ioe.printStackTrace(); }
		String test_cap = extractFile("test.cap").toString();
		
		// Dry-run so we don't cold-start the timing. 
		crack(test_cap, 
				"ALL YOUR BASE ARE BELONG TO US!!", 
				working_dir + "test_range0.txt");
		
		long[] times = new long[5];
		
		for (int i = 0; i < 4; i++) {
			long time_start = System.currentTimeMillis();
			
			crack(test_cap, 
					"ALL YOUR BASE ARE BELONG TO US!!", 
					working_dir + "test_range" + i + ".txt");
			
			times[i] = System.currentTimeMillis() - time_start;;
			
			// Subtract the baseline crack time from the other times.
			if (i > 0) times[i] = times[i] - times[0];
		}
		
		// delete wordlists + test .cap file
		for (int i = 0; i < 4; i++)
			new File(working_dir, "test_range" + i + ".txt").delete();
		new File(working_dir + "test.cap").delete();
		
		double average_time;
		
		average_time = (100 / (double) times[1]) + (1000 / (double) times[2]) + (3000 / (double) times[3]);
		average_time = average_time / 3;
		
		return new Double(60 * 1000 * average_time).intValue();
	}
	
	/**
	 * Tests that the estimated benchmark (number of password attempts per second) is accurate.
	 * Tries a word-list 1/6th the size of the benchmark and returns how long this takes minus 10 seconds.
	 * @return How much deviation from reality the estimated benchmark is.
	 */
	public int testBenchmark() {
		int expected_time = 10000;  // We divide the tries/min benchmark by 6
									// This way we can test the accuracy of the benchmark within 10 seconds.
		int sixth = benchmark / 6;  // Take 1/6th of the benchmark
									// This is how many passwords we *should* be able to crack in 15 seconds.
		// Set the start/end of the passwords to generate
		// We *should* be able to crack this list in roughly 10 seconds.
		String end = "" + sixth; while (end.length() < 7) end = "0" + end; end = "1" + end;
		String start = "10"; while (start.length() < end.length()) { start += "0"; }
		
		// Generate the wordlist that we should crack within 10 seconds.
		File test_wordlist = new File(working_dir, "test_wordlist.txt");
		try {
			new Range(start, end, "0123456789").generateFile(test_wordlist.toString(), false);
		} catch (IOException ioe) { ioe.printStackTrace(); return Integer.MAX_VALUE; }
		
		File test_cap = extractFile("test.cap"); // .CAP file needed for testing
		
		// Measure how long it takes to crack using the test wordlist.
		long start_time = System.currentTimeMillis();
		crack(test_cap.toString(), "ALL YOUR BASE ARE BELONG TO US!!", test_wordlist.toString());
		long end_time  = System.currentTimeMillis();
		
		test_cap.delete(); test_wordlist.delete(); // Clean-up.
		
		int crack_time = (int) (end_time - start_time);
		
		// System.err.println("    Crack: Total crack time:    " + crack_time + "ms"); System.err.println("    Crack: Expected crack time: " + expected_time + "ms");
		
		return crack_time - expected_time;
	}
	
	/** 
	 * Tests that the program can crack .CAP files properly. 
	 * Extracts the saved .CAP file and tries to crack it.
	 * @return True if the program can crack WPA passwords in .cap files, false otherwise. */
	public boolean testCrack() {
		final File temp_file  = new File(working_dir, "temp_range.txt");
		
		if (temp_file.exists())
			temp_file.delete();
		
		try {
			FileWriter fw = new FileWriter(temp_file);
			fw.write("00001111");
			fw.close();
		} catch (final IOException ioe) { ioe.printStackTrace(); }
		
		final File test_cap = extractFile("test.cap");
		
		final String result = crack(
						test_cap.toString(),                 // location of .cap file
						"ALL YOUR BASE ARE BELONG TO US!!",  // SSID of access point in .cap file
						temp_file.toString());               // location of wordlist
		
		temp_file.delete();
		test_cap.delete();
		
		return result.equals("00001111");
	}
	
	/**
	 * Tries to crack a capfile using a range of words in a text file. 
	 * @param cap_file Path to .cap file we are cracking
	 * @param ssid SSID of the access point associated with the .cap file.
	 * @param words_file Path to wordlist file.
	 * @return "" if no password is found, otherwise the password.
	 */
	public String crack(final String cap_file, final String ssid, final String words_file) {
		final File cracked = new File(working_dir, "cracked.txt");
		
		if (cracked.exists())
			cracked.delete();
		
		String[] command = {
				aircrack,           // path to aircrack 
				"-a", "2",          // cracking WPA
				"-w", words_file,   // path to wordlist (the range) 
				"-l", cracked.toString(), // path to output file
				"-e", ssid,         // ssid of the .cap file
				cap_file.toString() // path to the cap file
		};
		
		execAndWait(command);
		
		if (!cracked.exists())
			return "";
		
		String result = "";
		try {
			final FileReader fr = new FileReader(cracked);
			while (fr.ready())
				result += (char) fr.read();
		} catch (final FileNotFoundException fnfe) { fnfe.printStackTrace(); 
		} catch (final IOException ioe) { ioe.printStackTrace(); }
		
		cracked.delete();
		
		return result;
	}
	
	/** 
	 * Cracks the given .cap file using Threads.
	 * If cracked, the thread will call client.cracked() with appropriate info (password and client id).
	 * @param cap_file Location of the .cap file to crack.
	 * @param ssid     SSID of the access point in the .cap file to crack.
	 * @param wordlist Wordlist to attempt to crack with.
	 * @param id       Client id requesting the crack.
	 */
	public void crack_threaded(String cap_file, String ssid, String wordlist, int id) {
		this.thread_cap_file = new String(cap_file);
		this.thread_ssid     = new String(ssid);
		this.thread_wordlist = new String(wordlist);
		this.thread_id       = id;
	}
	
	/** Threaded cracking! */
	public void run() {
		final String result = crack(thread_cap_file, thread_ssid, thread_wordlist);
		if (!result.equals("")) {
			client.cracked(thread_id, result, thread_ssid);
		}
	}
	
	/** Executes a command and waits for the process to finish. */
	private void execAndWait(String[] the_command) {
		// System.out.println(); for (String c : the_command) System.out.print("" + c + " "); System.out.println();
		try {
			Process proc = Runtime.getRuntime().exec(the_command);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			
			while ( (br.readLine()) != null) { /* *could* read lines here. */ }
			proc.waitFor();
			
		} catch (final IOException ioe)         { ioe.printStackTrace();
		} catch (final InterruptedException ie) { ie.printStackTrace();
		}
	}
	
	/** Extracts file 'the_file' to 'my_dir'. */
	private File extractFile(final String the_file) {
		final File outfile = new File(working_dir + the_file);
		if (outfile.exists())
			outfile.delete(); // return outfile;
		
		try {
			final InputStream in = new BufferedInputStream(
				getClass().getResourceAsStream("/crack/" + the_file));
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
			final byte[] buffer = new byte[2048];
			while (true) {
		        int nBytes = in.read(buffer);
		        if (nBytes <= 0) break;
		        out.write(buffer, 0, nBytes);
			}
			out.flush();
			out.close(); in.close();
		} catch (final FileNotFoundException fnfe) { fnfe.printStackTrace();
		} catch (final IOException ioe)            { ioe.printStackTrace(); }
		
		return outfile;
	}
	
	/** @return The maximum number of crack attempts per second for this machine. */
	public int getBenchmark() { return this.benchmark; }
}
