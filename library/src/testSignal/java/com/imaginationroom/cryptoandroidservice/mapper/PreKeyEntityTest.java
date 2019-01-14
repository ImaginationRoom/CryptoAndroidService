package com.imaginationroom.cryptoandroidservice.mapper;

import org.junit.Test;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

import com.imaginationroom.cryptoandroidservice.IncomingInvite;
import com.imaginationroom.cryptoandroidservice.Invite;
import com.imaginationroom.cryptoandroidservice.SignalAdapter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PreKeyEntityTest {

    @Test
    public void testTransformFromBundle() throws InvalidKeyException, InvalidKeyIdException {
        int preKeyId = SignalAdapter.PRE_KEY_FIRST_ID;
        int signedPreKeyId = SignalAdapter.SIGNED_PRE_KEY_ID;

        int registrationId = KeyHelper.generateRegistrationId(false);
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId);
        ECPublicKey signedPreKeyPublic = signedPreKey.getKeyPair().getPublicKey();

        ECPublicKey preKeyPublic = KeyHelper.generatePreKeys(preKeyId, 1).get(0).getKeyPair().getPublicKey();

        PreKeyBundle bundle = new PreKeyBundle(registrationId, 1,
                preKeyId, preKeyPublic,
                signedPreKeyId, signedPreKeyPublic,
                signedPreKey.getSignature(),
                identityKeyPair.getPublicKey());

        Invite invite = PreKeyBundleMapper.transform(bundle);

        assertNotNull(invite);

        assertEquals(bundle.getRegistrationId(), invite.getRegId());

        assertEquals(bundle.getPreKeyId(), invite.getPreKeyId());
        assertArrayEquals(bundle.getPreKey().serialize(), invite.getPreKey());

        assertEquals(bundle.getSignedPreKeyId(), invite.getSignedPreKeyId());
        assertArrayEquals(bundle.getSignedPreKey().serialize(), invite.getSignedPreKey());

        assertArrayEquals(bundle.getSignedPreKeySignature(), invite.getSignature());
    }

    @Test
    public void testTransformIncomingInviteToBundle() throws InvalidKeyException, InvalidKeyIdException {
        int preKeyId = 55;
        int signedPreKeyId = 67;
        int deviceId = 1;

        int registrationId = KeyHelper.generateRegistrationId(false);
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId);
        ECPublicKey signedPreKeyPublic = signedPreKey.getKeyPair().getPublicKey();

        ECPublicKey preKeyPublic = KeyHelper.generatePreKeys(preKeyId, 1).get(0).getKeyPair().getPublicKey();

        // Create an incoming invite.
        IncomingInvite invite = new IncomingInvite(registrationId, preKeyId, preKeyPublic.serialize(),
                signedPreKeyId, signedPreKeyPublic.serialize(), signedPreKey.getSignature(),
                identityKeyPair.getPublicKey().getPublicKey().serialize());

        assertNotNull(invite);

        PreKeyBundle bundle = PreKeyBundleMapper.transform(invite);

        assertEquals(registrationId, bundle.getRegistrationId());
        assertEquals(deviceId, bundle.getDeviceId());
        assertEquals(preKeyId, bundle.getPreKeyId());
        assertArrayEquals(preKeyPublic.serialize(), bundle.getPreKey().serialize());
        assertEquals(signedPreKeyId, bundle.getSignedPreKeyId());
        assertArrayEquals(signedPreKeyPublic.serialize(), bundle.getSignedPreKey().serialize());
        assertArrayEquals(signedPreKey.getSignature(), bundle.getSignedPreKeySignature());
        assertArrayEquals(identityKeyPair.getPublicKey().getPublicKey().serialize(), bundle.getIdentityKey().getPublicKey().serialize());
    }
}