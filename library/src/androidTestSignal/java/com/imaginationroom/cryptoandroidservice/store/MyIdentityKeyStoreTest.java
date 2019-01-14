package com.imaginationroom.cryptoandroidservice.store;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Test;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MyIdentityKeyStoreTest {
    @Test
    public void create() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;
        MyIdentityKeyStore altStore = null;
        try {
            store = new MyIdentityKeyStore<>(database);
            assertNotNull(store);

            assertNotNull(store.getIdentityKeyPair());
            assertNotEquals(0, store.getLocalRegistrationId());

            altStore = new MyIdentityKeyStore<>(database);
            assertNotNull(altStore);

            assertArrayEquals(store.getIdentityKeyPair().serialize(), altStore.getIdentityKeyPair().serialize());
        } finally {

            if (altStore != null) {
                altStore.close();
            }

            if (store != null) {
                store.close();
            }

            if (database != null) {
                database.close();
            }
        }

    }

    @Test
    public void reset() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;

        try {
            store = new MyIdentityKeyStore<>(database);
            testReset(store);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    static void testReset(MyIdentityKeyStore store) {
        int first = store.getLocalRegistrationId();
        byte[] keys = store.getIdentityKeyPair().serialize();

        store.reset();
        int second = store.getLocalRegistrationId();
        assertNotEquals(first, second);

        byte[] newKeys = store.getIdentityKeyPair().serialize();
        boolean differ = false;
        for (int i = 0; i < keys.length && i < newKeys.length; i++) {
            differ = differ || (keys[i] == newKeys[i]);
        }
        assertTrue(differ);

    }

    @Test
    public void untrustedIdentitiesUnknownId() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;

        try {
            store = new MyIdentityKeyStore<>(database);
            testUntrustedIdentitiesUnknownId(store);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    static void testUntrustedIdentitiesUnknownId(IdentityKeyStore store) {
        assertTrue(store.isTrustedIdentity(new SignalProtocolAddress("non-existant", 1), KeyHelper.generateIdentityKeyPair().getPublicKey(), IdentityKeyStore.Direction.RECEIVING));
    }

    @Test
    public void trustedIdentities() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;

        try {
            store = new MyIdentityKeyStore<>(database);

            testTrustedIdentities(store);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    static void testTrustedIdentities(IdentityKeyStore store) {
        SignalProtocolAddress address = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);
        IdentityKey identityKey = KeyHelper.generateIdentityKeyPair().getPublicKey();
        store.saveIdentity(address, identityKey);

        assertTrue(store.isTrustedIdentity(address, identityKey, IdentityKeyStore.Direction.RECEIVING));
    }

    @Test
    public void trustedIdentitiesIgnoresDeviceId() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;

        try {
            store = new MyIdentityKeyStore<>(database);
            testTrustedIdentitiesIgnoresDeviceId(store);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    static void testTrustedIdentitiesIgnoresDeviceId(IdentityKeyStore store) {
        SignalProtocolAddress address = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);
        IdentityKey identityKey = KeyHelper.generateIdentityKeyPair().getPublicKey();
        store.saveIdentity(address, identityKey);

        SignalProtocolAddress otherDevice = new SignalProtocolAddress(address.getName(), 2);

        assertTrue(store.isTrustedIdentity(otherDevice, identityKey, IdentityKeyStore.Direction.RECEIVING));
    }

    @Test
    public void untrustedIdentitiesWrongIdentity() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;

        try {
            store = new MyIdentityKeyStore<>(database);
            testUntrustedIdentitiesWrongIdentity(store);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    static void testUntrustedIdentitiesWrongIdentity(IdentityKeyStore store) {
        SignalProtocolAddress address = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);
        IdentityKey identityKey = KeyHelper.generateIdentityKeyPair().getPublicKey();
        store.saveIdentity(address, identityKey);

        IdentityKey otherIdentityKey = KeyHelper.generateIdentityKeyPair().getPublicKey();

        assertFalse(store.isTrustedIdentity(address, otherIdentityKey, IdentityKeyStore.Direction.RECEIVING));
    }

    @Test
    public void updateTrustedIdentity() throws Exception {
        SQLiteDatabase database = Util.getDatabase("test.db", "test123");
        MyIdentityKeyStore.onCreate(database);

        MyIdentityKeyStore store = null;

        try {
            store = new MyIdentityKeyStore<>(database);
            testUpdateTrustedIdentity(store);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    static void testUpdateTrustedIdentity(IdentityKeyStore store) {
        SignalProtocolAddress address = new SignalProtocolAddress(UUID.randomUUID().toString(), 1);
        IdentityKey identityKey = KeyHelper.generateIdentityKeyPair().getPublicKey();
        store.saveIdentity(address, identityKey);

        IdentityKey otherIdentityKey = KeyHelper.generateIdentityKeyPair().getPublicKey();
        store.saveIdentity(address, otherIdentityKey);

        assertFalse(store.isTrustedIdentity(address, identityKey, IdentityKeyStore.Direction.RECEIVING));
        assertTrue(store.isTrustedIdentity(address, otherIdentityKey, IdentityKeyStore.Direction.RECEIVING));
    }
}