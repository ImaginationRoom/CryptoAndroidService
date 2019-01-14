package com.imaginationroom.cryptoandroidservice;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class CryptoResponse implements Parcelable {
    private byte[] mBytes;
    private String mErrorMessage;

    public CryptoResponse(byte[] in) {
        mBytes = Arrays.copyOf(in, in.length);
        mErrorMessage = "";
    }

    public CryptoResponse(String errorMessage) {
        mBytes = new byte[0];
        this.mErrorMessage = errorMessage;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public boolean isSuccessful() {
        return mErrorMessage == null || mErrorMessage.length() == 0;
    }

    protected CryptoResponse(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<CryptoResponse> CREATOR = new Creator<CryptoResponse>() {
        @Override
        public CryptoResponse createFromParcel(Parcel in) {
            return new CryptoResponse(in);
        }

        @Override
        public CryptoResponse[] newArray(int size) {
            return new CryptoResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mBytes = new byte[in.readInt()];
        in.readByteArray(mBytes);
        mErrorMessage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mBytes.length);
        out.writeByteArray(mBytes);
        out.writeString(mErrorMessage == null ? "" : mErrorMessage);
    }
}
