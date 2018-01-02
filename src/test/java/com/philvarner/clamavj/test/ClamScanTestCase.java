package com.philvarner.clamavj.test;

import com.philvarner.clamavj.ClamScan;
import com.philvarner.clamavj.ScanResult;
import com.philvarner.clamavj.ScanResult.Status;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static com.philvarner.clamavj.ScanResult.OK_SUFFIX;
import static com.philvarner.clamavj.ScanResult.STREAM_PATH;
import static org.junit.Assert.*;

public class ClamScanTestCase {

    private ClamScan scanner;

    @Before
    public void setUp() {
        scanner = new ClamScan("localhost", 3310, 60000);
    }

    @Test
    public void testSuccess() throws Exception {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("abcde12345");
        }

        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.PASSED, result.getStatus());
        assertEquals(ScanResult.getPrefix(STREAM_PATH) + OK_SUFFIX, result.getResult());
    }

    @Test
    public void testVirus() throws Exception {
        InputStream is = new ByteArrayInputStream(
                "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes());
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(ScanResult.getPrefix(STREAM_PATH) + "Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
    }

    @Test
    public void testVirusAsByteArray() throws Exception {
        byte[] bytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();
        ScanResult result = scanner.scan(bytes);
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(ScanResult.getPrefix(STREAM_PATH) + "Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
    }

    @Test
    public void test_virus_from_file() throws Exception {

        int byteCount;
        byte bytes[];
        given: {
            File f = new File("src/test/resources/eicar.txt");
            FileInputStream fis = new FileInputStream(f);
            bytes = new byte[(int) f.length()];
            byteCount = fis.read(bytes);
        }

        ScanResult result;
        when: {
            result = scanner.scan(bytes);
        }

        then: {
            assertTrue(byteCount > 0);
            assertEquals(Status.FAILED, result.getStatus());
            assertEquals("stream: Eicar-Test-Signature FOUND", result.getResult());
            assertEquals("Eicar-Test-Signature", result.getSignature());

        }
    }

    @Test
    public void testNoArgConstructor() throws Exception {
        scanner = new ClamScan();
        scanner.setHost("localhost");
        scanner.setPort(3310);
        scanner.setTimeout(60000);

        byte[] bytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();
        ScanResult result = scanner.scan(bytes);
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(ScanResult.getPrefix(STREAM_PATH) + "Eicar-Test-Signature FOUND", result.getResult());
        assertEquals("Eicar-Test-Signature", result.getSignature());
    }

    @Test
    public void testMultipleOfChunkSize() throws Exception {
        InputStream is = new ArbitraryInputStream(ClamScan.CHUNK_SIZE);
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(Status.PASSED, result.getStatus());
        assertEquals(ScanResult.getPrefix(STREAM_PATH) + OK_SUFFIX, result.getResult());
    }

    @Test
    public void testTooLarge() throws Exception {
        InputStream is = new ArbitraryInputStream();
        assertNotNull(is);
        ScanResult result = scanner.scan(is);
        assertEquals(result.getResult(), Status.ERROR, result.getStatus());
        assertEquals(ScanResult.RESPONSE_SIZE_EXCEEDED, result.getResult());
    }

    @Test
    public void testStats() throws Exception {
        String result = scanner.stats();
        assertTrue("didn't contain POOLS: \n" + result, result.contains("POOLS:") || result.contains("STATE:"));
    }

    @Test
    public void testPing() throws Exception {
        assertTrue(scanner.ping());
    }

}
