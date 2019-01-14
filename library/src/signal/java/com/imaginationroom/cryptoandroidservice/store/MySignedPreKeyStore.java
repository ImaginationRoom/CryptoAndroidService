package com.imaginationroom.cryptoandroidservice.store;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.imaginationroom.cryptoandroidservice.SignalAdapter.SIGNED_PRE_KEY_ID;

class MySignedPreKeyStore<DbContainer extends SQLiteDatabase> extends DbBaseStore<DbContainer> implements SignedPreKeyStore {
    private interface SignedPreKeysContract {
        String TABLE_NAME = "signedPreKeys";
        String COLUMN_NAME_ID = "id";
        String COLUMN_NAME_RECORD = "record";

        String CREATE_TABLE_CMD = "CREATE TABLE \"" + TABLE_NAME + "\"" +
                "(" +
                "\"" + COLUMN_NAME_ID + "\" INTEGER NOT NULL PRIMARY KEY," +
                "\"" + COLUMN_NAME_RECORD + "\" BLOB NOT NULL" +
                ") ";

        String SELECT_BY_ID = COLUMN_NAME_ID + " = ?";
    }

    static <DbContainer extends SQLiteDatabase> void onCreate(DbContainer sqLiteDatabase, IdentityKeyPair identityKeyPair) throws InvalidKeyException {
        sqLiteDatabase.execSQL(SignedPreKeysContract.CREATE_TABLE_CMD);

        init(sqLiteDatabase, identityKeyPair);
    }

    private static <DbContainer extends SQLiteDatabase> void init(DbContainer sqLiteDatabase, IdentityKeyPair identityKeyPair) throws InvalidKeyException {
        // Allocate a Signed Pre Key
        SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, SIGNED_PRE_KEY_ID);

        ContentValues values = new ContentValues();
        values.put(SignedPreKeysContract.COLUMN_NAME_ID, SIGNED_PRE_KEY_ID);
        values.put(SignedPreKeysContract.COLUMN_NAME_RECORD, signedPreKey.serialize());
        if (-1 == sqLiteDatabase.insert(SignedPreKeysContract.TABLE_NAME, null, values)) {
            if (-1 == sqLiteDatabase.updateWithOnConflict(SignedPreKeysContract.TABLE_NAME, values, SignedPreKeysContract.SELECT_BY_ID,
                    new String[]{String.valueOf(SIGNED_PRE_KEY_ID)}, SQLiteDatabase.CONFLICT_REPLACE)) {
                throw new RuntimeException("Failed to insert trusted identity");
            }
        }
    }

    MySignedPreKeyStore(DbContainer db) throws InvalidKeyException {
        super(db);
    }

    public void reset(IdentityKeyPair pair) throws InvalidKeyException {
        mDb.delete(SignedPreKeysContract.TABLE_NAME, null, null);
        init(mDb, pair);
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        assertOpen();
        if (record == null) {
            throw new IllegalArgumentException("record");
        }

        ContentValues values = new ContentValues();
        values.put(SignedPreKeysContract.COLUMN_NAME_ID, signedPreKeyId);
        values.put(SignedPreKeysContract.COLUMN_NAME_RECORD, record.serialize());
        if (-1 == mDb.insertWithOnConflict(SignedPreKeysContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)) {
            throw new RuntimeException("Failed to insert trusted identity");
        }
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        assertOpen();

        Cursor cursor = null;
        try {
            cursor = mDb.query(SignedPreKeysContract.TABLE_NAME,
                    new String[]{SignedPreKeysContract.COLUMN_NAME_ID},
                    SignedPreKeysContract.SELECT_BY_ID,
                    new String[]{String.valueOf(signedPreKeyId)},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        assertOpen();

        Cursor cursor = null;
        try {
            cursor = mDb.query(SignedPreKeysContract.TABLE_NAME,
                    new String[]{SignedPreKeysContract.COLUMN_NAME_ID, SignedPreKeysContract.COLUMN_NAME_RECORD},
                    SignedPreKeysContract.SELECT_BY_ID,
                    new String[]{String.valueOf(signedPreKeyId)},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return new SignedPreKeyRecord(cursor.getBlob(cursor.getColumnIndexOrThrow(SignedPreKeysContract.COLUMN_NAME_RECORD)));
            }
        } catch (IOException e) {
            throw new InvalidKeyIdException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        throw new InvalidKeyIdException("loadSignedPreKey(" + signedPreKeyId + ")");
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        assertOpen();

        Cursor cursor = null;
        List<SignedPreKeyRecord> result = new ArrayList<>();
        try {
            cursor = mDb.query(SignedPreKeysContract.TABLE_NAME,
                    new String[]{SignedPreKeysContract.COLUMN_NAME_ID, SignedPreKeysContract.COLUMN_NAME_RECORD},
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    result.add(new SignedPreKeyRecord(cursor.getBlob(cursor.getColumnIndexOrThrow(SignedPreKeysContract.COLUMN_NAME_RECORD))));
                } while (cursor.moveToNext());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        assertOpen();

        mDb.delete(SignedPreKeysContract.TABLE_NAME,
                SignedPreKeysContract.SELECT_BY_ID,
                new String[]{String.valueOf(signedPreKeyId)}
        );

    }
}
