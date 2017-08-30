package com.fengjw.apkupdatetool;

import android.annotation.SuppressLint;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.VerificationParams;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fengjw.apkupdatetool.utils.AppInfo;
import com.fengjw.apkupdatetool.utils.AppInfoProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int GET_ALL_APP_FINISH = 1;
    private final int INSTALL_REPLACE_EXISTING = 2;
    private ListView lv_app_manager;//应用信息列表
    private AppInfoProvider provider;
    private AppManagerAdapter adapter;
    private List<AppInfo> list;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case GET_ALL_APP_FINISH :
                    //进度条设置为不可见
                    adapter = new AppManagerAdapter();
                    lv_app_manager.setAdapter(adapter);
                    break;

                default :
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btn_startService);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                installPackage();
            }
        });

        lv_app_manager = (ListView) findViewById(R.id.lv);

        /**
         * //开一个线程用于完成对所有应用程序信息的搜索
         * 当搜索完成之后，就把一个成功的消息发送给Handler，
         * 然后handler把搜索到的数据设置进入listview里面  .
         * */
        new Thread()
        {
            public void run()
            {
                provider = new AppInfoProvider(MainActivity.this);
                list = provider.getAllApps();
                Message msg = new Message();
                msg.what = GET_ALL_APP_FINISH;
                handler.sendMessage(msg);
            };
        }.start();
    }

    //======================================================================

    private class AppManagerAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return list.size();
        }

        @Override
        public Object getItem(int position)
        {
            return list.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            AppInfo info = list.get(position);
            if(convertView == null)
            {
                View view = View.inflate(MainActivity.this, R.layout.app_manager_item, null);
                AppManagerViews views = new AppManagerViews();
                views.iv_app_icon = (ImageView) view.findViewById(R.id.iv_app_manager_icon);
                views.tv_app_name = (TextView) view.findViewById(R.id.tv_app_manager_name);
                views.tv_app_versionname = (TextView) view.findViewById(R.id.tv_app_manager_versionName);
                views.iv_app_icon.setImageDrawable(info.getIcon());
                views.tv_app_name.setText(info.getPkg_name());
                views.tv_app_versionname.setText(info.getVerCode()+"");
                view.setTag(views);
                return view;
            }
            else
            {
                AppManagerViews views = (AppManagerViews) convertView.getTag();
                views.iv_app_icon.setImageDrawable(info.getIcon());
                views.tv_app_name.setText(info.getPkg_name());
                views.tv_app_versionname.setText(info.getVerCode()+"");
                return convertView;
            }
        }

    }
    /**
     * 用来优化listview的类
     * */
    private class AppManagerViews
    {
        ImageView iv_app_icon;
        TextView tv_app_name;
        TextView tv_app_versionname;
    }

    public void installPackage()
    {
        String apkName = "NovaSettings";
        //String apkPath = sdPath.concat("/").concat(apkName).concat(".apk");
        PackageInstallObserver2 installObserver2 = new PackageInstallObserver2();
        try {
            //String apkPath = sdPath.concat("/").concat(apkName).concat(".apk");
            String apkPath = "/storage/emulated/0/Download/com.tencent.mm.apk";
            String apkurl = "/mnt/usb/0A4022DC4022CDEF/MTvPlayer.apk";
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
                iPm.installPackage(apkurl, installObserver2,INSTALL_REPLACE_EXISTING,
                        new File(apkurl).getPath(),verificationParams,null);
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
                Toast.makeText(MainActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
                Log.e("panzq", "packageName=" + packageName + ",returnCode=" + returnCode);
            } else {
                Toast.makeText(MainActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

