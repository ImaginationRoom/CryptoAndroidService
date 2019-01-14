package com.imaginationroom.cryptoandroidservice;

// TODO remove dependencies on libsignal

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.UntrustedIdentityException;

public interface CryptoDataSource {
    boolean hasSession(String uuid);

    Invite createInvite() throws Exception;

    void deleteInvite(int preKeyId);

    byte[] acceptInvite(String uuid, IncomingInvite invite) throws Exception;

    byte[] encrypt(String uuid, byte[] clear) throws SessionNotFoundException, InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, UntrustedIdentityException, LegacyMessageException;

    byte[] decrypt(String uuid, byte[] encrypted) throws SessionNotFoundException, InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, UntrustedIdentityException, LegacyMessageException, NoSessionException;

    void deleteSession(String uuid);

    void reset();

    byte[] getPublicKey();

    byte[] sign(byte[] nonce) throws InvalidKeyException;

    byte[] inviteAccepted(String uuid, byte[] preKeyMessage) throws Exception;

    class SessionNotFoundException extends Exception {
        public SessionNotFoundException(String uuid) {
            super("Unknown Session UUID: " + uuid);
        }
    }

    class SessionAlreadyExistsException extends Exception {
        public SessionAlreadyExistsException(String uuid) {
            super("Session UUID already exists: " + uuid);
        }
    }
}
