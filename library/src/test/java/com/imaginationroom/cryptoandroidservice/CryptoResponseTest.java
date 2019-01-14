package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;

import org.junit.Test;

import static org.junit.Assert.*;

public class CryptoResponseTest {
    @Test
    public void getZeroBytes() throws Exception {
        byte[] orig = new byte[0];
        CryptoResponse rsp = new CryptoResponse(orig);

        assertArrayEquals(orig, rsp.getBytes());
    }

    @Test
    public void getBytes() throws Exception {
        byte[] orig = "Some message to convert to UTF-8".getBytes("UTF-8");
        CryptoResponse rsp = new CryptoResponse(orig);

        assertArrayEquals(orig, rsp.getBytes());

        // assert that rsp has it's own copy...
        orig[0]++;
        assertNotEquals(orig[0], rsp.getBytes()[0]);
    }

    @Test
    public void getErrorMessage() throws Exception {
        String err = "Error message goes here";
        CryptoResponse rsp = new CryptoResponse(err);

        assertEquals(err, rsp.getErrorMessage());
    }

}