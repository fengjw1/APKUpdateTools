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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import com.fengjw.apkupdatetool.Adapter.DownloadAdapter;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.task.XExecutor;

import butterknife.Bind;
import butterknife.OnClick;


public class DownloadAllActivity extends BaseActivity implements XExecutor.OnAllTaskEndListener {

//    @Bind(R.id.toolbar)
//    Toolbar toolbar;
    @Bind(R.id.recyclerView) RecyclerView recyclerView;

    private DownloadAdapter adapter;
    private OkDownload okDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_all);
       //initToolBar(toolbar, true, "所有任务");

        okDownload = OkDownload.getInstance();
        //okDownload.removeAll(true);
        adapter = new DownloadAdapter(this);
        adapter.updateData(DownloadAdapter.TYPE_ALL);
        //recyclerView.setFocusableInTouchMode(true);
        //recyclerView.requestFocus();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        okDownload.addOnAllTaskEndListener(this);

    }


    //back keyboard
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
//            Intent intent = new Intent(DownloadAllActivity.this,DownloadListActivity.class);
//            startActivity(intent);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAllTaskEnd() {
        showToast("所有下载任务已结束");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        okDownload.removeOnAllTaskEndListener(this);
        adapter.unRegister();
        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        //finish();
    }

    @OnClick(R.id.removeAll)
    public void removeAll(View view) {
        //okDownload.removeAll();
        okDownload.removeAll(true);
        adapter.updateData(DownloadAdapter.TYPE_ALL);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.pauseAll)
    public void pauseAll(View view) {
        okDownload.pauseAll();
    }

    @OnClick(R.id.startAll)
    public void startAll(View view) {
        okDownload.startAll();
    }
}
