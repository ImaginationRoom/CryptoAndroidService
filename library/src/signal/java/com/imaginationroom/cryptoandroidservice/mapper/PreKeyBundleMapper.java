package com.imaginationroom.cryptoandroidservice.mapper;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.state.PreKeyBundle;

import com.imaginationroom.cryptoandroidservice.IncomingInvite;
import com.imaginationroom.cryptoandroidservice.Invite;

import static com.imaginationroom.cryptoandroidservice.SignalAdapter.DEVICE_ID;

public class PreKeyBundleMapper {

    public static PreKeyBundle transform(IncomingInvite invite) throws InvalidKeyException {
        return new PreKeyBundle(
                invite.getRegId(),
                DEVICE_ID,
                invite.getPreKeyId(),
                Curve.decodePoint(invite.getPreKey(), 0),
                invite.getSignedPreKeyId(),
                Curve.decodePoint(invite.getSignedPreKey(), 0),
                invite.getSignature(),
                new IdentityKey(invite.getIdentityKey(), 0));
    }

    public static Invite transform(PreKeyBundle bundle) {
        return new Invite(
                bundle.getRegistrationId(),
                bundle.getPreKeyId(),
                bundle.getPreKey().serialize(),
                bundle.getSignedPreKeyId(),
                bundle.getSignedPreKey().serialize(),
                bundle.getSignedPreKeySignature()
        );
    }
}
