package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;


import org.junit.Test;

import static org.junit.Assert.*;

public class IncomingInviteTest {
    @Test
    public void parcelable() throws Exception {
        int regId = 32;
        int preKeyId = Integer.MAX_VALUE;
        byte[] preKey = "preKey54".getBytes();
        int signedPreKeyId = Integer.MIN_VALUE;
        byte[] signedPreKey = ("signedPreKey" + signedPreKeyId).getBytes();
        byte[] signature = "epsom brother xerox".getBytes();
        byte[] publicKey = "nobodye expects the spanich equisaction".getBytes();

        IncomingInvite original = new IncomingInvite(regId, preKeyId, preKey, signedPreKeyId, signedPreKey, signature, publicKey);

        Parcel parcel = Parcel.obtain();
        int position = parcel.dataPosition();

        original.writeToParcel(parcel, 0);
        parcel.setDataPosition(position);

        IncomingInvite fromParcel = IncomingInvite.CREATOR.createFromParcel(parcel);

        assertEquals(regId, fromParcel.getRegId());
        assertEquals(preKeyId, fromParcel.getPreKeyId());
        assertArrayEquals(preKey, fromParcel.getPreKey());
        assertEquals(signedPreKeyId, fromParcel.getSignedPreKeyId());
        assertArrayEquals(signedPreKey, fromParcel.getSignedPreKey());
        assertArrayEquals(signature, fromParcel.getSignature());
        assertArrayEquals(publicKey, fromParcel.getIdentityKey());
    }

    @Test
    public void emptyArrays() throws Exception {
        int regId = 32;
        int preKeyId = Integer.MAX_VALUE;
        byte[] preKey = new byte[0];
        int signedPreKeyId = Integer.MIN_VALUE;
        byte[] signedPreKey = new byte[0];
        byte[] signature = new byte[0];
        byte[] publicKey = new byte[0];

        IncomingInvite original = new IncomingInvite(regId, preKeyId, preKey, signedPreKeyId, signedPreKey, signature, publicKey);

        Parcel parcel = Parcel.obtain();
        int position = parcel.dataPosition();

        original.writeToParcel(parcel, 0);
        parcel.setDataPosition(position);

        IncomingInvite fromParcel = IncomingInvite.CREATOR.createFromParcel(parcel);

        assertEquals(regId, fromParcel.getRegId());
        assertEquals(preKeyId, fromParcel.getPreKeyId());
        assertArrayEquals(preKey, fromParcel.getPreKey());
        assertEquals(signedPreKeyId, fromParcel.getSignedPreKeyId());
        assertArrayEquals(signedPreKey, fromParcel.getSignedPreKey());
        assertArrayEquals(signature, fromParcel.getSignature());
        assertArrayEquals(publicKey, fromParcel.getIdentityKey());
    }
}