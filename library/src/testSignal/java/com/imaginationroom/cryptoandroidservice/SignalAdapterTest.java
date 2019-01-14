package com.imaginationroom.cryptoandroidservice;

import org.junit.Before;
import org.junit.Test;
import org.whispersystems.libsignal.DecryptionCallback;
import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SignalAdapterTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRegistration() throws Exception {
        SignalProtocolStore aliceStore = new TestInMemoryExtendedSignalProtocolStore2();

        assertTrue(aliceStore.getLocalRegistrationId() != 0);
        assertNotNull(aliceStore.getIdentityKeyPair());
        assertNotNull(aliceStore.getIdentityKeyPair().getPublicKey());

        byte[] serialized = aliceStore.getIdentityKeyPair().getPublicKey().serialize();
        assertNotNull(serialized);
        assertNotEquals(0, serialized.length);

        serialized = aliceStore.getIdentityKeyPair().getPrivateKey().serialize();
        assertNotNull(serialized);
        assertNotEquals(0, serialized.length);

        // TODO test the session PreKeys
    }

    @Test
    public void testSignedPreKey() throws InvalidKeyException, InvalidKeyIdException, InterruptedException {
        SignalProtocolStore store = new TestInMemoryExtendedSignalProtocolStore2();
        {
            SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(store.getIdentityKeyPair(), 5);

            assertTrue(Curve.verifySignature(store.getIdentityKeyPair().getPublicKey().getPublicKey(),
                    signedPreKey.getKeyPair().getPublicKey().serialize(),
                    signedPreKey.getSignature()));

            store.storeSignedPreKey(22, signedPreKey);
        }

        {
            SignedPreKeyRecord loadedSignedPreKey = store.loadSignedPreKey(22);
            assertTrue(Curve.verifySignature(store.getIdentityKeyPair().getPublicKey().getPublicKey(),
                    loadedSignedPreKey.getKeyPair().getPublicKey().serialize(),
                    loadedSignedPreKey.getSignature()));
        }
    }

    @Test
    public void testAddSession() throws InvalidKeyException, InvalidKeyIdException, UntrustedIdentityException {
        // Setup Bob
        TestInMemoryExtendedSignalProtocolStore2 bobStore = new TestInMemoryExtendedSignalProtocolStore2();

        // Setup Alice
        TestInMemoryExtendedSignalProtocolStore2 aliceStore = new TestInMemoryExtendedSignalProtocolStore2();

        // pre-test
        SignalProtocolAddress bobAddress = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);
        assertFalse(aliceStore.containsSession(bobAddress));

        // build the session
        int preKeyId = bobStore.allocateNextPreKey();
        PreKeyBundle bobPreKey = SignalAdapter.getPreKeyBundle(bobStore, preKeyId, SignalAdapter.SIGNED_PRE_KEY_ID);

        // Add the session
        SessionBuilder aliceSessionBuilder = new SessionBuilder(aliceStore, bobAddress);
        aliceSessionBuilder.process(bobPreKey);

        // test that Alice has a session with Bob
        assertTrue(aliceStore.containsSession(bobAddress));
        assertNotNull(aliceStore.loadSession(bobAddress));
    }

    @Test
    public void testFirstMessage() throws InvalidKeyException, InvalidVersionException, InvalidMessageException, DuplicateMessageException, InvalidKeyIdException, UntrustedIdentityException, LegacyMessageException, NoSessionException, NoSuchAlgorithmException {
        final SignalProtocolAddress BOB_ADDRESS = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);
        final SignalProtocolAddress ALICE_ADDRESS = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);

        // Setup Bob
        TestInMemoryExtendedSignalProtocolStore2 bobStore = new TestInMemoryExtendedSignalProtocolStore2();

        // Setup Alice
        SignalProtocolStore aliceStore = new TestInMemoryExtendedSignalProtocolStore2();

        // pre-test
        assertFalse(aliceStore.containsSession(BOB_ADDRESS));
        assertFalse(bobStore.containsSession(ALICE_ADDRESS));

        // build the session from Bob's pre key
        int preKeyId = bobStore.allocateNextPreKey();
        PreKeyBundle bobPreKey = SignalAdapter.getPreKeyBundle(bobStore, preKeyId, SignalAdapter.SIGNED_PRE_KEY_ID);

        // TODO test PreKeyBundle serialization

        // Add the session to Alice's store
        SessionBuilder aliceSessionBuilder = new SessionBuilder(aliceStore, BOB_ADDRESS);
        aliceSessionBuilder.process(bobPreKey);

        // test that Alice has a session with Bob
        assertTrue(aliceStore.containsSession(BOB_ADDRESS));
        assertNotNull(aliceStore.loadSession(BOB_ADDRESS));

        // create message from Alice
        final String originalMessage = "";
        byte[] incoming = getFirstMessage(originalMessage, aliceStore, BOB_ADDRESS);

        // Bob receives Alice's first message
        PreKeySignalMessage incomingMessage = new PreKeySignalMessage(incoming);

        SessionCipher bobSessionCipher = new SessionCipher(bobStore, ALICE_ADDRESS);
        byte[] plaintext = bobSessionCipher.decrypt(incomingMessage, new DecryptionCallback() {
            @Override
            public void handlePlaintext(byte[] plaintext) {
            }
        });

        assertTrue(bobStore.containsSession(ALICE_ADDRESS));

        String receivedMessage = new String(plaintext);
        assertEquals(originalMessage, receivedMessage);

        runInteraction(aliceStore, bobStore, ALICE_ADDRESS, BOB_ADDRESS);
    }

    private byte[] getFirstMessage(String someText, SignalProtocolStore store, SignalProtocolAddress address) throws UntrustedIdentityException {
        SessionCipher aliceSessionCipher = new SessionCipher(store, address);
        CiphertextMessage outgoingMessage = aliceSessionCipher.encrypt(someText.getBytes());

        return outgoingMessage.serialize();
    }

    private void runInteraction(SignalProtocolStore aliceStore, SignalProtocolStore bobStore, SignalProtocolAddress alice, SignalProtocolAddress bob)
            throws DuplicateMessageException, LegacyMessageException, InvalidMessageException, NoSuchAlgorithmException, NoSessionException, InvalidVersionException, InvalidKeyIdException, InvalidKeyException, UntrustedIdentityException {

        SessionCipher aliceCipher = new SessionCipher(aliceStore, bob);
        SessionCipher bobCipher = new SessionCipher(bobStore, alice);

        byte[] alicePlaintext = "This is a plaintext message.".getBytes();
        CiphertextMessage message = aliceCipher.encrypt(alicePlaintext);
        byte[] bobPlaintext;
        if (message.getType() == CiphertextMessage.WHISPER_TYPE) {
            bobPlaintext = bobCipher.decrypt(new SignalMessage(message.serialize()));
        } else if (message.getType() == CiphertextMessage.PREKEY_TYPE) {
            PreKeySignalMessage incomingMessage = new PreKeySignalMessage(message.serialize());
            bobPlaintext = bobCipher.decrypt(incomingMessage);
        } else {
            fail("Unexpected message type: " + message.getType());
            return;
        }

        assertTrue(Arrays.equals(alicePlaintext, bobPlaintext));

        byte[] bobReply = "This is a message from Bob.".getBytes();
        CiphertextMessage reply = bobCipher.encrypt(bobReply);
        byte[] receivedReply = aliceCipher.decrypt(new SignalMessage(reply.serialize()));

        assertTrue(Arrays.equals(bobReply, receivedReply));

        List<CiphertextMessage> aliceCiphertextMessages = new ArrayList<>();
        List<byte[]> alicePlaintextMessages = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            alicePlaintextMessages.add(("смерть за смерть " + i).getBytes());
            aliceCiphertextMessages.add(aliceCipher.encrypt(("смерть за смерть " + i).getBytes()));
        }

        long seed = System.currentTimeMillis();

        Collections.shuffle(aliceCiphertextMessages, new Random(seed));
        Collections.shuffle(alicePlaintextMessages, new Random(seed));

        for (int i = 0; i < aliceCiphertextMessages.size() / 2; i++) {
            byte[] receivedPlaintext = bobCipher.decrypt(new SignalMessage(aliceCiphertextMessages.get(i).serialize()));
            assertTrue(Arrays.equals(receivedPlaintext, alicePlaintextMessages.get(i)));
        }

        List<CiphertextMessage> bobCiphertextMessages = new ArrayList<>();
        List<byte[]> bobPlaintextMessages = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            bobPlaintextMessages.add(("смерть за смерть " + i).getBytes());
            bobCiphertextMessages.add(bobCipher.encrypt(("смерть за смерть " + i).getBytes()));
        }

        seed = System.currentTimeMillis();

        Collections.shuffle(bobCiphertextMessages, new Random(seed));
        Collections.shuffle(bobPlaintextMessages, new Random(seed));

        for (int i = 0; i < bobCiphertextMessages.size() / 2; i++) {
            byte[] receivedPlaintext = aliceCipher.decrypt(new SignalMessage(bobCiphertextMessages.get(i).serialize()));
            assertTrue(Arrays.equals(receivedPlaintext, bobPlaintextMessages.get(i)));
        }

        for (int i = aliceCiphertextMessages.size() / 2; i < aliceCiphertextMessages.size(); i++) {
            byte[] receivedPlaintext = bobCipher.decrypt(new SignalMessage(aliceCiphertextMessages.get(i).serialize()));
            assertTrue(Arrays.equals(receivedPlaintext, alicePlaintextMessages.get(i)));
        }

        for (int i = bobCiphertextMessages.size() / 2; i < bobCiphertextMessages.size(); i++) {
            byte[] receivedPlaintext = aliceCipher.decrypt(new SignalMessage(bobCiphertextMessages.get(i).serialize()));
            assertTrue(Arrays.equals(receivedPlaintext, bobPlaintextMessages.get(i)));
        }
    }

}