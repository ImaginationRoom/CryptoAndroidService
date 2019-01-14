package com.imaginationroom.cryptoandroidservice.store;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.io.File;

import com.imaginationroom.cryptoandroidservice.PasswordProvider;

import static com.imaginationroom.cryptoandroidservice.SignalAdapter.SIGNED_PRE_KEY_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MySignalProtocolStoreTest {
    private MySignalProtocolStore mStore;

    @Before
    public void setUp() throws Exception {
        mStore = getStore();
    }

    @After
    public void tearDown() throws Exception {
        mStore.close();
    }

    public static MySignalProtocolStore getStore() throws InvalidKeyException {
        Context context = InstrumentationRegistry.getTargetContext();

        File dbFile = context.getDatabasePath(PasswordProvider.getDatabaseName());

        if (dbFile.exists()) {
            assertTrue(dbFile.delete());
        }

        assertFalse(dbFile.exists());

        return new MySignalProtocolStore(context);
    }

    @Test
    public void create() throws Exception {
        MySignalProtocolStore store = getStore();

        assertNotNull(store.getIdentityKeyPair());
        assertNotEquals(0, store.getLocalRegistrationId());
    }

    @Test
    public void reset() throws Exception {
        MySignalProtocolStore store = getStore();

        int origRegId = store.getLocalRegistrationId();
        int knownId = MyPreKeyStoreTest.testStoreAndLoad(store);
        SignalProtocolAddress knownAddress = MySessionStoreTest.testStoreAndLoad(store);
        assertEquals(1, store.loadSignedPreKeys().size());
        assertTrue(store.containsSignedPreKey(SIGNED_PRE_KEY_ID));
        SignedPreKeyRecord record = store.loadSignedPreKey(SIGNED_PRE_KEY_ID);
        byte[] origSignedPreKey = record.serialize();

        store.reset();

        // Delegated to MyIdentityKeyStore
        int updatedRegId = store.getLocalRegistrationId();
        assertNotEquals(origRegId, updatedRegId);

        // Delegated to MyPreKeyStore
        assertFalse(store.containsPreKey(knownId));

        // Delegated to MySessionStore
        assertFalse(store.containsSession(knownAddress));

        // Delegated to SignedPreKey
        assertEquals(1, store.loadSignedPreKeys().size());
        assertTrue(store.containsSignedPreKey(SIGNED_PRE_KEY_ID));
        record = store.loadSignedPreKey(SIGNED_PRE_KEY_ID);
        byte[] updatedSignedPreKey = record.serialize();

        Util.assertArraysNotEqual(origSignedPreKey, updatedSignedPreKey);
    }

    @Test
    public void testTrustedIdentities() throws Exception {
        MyIdentityKeyStoreTest.testTrustedIdentities(mStore);
    }

    @Test
    public void testUntrustedIdentitiesUnknownId() throws Exception {
        MyIdentityKeyStoreTest.testUntrustedIdentitiesUnknownId(mStore);
    }

    @Test
    public void testTrustedIdentitiesIgnoresDeviceId() throws Exception {
        MyIdentityKeyStoreTest.testTrustedIdentitiesIgnoresDeviceId(mStore);
    }

    @Test
    public void testUntrustedIdentitiesWrongIdentity() throws Exception {
        MyIdentityKeyStoreTest.testUntrustedIdentitiesWrongIdentity(mStore);
    }

    @Test
    public void testUpdateTrustedIdentity() throws Exception {
        MyIdentityKeyStoreTest.testUpdateTrustedIdentity(mStore);
    }

    @Test
    public void testCreateEmptyPreKey() throws Exception {
        MyPreKeyStoreTest.testCreateEmpty(mStore);
    }

    @Test
    public void testStoreAndLoadPreKey() throws Exception {
        MyPreKeyStoreTest.testStoreAndLoad(mStore);
    }

    @Test
    public void testUpdatePreKey() throws Exception {
        MyPreKeyStoreTest.testUpdate(mStore);
    }

    @Test
    public void testContainsPreKey() throws Exception {
        MyPreKeyStoreTest.testContains(mStore);
    }

    @Test
    public void testtestStoreAndRemotePreKey() throws Exception {
        MyPreKeyStoreTest.testStoreAndRemote(mStore);
    }

    @Test
    public void testEmptySession() throws Exception {
        MySessionStoreTest.testEmpty(mStore);
    }

    @Test
    public void testStoreAndLoadSession() throws Exception {
        MySessionStoreTest.testStoreAndLoad(mStore);
    }

    @Test
    public void testUpdateSession() throws Exception {
        MySessionStoreTest.testUpdate(mStore);
    }

    @Test
    public void testStoreAndRemoveSession() throws Exception {
        MySessionStoreTest.testStoreAndRemove(mStore);
    }

    @Test
    public void testStoreAndRemoveAllSessions() throws Exception {
        MySessionStoreTest.testStoreAndRemoveAll(mStore);
    }

    @Test
    public void testEmptySignedPreKey() throws Exception {
        MySignedPreKeyStoreTest.testInitial(mStore);
    }

    @Test
    public void testStoreAndLoadSignedPreKeys() throws Exception {
        MySignedPreKeyStoreTest.testStoreAndLoad(mStore);
    }

    @Test
    public void testUpdateSignedPreKey() throws Exception {
        MySignedPreKeyStoreTest.testUpdate(mStore);
    }

    @Test
    public void testManyEntriesSignedPreKey() throws Exception {
        MySignedPreKeyStoreTest.testManyEntries(mStore);
    }

    @Test
    public void testStoreAndRemoveSignedPreKey() throws Exception {
        MySignedPreKeyStoreTest.testStoreAndRemove(mStore);
    }

    @Test
    public void testLoadSignedPreKeys() throws Exception {
        MySignedPreKeyStoreTest.testLoadSignedPreKeys(mStore);
    }
}