package com.imaginationroom.cryptoandroidservice.store;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Test;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.ArrayList;
import java.util.List;

import static com.imaginationroom.cryptoandroidservice.SignalAdapter.SIGNED_PRE_KEY_ID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MySignedPreKeyStoreTest {

    @Test
    public void create() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;
        try {
            store = new MySignedPreKeyStore<>(database);
            assertNotNull(store);

            testInitial(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testInitial(SignedPreKeyStore store) throws InvalidKeyIdException {
        assertFalse(store.containsSignedPreKey(1));
        try {
            assertNull(store.loadSignedPreKey(1));
            fail("load should have thrown exception");
        } catch (InvalidKeyIdException invalidKeyException) {
            // success!
        }
        store.removeSignedPreKey(1);

        assertTrue(store.containsSignedPreKey(SIGNED_PRE_KEY_ID));
        assertNotNull(store.loadSignedPreKey(SIGNED_PRE_KEY_ID));
    }

    @Test
    public void reset() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;

        try {
            store = new MySignedPreKeyStore<>(database);

            assertEquals(1, store.loadSignedPreKeys().size());
            assertTrue(store.containsSignedPreKey(SIGNED_PRE_KEY_ID));
            SignedPreKeyRecord record = store.loadSignedPreKey(SIGNED_PRE_KEY_ID);
            byte[] orig = record.serialize();

            store.reset(KeyHelper.generateIdentityKeyPair());
            assertEquals(1, store.loadSignedPreKeys().size());
            assertTrue(store.containsSignedPreKey(SIGNED_PRE_KEY_ID));
            record = store.loadSignedPreKey(SIGNED_PRE_KEY_ID);
            byte[] reset = record.serialize();

            Util.assertArraysNotEqual(orig,reset);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    @Test
    public void storeAndLoad() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;

        try {
            store = new MySignedPreKeyStore<>(database);
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

    private static int signedPreKeyId = 10;

    static void testStoreAndLoad(SignedPreKeyStore store) throws Exception {
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        SignedPreKeyRecord original = KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId);
        store.storeSignedPreKey(signedPreKeyId, original);

        assertTrue(store.containsSignedPreKey(signedPreKeyId));

        SignedPreKeyRecord record = store.loadSignedPreKey(signedPreKeyId);
        assertNotNull(record);
        assertArrayEquals(original.serialize(), record.serialize());
    }

    @Test
    public void update() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;
        try {
            store = new MySignedPreKeyStore<>(database);
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

    static void testUpdate(SignedPreKeyStore store) throws Exception {
        int id = 10;
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();

        SignedPreKeyRecord original = KeyHelper.generateSignedPreKey(identityKeyPair, id);
        store.storeSignedPreKey(id, original);

        assertTrue(store.containsSignedPreKey(id));

        SignedPreKeyRecord updated = KeyHelper.generateSignedPreKey(identityKeyPair, id);
        store.storeSignedPreKey(id, updated);

        SignedPreKeyRecord record = store.loadSignedPreKey(id);
        assertNotNull(record);
        assertArrayEquals(updated.serialize(), record.serialize());
    }

    @Test
    public void manyEntries() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;
        try {
            store = new MySignedPreKeyStore<>(database);
            assertNotNull(store);

            testManyEntries(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testManyEntries(SignedPreKeyStore store) throws Exception {
        int id = 10;
        int count = 1000;
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        ArrayList<SignedPreKeyRecord> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int thisId = id + i;
            SignedPreKeyRecord record = KeyHelper.generateSignedPreKey(identityKeyPair, thisId);
            list.add(record);
            store.storeSignedPreKey(thisId, record);
            assertTrue(store.containsSignedPreKey(thisId));
        }

        for (int i = 0; i < count; i++) {
            int thisId = id + i;
            SignedPreKeyRecord record = store.loadSignedPreKey(thisId);
            assertNotNull(record);
            assertArrayEquals(list.get(i).serialize(), record.serialize());
        }
    }

    @Test
    public void storeAndRemove() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;
        try {
            store = new MySignedPreKeyStore<>(database);
            assertNotNull(store);

            testStoreAndRemove(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testStoreAndRemove(SignedPreKeyStore store) throws Exception {
        int id = 10;
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        SignedPreKeyRecord record = KeyHelper.generateSignedPreKey(identityKeyPair, id);
        store.storeSignedPreKey(id, record);
        assertTrue(store.containsSignedPreKey(id));

        store.removeSignedPreKey(id);

        assertFalse(store.containsSignedPreKey(id));
    }

    @Test
    public void loadSignedPreKeys() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        IdentityKeyPair pair = KeyHelper.generateIdentityKeyPair();
        MySignedPreKeyStore.onCreate(database, pair);
        MySignedPreKeyStore store = null;
        try {
            store = new MySignedPreKeyStore<>(database);
            assertNotNull(store);

            testLoadSignedPreKeys(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testLoadSignedPreKeys(SignedPreKeyStore store) throws Exception {
        int id = 1; // also overwrites the initial SignedPreKey with id = 5 (SIGNED_PRE_KEY_ID)
        int count = 1000;
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        ArrayList<SignedPreKeyRecord> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int thisId = id + i;
            SignedPreKeyRecord record = KeyHelper.generateSignedPreKey(identityKeyPair, thisId);
            list.add(record);
            store.storeSignedPreKey(thisId, record);
            assertTrue(store.containsSignedPreKey(thisId));
        }

        List<SignedPreKeyRecord> loadedList = store.loadSignedPreKeys();
        assertEquals(list.size(), loadedList.size());

        for (int i = 0; i < loadedList.size(); i++) {
            int thisId = id + i;
            SignedPreKeyRecord record = findRecord(loadedList, thisId);
            assertNotNull(record);
            assertArrayEquals(list.get(i).serialize(), record.serialize());
        }
    }

    private static SignedPreKeyRecord findRecord(List<SignedPreKeyRecord> list, int id) {
        for (SignedPreKeyRecord record : list) {
            if (record.getId() == id) {
                return record;
            }
        }
        return null;
    }

}