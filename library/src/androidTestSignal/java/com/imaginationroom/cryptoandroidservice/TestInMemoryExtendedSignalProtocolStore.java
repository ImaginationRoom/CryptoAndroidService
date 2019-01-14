package com.imaginationroom.cryptoandroidservice;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.List;

import static com.imaginationroom.cryptoandroidservice.SignalAdapter.SIGNED_PRE_KEY_ID;

public class TestInMemoryExtendedSignalProtocolStore extends InMemorySignalProtocolStore implements ExtendedSignalProtocolStore {
    public TestInMemoryExtendedSignalProtocolStore() throws InvalidKeyException {
        super(generateIdentityKeyPair(), generateRegistrationId());

        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(getIdentityKeyPair(), SIGNED_PRE_KEY_ID);
        storeSignedPreKey(SIGNED_PRE_KEY_ID, signedPreKey);
    }

    private static IdentityKeyPair generateIdentityKeyPair() {
        ECKeyPair identityKeyPairKeys = Curve.generateKeyPair();

        return new IdentityKeyPair(new IdentityKey(identityKeyPairKeys.getPublicKey()),
                identityKeyPairKeys.getPrivateKey());
    }

    private static int generateRegistrationId() {
        return KeyHelper.generateRegistrationId(false);
    }

    private int nextPreKeyId = 1;

    @Override
    public int allocateNextPreKey() {
        int id = nextPreKeyId;
        nextPreKeyId++;
        List<PreKeyRecord> records = KeyHelper.generatePreKeys(id, 1);
        storePreKey(id, records.get(0));
        return id;
    }

    @Override
    public void reset() {

    }
}
