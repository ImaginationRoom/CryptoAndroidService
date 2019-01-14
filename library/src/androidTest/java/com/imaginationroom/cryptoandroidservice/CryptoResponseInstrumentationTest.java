package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;

import org.junit.Test;

import static org.junit.Assert.*;

public class CryptoResponseInstrumentationTest {
    @Test
    public void writeBytesToParcel() throws Exception {
        CryptoResponse expectedResponse = new CryptoResponse("writeBytesToParcel".getBytes());
        Parcel parcel = Parcel.obtain();

        try {
            int position = parcel.dataPosition();
            expectedResponse.writeToParcel(parcel, 0);

            parcel.setDataPosition(position);
            CryptoResponse actualResponse = new CryptoResponse(parcel);

            assertArrayEquals(expectedResponse.getBytes(), actualResponse.getBytes());
            assertTrue(actualResponse.getErrorMessage() == null || "".equalsIgnoreCase(actualResponse.getErrorMessage()));
        } finally {
            parcel.recycle();
        }
    }

    @Test
    public void writeErrorToParcel() throws Exception {
        String err = "Error message goes here";
        CryptoResponse expectedResponse = new CryptoResponse(err);

        Parcel parcel = Parcel.obtain();

        try {
            int position = parcel.dataPosition();
            expectedResponse.writeToParcel(parcel, 0);

            parcel.setDataPosition(position);
            CryptoResponse actualResponse = new CryptoResponse(parcel);

            assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
            assertArrayEquals(expectedResponse.getBytes(), actualResponse.getBytes());
        } finally {
            parcel.recycle();
        }
    }

}