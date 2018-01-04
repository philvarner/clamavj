package com.philvarner.clamavj.test;

import com.philvarner.clamavj.ScanResult;
import static org.junit.Assert.*;
import org.junit.Test;

public class ScanResultTestCase {

	/**
	 * Detect scan-failures correctly, as reported by ClamAV.
	 * 
	 * ClamAV reports errors by using a suffix of "ERROR". See
	 * https://github.com/vrtadmin/clamav-devel/blob/79efd61b432f71b3f420d2582c352f15a81099cf/clamd/session.c#L169
	 * 
	 * To reproduce, set <code>TemporaryDirectory</code> in clamd.conf to
	 * directory with limited diskspace, then attempt to scan a file larger
	 * than the available space.
	 */
	@Test
	public void testErrorDetection() {
		String response = "Error writing to temporary file ERROR";
		ScanResult scanResult = new ScanResult(response, "stream");
		
		assertEquals(ScanResult.Status.ERROR, scanResult.getStatus());
	}
}
