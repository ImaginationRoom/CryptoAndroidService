package com.imaginationroom.cryptoandroidservice.store;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.Arrays;

class MyIdentityKeyStore<DbContainer extends SQLiteDatabase> extends DbBaseStore<DbContainer> implements IdentityKeyStore {
    private interface RegistrationContract {
        String TABLE_NAME = "registation";
        String COLUMN_NAME_REG_ID = "regId";
        String COLUMN_NAME_KEY_PAIR = "keyPair";

        String CREATE_TABLE_CMD = "CREATE TABLE \"" + TABLE_NAME + "\"" +
                "(" +
                "\"" + COLUMN_NAME_REG_ID + "\" integer NOT NULL," +
                "\"" + COLUMN_NAME_KEY_PAIR + "\" BLOB NOT NULL" +
                ") ";
    }

    private interface TrustedIdentitiesContract {
        String TABLE_NAME = "trustedIdentities";
        String COLUMN_NAME_NAME = "name";
        String COLUMN_NAME_IDENTITY_KEY = "identityKey";
        String CREATE_TABLE_CMD = "CREATE TABLE \"" + TABLE_NAME + "\"" +
                "(" +
                "\"" + COLUMN_NAME_NAME + "\" TEXT NOT NULL PRIMARY KEY," +
                "\"" + COLUMN_NAME_IDENTITY_KEY + "\" BLOB NOT NULL" +
                ") ";
        String SELECT_BY_NAME = COLUMN_NAME_NAME + " is ?";
    }

    static <DbContainer extends SQLiteDatabase> IdentityKeyPair onCreate(DbContainer sqLiteDatabase) {
        sqLiteDatabase.execSQL(RegistrationContract.CREATE_TABLE_CMD);
        sqLiteDatabase.execSQL(TrustedIdentitiesContract.CREATE_TABLE_CMD);

        return init(sqLiteDatabase);
    }

    static <DbContainer extends SQLiteDatabase> IdentityKeyPair init(DbContainer sqLiteDatabase) {
        int regId = KeyHelper.generateRegistrationId(false);
        IdentityKeyPair identity = KeyHelper.generateIdentityKeyPair();

        ContentValues values = new ContentValues();
        values.put(RegistrationContract.COLUMN_NAME_REG_ID, regId);
        values.put(RegistrationContract.COLUMN_NAME_KEY_PAIR, identity.serialize());
        if (sqLiteDatabase.insert(RegistrationContract.TABLE_NAME, null, values) < 0) {
            throw new RuntimeException("Failed to initialize IdentityKeyStore db");
        }

        return identity;
    }

    private int registrationId;
    private IdentityKeyPair pair;

    MyIdentityKeyStore(DbContainer db) throws InvalidKeyException {
        super(db);
        open();
    }

    private void open() throws InvalidKeyException {

        Cursor cursor = null;
        try {
            cursor = mDb.query(RegistrationContract.TABLE_NAME,
                    new String[]{RegistrationContract.COLUMN_NAME_REG_ID, RegistrationContract.COLUMN_NAME_KEY_PAIR},
                    null,
                    null, null,
                    null,
                    null,
                    "1"
            );

            if (cursor == null || !cursor.moveToFirst()) {
                throw new IllegalStateException("Registration Id not found");
            }

            registrationId = cursor.getInt(cursor.getColumnIndexOrThrow(RegistrationContract.COLUMN_NAME_REG_ID));

            pair = new IdentityKeyPair(cursor.getBlob(cursor.getColumnIndexOrThrow(RegistrationContract.COLUMN_NAME_KEY_PAIR)));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void reset() {
        mDb.delete(RegistrationContract.TABLE_NAME, null, null);
        mDb.delete(TrustedIdentitiesContract.TABLE_NAME, null, null);
        init(mDb);
        try {
            open();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        assertOpen();
        return pair;
    }

    @Override
    public int getLocalRegistrationId() {
        assertOpen();
        return registrationId;
    }

    @Override
    public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        assertOpen();
        if (address == null) {
            throw new IllegalArgumentException("address");
        }
        if (identityKey == null) {
            throw new IllegalArgumentException("identityKey");
        }

        ContentValues values = new ContentValues();
        values.put(TrustedIdentitiesContract.COLUMN_NAME_NAME, address.getName());
        values.put(TrustedIdentitiesContract.COLUMN_NAME_IDENTITY_KEY, identityKey.serialize());
        if (-1 == mDb.insertWithOnConflict(TrustedIdentitiesContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)) {
            throw new RuntimeException("Failed to insert trusted identity");
        }
        return false;
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        assertOpen();
        if (address == null) {
            throw new IllegalArgumentException("address");
        }
        if (identityKey == null) {
            throw new IllegalArgumentException("identityKey");
        }

        Cursor cursor = null;
        try {
            cursor = mDb.query(TrustedIdentitiesContract.TABLE_NAME,
                    new String[]{TrustedIdentitiesContract.COLUMN_NAME_NAME, TrustedIdentitiesContract.COLUMN_NAME_IDENTITY_KEY},
                    TrustedIdentitiesContract.SELECT_BY_NAME,
                    new String[]{address.getName()},
                    null,
                    null,
                    null,
                    null
            );

            // 'trust on first use.'
            if (cursor == null || !cursor.moveToFirst()) {
                return true;
            }
            return Arrays.equals(identityKey.serialize(),
                    cursor.getBlob(cursor.getColumnIndexOrThrow(TrustedIdentitiesContract.COLUMN_NAME_IDENTITY_KEY)));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
