package com.imaginationroom.cryptoandroidservice;

import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.util.KeyHelper;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.imaginationroom.cryptoandroidservice.store.MySignalProtocolStore;
import com.imaginationroom.cryptoandroidservice.store.MySignalProtocolStoreTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SignalDataSourceInstrumentedTest {
    @Test
    public void testEndToEnd() throws Exception {
        ExtendedSignalProtocolStore aliceStore = new TestInMemoryExtendedSignalProtocolStore();
        ExtendedSignalProtocolStore bobStore = new TestInMemoryExtendedSignalProtocolStore();

        testEndToEnd(aliceStore, bobStore);
    }

    @Test
    public void testEndToEndAliceMyStore() throws Exception {
        MySignalProtocolStore aliceStore = MySignalProtocolStoreTest.getStore();
        ExtendedSignalProtocolStore bobStore = new TestInMemoryExtendedSignalProtocolStore();

        testEndToEnd(aliceStore, bobStore);
    }

    @Test
    public void testEndToEndBobMyStore() throws Exception {
        ExtendedSignalProtocolStore aliceStore = new TestInMemoryExtendedSignalProtocolStore();
        MySignalProtocolStore bobStore = MySignalProtocolStoreTest.getStore();

        testEndToEnd(aliceStore, bobStore);
    }

    private void testEndToEnd(ExtendedSignalProtocolStore aliceStore, ExtendedSignalProtocolStore bobStore) throws InvalidKeyException, InvalidKeyIdException, UntrustedIdentityException, InvalidVersionException, DuplicateMessageException, CryptoDataSource.SessionNotFoundException, InvalidMessageException, LegacyMessageException, NoSessionException, NoSuchAlgorithmException {
        String aliceUuid = UUID.randomUUID().toString();
        String bobUuid = UUID.randomUUID().toString();

        SignalDataSource alice = new SignalDataSource(aliceStore);
        SignalDataSource bob = new SignalDataSource(bobStore);

        Invite invite = bob.createInvite();

        IncomingInvite incomingInvite = new IncomingInvite(invite.getRegId(), invite.getPreKeyId(), invite.getPreKey(), invite.getSignedPreKeyId(), invite.getSignedPreKey(), invite.getSignature(), bobStore.getIdentityKeyPair().getPublicKey().getPublicKey().serialize());

        byte[] preKeyMessage = alice.acceptInvite(bobUuid, incomingInvite);
        assertEquals(CiphertextMessage.PREKEY_TYPE, preKeyMessage[0]);
        assertTrue(alice.hasSession(bobUuid));

        byte[] decrypted = bob.inviteAccepted(aliceUuid, preKeyMessage);
        assertTrue(bob.hasSession(aliceUuid));
        assertEquals(0, decrypted.length);

        final String aliceFirstMessage = "Hello - Alice";
        byte[] firstMsg = alice.encrypt(bobUuid, aliceFirstMessage.getBytes());

        final String bobFirstMessage = "Hello from the other side - Bob";
        byte[] bytesFromBobEnc = bob.encrypt(aliceUuid, bobFirstMessage.getBytes());

        // Alice receives Bob's message
        decrypted = bob.decrypt(aliceUuid, firstMsg);
        assertEquals(aliceFirstMessage, new String(decrypted));

        // Bob receives Alice's message
        assertEquals(bobFirstMessage, new String(alice.decrypt(bobUuid, bytesFromBobEnc)));

        runInteraction(alice, bob, aliceUuid, bobUuid);
    }

    private void runInteraction(SignalDataSource alice, SignalDataSource bob, String aliceUuid, String bobUuid)
            throws DuplicateMessageException, LegacyMessageException, InvalidMessageException, NoSuchAlgorithmException, NoSessionException, InvalidVersionException, InvalidKeyIdException, InvalidKeyException, UntrustedIdentityException, CryptoDataSource.SessionNotFoundException {

        byte[] alicePlaintext = "This is a plaintext message.".getBytes();
        byte[] message = alice.encrypt(bobUuid, alicePlaintext);
        byte[] bobPlaintext = bob.decrypt(aliceUuid, message);
        assertTrue(Arrays.equals(alicePlaintext, bobPlaintext));

        byte[] bobReply = "This is a message from Bob.".getBytes();
        byte[] reply = bob.encrypt(aliceUuid, bobReply);
        byte[] receivedReply = alice.decrypt(bobUuid, reply);

        assertTrue(Arrays.equals(bobReply, receivedReply));

        // Prepare messages from Alice
        List<byte[]> aliceCiphertextMessages = new ArrayList<>();
        List<byte[]> alicePlaintextMessages = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            alicePlaintextMessages.add(("смерть за смерть " + i).getBytes());
            aliceCiphertextMessages.add(alice.encrypt(bobUuid, ("смерть за смерть " + i).getBytes()));
        }

        long seed = System.currentTimeMillis();

        Collections.shuffle(aliceCiphertextMessages, new Random(seed));
        Collections.shuffle(alicePlaintextMessages, new Random(seed));

        // test receiving first half of Alice's messages
        for (int i = 0; i < aliceCiphertextMessages.size() / 2; i++) {
            byte[] receivedPlaintext = bob.decrypt(aliceUuid, aliceCiphertextMessages.get(i));
            assertTrue(Arrays.equals(receivedPlaintext, alicePlaintextMessages.get(i)));
        }

        // Prepare messages from Bob
        List<byte[]> bobCiphertextMessages = new ArrayList<>();
        List<byte[]> bobPlaintextMessages = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            bobPlaintextMessages.add(("смерть за смерть " + i).getBytes());
            bobCiphertextMessages.add(bob.encrypt(aliceUuid, ("смерть за смерть " + i).getBytes()));
        }

        seed = System.currentTimeMillis();

        Collections.shuffle(bobCiphertextMessages, new Random(seed));
        Collections.shuffle(bobPlaintextMessages, new Random(seed));

        // receive 1/2 of bob's messages
        for (int i = 0; i < bobCiphertextMessages.size() / 2; i++) {
            byte[] receivedPlaintext = alice.decrypt(bobUuid, bobCiphertextMessages.get(i));
            assertTrue(Arrays.equals(receivedPlaintext, bobPlaintextMessages.get(i)));
        }

        // receive second half of Alice's messages
        for (int i = aliceCiphertextMessages.size() / 2; i < aliceCiphertextMessages.size(); i++) {
            byte[] receivedPlaintext = bob.decrypt(aliceUuid, aliceCiphertextMessages.get(i));
            assertTrue(Arrays.equals(receivedPlaintext, alicePlaintextMessages.get(i)));
        }

        // receive second half of Bob's messages
        for (int i = bobCiphertextMessages.size() / 2; i < bobCiphertextMessages.size(); i++) {
            byte[] receivedPlaintext = alice.decrypt(bobUuid, bobCiphertextMessages.get(i));
            assertTrue(Arrays.equals(receivedPlaintext, bobPlaintextMessages.get(i)));
        }
    }

    @Test
    public void testSigning() throws Exception {
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();

        byte[] nonce = UUID.randomUUID().toString().getBytes();

        byte[] signed = Curve.calculateSignature(pair.getPrivateKey(), nonce);

        assertTrue(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signed));

    }

    @Test
    public void testSigningFails() throws Exception {
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();

        byte[] nonce = UUID.randomUUID().toString().getBytes();

        byte[] signed = Curve.calculateSignature(pair.getPrivateKey(), nonce);

        // insert garbage
        signed[1] ^= (0x01 << 1);

        assertFalse(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signed));
    }


    @Test
    public void base64Nonce() throws Exception {
        String publicKeyHex = "05 F6 68 C4 11 7E A2 05 00 61 96 0D EB 32 96 25 BC 37 9D 03 0B 64 53 6F C2 78 05 AA 71 CB 57 3C 63";
        String privateKeyHex = "C0 16 63 F2 2B 3B 00 F9 9C 8F 9D F8 9B 72 18 44 B8 3A D7 32 D0 DD 1B C5 40 F1 66 8E 64 6A B6 78";
        String nonceHex = "6D 5A 49 48 45 79 74 7A 6E 43 4A 41 6C 59 36 4C 56 2F 61 46 63 32 75 41 48 76 36 67 77 77 69 75 51 6E 6E 64 53 52 6C 78 72 4A 45 3D";
        String signatureHex = "D2 B3 70 8C 78 A6 F9 9E 6C 95 2C FF B7 2F 95 D9 5B ED A3 E7 2F 60 FD C2 43 B6 DB 74 30 45 64 41 56 F0 B8 2E 5E 74 3F BB 2A 33 A8 7C 68 18 94 8C A9 9F 90 F5 B9 E6 71 D8 F6 27 56 0B 9C 84 BF 8A";


        // Test 1
        IdentityKeyPair pair = new IdentityKeyPair(new IdentityKey(Curve.decodePoint(hexStringToBytes(publicKeyHex), 0)), Curve.decodePrivatePoint(hexStringToBytes(privateKeyHex)));

        byte[] nonce = hexStringToBytes(nonceHex);
        byte[] signature = hexStringToBytes(signatureHex);

        assertTrue(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signature));
    }

    @Test
    public void base64Nonce2() throws Exception {
        String publicKeyHex = "05 9F 58 A2 57 E2 BA 3C 13 53 8D 2B E2 C7 96 42 3F 4C 2D 08 D7 16 25 5B 53 DD EA D2 6A F5 DD AD 32";
        String privateKeyHex = "80 AC 7B CA C0 6D 65 47 4F 36 C1 99 A8 53 C8 7E 59 0C 6B D1 92 66 3F 22 0F 32 53 D4 7F 3A 40 72";
        String nonceHex = "34 49 72 4A 49 4C 6F 6C 30 44 6B 6C 41 47 38 73 77 6C 64 6C 46 39 55 51 37 4F 4D 48 50 54 44 6D 34 52 41 4D 69 41 58 51 50 2B 63 3D";
        String signatureHex = "0C EE 3E E0 5C 14 FB 7C 92 B3 3A 5D BA 9D 87 21 10 71 98 41 D9 7F 03 7B E8 65 A2 3C D3 6F 8F 56 BB 83 04 15 5B AE 77 0B CB 60 44 7F 6E EA 2B 43 D2 52 A2 C2 FF FC 7C DB 0E 2E C6 1A DF 22 5C 0F";

        // Test 2
        IdentityKeyPair pair = new IdentityKeyPair(new IdentityKey(Curve.decodePoint(hexStringToBytes(publicKeyHex), 0)), Curve.decodePrivatePoint(hexStringToBytes(privateKeyHex)));
        {
            byte[] nonce = hexStringToBytes(nonceHex);
            byte[] signature = hexStringToBytes(signatureHex);

            assertTrue(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signature));
        }

        // Test 3
        nonceHex = "47 6B 6D 4F 59 54 38 4F 66 31 65 48 6A 33 41 44 35 6B 7A 6B 57 53 79 32 6A 7A 43 50 65 46 4E 36 6C 58 42 31 4D 4F 57 35 42 37 67 3D";
        signatureHex = "6B 4A 52 53 A1 36 90 02 B2 17 46 B1 42 E9 8B 3C 3C 68 5E 78 3F F2 AC 6A 1C 9D 06 C8 FD C8 49 61 EF 32 40 06 79 C7 E5 49 4E 5A 26 02 6D B6 2A 17 DF 7C 11 71 38 09 DA 23 D7 45 C4 A8 C5 93 F4 01";
        {
            byte[] nonce = hexStringToBytes(nonceHex);
            byte[] signature = hexStringToBytes(signatureHex);

            assertTrue(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signature));
        }

        // Test 4
        nonceHex = "39 6C 67 47 34 69 30 4D 57 4C 42 4A 72 39 4C 41 39 63 52 56 37 33 4B 2B 4B 6B 48 30 2F 39 42 77 31 38 48 49 71 66 71 36 6B 67 63 3D";
        signatureHex = "E7 E7 8F 41 78 26 66 5E 3B 6C AE 9B 59 41 07 05 0D 32 76 13 E2 EC 93 DE 08 04 C3 83 07 5F 08 A6 61 45 A8 14 E4 36 0F 69 11 80 B4 6F E7 4C 97 11 D5 31 3C 25 93 68 E5 01 03 ED 43 C1 CD 64 D9 0C";
        {
            byte[] nonce = hexStringToBytes(nonceHex);
            byte[] signature = hexStringToBytes(signatureHex);

            assertTrue(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signature));
        }


        // Test 5, using nonce and signature captured with Charles
        {
            String pub64 = Base64.encodeToString(pair.getPublicKey().serialize(), Base64.NO_WRAP);

            assertEquals("BZ9YolfiujwTU40r4seWQj9MLQjXFiVbU93q0mr13a0y", pub64);
            String nonceBase64 = "9lgG4i0MWLBJr9LA9cRV73K+KkH0/9Bw18HIqfq6kgc=";
            String signatureBase64 = "5+ePQXgmZl47bK6bWUEHBQ0ydhPi7JPeCATDgwdfCKZhRagU5DYPaRGAtG/nTJcR1TE8JZNo5QED7UPBzWTZDA==";
            byte[] nonce = Base64.decode(nonceBase64, Base64.NO_WRAP);
            byte[] signature = Base64.decode(signatureBase64, Base64.NO_WRAP);

            // test that the nonce Base64 encoding was symmetric
            assertEquals(nonceBase64, Base64.encodeToString(nonce, Base64.NO_WRAP));
            // test that the signature Base64 encoding was symmetric
            assertEquals(signatureBase64, Base64.encodeToString(signature, Base64.NO_WRAP));

            byte[] newSignature = Curve.calculateSignature(pair.getPrivateKey(), nonce);

            assertTrue(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, newSignature));

            // turns out the nonce was corrupted - causing this verifySignature to fail.
            assertFalse(Curve.verifySignature(pair.getPublicKey().getPublicKey(), nonce, signature));
        }
    }

    private byte[] hexStringToBytes(String hexWithSpaces) {
        String[] hexes = hexWithSpaces.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String hex : hexes) {
            builder.append(hex);
        }

        return hexStringToByteArray(builder.toString());
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}