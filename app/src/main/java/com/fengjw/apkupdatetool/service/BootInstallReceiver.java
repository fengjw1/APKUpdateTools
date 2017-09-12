package com.fengjw.apkupdatetool.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fengjw.apkupdatetool.Adapter.DownloadAdapter;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

/**
 * Created by fengjw on 2017/9/11.
 */

public class BootInstallReceiver extends BroadcastReceiver {

    private final static String TGA = "BootInstallReceiver";
    private OkDownload mOkDownload;
    private String packageName;
    private DownloadTask mTask;
    private DownloadAdapter mAdapter;
    public final static int INSTALL_APK = 1;
    private Handler InstallHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INSTALL_APK:
                    try {
                        mTask.remove(true);
                        Log.d(TGA, "mTask remove!");
                        mAdapter = new DownloadAdapter();
                        mAdapter.updateData(0);
                        Log.d(TGA, "mAdapter!");
                    }catch(Exception e){
                        Log.d(TGA, "INSTALL_APK Execption!");
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
//        mOkDownload = OkDownload.getInstance();
//        try {
//            if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")){
//                Log.d(TGA, "Name : " + intent.getDataString());
//                if (mOkDownload.hasTask(intent.getDataString())){
//                    packageName = intent.getDataString();
//                    Log.d(TGA, "packageName = " + packageName);
//                    //mTask = mOkDownload.getTask(packageName);
//                    Log.d(TGA, "mTask!");
//                    //InstallHandler.sendEmptyMessage(INSTALL_APK);
//                    Log.d(TGA, "Handler!");
//                }else {
//                    Log.d(TGA, "not tag is " + intent.getDataString());
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }



}
