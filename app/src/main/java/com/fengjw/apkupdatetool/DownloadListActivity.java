/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fengjw.apkupdatetool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.VerificationParams;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fengjw.apkupdatetool.Adapter.BaseRecyclerAdapter;
import com.fengjw.apkupdatetool.utils.ApkModel;
import com.fengjw.apkupdatetool.utils.AppInfo;
import com.fengjw.apkupdatetool.utils.AppInfoProvider;
import com.fengjw.apkupdatetool.utils.HeadBean;
import com.fengjw.apkupdatetool.utils.HttpUtil;
import com.fengjw.apkupdatetool.utils.LogDownloadListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

public class DownloadListActivity extends BaseActivity {

    private static final int GET_ALL_APP_FINISH = 1;

    private static final int REQUEST_PERMISSION_STORAGE = 0x01;

    private final int INSTALL_REPLACE_EXISTING = 2;
    private final  String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private List<String> urls = new ArrayList<>();



    private static final String TGA = "DownloadListActivity";

//    @Bind(R.id.toolbar)
//    Toolbar toolbar;

    @Bind(R.id.targetFolder)
    TextView folder;
    @Bind(R.id.recyclerView) RecyclerView recyclerView;

    private List<ApkModel> apks; //类型是ApkModel
    private DownloadListAdapter adapter;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_ALL_APP_FINISH:
                    Log.d(TGA, "这里是handler");
                    recyclerView.setLayoutManager(new LinearLayoutManager(DownloadListActivity.this));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.addItemDecoration(new DividerItemDecoration(DownloadListActivity.this,
                            LinearLayoutManager.VERTICAL));
                    // 聚焦
//                    recyclerView.setFocusable(true);
//                    recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                        @Override
//                        public void onFocusChange(View v, boolean hasFocus) {
//                            Log.i("abc","hasfocus:"+hasFocus);
//                            if(hasFocus){
//                                if(recyclerView.getChildCount()>0){
//                                    recyclerView.getChildAt(0).requestFocus();
//                                }
//                            }
//                        }
//                    });

//                    recyclerView.setFocusableInTouchMode(true);
//                    recyclerView.requestFocus();
                    adapter = new DownloadListAdapter(DownloadListActivity.this, apks);
                    recyclerView.setAdapter(adapter);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            adapter = new DownloadListAdapter(DownloadListActivity.this, apks);
////                    //listView.setAdapter(adapter);
//                        recyclerView.setAdapter(adapter);
//                        }
//                    });
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        //initToolBar(toolbar, true, "开始下载");
        Button button = (Button) findViewById(R.id.enterDownload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadListActivity.this, DownloadAllActivity.class);
                startActivity(intent);
            }
        });

        Button button1 = (Button) findViewById(R.id.enterTest);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadListActivity.this, MainActivity.class);
                startActivity(intent);
                //installPackage();
               /// String apkPath = "/storage/emulated/0/Download/com.qiyi.video.apk";
                //install(apkPath);
                Log.d(TGA, " inter StartOnBootService");
            }
        });

        Log.d(TGA, "继续执行？");
        try {
            //initData(); //这里获取了list数据，封装在一个list中
            OkDownload.getInstance().setFolder(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS).getPath());
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            Log.d(TGA, path);
            //OkDownload.getInstance().setFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa/");
            OkDownload.getInstance().getThreadPool().setCorePoolSize(3);//默认同时下载三个

            //file path
            folder.setText(String.format("下载路径: %s", OkDownload.getInstance().getFolder()));//显示的是下载的路径
            Log.d(TGA, "file path : " + OkDownload.getInstance().getFolder());
            //  file path : /storage/emulated/0/aaa/com.qiyi.video.apk

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));//recyclerView参数设置

            //从数据库中恢复数据
            List<Progress> progressList = DownloadManager.getInstance().getAll();//启动数据库未完成的状态
            OkDownload.restore(progressList);
            //recyclerView.setFocusable(true);
            //recyclerView.setFocusableInTouchMode(true);
            recyclerView.requestFocus();
            //recyclerView.setVerticalScrollBarEnabled(true);
            recyclerView.setHorizontalFadingEdgeEnabled(true);
            recyclerView.getPreserveFocusAfterLayout();
            // 聚焦
