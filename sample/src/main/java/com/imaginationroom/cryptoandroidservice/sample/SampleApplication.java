package com.imaginationroom.cryptoandroidservice.sample;

import android.app.Application;

import timber.log.Timber;

public class SampleApplication extends Application {
    public SampleApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
