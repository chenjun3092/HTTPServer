package com.abt.httpserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by hwq on 2017/10/27 0027.
 */
public class IPAddressUtils {

    private static final String TAG = IPAddressUtils.class.getSimpleName();

    public static String getLocalIP() {
        WifiManager manager = (WifiManager) LauncherApp.getInstance()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //检查Wifi状态
        if (!manager.isWifiEnabled()) {
            return LauncherApp.getInstance().getString(R.string.wifi_disable);
        }
        WifiInfo info = manager.getConnectionInfo();

        //获取32位整型IP地址
        int ipAddress = info.getIpAddress();

        //把整型地址转换成“*.*.*.*”地址
        return intToIp(ipAddress);
    }

    private static String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
}
