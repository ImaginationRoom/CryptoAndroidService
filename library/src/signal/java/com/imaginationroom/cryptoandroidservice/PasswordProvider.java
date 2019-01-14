package com.imaginationroom.cryptoandroidservice;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class PasswordProvider {
    private static final String TAG = "Signal:PasswordProvider";

    private final Object mLock = new Object();

    private static final String DATABASE_NAME = "SignalProtocolStore.db";
    private static final String KEY_ALIAS = DATABASE_NAME;
    private static final String KEY_STORE = "AndroidKeyStore";

    private static final String UNSECURE_PREF_FILENAME = "signal-protocol-password";
    private static final String UNSECURE_PREF_PASSWORD_KEY = "password";

    private KeyStore keyStore;
    private final Context mAppContext;

    private String mPlainTextPassword = null;

    public PasswordProvider(Context context) {
        mAppContext = context.getApplicationContext();

        try {
            keyStore = KeyStore.getInstance(KEY_STORE);
            keyStore.load(null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open key store: " + e);
        }
    }

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    public String getPassword() {
        synchronized (mLock) {
            if (mPlainTextPassword != null) {
                Log.d(TAG, "Password already loaded, using cached...");
                return mPlainTextPassword;
            }

            try {
                String encryptedBase64Password;

                if (keyStore.containsAlias(KEY_ALIAS) &&
                        (!TextUtils.isEmpty(encryptedBase64Password = load(mAppContext)))) {
                    Log.d(TAG, "Password alias found, loaded \"" + encryptedBase64Password + "\" ...");

                    byte[] encryptedPassword = Base64.decode(encryptedBase64Password, Base64.NO_WRAP);
                    byte[] passwordBytes = decrypt(keyStore, KEY_ALIAS, encryptedPassword);
                    mPlainTextPassword = new String(passwordBytes, 0, passwordBytes.length, "UTF-8");
                } else {
                    Log.d(TAG, "No password alias found, creating new");

                    createNewKeys(mAppContext, keyStore, KEY_ALIAS);
                    mPlainTextPassword = UUID.randomUUID().toString();
                    byte[] encrypted = encrypt(keyStore, KEY_ALIAS, mPlainTextPassword.getBytes("UTF-8"));
                    encryptedBase64Password = Base64.encodeToString(encrypted, Base64.NO_WRAP);

                    Log.d(TAG, "storing \"" + encryptedBase64Password + "%s\"");

                    store(mAppContext, encryptedBase64Password);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to getPassword(), generating new\n" + e);
                mPlainTextPassword = UUID.randomUUID().toString();
            }

            return mPlainTextPassword;
        }
    }

    private static void store(Context context, String password) {
        context.getSharedPreferences(UNSECURE_PREF_FILENAME, Context.MODE_PRIVATE)
                .edit()
                .putString(UNSECURE_PREF_PASSWORD_KEY, password)
                .apply();
    }

    private static String load(Context context) {
        return context.getSharedPreferences(UNSECURE_PREF_FILENAME, Context.MODE_PRIVATE)
                .getString(UNSECURE_PREF_PASSWORD_KEY, null);
    }

    private static void createNewKeys(Context context, KeyStore keyStore, String alias) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (keyStore.containsAlias(alias)) {
            Log.d(TAG, "Reusing old Alias, deleting old keys first...");
            keyStore.deleteEntry(alias);
        }

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec spec =
                    new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                            .setCertificateSerialNumber(BigInteger.ONE)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .setCertificateNotBefore(start.getTime())
                            .setCertificateNotAfter(end.getTime())
                            .setCertificateSubject(new X500Principal("CN=" + alias + ", O=Imagination Room"))
                            .build();

            KeyPairGenerator kg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            kg.initialize(spec);
            kg.generateKeyPair();
        } else {
            //noinspection deprecation
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(new X500Principal("CN=" + alias + ", O=Imagination Room"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            generator.initialize(spec);

            generator.generateKeyPair();
        }
    }


    private static byte[] encrypt(KeyStore keyStore, String alias, byte[] plain) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, UnrecoverableEntryException, KeyStoreException {
        Cipher inCipher = getCipher(keyStore, alias, Cipher.ENCRYPT_MODE);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inCipher);
        cipherOutputStream.write(plain);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private static byte[] decrypt(KeyStore keyStore, String alias, byte[] encrypted) throws Exception {
        Cipher output = getCipher(keyStore, alias, Cipher.DECRYPT_MODE);

        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }

        return bytes;
    }

    private static Cipher getCipher(KeyStore keyStore, String alias, int opMode) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);

        if (opMode == Cipher.ENCRYPT_MODE) {
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } else {
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        }

        return cipher;
    }
}
