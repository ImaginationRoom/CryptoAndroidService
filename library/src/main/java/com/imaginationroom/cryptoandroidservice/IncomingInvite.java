package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;
import android.os.Parcelable;

public class IncomingInvite implements Parcelable {
    private int mRegId;
    private int mPreKeyId;
    private byte[] mPreKey;
    private int mSignedPreKeyId;
    private byte[] mSignedPreKey;
    private byte[] mSignature;
    private byte[] mPublicKey;

    public IncomingInvite(int regId, int preKeyId, byte[] preKey, int signedPreKeyId, byte[] signedPreKey, byte[] signature, byte[] publicKey) {
        this.mRegId = regId;
        this.mPreKeyId = preKeyId;
        this.mPreKey = preKey;
        this.mSignedPreKeyId = signedPreKeyId;
        this.mSignedPreKey = signedPreKey;
        this.mSignature = signature;
        this.mPublicKey = publicKey;
    }

    private IncomingInvite(Parcel in) {
        readFromParcel(in);
    }

    public int getRegId() {
        return mRegId;
    }

    public int getPreKeyId() {
        return mPreKeyId;
    }

    public byte[] getPreKey() {
        return mPreKey;
    }

    public int getSignedPreKeyId() {
        return mSignedPreKeyId;
    }

    public byte[] getSignedPreKey() {
        return mSignedPreKey;
    }

    public byte[] getSignature() {
        return mSignature;
    }

    public byte[] getIdentityKey() {
        return mPublicKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mRegId = in.readInt();
        mPreKeyId = in.readInt();
        mPreKey = readByteArray(in);
        mSignedPreKeyId = in.readInt();
        mSignedPreKey = readByteArray(in);
        mSignature = readByteArray(in);
        mPublicKey = readByteArray(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mRegId);
        dest.writeInt(mPreKeyId);
        writeByteArray(dest, mPreKey);
        dest.writeInt(mSignedPreKeyId);
        writeByteArray(dest, mSignedPreKey);
        writeByteArray(dest, mSignature);
        writeByteArray(dest, mPublicKey);
    }

    private byte[] readByteArray(Parcel in) {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readByteArray(bytes);
        return bytes;
    }

    private void writeByteArray(Parcel out, byte[] bytes) {
        out.writeInt(bytes.length);
        out.writeByteArray(bytes);
    }

    public static final Creator<IncomingInvite> CREATOR = new Creator<IncomingInvite>() {
        @Override
        public IncomingInvite createFromParcel(Parcel in) {
            return new IncomingInvite(in);
        }

        @Override
        public IncomingInvite[] newArray(int size) {
            return new IncomingInvite[size];
        }
    };

}
