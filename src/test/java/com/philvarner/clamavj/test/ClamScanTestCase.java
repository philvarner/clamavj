package com.philvarner.clamavj.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.philvarner.clamavj.ClamScan;
import com.philvarner.clamavj.ScanResult;
import com.philvarner.clamavj.ScanResult.Status;
import junit.framework.TestCase;

import static com.philvarner.clamavj.ScanResult.*;

public class ClamScanTestCase extends TestCase {

    ClamScan scanner;

    public void setUp() {
        scanner = new ClamScan("localhost", 3310, 60000);
    }

	public void testSuccess() throws Exception {

        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < 10000; i++){
            sb.append("abcde12345");
        }

		InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
		assertNotNull(is);
        ScanResult result = scanner.scan(is);
		assertEquals(Status.PASSED, result.getStatus());
		assertEquals(RESPONSE_OK, result.getResult());
	}
	
	public void testVirus () throws Exception {
		InputStream is = new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes());
		assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.FAILED, result.getStatus());
		assertEquals("stream: Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
	}

    public void testTooLarge() throws Exception {
        InputStream is = new InfiniteInputStream();
		assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(result.getResult(), Status.ERROR, result.getStatus());
		assertEquals(ScanResult.RESPONSE_SIZE_EXCEEDED, result.getResult());
	}

    public void testStats() throws Exception {
        String result = scanner.stats();
        assertTrue(result.startsWith("POOLS"));
        assertTrue("didn't end with END: " + result, result.endsWith("END\n"));
	}

    public void testPing() throws Exception {
        assertTrue(scanner.ping());
	}


	
}
