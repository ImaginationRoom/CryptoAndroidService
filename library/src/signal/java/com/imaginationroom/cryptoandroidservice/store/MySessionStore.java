package com.imaginationroom.cryptoandroidservice.store;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MySessionStore<DbContainer extends SQLiteDatabase> extends DbBaseStore<DbContainer> implements SessionStore {
    static final int DEVICE_ID = 1;

    private interface SessionContract {
        String TABLE_NAME = "sessions";
        String COLUMN_NAME_NAME = "addressName";
        String COLUMN_NAME_RECORD = "record";

        String CREATE_TABLE_CMD = "CREATE TABLE \"" + TABLE_NAME + "\"" +
                "(" +
                "\"" + COLUMN_NAME_NAME + "\" TEXT NOT NULL PRIMARY KEY," +
                "\"" + COLUMN_NAME_RECORD + "\" BLOB NOT NULL" +
                ") ";

        String SELECT_BY_NAME = COLUMN_NAME_NAME + " is ?";
    }

    static <DbContainer extends SQLiteDatabase> void onCreate(DbContainer sqLiteDatabase) {
        sqLiteDatabase.execSQL(SessionContract.CREATE_TABLE_CMD);
    }

    MySessionStore(DbContainer db) throws InvalidKeyException {
        super(db);
    }

    public void reset() {
        mDb.delete(SessionContract.TABLE_NAME, null, null);
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {
        assertOpen();
        if (address == null) {
            throw new IllegalArgumentException("address");
        }
        if (address.getDeviceId() != DEVICE_ID) {
            throw new IllegalArgumentException("address must use Device Id 1");
        }
        if (record == null) {
            throw new IllegalArgumentException("record");
        }

        ContentValues values = new ContentValues();
        values.put(SessionContract.COLUMN_NAME_NAME, address.getName());
        values.put(SessionContract.COLUMN_NAME_RECORD, record.serialize());
        if (-1 == mDb.insertWithOnConflict(SessionContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)) {
            throw new RuntimeException("Failed to store session");
        }
    }

    @Override
    public boolean containsSession(SignalProtocolAddress address) {
        assertOpen();
        if (address == null) {
            throw new IllegalArgumentException("address");
        }

        return address.getDeviceId() == DEVICE_ID && containsSession(address.getName());
    }

    private boolean containsSession(String name) {

        Cursor cursor = null;
        try {
            cursor = mDb.query(SessionContract.TABLE_NAME,
                    new String[]{SessionContract.COLUMN_NAME_NAME, SessionContract.COLUMN_NAME_RECORD},
                    SessionContract.SELECT_BY_NAME,
                    new String[]{name},
                    null,
                    null,
                    null,
                    null
            );

            return (cursor != null && cursor.moveToFirst());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress address) {
        assertOpen();
        if (address == null) {
            throw new IllegalArgumentException("address");
        }

        if (address.getDeviceId() != DEVICE_ID) {
            return null;
        }

        Cursor cursor = null;
        try {
            cursor = mDb.query(SessionContract.TABLE_NAME,
                    new String[]{SessionContract.COLUMN_NAME_NAME, SessionContract.COLUMN_NAME_RECORD},
                    SessionContract.SELECT_BY_NAME,
                    new String[]{address.getName()},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return new SessionRecord(cursor.getBlob(cursor.getColumnIndexOrThrow(SessionContract.COLUMN_NAME_RECORD)));
            } else {
                return new SessionRecord();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        if (address.getDeviceId() == DEVICE_ID) {
            deleteAllSessions(address.getName());
        }
    }

    @Override
    public void deleteAllSessions(String name) {
        assertOpen();

        mDb.delete(SessionContract.TABLE_NAME,
                SessionContract.SELECT_BY_NAME,
                new String[]{name}
        );
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        List<Integer> result = new ArrayList<>();

        if (containsSession(name)) {
            result.add(DEVICE_ID);
        }

        return result;
    }
}
