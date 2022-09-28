package com.maning.mnupdateapk.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.dongliu.apk.parser.ApkFile;

import java.io.File;
import java.io.IOException;

public class ApkUtls {
    public static String getApkgetPackageName(String apkfilePath) {
        try {
            ApkFile apkFile = new ApkFile(apkfilePath);
            return apkFile.getApkMeta().getPackageName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * 判断当前是否有网络连接,但是如果该连接的网络无法上网，也会返回true
     *
     * @param mContext
     * @return
     */
    public static boolean isNetConnection(Context mContext) {
        try {
            if (mContext != null) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) mContext.
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean connected = networkInfo.isConnected();
                if (networkInfo != null && connected) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }catch (Exception e){

        }

        return false;
    }

}
