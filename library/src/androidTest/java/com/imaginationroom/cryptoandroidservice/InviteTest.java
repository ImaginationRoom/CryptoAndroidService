package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InviteTest {
    @Test
    public void parcelable() throws Exception {
        int regId = 32;
        int preKeyId = Integer.MAX_VALUE;
        byte[] preKey = "preKey54".getBytes();
        int signedPreKeyId = Integer.MIN_VALUE;
        byte[] signedPreKey = ("signedPreKey" + signedPreKeyId).getBytes();
        byte[] signature = "epsom brother xerox".getBytes();

        Invite original = new Invite(regId, preKeyId, preKey, signedPreKeyId, signedPreKey, signature);

        Parcel parcel = Parcel.obtain();
        int position = parcel.dataPosition();

        original.writeToParcel(parcel, 0);
        parcel.setDataPosition(position);

        Invite fromParcel = Invite.CREATOR.createFromParcel(parcel);

        assertEquals(regId, fromParcel.getRegId());
        assertEquals(preKeyId, fromParcel.getPreKeyId());
        assertArrayEquals(preKey, fromParcel.getPreKey());
        assertEquals(signedPreKeyId, fromParcel.getSignedPreKeyId());
        assertArrayEquals(signedPreKey, fromParcel.getSignedPreKey());
        assertArrayEquals(signature, fromParcel.getSignature());
    }

    @Test
    public void emptyArrays() throws Exception {
        int regId = 32;
        int preKeyId = Integer.MAX_VALUE;
        byte[] preKey = new byte[0];
        int signedPreKeyId = Integer.MIN_VALUE;
        byte[] signedPreKey = new byte[0];
        byte[] signature = new byte[0];

        Invite original = new Invite(regId, preKeyId, preKey, signedPreKeyId, signedPreKey, signature);

        Parcel parcel = Parcel.obtain();
        int position = parcel.dataPosition();

        original.writeToParcel(parcel, 0);
        parcel.setDataPosition(position);

        Invite fromParcel = Invite.CREATOR.createFromParcel(parcel);

        assertEquals(regId, fromParcel.getRegId());
        assertEquals(preKeyId, fromParcel.getPreKeyId());
        assertArrayEquals(preKey, fromParcel.getPreKey());
        assertEquals(signedPreKeyId, fromParcel.getSignedPreKeyId());
        assertArrayEquals(signedPreKey, fromParcel.getSignedPreKey());
        assertArrayEquals(signature, fromParcel.getSignature());
    }
}