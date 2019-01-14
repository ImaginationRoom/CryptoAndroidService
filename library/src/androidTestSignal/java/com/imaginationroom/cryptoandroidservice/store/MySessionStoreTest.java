package com.imaginationroom.cryptoandroidservice.store;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Test;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionStore;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MySessionStoreTest {
    private static SignalProtocolAddress UnknownAddress = new SignalProtocolAddress(UUID.randomUUID().toString(), MySessionStore.DEVICE_ID);
    private static SignalProtocolAddress KnownAddress = new SignalProtocolAddress(UUID.randomUUID().toString(), MySessionStore.DEVICE_ID);

    @Test
    public void create() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MySessionStore.onCreate(database);
        MySessionStore store = null;
        try {
            store = new MySessionStore<>(database);
            assertNotNull(store);

            testEmpty(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testEmpty(SessionStore store) throws Exception {
        assertFalse(store.containsSession(UnknownAddress));
        assertNotNull(store.loadSession(UnknownAddress));
        store.deleteAllSessions(UnknownAddress.getName());
        store.deleteSession(UnknownAddress);
    }

    @Test
    public void reset() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MySessionStore.onCreate(database);
        MySessionStore store = null;
        try {
            store = new MySessionStore<>(database);
            assertNotNull(store);
            testStoreAndLoad(store);
            store.reset();
            assertFalse(store.containsSession(KnownAddress));
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
        MySessionStore.onCreate(database);
        MySessionStore store = null;

        try {
            store = new MySessionStore<>(database);
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

    static SignalProtocolAddress testStoreAndLoad(SessionStore store) {
        SessionRecord original = new SessionRecord();
        store.storeSession(KnownAddress, original);

        assertTrue(store.containsSession(KnownAddress));

        SessionRecord record = store.loadSession(KnownAddress);
        assertNotNull(record);
        assertArrayEquals(original.serialize(), record.serialize());

        return KnownAddress;
    }

    @Test
    public void update() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MySessionStore.onCreate(database);
        MySessionStore store = null;
        try {
            store = new MySessionStore<>(database);
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

    static void testUpdate(SessionStore store) {
        SessionRecord original = new SessionRecord();
        store.storeSession(KnownAddress, original);

        assertTrue(store.containsSession(KnownAddress));

        SessionRecord updated = new SessionRecord();
        store.storeSession(KnownAddress, updated);

        SessionRecord record = store.loadSession(KnownAddress);
        assertNotNull(record);
        assertArrayEquals(updated.serialize(), record.serialize());
    }

    @Test
    public void storeAndRemove() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MySessionStore.onCreate(database);
        MySessionStore store = null;
        try {
            store = new MySessionStore<>(database);
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

    static void testStoreAndRemove(SessionStore store) {
        SessionRecord record = new SessionRecord();
        store.storeSession(KnownAddress, record);
        assertTrue(store.containsSession(KnownAddress));

        store.deleteSession(KnownAddress);

        assertFalse(store.containsSession(KnownAddress));
    }

    @Test
    public void storeAndRemoveAll() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MySessionStore.onCreate(database);
        MySessionStore store = null;
        try {
            store = new MySessionStore<>(database);
            assertNotNull(store);

            testStoreAndRemoveAll(store);
        } finally {
            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    static void testStoreAndRemoveAll(SessionStore store) {
        SessionRecord record = new SessionRecord();
        store.storeSession(KnownAddress, record);
        assertTrue(store.containsSession(KnownAddress));

        store.deleteAllSessions(KnownAddress.getName());

        assertFalse(store.containsSession(KnownAddress));
    }
}