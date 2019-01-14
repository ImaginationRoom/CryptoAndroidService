package com.imaginationroom.cryptoandroidservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class CryptoService extends Service {
    private CryptoDataSource mDataSource;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Log.e("CryptoService", "Opening data source");

            mDataSource = Injection.provideCryptoDataSource(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDataSource = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final ICryptoService.Stub mBinder = new ICryptoService.Stub() {

        @Override
        public byte[] getPublicKey() throws RemoteException {
            return mDataSource.getPublicKey();
        }

        @Override
        public CryptoResponse sign(byte[] nonce) throws RemoteException {
            try {
                return new CryptoResponse(mDataSource.sign(nonce));
            } catch (Throwable e) {
                return new CryptoResponse(e.getMessage());
            }
        }

        @Override
        public boolean hasSession(String uuid) throws RemoteException {
            return mDataSource.hasSession(uuid);
        }

        @Override
        public Invite createInvite() throws RemoteException {
            try {
                return mDataSource.createInvite();
            } catch (Throwable e) {
                return new Invite(e.getMessage());
            }
        }

        @Override
        public CryptoResponse deleteInvite(int preKeyId) throws RemoteException {
            try {
                mDataSource.deleteInvite(preKeyId);
                return new CryptoResponse(new byte[0]);
            } catch (Throwable t) {
                return new CryptoResponse(t.getMessage());
            }
        }

        @Override
        public CryptoResponse inviteAccepted(String uuid, byte[] preKeyMessage) throws RemoteException {
            try {
                return new CryptoResponse(mDataSource.inviteAccepted(uuid, preKeyMessage));
            } catch (Throwable t) {
                return new CryptoResponse(t.getMessage());
            }
        }

        @Override
        public CryptoResponse acceptInvite(String uuid, IncomingInvite invite) throws RemoteException {
            try {
                return new CryptoResponse(mDataSource.acceptInvite(uuid, invite));
            } catch (Throwable e) {
                return new CryptoResponse(e.getMessage());
            }
        }

        @Override
        public CryptoResponse encrypt(String uuid, byte[] clear) throws RemoteException {
            try {
                return new CryptoResponse(mDataSource.encrypt(uuid, clear));
            } catch (Throwable e) {
                return new CryptoResponse(e.getMessage());
            }
        }

        @Override
        public CryptoResponse decrypt(String uuid, byte[] encrypted) throws RemoteException {
            try {
                return new CryptoResponse(mDataSource.decrypt(uuid, encrypted));
            } catch (Throwable e) {
                return new CryptoResponse(e.getMessage());
            }
        }

        @Override
        public CryptoResponse deleteSession(String uuid) throws RemoteException {
            mDataSource.deleteSession(uuid);
            return new CryptoResponse(new byte[0]);
        }

        @Override
        public void reset() throws RemoteException {
            mDataSource.reset();
        }
    };
}
