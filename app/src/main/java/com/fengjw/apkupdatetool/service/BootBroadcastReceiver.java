package com.fengjw.apkupdatetool.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

import java.util.List;

/**
 * Created by fengjw on 2017/9/3.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    //private static String apkPath = "/storage/emulated/0/Download/";
    private OkDownload okDownload;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot this system , BootBroadcastReceiver onReceive()");
        Toast.makeText(context,"BootBroadcastReceiver", Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals(ACTION_BOOT)) {
            Log.d(TAG, "BootBroadcastReceiver onReceive(), Do thing!");
            okDownload = OkDownload.getInstance();
            List<Progress> progressList = DownloadManager.getInstance().getAll(); //同步数据库的数据很有必要
            Log.d(TAG, "progressList : " + progressList);
            OkDownload.restore(progressList);
            okDownload.removeAll(true);
            Log.d(TAG, "removeAll true!");
        }
    }
}
