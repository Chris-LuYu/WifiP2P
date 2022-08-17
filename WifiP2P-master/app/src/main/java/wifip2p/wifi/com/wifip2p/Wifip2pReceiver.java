package wifip2p.wifi.com.wifip2p;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.lang.reflect.Method;


import wifip2p.wifi.com.wifip2p.activity.BaseActivity;
import wifip2p.wifi.com.wifip2p.activity.SendFileActivity;

/**
 * date：2018/2/24 on 11:10
 * description: 客户端监听连接服务端信息的变化，以回调的形式把信息传递给发送文件界面
 */

public class Wifip2pReceiver extends BroadcastReceiver {

    public static final String TAG = "Wifip2pReceiver";

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private Wifip2pActionListener mListener;


    public Wifip2pReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel,
                           Wifip2pActionListener listener) {
        mWifiP2pManager = wifiP2pManager;
        mChannel = channel;
        mListener = listener;
    }



    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "接收到广播： " + intent.getAction());
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        switch (intent.getAction()) {
            //WiFi P2P是否可用
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:

                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    mListener.wifiP2pEnabled(true);

                } else {
                    mListener.wifiP2pEnabled(false);
                }
                break;

            // peers列表发生变化
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:

                mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mListener.onPeersInfo(peers.getDeviceList());

                    }
                });
                break;

            // WiFi P2P连接发生变化
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()){
                    mWifiP2pManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            mListener.onConnection(info);

                        }
                    });
                }else {
                    mListener.onDisconnection();
                }
                break;

            // WiFi P2P设备信息发生变化
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:

                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                mListener.onDeviceInfo(device);

                break;
            default:
                break;
        }
    }
    /**********************
    private void setWFDInfoInner(WifiP2pWfdInfo wfdInfo) {
        Log.e(TAG,"setWFDInfoInner被调用中");
        try {
            Method method = ReflectUtil.getMethod(mWifiP2pManager.getClass(), "setWFDInfo",
                    WifiP2pManager.Channel.class,
                    WifiP2pWfdInfo.class,
                    WifiP2pManager.ActionListener.class);
            Log.e(TAG,"反射调用中");
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
            Log.e(TAG,"反射调用结束1");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"反射调用结束2");
        }
    }


    public void setWfdInfoo() {
        Log.e(TAG,"setWfdInfoo开始被调用");
        final WifiP2pWfdInfo wfdInfo = new WifiP2pWfdInfo();
        // 开启WiFi Display
        wfdInfo.setEnabled(true);
        Log.e(TAG,"设置完setWfdEnabled");
        wfdInfo.setSessionAvailable(true);
        Log.e(TAG,"设置完setSessionAvailable");
        wfdInfo.setCoupledSinkSupportAtSink(false);
        Log.e(TAG,"设置完setCoupledSinkSupportAtSink");
        wfdInfo.setCoupledSinkSupportAtSource(false);
        Log.e(TAG,"setCoupledSinkSupportAtSource");
        //wfdInfo.setContentProtectionSupported(false);
        //wfdInfo.setCoupledSinkSupportAtSource(false);
        // 设置设备模式为SINK端（Miracast接收端）
        wfdInfo.setDeviceType(WifiP2pWfdInfo.PRIMARY_SINK);//
        Log.e(TAG,"setDeviceType");
        wfdInfo.setControlPort(7236);
        Log.e(TAG,"设置完setControlPort");
        wfdInfo.setMaxThroughput(50);
        Log.e(TAG,"设置完setMaxThroughput");
        setWFDInfoInner(wfdInfo);
        Log.e(TAG,"setWFDInfoInner调用结束");
    }*/

}
