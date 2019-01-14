package com.imaginationroom.cryptoandroidservice.store;

import net.sqlcipher.database.SQLiteDatabase;

class DbBaseStore<DbContainer extends SQLiteDatabase> {
    DbContainer mDb;

    DbBaseStore(DbContainer db) {
        mDb = db;
    }

    public void close() {
        mDb = null;
    }

    void assertOpen() {
        if (mDb == null) {
            throw new RuntimeException("Db not opened");
        }
    }
}
