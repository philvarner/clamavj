package com.philvarner.clamavj.test;

import java.io.InputStream;

public class ArbitraryInputStream extends InputStream {

    private int totalSize = -1;
    private int currentOffset = 0;

    public ArbitraryInputStream() {}

    public ArbitraryInputStream(int totalSize) {
        this.totalSize = totalSize;
    }

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

    public int read(byte[] bytes, int offset, int length) {
        if (bytes.length < offset + length) throw new IllegalArgumentException("bytes.length < offset + length");
        if (totalSize == currentOffset) return -1;
        if (totalSize < 0) {
            // no change
        } else if (totalSize - currentOffset < length) {
            length = totalSize - currentOffset;
            currentOffset = currentOffset + length;
        } else {
            currentOffset = currentOffset + length;
        }
        for (int i = offset; i < offset + length; i++) bytes[i] = 1;
        return length;
    }

    public long skip(long n) {
        return n;
    }
}
