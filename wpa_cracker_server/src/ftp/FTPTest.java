package ftp;

public class FTPTest {
	
	public FTPTest() { }
	
	public void test() {	
    // TODO Insert valid FTP credentials.
		FTP f = new FTP("user@domain.com", "password", "wesite");
		boolean result = f.upload("C:\\test.txt", "test_file.txt");
		System.out.println(result);
		
	}
}
