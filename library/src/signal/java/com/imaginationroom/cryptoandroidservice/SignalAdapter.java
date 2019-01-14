package com.imaginationroom.cryptoandroidservice;

import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.ByteUtil;

import java.util.Locale;

public class SignalAdapter {
    public static final int DEVICE_ID = 1;
    public static final int PRE_KEY_FIRST_ID = 1;
    public static final int SIGNED_PRE_KEY_ID = 5;

    static PreKeyBundle getPreKeyBundle(SignalProtocolStore store, int preKeyId, int signedPreKeyId) throws InvalidKeyIdException {
        SignedPreKeyRecord signedPreKeyRecord = store.loadSignedPreKey(signedPreKeyId);
        ECPublicKey signedPreKeyPublic = signedPreKeyRecord.getKeyPair().getPublicKey();
        ECPublicKey preKeyPublic = store.loadPreKey(preKeyId).getKeyPair().getPublicKey();


        return new PreKeyBundle(store.getLocalRegistrationId(), DEVICE_ID,
                preKeyId, preKeyPublic,
                signedPreKeyId, signedPreKeyPublic,
                signedPreKeyRecord.getSignature(),
                store.getIdentityKeyPair().getPublicKey());
    }

    static String toString(PreKeyBundle bundle) {
        return String.format(Locale.US, "PreKeyBundle[%d(%d bytes), %d(%d bytes)]",
                bundle.getPreKeyId(), bundle.getPreKey().serialize().length,
                bundle.getSignedPreKeyId(), bundle.getSignedPreKey().serialize().length);
    }

    static byte[] getBytesFromKeys(ECPublicKey rachet, ECPublicKey signedPreKey, ECPublicKey identityKey) {
        return ByteUtil.combine(rachet.serialize(), signedPreKey.serialize(), identityKey.serialize());
    }
}
