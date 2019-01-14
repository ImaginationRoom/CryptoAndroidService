package com.imaginationroom.cryptoandroidservice.store;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.util.List;

import com.imaginationroom.cryptoandroidservice.ExtendedSignalProtocolStore;
import com.imaginationroom.cryptoandroidservice.PasswordProvider;


public class MySignalProtocolStore implements ExtendedSignalProtocolStore {
    private final static String TAG = "Signal";
    //    final static String DB_FILENAME = PasswordProvider.getDatabaseName();
    private final static int DB_VERSION = 1;

    private final PasswordProvider passwordProvider;
    private SQLiteDatabase db;
    private final MyIdentityKeyStore identityKeyStore;
    private final MyPreKeyStore preKeyStore;
    private final MySessionStore sessionStore;
    private final MySignedPreKeyStore signedPreKeyStore;


    public MySignalProtocolStore(Context context) throws InvalidKeyException {
        SQLiteDatabase.loadLibs(context);

        passwordProvider = new PasswordProvider(context);

        SQLiteOpenHelper dbOpenHelper = new SQLiteOpenHelper(context, PasswordProvider.getDatabaseName(), null, DB_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {
                IdentityKeyPair pair = MyIdentityKeyStore.onCreate(sqLiteDatabase);
                MyPreKeyStore.onCreate(sqLiteDatabase);
                MySessionStore.onCreate(sqLiteDatabase);
                try {
                    MySignedPreKeyStore.onCreate(sqLiteDatabase, pair);
                } catch (InvalidKeyException e) {
                    Log.e(TAG, "Failed to create signed pre key: " + e);
                }
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
                //

            }
        };

        db = dbOpenHelper.getWritableDatabase(passwordProvider.getPassword());
        identityKeyStore = new MyIdentityKeyStore<>(db);

        preKeyStore = new MyPreKeyStore<>(db);
        sessionStore = new MySessionStore<>(db);
        signedPreKeyStore = new MySignedPreKeyStore<>(db);
    }

    public void close() {
        identityKeyStore.close();
        preKeyStore.close();
        sessionStore.close();
        signedPreKeyStore.close();
        db.close();
    }


    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyStore.getIdentityKeyPair();
    }

    @Override
    public int getLocalRegistrationId() {
        return identityKeyStore.getLocalRegistrationId();
    }

    @Override
    public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        return identityKeyStore.saveIdentity(address, identityKey);
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        return identityKeyStore.isTrustedIdentity(address, identityKey, direction);
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        return preKeyStore.loadPreKey(preKeyId);
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {
        preKeyStore.storePreKey(preKeyId, record);
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        return preKeyStore.containsPreKey(preKeyId);
    }

    @Override
    public void removePreKey(int preKeyId) {
        preKeyStore.removePreKey(preKeyId);
    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress address) {
        return sessionStore.loadSession(address);
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        return sessionStore.getSubDeviceSessions(name);
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {
        sessionStore.storeSession(address, record);
    }

    @Override
    public boolean containsSession(SignalProtocolAddress address) {
        return sessionStore.containsSession(address);
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        sessionStore.deleteSession(address);
    }

    @Override
    public void deleteAllSessions(String name) {
        sessionStore.deleteAllSessions(name);
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        return signedPreKeyStore.loadSignedPreKey(signedPreKeyId);
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return signedPreKeyStore.loadSignedPreKeys();
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        signedPreKeyStore.storeSignedPreKey(signedPreKeyId, record);
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        return signedPreKeyStore.containsSignedPreKey(signedPreKeyId);
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        signedPreKeyStore.removeSignedPreKey(signedPreKeyId);
    }

    @Override
    public int allocateNextPreKey() {
        return preKeyStore.allocateNextPreKey();
    }

    @Override
    public void reset() {
        identityKeyStore.reset();
        preKeyStore.reset();
        sessionStore.reset();
        try {
            signedPreKeyStore.reset(identityKeyStore.getIdentityKeyPair());
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Failed to reset signed pre key: " + e);
        }
    }
}
