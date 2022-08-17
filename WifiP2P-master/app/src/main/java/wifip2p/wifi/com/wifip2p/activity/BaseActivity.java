package wifip2p.wifi.com.wifip2p.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
//import android.net.wifi.p2p.;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import wifip2p.wifi.com.wifip2p.Wifip2pReceiver;
import wifip2p.wifi.com.wifip2p.Wifip2pActionListener;

public class BaseActivity extends AppCompatActivity implements Wifip2pActionListener {

    private static final String TAG = "BaseActivity";
    private static final boolean DEBUG = true;
   // private static WifiP2pManager mWifiP2pManager;
    Handler mHandler;

    public WifiP2pManager mWifiP2pManager;
    public WifiP2pManager.Channel mChannel;
    public Wifip2pReceiver mWifip2pReceiver;
    public WifiP2pInfo mWifiP2pInfo;
    public Wifip2pActionListener mActionListener;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//注册WifiP2pManager
        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), this);
        //注册广播
        mWifip2pReceiver = new Wifip2pReceiver(mWifiP2pManager, mChannel, this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mWifip2pReceiver, intentFilter);

        //setWfdInfoo();

        setEnableWFD(mWifiP2pManager, mChannel, true, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"Successfully enabled WFD.");
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG,"Failed to enable WFD with reason " + reason + ".");
            }
        });
