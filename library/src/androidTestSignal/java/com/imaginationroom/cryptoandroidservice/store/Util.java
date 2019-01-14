package com.imaginationroom.cryptoandroidservice.store;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

import static org.junit.Assert.assertTrue;

class Util {
    static SQLiteDatabase getDatabase(String dbName, @SuppressWarnings("UnusedParameters") String password) {
        Context context = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase.loadLibs(context);

        File databaseFile = context.getDatabasePath(dbName);

        assertTrue(databaseFile.mkdirs() || (databaseFile.getParentFile().exists() && databaseFile.getParentFile().isDirectory()));
        assertTrue(databaseFile.delete() || !databaseFile.exists());
        return SQLiteDatabase.openOrCreateDatabase(databaseFile, password, null);
    }

    private Util() {
    }

    static void assertArraysNotEqual(byte[] expected, byte[] actual) {
        boolean differ = expected.length != actual.length;
        for (int i = 0; i < expected.length && i < actual.length; i++) {
            differ = differ || (expected[i] == actual[i]);
        }
        assertTrue(differ);
    }
}
