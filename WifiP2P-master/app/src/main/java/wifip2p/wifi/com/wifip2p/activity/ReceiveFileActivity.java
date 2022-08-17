package wifip2p.wifi.com.wifip2p.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import wifip2p.wifi.com.wifip2p.ProgressDialog;
import wifip2p.wifi.com.wifip2p.R;
import wifip2p.wifi.com.wifip2p.Wifip2pService;
import wifip2p.wifi.com.wifip2p.socket.ReceiveSocket;


/**
 * 接收文件界面
 * 1、创建组群信息
 * 2、移除组群信息
 * 3、启动服务，创建serversocket，监听客户端端口，把信息写入文件
 */
public class ReceiveFileActivity extends BaseActivity implements ReceiveSocket.ProgressReceiveListener, View.OnClickListener {

    private static final String TAG = "ReceiveFileActivity";
    private Wifip2pService.MyBinder mBinder;
    private ProgressDialog mProgressDialog;
    private Intent mIntent;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //调用服务里面的方法进行绑定
            mBinder = (Wifip2pService.MyBinder) service;
            mBinder.initListener(ReceiveFileActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //服务断开重新绑定
            bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);
        Button btnCreate = (Button) findViewById(R.id.btn_create);
        Button btnRemove = (Button) findViewById(R.id.btn_remove);
        btnCreate.setOnClickListener(this);
        btnRemove.setOnClickListener(this);

        mIntent = new Intent(ReceiveFileActivity.this, Wifip2pService.class);
        startService(mIntent);
        bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create:

                createGroup();
               // setWfdInfoo();
                break;
            case R.id.btn_remove:
                removeGroup();
                break;
        }
        //setWfdInfoo();
    }

    /**
     * 创建组群，等待连接
     */
    public void createGroup() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        //    setWfdInfoo();
            return;
        }

        mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Log.e(TAG, "创建群组成功");
                //Log.e(TAG, "开始调用setwfdinfoo");
                //setWfdInfoo();
                Toast.makeText(ReceiveFileActivity.this, "创建群组成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "创建群组失败: " + reason);
                Toast.makeText(ReceiveFileActivity.this, "创建群组失败,请移除已有的组群或者连接同一WIFI重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 移除组群
     */
    public void removeGroup() {
        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "移除组群成功");
                Toast.makeText(ReceiveFileActivity.this, "移除组群成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "移除组群失败");
                Toast.makeText(ReceiveFileActivity.this, "移除组群失败,请创建组群重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSatrt() {
        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    public void onProgressChanged(File file, int progress) {
        Log.e(TAG, "接收进度：" + progress);
        mProgressDialog.setProgress(progress);
        mProgressDialog.setProgressText(progress + "%");
    }

    @Override
    public void onFinished(File file) {
        Log.e(TAG, "接收完成");
        mProgressDialog.dismiss();
        Toast.makeText(this, file.getName() + "接收完毕！", Toast.LENGTH_SHORT).show();
        //接收完毕后再次启动服务等待下载一次连接，不启动只能接收一次，第二次无效，原因待尚不清楚
        clear();
        startService(mIntent);
        bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onFaliure(File file) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        Toast.makeText(this, "接收失败，请重试！", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        clear();
    }

    /**
     * 释放资源
     */
    private void clear() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        if (mIntent != null) {
            stopService(mIntent);
        }
    }
}


    /*****************************************************/
    /*public static class ReflectUtil {

        private static Object Method;

        public static Method getMethod(Class<? extends WifiP2pManager> aClass,
                                       String setWFDInfo, Class<WifiP2pManager.Channel> channelClass,
                                       Class<WifiP2pWfdInfo> wifiP2pWfdInfoClass,
                                       Class<WifiP2pManager.ActionListener> actionListenerClass) {
            return (java.lang.reflect.Method) Method;}
    }
*/
    /*
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setWFDInfoInner(WifiP2pWfdInfo wfdInfo) {
        try {
            Method method = ReflectUtil.getMethod(mWifiP2pManager.getClass(), "setWFDInfo",
                    WifiP2pManager.Channel.class,
                    WifiP2pWfdInfo.class,
                    WifiP2pManager.ActionListener.class);
            method.invoke(mWifiP2pManager, mChannel, wfdInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "setWFDInfo onSuccess:");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "setWFDInfo onFailure:" + reason);
                }
            });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void setWfdInfoo() {
        final WifiP2pWfdInfo wfdInfo = new WifiP2pWfdInfo();
        // 开启WiFi Display
        wfdInfo.setEnabled(true);
        wfdInfo.setSessionAvailable(true);
        wfdInfo.setContentProtectionSupported(false);
       // wfdInfo.(false);
        // 设置设备模式为SINK端（Miracast接收端）
        wfdInfo.setDeviceType(WifiP2pWfdInfo.DEVICE_TYPE_PRIMARY_SINK);//
        wfdInfo.setControlPort(7236);
        wfdInfo.setMaxThroughput(50);
        setWFDInfoInner(wfdInfo);
    }
}*/
