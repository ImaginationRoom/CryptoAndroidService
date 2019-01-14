package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Invite implements Parcelable {
    private int mRegId;
    private int mPreKeyId;
    private byte[] mPreKey;
    private int mSignedPreKeyId;
    private byte[] mSignedPreKey;
    private byte[] mSignature;

    private String mErrorMessage;
    private static final String BLANK_ERROR_MESSAGE = "";

    public Invite(int regId, int preKeyId, byte[] preKey, int signedPreKeyId, byte[] signedPreKey, byte[] signature) {
        this.mRegId = regId;
        this.mPreKeyId = preKeyId;
        this.mPreKey = preKey;
        this.mSignedPreKeyId = signedPreKeyId;
        this.mSignedPreKey = signedPreKey;
        this.mSignature = signature;
        this.mErrorMessage = BLANK_ERROR_MESSAGE;
    }

    public Invite(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    private Invite(Parcel in) {
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

    public boolean hasError() {
        return !BLANK_ERROR_MESSAGE.equals(mErrorMessage);
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mErrorMessage = in.readString();
        if (BLANK_ERROR_MESSAGE.equals(mErrorMessage)) {
            mRegId = in.readInt();
            mPreKeyId = in.readInt();
            mPreKey = readByteArray(in);
            mSignedPreKeyId = in.readInt();
            mSignedPreKey = readByteArray(in);
            mSignature = readByteArray(in);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (!TextUtils.isEmpty(mErrorMessage)) {
            dest.writeString(mErrorMessage);
        } else {
            dest.writeString(BLANK_ERROR_MESSAGE);
            dest.writeInt(mRegId);
            dest.writeInt(mPreKeyId);
            writeByteArray(dest, mPreKey);
            dest.writeInt(mSignedPreKeyId);
            writeByteArray(dest, mSignedPreKey);
            writeByteArray(dest, mSignature);
        }
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

    public static final Creator<Invite> CREATOR = new Creator<Invite>() {
        @Override
        public Invite createFromParcel(Parcel in) {
            return new Invite(in);
        }

        @Override
        public Invite[] newArray(int size) {
            return new Invite[size];
        }
    };
}
