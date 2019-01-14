package com.imaginationroom.cryptoandroidservice;

import android.content.Context;
import android.util.Log;

import com.imaginationroom.cryptoandroidservice.store.MySignalProtocolStore;

class Injection {
    static CryptoDataSource provideCryptoDataSource(Context context) throws Exception {
        Log.d("Injection", "Instantiating: SignalDataSource");

        return new SignalDataSource(new MySignalProtocolStore(context));
    }
}
