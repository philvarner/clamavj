package com.philvarner.clamavj.test;

import com.philvarner.clamavj.ClamScan;
import com.philvarner.clamavj.ScanResult;
import com.philvarner.clamavj.ScanResult.Status;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import static com.philvarner.clamavj.ScanResult.OK_SUFFIX;

public class ScanFileTestCase extends TestCase {

    ClamScan scanner;

    public void setUp() {
        scanner = new ClamScan("localhost", 3310, 60000);
    }

    public void testSuccess() throws Exception {

    	File f = File.createTempFile("clean", ".txt");
    	f.deleteOnExit();
    	
    	FileOutputStream s = new FileOutputStream(f);
    	s.write("test".getBytes());
    	s.close();
    	
        ScanResult result = scanner.scan(f);
        
        assertEquals(f.getAbsolutePath() + ": " + OK_SUFFIX, result.getResult());
        assertEquals(Status.PASSED, result.getStatus());
        
    }

    public void testVirus() throws Exception {
    	
    	File f = File.createTempFile("eicar", ".com");
    	f.deleteOnExit();
    	
    	FileOutputStream s = new FileOutputStream(f);
    	s.write("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes());
    	s.close();
    	
        ScanResult result = scanner.scan(f);

        assertEquals(f.getAbsolutePath() + ": Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
        assertEquals(Status.FAILED, result.getStatus());
        
    }
    
}
