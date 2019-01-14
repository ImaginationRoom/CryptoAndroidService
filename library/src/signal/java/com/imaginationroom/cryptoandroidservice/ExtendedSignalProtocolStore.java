package com.imaginationroom.cryptoandroidservice;

import org.whispersystems.libsignal.state.SignalProtocolStore;

public interface ExtendedSignalProtocolStore extends SignalProtocolStore {
    int allocateNextPreKey();

    void reset();
}
