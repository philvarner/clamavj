package com.philvarner.clamavj.test;

import com.philvarner.clamavj.ClamScan;
import com.philvarner.clamavj.ScanResult;
import com.philvarner.clamavj.ScanResult.Status;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.philvarner.clamavj.ScanResult.RESPONSE_OK;

public class ClamScanTestCase extends TestCase {

    ClamScan scanner;

    public void setUp() {
        scanner = new ClamScan("localhost", 3310, 60000);
    }

    public void testSuccess() throws Exception {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("abcde12345");
        }

        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.PASSED, result.getStatus());
        assertEquals(RESPONSE_OK, result.getResult());
    }

    public void testVirus() throws Exception {
        InputStream is = new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes());
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals("stream: Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
    }

    public void testVirusAsByteArray() throws Exception {
        byte[] bytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();
        ScanResult result = scanner.scan(bytes);
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals("stream: Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
    }

    public void testNoArgConstructor() throws Exception {
        scanner = new ClamScan();
        scanner.setHost("localhost");
        scanner.setPort(3310);
        scanner.setTimeout(60000);

        byte[] bytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();
        ScanResult result = scanner.scan(bytes);
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals("stream: Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
    }

    public void testMultipleOfChunkSize() throws Exception {
        InputStream is = new ArbitraryInputStream(ClamScan.CHUNK_SIZE);
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.PASSED, result.getStatus());
        assertEquals(RESPONSE_OK, result.getResult());
    }

    public void testTooLarge() throws Exception {
        InputStream is = new ArbitraryInputStream();
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(result.getResult(), Status.ERROR, result.getStatus());
        assertEquals(ScanResult.RESPONSE_SIZE_EXCEEDED, result.getResult());
    }

    public void testStats() throws Exception {
        String result = scanner.stats();
        assertTrue("didn't contain POOLS: \n" + result, result.contains("POOLS:"));
        assertTrue("didn't contains STATE: \n" + result, result.contains("STATE:"));
    }

    public void testPing() throws Exception {
        assertTrue(scanner.ping());
    }

}
