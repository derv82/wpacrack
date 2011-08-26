import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Timer;

import network.Client;
import network.WebListener;
import crack.Crack;

public class ClientMain {
	
	public static void main(String[] args) {
		// Client object; the "model" for the client.
		final Client client = new Client("127.0.0.1", 9001);
		
		// The 'crack' object. Handles cracking, extracting aircrack files, etc.
		final Crack crack = new Crack(client);
		
		// Initialize Crack object; exit if unable to initialize all components.
		final String init = crack.init();
		if (!"".equals(init)) {
			// Initialization failed.
			System.err.println("Initialization failed: " + init);
			System.exit(0);
		}
		
		// TODO
		// Ensure crack.testBenchmark() is within bounds..
		// Alter benchmark until it "passes".
		
		// Set-up the "web timer" which frequently checks the website for new .cap files to crack.
		final Timer web_timer = new Timer(0, new WebListener(crack, "", client.getWordlist()));
		web_timer.stop();
		client.setWebTimer(web_timer);
		
		// Set-up the 'update timer' which phones home to the server every-so-often.
		final Timer update_timer = new Timer(0, new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				try {
					client.update();
				} catch (IOException ioe) { ioe.printStackTrace(); }
			}
		} );
		update_timer.stop();
		client.setUpdateTimer(update_timer);
		
		try {
			client.connect(crack.getBenchmark());
			
			if (!client.getConnected()) {
				// We are not connected!
				System.out.println("We are not connected!!!");
				client.getWebTimer().stop();
				update_timer.stop();
				System.exit(1);
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) { ie.printStackTrace(); }
			
			client.update();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) { ie.printStackTrace(); }
			
			// client.crack("ALL YOUR BASE ARE BELONG TO US!!", "C:\\test.cap");
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) { ie.printStackTrace(); }
			
			client.disconnect();
			
		} catch (IOException ioe) { ioe.printStackTrace(); return; }
		
		System.out.println(client);
		
		web_timer.stop();
		update_timer.stop();
		
	}
}
