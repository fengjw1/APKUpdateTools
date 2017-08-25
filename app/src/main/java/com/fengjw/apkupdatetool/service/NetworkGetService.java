package com.fengjw.apkupdatetool.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.fengjw.apkupdatetool.DownloadListActivity;

public class NetworkGetService extends Service {

    private static final String TGA = "NetworkGetService";

    public NetworkGetService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TGA, "onCreate");
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();//这里只启动一次
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TGA, "onStartCommand");
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
        Intent intentActivity = new Intent(this, DownloadListActivity.class);
        intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentActivity);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TGA, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
