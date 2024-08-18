package com.example.drawer.core;

import android.app.Application;
import android.content.Context;

public class MyNote extends Application {

    private static MyNote instance;

    public static MyNote getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

}
