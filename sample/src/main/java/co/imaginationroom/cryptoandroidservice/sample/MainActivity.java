package com.imaginationroom.cryptoandroidservice.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.imaginationroom.cryptoandroidservice.ICryptoService;
import timber.log.Timber;

import com.imaginationroom.cryptoandroidservice.CryptoService;

public class MainActivity
        extends AppCompatActivity
        implements AliceFragment.OnFragmentInteractionListener, BobFragment.OnFragmentInteractionListener {

    private AliceFragment mAliceFragment = null;
    private BobFragment mBobFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBound) {
                    try {
                        Timber.d("Show thread and process in Activity");
                        boolean hasSession = mCryptoService.hasSession("ha!");
                        Toast.makeText(MainActivity.this, "Result: " + hasSession, Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(view, "Not Bound", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        Fragment aliceFragment = getSupportFragmentManager().findFragmentById(R.id.topContent);
        if (aliceFragment == null || !(aliceFragment instanceof AliceFragment)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.topContent, (aliceFragment = new AliceFragment()))
                    .commit();
        }
        mAliceFragment = (AliceFragment) aliceFragment;

        Fragment bobFragment = getSupportFragmentManager().findFragmentById(R.id.bottomContent);
        if (bobFragment == null || !(bobFragment instanceof BobFragment)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bottomContent, (bobFragment = new BobFragment()))
                    .commit();
        }
        mBobFragment = (BobFragment) bobFragment;
    }

    ICryptoService mCryptoService;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            mCryptoService = ICryptoService.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Timber.w("Service has unexpectedly disconnected");
            mCryptoService = null;
        }
    };

    private boolean mIsBound;

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, CryptoService.class);
        intent.setAction(ICryptoService.class.getName());
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void sendToBob(byte[] bytes) {
        mBobFragment.receive(bytes);
    }
}
