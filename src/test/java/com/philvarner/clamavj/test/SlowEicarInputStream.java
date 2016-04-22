package com.philvarner.clamavj.test;

import java.io.IOException;
import java.io.InputStream;

// The purpose of this class is to provide an InputStream that does not completely 
// fill the provided byte array for read(byte[]...).  The InputStream class guarantees at least one byte
// is read if the stream hasn't reached the end of file.
//
// As the Java documentation states:
//   If the length of b is zero, then no bytes are read and 0 is returned; 
//   otherwise, there is an attempt to read at least one byte.  
//     If no byte is available because the stream is at the end of the file, the value -1 is returned; 
//     otherwise, at least one byte is read and stored into b.
//
// https://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html#read(byte[])

public class SlowEicarInputStream extends InputStream {

    final private byte[] bytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes();
    private int pos = 0;

    public int read() {
        if (this.pos == bytes.length) {
          return -1;
        }
        return bytes[this.pos++];
    }

    public int read(byte b[], int off, int len) throws IOException {
        // Begin copied code from superclass...
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;
        // End copied code from superclass

        // Only ever return one byte
        return 1;
    }

}
