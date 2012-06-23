package com.philvarner.clamavj.test;

import java.io.InputStream;

public class ArbitraryInputStream extends InputStream {
    public int available() {
        return 1;
    }

    public void mark(int readlimit) {
    }

    public boolean markSupported() {
        return false;
    }

    public int read() {
        return 1;
    }

    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) {
        for (int i = off; i < off + len; i++) b[i] = '1';
        return len;
    }

    public long skip(long n) {
        return n;
    }
}
