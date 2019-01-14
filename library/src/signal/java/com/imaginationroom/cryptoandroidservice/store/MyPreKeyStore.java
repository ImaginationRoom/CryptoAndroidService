package com.imaginationroom.cryptoandroidservice.store;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.IOException;
import java.util.List;

class MyPreKeyStore<DbContainer extends SQLiteDatabase> extends DbBaseStore<DbContainer> implements PreKeyStore {
    private interface PreKeyContract {
        String TABLE_NAME = "preKeys";
        String COLUMN_NAME_ID = "id";
        String COLUMN_NAME_RECORD = "record";
        String CREATE_TABLE_CMD = "CREATE TABLE \"" + TABLE_NAME + "\"" +
                "(" +
                "\"" + COLUMN_NAME_ID + "\" INTEGER NOT NULL PRIMARY KEY," +
                "\"" + COLUMN_NAME_RECORD + "\" BLOB NOT NULL" +
                ") ";
        String SELECT_BY_ID = COLUMN_NAME_ID + " = ?";
    }

    static <DbContainer extends SQLiteDatabase> void onCreate(DbContainer sqLiteDatabase) {
        sqLiteDatabase.execSQL(PreKeyContract.CREATE_TABLE_CMD);
    }

    MyPreKeyStore(DbContainer db) throws InvalidKeyException {
        super(db);
    }

    public void reset() {
        mDb.delete(PreKeyContract.TABLE_NAME, null, null);
    }

    private static final String TAG = "SIGNAL";

    int allocateNextPreKey() {
        int id = getLargestId();
        Log.d(TAG, "Allocate pre key for id = " + id);
        List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(id, 1);
        for (PreKeyRecord preKeyRecord : preKeys) {
            storePreKey(id, preKeyRecord);
        }

        return id;
    }

    private int getLargestId() {
        assertOpen();

        int id = 0;
        Cursor cursor = null;
        try {
            cursor = mDb.query(PreKeyContract.TABLE_NAME,
                    new String[]{PreKeyContract.COLUMN_NAME_ID},
                    null,
                    null,
                    null,
                    null,
                    PreKeyContract.COLUMN_NAME_ID + " DESC",
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(PreKeyContract.COLUMN_NAME_ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id + 1;
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        assertOpen();

        Cursor cursor = null;
        try {
            cursor = mDb.query(PreKeyContract.TABLE_NAME,
                    new String[]{PreKeyContract.COLUMN_NAME_ID, PreKeyContract.COLUMN_NAME_RECORD},
                    PreKeyContract.SELECT_BY_ID,
                    new String[]{String.valueOf(preKeyId)},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return new PreKeyRecord(cursor.getBlob(cursor.getColumnIndexOrThrow(PreKeyContract.COLUMN_NAME_RECORD)));
            }
        } catch (IOException e) {
            throw new InvalidKeyIdException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        throw new InvalidKeyIdException("loadPreKey(" + preKeyId + ")");
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {
        assertOpen();
        if (record == null) {
            throw new IllegalArgumentException("record");
        }

        ContentValues values = new ContentValues();
        values.put(PreKeyContract.COLUMN_NAME_ID, preKeyId);
        values.put(PreKeyContract.COLUMN_NAME_RECORD, record.serialize());
        if (-1 == mDb.insertWithOnConflict(PreKeyContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)) {
            throw new RuntimeException("Failed to insert trusted identity");
        }
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        assertOpen();

        Cursor cursor = null;
        try {
            cursor = mDb.query(PreKeyContract.TABLE_NAME,
                    new String[]{PreKeyContract.COLUMN_NAME_ID},
                    PreKeyContract.SELECT_BY_ID,
                    new String[]{String.valueOf(preKeyId)},
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
    public void removePreKey(int preKeyId) {
        assertOpen();

        mDb.delete(PreKeyContract.TABLE_NAME,
                PreKeyContract.SELECT_BY_ID,
                new String[]{String.valueOf(preKeyId)}
        );

    }
}
