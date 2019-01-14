package com.imaginationroom.cryptoandroidservice.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import com.imaginationroom.cryptoandroidservice.CryptoService;
import com.imaginationroom.cryptoandroidservice.ICryptoService;
import timber.log.Timber;

public abstract class BoundBaseFragment extends Fragment {

    ICryptoService mCryptoService;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mCryptoService = ICryptoService.Stub.asInterface(service);
            onServiceBound();
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Timber.w("Service has unexpectedly disconnected");
            mCryptoService = null;
            onServiceUnbound();
        }
    };

    protected abstract void onServiceBound();
    protected abstract void onServiceUnbound();

    private boolean mIsBound;

    public boolean isBound() {
        return mIsBound;
    }

    public ICryptoService getCryptoService() {
        return mCryptoService;
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent(getContext(), CryptoService.class);
        intent.setAction(ICryptoService.class.getName());
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mIsBound) {
            getContext().unbindService(mConnection);
            mIsBound = false;
        }
    }
}
