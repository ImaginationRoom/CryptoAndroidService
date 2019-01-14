package com.imaginationroom.cryptoandroidservice;

import com.imaginationroom.cryptoandroidservice.CryptoResponse;
import com.imaginationroom.cryptoandroidservice.Invite;
import com.imaginationroom.cryptoandroidservice.IncomingInvite;

interface ICryptoService {
    byte[] getPublicKey();

    CryptoResponse sign(in byte[] nonce);

    boolean hasSession(in String uuid);

    Invite createInvite();

    CryptoResponse deleteInvite(in int preKeyId);

    CryptoResponse acceptInvite(in String uuid, in IncomingInvite invite);

    CryptoResponse inviteAccepted(in String uuid, in byte[] preKeyMessage);

    CryptoResponse encrypt(in String uuid, in byte[] clear);

    CryptoResponse decrypt(in String uuid, in byte[] encrypted);

    CryptoResponse deleteSession(in String uuid);

    void reset();
}