/*
        setP2pDeviceName(mWifiP2pManager, mChannel, getNickName(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"Successfully set P2pDeviceName:" + getNickName());
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG,"Failed to set P2pDeviceName with reason " + reason + ".");
            }
        });
*/
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销广播
        unregisterReceiver(mWifip2pReceiver);
        mWifip2pReceiver = null;
    }


    @Override
    public void wifiP2pEnabled(boolean enabled) {
        Log.e(TAG, "传输通道是否可用：" + enabled);
    }

    @Override
    public void onConnection(WifiP2pInfo wifiP2pInfo) {
        if (wifiP2pInfo != null) {
            mWifiP2pInfo = wifiP2pInfo;
            Log.e(TAG, "WifiP2pInfo:" + wifiP2pInfo);
        }
    }

    @Override
    public void onDisconnection() {
        Log.e(TAG, "连接断开");
    }

    @Override
    public void onDeviceInfo(WifiP2pDevice wifiP2pDevice) {
        Log.e(TAG, "当前的的设备名称" + wifiP2pDevice.deviceName);
    }
    @Override
    public void onPeersInfo(Collection<WifiP2pDevice> wifiP2pDeviceList) {
        for (WifiP2pDevice device : wifiP2pDeviceList) {
            Log.e(TAG, "连接的设备信息：" + device.deviceName + "--------" + device.deviceAddress);
        }
    }

    @Override
    public void onChannelDisconnected() {

    }


    /*****************************************************************
     * Chris
     * 反射调用WifiP2pWfdInfo类 和 setWFDInfo()
     * **************************************************************/
    private void setEnableWFD(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, boolean enable, WifiP2pManager.ActionListener listener) {
        try {
            //WifiP2pWfdInfo wifiP2pWfdInfo = new WifiP2pWfdInfo();
            Class clsWifiP2pWfdInfo = Class.forName("android.net.wifi.p2p.WifiP2pWfdInfo");
            Constructor ctorWifiP2pWfdInfo = clsWifiP2pWfdInfo.getConstructor();
            Object wifiP2pWfdInfo = ctorWifiP2pWfdInfo.newInstance();

            //wifiP2pWfdInfo.setWfdEnabled(true);
            Method mtdSetWfdEnabled = clsWifiP2pWfdInfo.getMethod("setWfdEnabled", boolean.class);
            mtdSetWfdEnabled.invoke(wifiP2pWfdInfo, enable);

            //wifiP2pWfdInfo.setDeviceType(WifiP2pWfdInfo.PRIMARY_SINK);
            Method mtdSetDeviceTypes = clsWifiP2pWfdInfo.getMethod("setDeviceType", int.class);
            mtdSetDeviceTypes.invoke(wifiP2pWfdInfo, 1);

            //wifiP2pWfdInfo.setSessionAvailable(true);
            Method mtdSetSessionAvailable = clsWifiP2pWfdInfo.getMethod("setSessionAvailable", boolean.class);
            mtdSetSessionAvailable.invoke(wifiP2pWfdInfo, enable);

            //wifiP2pWfdInfo.setMaxThroughput(MAX_THROUGHPUT);
            Method mtdSetMaxThroughput = clsWifiP2pWfdInfo.getMethod("setMaxThroughput", int.class);
            mtdSetMaxThroughput.invoke(wifiP2pWfdInfo, 50);

            if (listener != null) {
                Class clsWifiP2pManager = Class.forName("android.net.wifi.p2p.WifiP2pManager");
                Method methodSetWFDInfo = clsWifiP2pManager.getMethod("setWFDInfo",
                        WifiP2pManager.Channel.class, clsWifiP2pWfdInfo, WifiP2pManager.ActionListener.class);
                methodSetWFDInfo.invoke(wifiP2pManager, channel, wifiP2pWfdInfo, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Failed to setEnableWFD: " + e.getLocalizedMessage());
        }
    }
    /*
    private void setP2pDeviceName(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, String deviceName, WifiP2pManager.ActionListener listener) {
        try {
            Method m = wifiP2pManager.getClass().getMethod(
                    "setDeviceName", WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class);
            m.invoke(wifiP2pManager, channel, deviceName, listener);
        } catch (Exception e) {
            Log.e(TAG,"Failed to setP2pDeviceName:" + e.getLocalizedMessage());
        }
    }

    private String getNickName() {
        return "Android_1826";
    }*/
}

    /****
     * Test
     *
    private void setWFDInfoInner(WifiP2pWfdInfo wfdInfo)  {
        Log.e(TAG,"setWFDInfoInner被调用中");
        //  Class ReflectUtil1 = Class.forName("WifiP2pManager");
        try {
            Log.e(TAG,"getinvoke1");
            Method method = ReflectUtil.getMethod(mWifiP2pManager.getClass(), "setWFDInfo",
                    WifiP2pManager.Channel.class,
                    WifiP2pWfdInfo.class,
                    WifiP2pManager.ActionListener.class);
            Log.e(TAG,"getinvoke2");
            //WifiP2pManager mPManager = new WifiP2pManager();
            method.invoke(mWifiP2pManager,mChannel, wfdInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "setWFDInfo onSuccess:");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "setWFDInfo onFailure:" + reason);
                }

            });
            Log.e(TAG,"getinvoke3");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG,"反射调用结束1");
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"反射调用结束2");
        }
    }


    public void setWfdInfoo() {
        Log.e(TAG,"setWfdInfoo开始被调用");
        final WifiP2pWfdInfo wfdInfo = new WifiP2pWfdInfo();
        // 开启WiFi Display
        wfdInfo.setWfdEnabled(true);
        Log.e(TAG,"设置完setWfdEnabled");
        wfdInfo.setDeviceType(WifiP2pWfdInfo.PRIMARY_SINK);//
        Log.e(TAG,"setDeviceType");
        wfdInfo.setSessionAvailable(true);
        Log.e(TAG,"设置完setSessionAvailable");
        //wfdInfo.setCoupledSinkSupportAtSink(false);
        Log.e(TAG,"设置完setCoupledSinkSupportAtSink");
        //wfdInfo.setCoupledSinkSupportAtSource(false);
        Log.e(TAG,"setCoupledSinkSupportAtSource");
        //wfdInfo.setContentProtectionSupported(false);
        //wfdInfo.setCoupledSinkSupportAtSource(false);
        // 设置设备模式为SINK端（Miracast接收端）

        wfdInfo.setControlPort(7236);
        Log.e(TAG,"设置完setControlPort");
        wfdInfo.setMaxThroughput(50);
        Log.e(TAG,"设置完setMaxThroughput");
        setWFDInfoInnerr(wfdInfo);
        Log.e(TAG,"setWFDInfoInner调用结束");
        Log.e(TAG,"调用run");
        // mWifip2pReceiver.onReceive(context,intent);

        run();
    }

    public void run(){

        mWifiP2pManager.discoverPeers(mChannel, (WifiP2pManager.ActionListener) mActionListener);
        Log.e(TAG,"调用run结束");
    }
    *****************************/
    /**
    public boolean  setWFDInfoInnerr(WifiP2pWfdInfo wfdInfo) {
        Log.e(TAG, "setWFDInfoInnerr调用中");
        try {
            Log.e(TAG, "getinvoke1");
            //Class managerClass = WifiP2pManager.class;
            Class<WifiP2pManager> managerClass = (Class<android.net.wifi.p2p.WifiP2pManager>) Class.forName("android.net.wifi.p2p.WifiP2pManager");
            Log.e(TAG, "getinvoke2");
            Method localmethod = managerClass.getMethod("setWFDInfo",
                    WifiP2pManager.Channel.class,
                    WifiP2pWfdInfo.class,
                    WifiP2pManager.ActionListener.class);
            Log.e(TAG, "getinvoke3");
            localmethod.invoke(mWifiP2pManager, mChannel, wfdInfo, new WifiP2pManager.ActionListener() {
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
            Log.e(TAG, "反射调用结束1");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "反射调用结束2");
        }
        Log.e(TAG, "返回true");
        return true;
    }
    **/