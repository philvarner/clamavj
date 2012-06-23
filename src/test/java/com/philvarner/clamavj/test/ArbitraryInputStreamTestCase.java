package com.philvarner.clamavj.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import static org.junit.Assert.*;

@RunWith(BlockJUnit4ClassRunner.class)
public class ArbitraryInputStreamTestCase {

    @Test
    public void test_infinite() throws Exception {

        int SIZE = 100000;
        ArbitraryInputStream is = new ArbitraryInputStream();

        byte[] bytes = new byte[SIZE];
        assertEquals(SIZE, is.read(bytes, 0, SIZE));
        for (byte b : bytes) assertEquals(b, 1);

    }

    @Test
    public void test_infinite_partially_written() throws Exception {

        ArbitraryInputStream is = new ArbitraryInputStream();

        byte[] bytes = new byte[1000];
        assertEquals(100, is.read(bytes, 0, 100));
        for (int i = 0; i < 100 ; i++) assertEquals(bytes[i], 1);
        for (int i = 100; i < 1000 ; i++) assertEquals(bytes[i], 0);

    }

    @Test
    public void test_fixed_length() throws Exception {

        int SIZE = 100;
        ArbitraryInputStream is = new ArbitraryInputStream(SIZE);

        byte[] bytes = new byte[SIZE];
        assertEquals(SIZE, is.read(bytes, 0, SIZE));

    }

    @Test
    public void test_fixed_length_larger_array() throws Exception {

        int SIZE = 100;
        ArbitraryInputStream is = new ArbitraryInputStream(SIZE*10);

        byte[] bytes = new byte[SIZE*10];
        assertEquals(SIZE, is.read(bytes, 0, SIZE));
        assertEquals(SIZE+1, is.read(bytes, 0, SIZE+1));

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_overflow() throws Exception {
        int SIZE = 100;
        assertEquals(SIZE, new ArbitraryInputStream(SIZE).read(new byte[SIZE], 1, SIZE+1));
    }

    @Test
    public void test_total_size() throws Exception {
        int SIZE = 100;
        ArbitraryInputStream is = new ArbitraryInputStream(SIZE);
        is.read(new byte[SIZE], 0, SIZE);

        assertEquals(-1, is.read(new byte[SIZE], 0, 1));
    }

}
