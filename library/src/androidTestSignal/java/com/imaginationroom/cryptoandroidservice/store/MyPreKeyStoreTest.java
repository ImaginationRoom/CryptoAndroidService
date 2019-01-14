package com.imaginationroom.cryptoandroidservice.store;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Test;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.List;

import static org.junit.Assert.*;

public class MyPreKeyStoreTest {
    private final static int UnknownId = 1;
    private final static int KnownId = 10;

    @Test
    public void createEmpty() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyPreKeyStore.onCreate(database);
        MyPreKeyStore store = null;
        try {
            store = new MyPreKeyStore<>(database);
            assertNotNull(store);

            testCreateEmpty(store);

        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testCreateEmpty(PreKeyStore store) throws InvalidKeyIdException {
        assertFalse(store.containsPreKey(UnknownId));
        try {
            assertNull(store.loadPreKey(UnknownId));
            fail("Should have thrown an exception");
        } catch (InvalidKeyIdException invalidKeyId) {
            // success!
        }
        store.removePreKey(UnknownId);
    }

    @Test
    public void reset() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyPreKeyStore.onCreate(database);
        MyPreKeyStore store = null;
        try {
            store = new MyPreKeyStore<>(database);
            assertNotNull(store);
            testStoreAndLoad(store);
            store.reset();

            assertFalse(store.containsPreKey(KnownId));
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    @Test
    public void storeAndLoad() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyPreKeyStore.onCreate(database);
        MyPreKeyStore store = null;
        try {
            store = new MyPreKeyStore<>(database);
            assertNotNull(store);

            testStoreAndLoad(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static int testStoreAndLoad(PreKeyStore store) throws InvalidKeyIdException {
        PreKeyRecord original = KeyHelper.generatePreKeys(KnownId, 1).get(0);
        store.storePreKey(KnownId, original);

        assertTrue(store.containsPreKey(KnownId));

        PreKeyRecord record = store.loadPreKey(KnownId);
        assertNotNull(record);
        assertArrayEquals(original.serialize(), record.serialize());

        return KnownId;
    }

    @Test
    public void update() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyPreKeyStore.onCreate(database);
        MyPreKeyStore store = null;
        try {
            store = new MyPreKeyStore<>(database);
            assertNotNull(store);

            testUpdate(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testUpdate(PreKeyStore store) throws InvalidKeyIdException {
        PreKeyRecord original = KeyHelper.generatePreKeys(KnownId, 1).get(0);
        store.storePreKey(KnownId, original);

        assertTrue(store.containsPreKey(KnownId));

        PreKeyRecord updated = KeyHelper.generatePreKeys(KnownId, 1).get(0);
        store.storePreKey(KnownId, updated);

        PreKeyRecord record = store.loadPreKey(KnownId);
        assertNotNull(record);
        assertArrayEquals(updated.serialize(), record.serialize());
    }

    @Test
    public void contains() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyPreKeyStore.onCreate(database);
        MyPreKeyStore store = null;
        try {
            store = new MyPreKeyStore<>(database);
            assertNotNull(store);

            testContains(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testContains(PreKeyStore store) throws InvalidKeyIdException {
        int count = 1000;
        List<PreKeyRecord> l = KeyHelper.generatePreKeys(KnownId, count);
        for (int i = 0; i < l.size(); i++) {
            int thisId = KnownId + i;
            store.storePreKey(thisId, l.get(i));
            assertTrue(store.containsPreKey(thisId));

            PreKeyRecord record = store.loadPreKey(thisId);
            assertNotNull(record);
            assertArrayEquals(l.get(i).serialize(), record.serialize());
        }
    }

    @Test
    public void storeAndRemove() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyPreKeyStore.onCreate(database);
        MyPreKeyStore store = null;
        try {
            store = new MyPreKeyStore<>(database);
            assertNotNull(store);

            testStoreAndRemote(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testStoreAndRemote(PreKeyStore store) {
        List<PreKeyRecord> l = KeyHelper.generatePreKeys(KnownId, 1);
        store.storePreKey(KnownId, l.get(0));
        assertTrue(store.containsPreKey(KnownId));

        store.removePreKey(KnownId);

        assertFalse(store.containsPreKey(KnownId));
    }
}