//            recyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    Log.i("abc","hasfocus:"+hasFocus);
//                    if(hasFocus){
//                        if(recyclerView.getChildCount()>0){
//                            recyclerView.getChildAt(0).requestFocus();
//                        }
//                    }
//                }
//            });


            adapter = new DownloadListAdapter(this);
            Log.d(TGA, "DownloadListAdapter");
            recyclerView.setAdapter(adapter);



            checkSDCardPermission();
        }catch (Exception e){
            e.printStackTrace();
        }



        new Thread(new Runnable() {
            @Override
            public void run() {
                sendRequestWithOKHttp();
            }
        }).start();


//        NetworkGetReceiver networkGetReceiver = new NetworkGetReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(networkGetReceiver, filter);
//        Log.d(TGA, "这里执行了Receiver");

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        http();
//    }

    /** 检查SD卡权限 */
    protected void checkSDCardPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //获取权限
            } else {
                showToast("权限被禁止，无法下载文件！");
            }
        }
    }

    private void sendRequestWithOKHttp(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String url = "https://10.0.2.2/get_data.json";
//                    OkHttpClient client = new OkHttpClient.Builder()
//                            .sslSocketFactory(createSSLSocketFactory())
//                            .hostnameVerifier(new TrustAllHostnameVerifier()).build();
//                    Request request = new Request.Builder().url(url).build();
//                    Response response = client.newCall(request).execute();
//                    String responseData = response.body().string();
//                    //showResponse(responseData);
//                    //parseJSONWithJSONObject(responseData);
//                    parseNewJSONWithJSONObject(responseData);
//                }catch (Exception e){
//                    e.getMessage();
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        //String url = "http://192.168.1.14:2700/6a648/ktc/test/version.json";
        String url = "http://192.168.1.14:8800/index.php/apkapi?model=TV918&product=ktc&sdanum=SDA123456789";
        //String url = "https://10.0.2.2/get_data.json";
        apks = new ArrayList<>();
        Log.d(TGA, url);
        HttpUtil.sendOKHttpResquest(url, new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TGA, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    String responseData = response.body().string();
                    Log.d(TGA, responseData);
                    parseNewJSONWithJSONObject(responseData);
                    //parseTestJSONWithJSONObject(responseData);
            }
        });
    }

    private void parseTestJSONWithJSONObject(String responseData){
        Gson gson = new Gson();
        List<AppInfo> appList = gson.fromJson(responseData, new TypeToken<List<AppInfo>>(){}.getType());

        for (AppInfo app : appList){ //foreach()
            Log.d(TGA, "appName : " + app.getApp_name());
            Log.d(TGA, "fileName : " + app.getFile_name());
            Log.d(TGA, "verName : " + app.getVer_name());
            Log.d(TGA, "verCode : " + app.getVerCode());
            Log.d(TGA, "url : " + app.getUrl());
            Log.d(TGA, "MD5 : " + app.getMD5());
            Log.d(TGA, "packageName : " + app.getPkg_name());
            Log.d(TGA, "-----------------------------------------");
        }
//        Log.d(TGA, gson.toString());
//        List<AppInfo> appList = gson.fromJson(responseData, new TypeToken<List<AppInfo>>(){}.getType());
//        for (AppInfo app : appList){ //foreach()
//            Log.d(TGA, "appName : " + app.getApp_name());
//            Log.d(TGA, "fileName : " + app.getFile_name());
//            Log.d(TGA, "verName : " + app.getVer_name());
//            Log.d(TGA, "verCode : " + app.getVerCode());
//            Log.d(TGA, "url : " + app.getUrl());
//            Log.d(TGA, "MD5 : " + app.getMD5());
//            Log.d(TGA, "packageName : " + app.getPkg_name());
//            Log.d(TGA, "-----------------------------------------");
//        }

        //本地信息
        AppInfoProvider appInfoProvider = new AppInfoProvider(this);
        List<AppInfo> appInfoList = appInfoProvider.getAllApps();
        for (AppInfo appInfo : appInfoList){
            Log.d(TGA, "appName : " + appInfo.getApp_name());
            Log.d(TGA, "fileName : " + appInfo.getFile_name());
            Log.d(TGA, "verName : " + appInfo.getVer_name());
            Log.d(TGA, "verCode : " + appInfo.getVerCode());
            Log.d(TGA, "url : " + appInfo.getUrl());
            Log.d(TGA, "MD5 : " + appInfo.getMD5());
            Log.d(TGA, "packageName : " + appInfo.getPkg_name());
            Log.d(TGA, "-----------------------------------------");
        }
//
        Log.d(TGA, "-----------------------------------------");
        Log.d(TGA, "-----------------------------------------");
        Log.d(TGA, "-----------------------------------------");
        try{
            for (AppInfo app : appList){
                for (AppInfo appInfo : appInfoList){
                    //http
                    String httpAppPkgName = app.getPkg_name();
                    Log.d(TGA, "httpAppPkgName : " + httpAppPkgName);
                    int httpAppverCode = app.getVerCode();
                    Log.d(TGA, "httpAppverCode" + httpAppverCode);
                    //info
                    String AppInfoPkgName = appInfo.getPkg_name();
                    Log.d(TGA, "AppInfoPkgName : " + AppInfoPkgName);
                    int AppInfoverCode = appInfo.getVerCode();
                    Log.d(TGA, "AppInfoverCode" + AppInfoverCode);

                    //
//                    Log.d(TGA, AppInfoverCode + "");
//                    Log.d(TGA, httpAppPkgName);
//                    Log.d(TGA, httpAppverCode + "");
//                    Log.d(TGA, AppInfoPkgName);

                    //判断是否更新
                    if (httpAppPkgName.equals(AppInfoPkgName)) {
                        if (httpAppverCode > AppInfoverCode) {
                            String name =  app.getApp_name();
                            String url = app.getUrl();
                            //String iconUrl = app.get();
                            int type = app.getType();
                            Log.d(TGA, "name = " + name);
                            Log.d(TGA, "url = " + url);
                            ApkModel apkModel = new ApkModel();
                            apkModel.name = name;
                            apkModel.url = url;
                            apkModel.iconUrl = "http://file.market.xiaomi.com/thumbnail/" +
                                    "PNG/l114/AppStore/0c10c4c0155c9adf1282af008ed329378d54112ac";
                            //apkModel.iconUrl = iconUrl;
                            apkModel.priority = 2;
                            Log.d(TGA, "apkModel.url = " + apkModel.url);
                            apks.add(apkModel);
                            //count++;
//                            Log.d(TGA, "httpAppPkgName : " + httpAppPkgName + " httpAppverCode : "
//                                    + httpAppverCode + " count = " + count);
                        }
                    }
//                    else {
//                        Log.d(TGA, "httpAppPkgName : " + httpAppPkgName + " is not equal " + AppInfoPkgName);
//                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //test url
        for (ApkModel apk : apks){
            Log.d(TGA, "apk url = " + apk.url);
        }
        Log.d(TGA, "这里之后呢？");
        //initData();
        //发送消息，进行异步加载
        handler.sendEmptyMessage(GET_ALL_APP_FINISH);
    }
    private void parseNewJSONWithJSONObject(String responseData){

        try {
        Gson gson = new Gson();
        HeadBean bean = gson.fromJson(responseData, HeadBean.class);
        List<HeadBean.ApklistBean> appList = bean.getApklist();

        for (HeadBean.ApklistBean app : appList){ //foreach()
            Log.d(TGA, "appName : " + app.getApp_name());
            Log.d(TGA, "fileName : " + app.getFile_name());
            Log.d(TGA, "verName : " + app.getVer_name());
            Log.d(TGA, "verCode : " + app.getVer_code());
            Log.d(TGA, "url : " + app.getApk_url());
            Log.d(TGA, "MD5 : " + app.getMD5());
            Log.d(TGA, "packageName : " + app.getPkg_name());
            Log.d(TGA, "type : " + app.getUpdate_type());
            Log.d(TGA, "Introduction : " + app.getIntroduction());
            Log.d(TGA, "-----------------------------------------");
        }

        //本地信息
        AppInfoProvider appInfoProvider = new AppInfoProvider(this);
        List<AppInfo> appInfoList = appInfoProvider.getAllApps();
//        for (AppInfo appInfo : appInfoList){
//            Log.d(TGA, "appName : " + appInfo.getApp_name());
//            Log.d(TGA, "fileName : " + appInfo.getFile_name());
//            Log.d(TGA, "verName : " + appInfo.getVer_name());
//            Log.d(TGA, "verCode : " + appInfo.getVerCode());
//            Log.d(TGA, "url : " + appInfo.getUrl());
//            Log.d(TGA, "MD5 : " + appInfo.getMD5());
//            Log.d(TGA, "packageName : " + appInfo.getPkg_name());
//            Log.d(TGA, "-----------------------------------------");
//        }
//
        Log.d(TGA, "-----------------------------------------");
        Log.d(TGA, "-----------------------------------------");
        Log.d(TGA, "-----------------------------------------");
            for (HeadBean.ApklistBean app : appList){
                for (AppInfo appInfo : appInfoList){
                    //http
                    String httpAppPkgName = app.getPkg_name();
                    int httpAppverCode = app.getVer_code();
                    //info
                    String AppInfoPkgName = appInfo.getPkg_name();
                    int AppInfoverCode = appInfo.getVerCode();

//                    Log.d(TGA, "httpAppverCode" + httpAppverCode);
//                    Log.d(TGA, "httpAppPkgName : " + httpAppPkgName);
//
//                    Log.d(TGA, "AppInfoverCode" + AppInfoverCode);
//                    Log.d(TGA, "AppInfoPkgName : " + AppInfoPkgName);

                    //
//                    Log.d(TGA, AppInfoverCode + "");
//                    Log.d(TGA, httpAppPkgName);
//                    Log.d(TGA, httpAppverCode + "");
//                    Log.d(TGA, AppInfoPkgName);

                    //判断是否更新
                    if (httpAppPkgName.equals(AppInfoPkgName)) {
                        if (httpAppverCode > AppInfoverCode) {
                            String name =  app.getApp_name();
                            String url = app.getApk_url();
                            String iconUrl = app.getPic_url();
                            String description = app.getIntroduction();
                            int type = app.getUpdate_type();
                            String verName = app.getVer_name() + app.getVer_code();
                            Log.d(TGA, "name = " + name);
                            Log.d(TGA, "url = " + url);
                            Log.d(TGA, "verCode = " + app.getVer_code());
                            Log.d(TGA, "type = " + app.getUpdate_type());
                            ApkModel apkModel = new ApkModel();
                            apkModel.name = name;
                            apkModel.url = url;
//                            apkModel.iconUrl = "http://file.market.xiaomi.com/thumbnail/" +
//                                    "PNG/l114/AppStore/0c10c4c0155c9adf1282af008ed329378d54112ac";
                            apkModel.verName = verName;
                            apkModel.iconUrl = iconUrl;
                            apkModel.priority = type;
                            apkModel.description = description;
                            Log.d(TGA, "apkModel.url = " + apkModel.url);
                            apks.add(apkModel);
                        }

                    }
//                    else {
//                        Log.d(TGA, "httpAppPkgName : " + httpAppPkgName + " is not equal " + AppInfoPkgName);
//                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //test url
//        for (ApkModel apk : apks){
//            Log.d(TGA, "apk url = " + apk.url);
//        }
//        Log.d(TGA, "这里之后呢？");
        //initData();
        //发送消息，进行异步加载

//        List<HeadBean.ApklistBean> tempList = new ArrayList<>();
//
//        for (HeadBean.ApklistBean app : appList){
//            for (ApkModel apkModel : apks){
//                if (apkModel.getName().equals(app.getApp_name()))
//            }
//        }

        handler.sendEmptyMessage(GET_ALL_APP_FINISH);
    }



    @OnClick(R.id.startAll)
    public void startAll(View view) {
        for (ApkModel apk : apks) { //循环体，操作所有下载按钮

            //这里只是演示，表示请求可以传参，怎么传都行，和okgo使用方法一样
            GetRequest<File> request = OkGo.<File>get(apk.url);
//                    .headers("aaa", "111")//
//                    .params("bbb", "222");

            //这里第一个参数是tag，代表下载任务的唯一标识，传任意字符串都行，需要保证唯一,我这里用url作为了tag
            Log.d(TGA, "startAll");
            OkDownload.request(apk.url, request)//
                    .priority(apk.priority)//
                    .extra1(apk)//
                    .save()//
                    .register(new LogDownloadListener())//
                    .start();
            adapter.notifyDataSetChanged();
        }
        //jump
        Intent intent2 = new Intent(DownloadListActivity.this, DownloadAllActivity.class);
        startActivity(intent2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        //finish();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
//
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    private class DownloadListAdapter extends BaseRecyclerAdapter<ApkModel, ViewHolder> {

        private List<ApkModel> apks;
        private Context context;
        DownloadListAdapter(Context context, List<ApkModel> apks) {
            super(context, apks); //这里传入list<ApkModel>的内容
            this.apks = apks;
            this.context = context;
            Log.d(TGA, "这里传入list<ApkModel>的内容");
        }

        DownloadListAdapter(Context context){
            super(context);
            this.context = context;
            Log.d(TGA, "这里传入list<ApkModel>的内容");
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_download_list, parent, false); //list界面加载的item
            //parent.setFocusable(true);
            return new ViewHolder(view);//这里是具体操作
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ApkModel apkModel = mDatas.get(position);
            //holder.itemView.setFocusable(true);
            holder.bind(apkModel);
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.priority)
        TextView priority;
        @Bind(R.id.icon)
        ImageView icon;
        @Bind(R.id.download)
        Button download;
        @Bind(R.id.description)
        TextView description;
        @Bind(R.id.verName)
        TextView vername;

        private ApkModel apk;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView); //加载控件
        }

        public void bind(ApkModel apk) {
            this.apk = apk;
            if (OkDownload.getInstance().getTask(apk.url) != null) {
                download.setText("已在队列");
                download.setEnabled(false);
            } else {
                download.setText("下载");
                download.setEnabled(true);
            }
            priority.setText(String.format("优先级：%s", apk.priority));
            name.setText(apk.name);
            description.setText(apk.description);
            displayImage(apk.iconUrl, icon);
            vername.setText(apk.verName);
            itemView.setOnClickListener(this);
        }

        //这是下载button点击事件
        @OnClick(R.id.download)
        public void download() {

            //这里只是演示，表示请求可以传参，怎么传都行，和okgo使用方法一样
            GetRequest<File> request = OkGo.<File>get(apk.url)//
                    .headers("aaa", "111")//
                    .params("bbb", "222");

            //这里第一个参数是tag，代表下载任务的唯一标识，传任意字符串都行，需要保证唯一,我这里用url作为了tag
            OkDownload.request(apk.url, request)//
                    .priority(apk.priority)//
                    .extra1(apk)//
                    .save()//
                    .register(new LogDownloadListener())//
                    .start();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {

            //v.setFocusable(true);
            Intent intent = new Intent(getApplicationContext(), DownloadAllActivity.class);
            //Intent intent = new Intent(DownloadListActivity.this, DesActivity.class);
            intent.putExtra("apk", apk);
            startActivity(intent);
        }
    }

    private void initData() {
        Log.d(TGA, "initData");
        //apks = mApkModels;

        apks = new ArrayList<>();
        //ApkModel apkModel = new ApkModel();
        //
        // apkModel.name =


        ApkModel apk1 = new ApkModel();
        apk1.name = "爱奇艺";
        apk1.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0c10c4c0155c9adf1282af008ed329378d54112ac";
        apk1.url = "http://121.29.10.1/f5.market.mi-img.com/download/AppStore/0b8b552a1df0a8bc417a5afae3a26b2fb1342a909/com.qiyi.video.apk";
        apks.add(apk1);
        ApkModel apk2 = new ApkModel();
        apk2.name = "微信";
        apk2.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/00814b5dad9b54cc804466369c8cb18f23e23823f";
        apk2.url = "http://116.117.158.129/f2.market.xiaomi.com/download/AppStore/04275951df2d94fee0a8210a3b51ae624cc34483a/com.tencent.mm.apk";
        apks.add(apk2);
        ApkModel apk3 = new ApkModel();
        apk3.name = "新浪微博";
        apk3.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/01db44d7f809430661da4fff4d42e703007430f38";
        apk3.url = "http://60.28.125.129/f1.market.xiaomi.com/download/AppStore/0ff41344f280f40c83a1bbf7f14279fb6542ebd2a/com.sina.weibo.apk";
        apks.add(apk3);
        ApkModel apk4 = new ApkModel();
        apk4.name = "QQ";
        apk4.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/072725ca573700292b92e636ec126f51ba4429a50";
        apk4.url = "http://121.29.10.1/f3.market.xiaomi.com/download/AppStore/0ff0604fd770f481927d1edfad35675a3568ba656/com.tencent.mobileqq.apk";
        apks.add(apk4);
        ApkModel apk5 = new ApkModel();
        apk5.name = "陌陌";
        apk5.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/06006948e655c4dd11862d060bd055b4fd2b5c41b";
        apk5.url = "http://121.18.239.1/f4.market.xiaomi.com/download/AppStore/096f34dec955dbde0597f4e701d1406000d432064/com.immomo.momo.apk";
        apks.add(apk5);
        ApkModel apk6 = new ApkModel();
        apk6.name = "手机淘宝";
        apk6.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/017a859792d09d7394108e0a618411675ec43f220";
        apk6.url = "http://121.29.10.1/f3.market.xiaomi.com/download/AppStore/0afc00452eb1a4dc42b20c9351eacacab4692a953/com.taobao.taobao.apk";
        apks.add(apk6);
        ApkModel apk7 = new ApkModel();
        apk7.name = "酷狗音乐";
        apk7.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0f2f050e21e42f75c7ecca55d01ac4e5e4e40ca8d";
        apk7.url = "http://121.18.239.1/f5.market.xiaomi.com/download/AppStore/053ed49c1545c6eec3e3e23b31568c731f940934f/com.kugou.android.apk";
        apks.add(apk7);
        ApkModel apk8 = new ApkModel();
        apk8.name = "网易云音乐";
        apk8.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/02374548ac39f3b7cdbf5bea4b0535b5d1f432f23";
        apk8.url = "http://121.18.239.1/f4.market.xiaomi.com/download/AppStore/0f458c5661acb492e30b808a2e3e4c8672e6b55e2/com.netease.cloudmusic.apk";
        apks.add(apk8);
        ApkModel apk9 = new ApkModel();
        apk9.name = "ofo共享单车";
        apk9.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0fe1a5c6092f3d9fa5c4c1e3158e6ff33f6418152";
        apk9.url = "http://60.28.125.1/f4.market.mi-img.com/download/AppStore/06954949fcd48414c16f726620cf2d52200550f56/so.ofo.labofo.apk";
        apks.add(apk9);
        ApkModel apk10 = new ApkModel();
        apk10.name = "摩拜单车";
        apk10.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0863a058a811148a5174d9784b7be2f1114191f83";
        apk10.url = "http://60.28.125.1/f4.market.xiaomi.com/download/AppStore/00cdeb4865c5a4a7d350fe30b9f812908a569cc8a/com.mobike.mobikeapp.apk";
        apks.add(apk10);
        ApkModel apk11 = new ApkModel();
        apk11.name = "贪吃蛇大作战";
        apk11.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/09f7f5756d9d63bb149b7149b8bdde0769941f09b";
        apk11.url = "http://60.22.46.1/f3.market.xiaomi.com/download/AppStore/0b02f24ffa8334bd21b16bd70ecacdb42374eb9cb/com.wepie.snake.new.mi.apk";
        apks.add(apk11);
        ApkModel apk12 = new ApkModel();
        apk12.name = "蘑菇街";
        apk12.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0ab53044735e842c421a57954d86a77aea30cc1da";
        apk12.url = "http://121.29.10.1/f5.market.xiaomi.com/download/AppStore/07a6ee4955e364c3f013b14055c37b8e4f6668161/com.mogujie.apk";
        apks.add(apk12);
        ApkModel apk13 = new ApkModel();
        apk13.name = "聚美优品";
        apk13.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/080ed520b76d943e5533017a19bc76d9f554342d0";
        apk13.url = "http://121.29.10.1/f5.market.mi-img.com/download/AppStore/0e70a572cd5fd6a3718941328238d78d71942aee0/com.jm.android.jumei.apk";
        apks.add(apk13);
        ApkModel apk14 = new ApkModel();
        apk14.name = "全民K歌";
        apk14.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0f1f653261ff8b3a64324097224e40eface432b99";
        apk14.url = "http://60.28.123.129/f4.market.xiaomi.com/download/AppStore/04f515e21146022934085454a1121e11ae34396ae/com.tencent.karaoke.apk";
        apks.add(apk14);
        ApkModel apk15 = new ApkModel();
        apk15.name = "书旗小说";
        apk15.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0c9ce345aa2734b1202ddf32b6545d9407b18ba0b";
        apk15.url = "http://60.28.125.129/f5.market.mi-img.com/download/AppStore/02d9c4035b248753314f46600cf7347a306426dc1/com.shuqi.controller.apk";
        apks.add(apk15);
    }

    public void installPackage()
    {
        String apkName = "NovaSettings";
        //String apkPath = sdPath.concat("/").concat(apkName).concat(".apk");
        PackageInstallObserver2 installObserver2 = new PackageInstallObserver2();
        try {
            //String apkPath = sdPath.concat("/").concat(apkName).concat(".apk");
            String apkPath = "/storage/emulated/0/Download/com.tencent.mm.apk";
            String apkurl = "/storage/emulated/0/Download/com.tencent.mm.apk";
            //ApkUtils.install(this, new File(apkPath));
            //Log.d(TGA, "apkPath = " + apkPath);
            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            IBinder packAgeBinder = (IBinder) getService.invoke(null, "package");
            IPackageManager iPm = IPackageManager.Stub.asInterface(packAgeBinder);
            VerificationParams verificationParams=new VerificationParams();
            try {
                Log.i("maogl","1");
                iPm.installPackage(apkPath, installObserver2,INSTALL_REPLACE_EXISTING,
                        new File(apkPath).getPath(),verificationParams,null);
                Log.i("maogl","2");

            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
            Log.d("panzq", "安装失败1");

        }
    }

    public class PackageInstallObserver2 extends IPackageInstallObserver2.Stub {

        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            if (returnCode == 1) //返回1表示安装成功，否则安装失败
            {
                Toast.makeText(DownloadListActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
                Log.e("panzq", "packageName=" + packageName + ",returnCode=" + returnCode);
            } else {
                Toast.makeText(DownloadListActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean install(String apkPath) {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String command = " mount -o remount,rw /system\rpm install -r " + apkPath + "\r";
            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            // 读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            Log.d("TAG", "install msg is " + msg);
            Toast.makeText(this,"msg:" + msg, Toast.LENGTH_SHORT).show();

            // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
            if (!msg.contains("Failure")) {
                Toast.makeText(this, "成功！", Toast.LENGTH_SHORT).show();
                result = true;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception", Toast.LENGTH_SHORT).show();
            Log.e(TGA, e.getMessage(), e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                Log.e(TGA, e.getMessage(), e);
            }
        }
        return result;
    }

}

