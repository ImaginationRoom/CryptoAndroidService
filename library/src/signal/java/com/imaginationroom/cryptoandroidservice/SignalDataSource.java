package com.imaginationroom.cryptoandroidservice;

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

import com.imaginationroom.cryptoandroidservice.mapper.PreKeyBundleMapper;

class SignalDataSource implements CryptoDataSource {
    private final ExtendedSignalProtocolStore mStore;
    private static final int DEVICE_ID = 1; // multi-device not supported

    SignalDataSource(ExtendedSignalProtocolStore store) throws InvalidKeyException {
        mStore = store;
    }

    @Override
    public boolean hasSession(String uuid) {
        return mStore.containsSession(new SignalProtocolAddress(uuid, DEVICE_ID));
    }

    @Override
    public Invite createInvite() throws InvalidKeyIdException {
        int id = mStore.allocateNextPreKey();

        PreKeyBundle preKey = SignalAdapter.getPreKeyBundle(mStore, id, SignalAdapter.SIGNED_PRE_KEY_ID);
        return PreKeyBundleMapper.transform(preKey);
    }

    @Override
    public void deleteInvite(int preKeyId) {
        mStore.removePreKey(preKeyId);
    }

    @Override
    public byte[] acceptInvite(String uuid, IncomingInvite invite) throws InvalidKeyException, UntrustedIdentityException {
        SignalProtocolAddress address = new SignalProtocolAddress(uuid, DEVICE_ID);
        SessionBuilder sessionBuilder = new SessionBuilder(mStore, address);

        sessionBuilder.process(PreKeyBundleMapper.transform(invite));

        SessionCipher aliceSessionCipher = new SessionCipher(mStore, address);
        CiphertextMessage outgoingMessage = aliceSessionCipher.encrypt(new byte[0]);

        return wrap((byte) outgoingMessage.getType(), outgoingMessage.serialize());
    }

    @Override
    public byte[] inviteAccepted(String uuid, byte[] preKeyMessage) throws InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, UntrustedIdentityException, LegacyMessageException {
        if (preKeyMessage == null || preKeyMessage.length == 0) {
            throw new InvalidMessageException("Empty PreKey");
        }

        byte type = preKeyMessage[0];
        if (type != CiphertextMessage.PREKEY_TYPE) {
            throw new InvalidMessageException("Unsupported cipher message type: " + type);
        }

        SignalProtocolAddress address = new SignalProtocolAddress(uuid, DEVICE_ID);

        PreKeySignalMessage incomingMessage = new PreKeySignalMessage(unwrap(preKeyMessage));
        SessionCipher sessionCipher = new SessionCipher(mStore, address);

        return sessionCipher.decrypt(incomingMessage);
    }

    private byte[] wrap(byte type, byte[] message) {
        byte[] result = new byte[1 + message.length];

        result[0] = type;

        System.arraycopy(message, 0, result, 1, message.length);

        return result;
    }

    private byte[] unwrap(byte[] message) {
        return java.util.Arrays.copyOfRange(message, 1, message.length);
    }

    @Override
    public byte[] encrypt(String uuid, byte[] clear) throws SessionNotFoundException, InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, UntrustedIdentityException, LegacyMessageException {
        SessionCipher aliceSessionCipher = new SessionCipher(mStore, new SignalProtocolAddress(uuid, DEVICE_ID));
        CiphertextMessage outgoingMessage = aliceSessionCipher.encrypt(clear);
        return wrap((byte) outgoingMessage.getType(), outgoingMessage.serialize());
    }

    @Override
    public byte[] decrypt(String uuid, byte[] encrypted) throws SessionNotFoundException, InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, UntrustedIdentityException, LegacyMessageException, NoSessionException {
        byte type = encrypted[0];
        byte[] unwrapped = unwrap(encrypted);

        switch (type) {
            case CiphertextMessage.PREKEY_TYPE: {
                PreKeySignalMessage incomingMessage = new PreKeySignalMessage(unwrapped);

                SessionCipher bobSessionCipher = new SessionCipher(mStore, new SignalProtocolAddress(uuid, DEVICE_ID));
                return bobSessionCipher.decrypt(incomingMessage);
            }
            case CiphertextMessage.WHISPER_TYPE: {
                SessionCipher bobSessionCipher = new SessionCipher(mStore, new SignalProtocolAddress(uuid, DEVICE_ID));
                return bobSessionCipher.decrypt(new SignalMessage(unwrapped));
            }
            default:
                throw new InvalidMessageException("Unsupported cipher message type: " + type);
        }
    }

    @Override
    public void deleteSession(String uuid) {
        SignalProtocolAddress remoteAddress = new SignalProtocolAddress(uuid, DEVICE_ID);
        mStore.deleteSession(remoteAddress);
    }

    @Override
    public void reset() {
        mStore.reset();
    }

    @Override
    public byte[] getPublicKey() {
        return mStore.getIdentityKeyPair().getPublicKey().getPublicKey().serialize();
    }

    @Override
    public byte[] sign(byte[] nonce) throws InvalidKeyException {
        return Curve.calculateSignature(mStore.getIdentityKeyPair().getPrivateKey(), nonce);
    }
}
