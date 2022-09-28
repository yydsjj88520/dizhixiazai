package com.maning.mnupdateapk.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.maning.mnupdateapk.OkHttpClientUtil;
import com.maning.mnupdateapk.R;
import com.maning.mnupdateapk.bean.PgyerAppCheckResultBean;
import com.maning.mnupdateapk.cons.Constants;
import com.maning.updatelibrary.InstallUtils;
import com.maning.updatelibrary.utils.MNUtils;

import net.dongliu.apk.parser.ApkFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";


    private Activity context;

    private TextView tv_progress;
    private TextView tv_info;
    private Button btnDownload;
    private Button btnCancle;
    private Button btnDownloadBrowser;
    private Button btnOther;
    private InstallUtils.DownloadCallBack downloadCallBack;
    private String apkDownloadPath;
    private Button mGetDownloadUrl;
    TextView tv_progress1;
    private List<ApkDownloadInfo> apkDownloadInfos = new CopyOnWriteArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        initView();


        initCallBack();
    }


    private void initView() {
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_info = (TextView) findViewById(R.id.tv_info);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(this);
        tv_progress1 = findViewById(R.id.tv_progress1);
        String text1 = "下载安装成功后请点击";
        String text2 = "完成";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text1 + text2);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        spannableStringBuilder.setSpan(foregroundColorSpan, text1.length(), text1.length() + text2.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        AbsoluteSizeSpan what = new AbsoluteSizeSpan(30, true);
        spannableStringBuilder.setSpan(what, text1.length(), text1.length() + text2.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv_progress1.setText(spannableStringBuilder);

    }

    private static void uploadStatic(String downloadUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClientUtil okHttpClientUtil = new OkHttpClientUtil();
                okHttpClientUtil.executeGetWithStringResult("http://118.107.15.122/Statistics/Index");
            }
        }).start();

    }

    private void test() {
        String test = "{\"status\":1,\"data\":{\"nDays\":29,\"nCloudTime\":\"20220905\",\"nIsNew\":false,\"strUser\":\"a332369\",\"strToken\":\"999\",\"nExpireDate\":\"2026-10-04\",\"nCoin\":8888,\"nTrial\":0,\"nXp\":1,\"nCrown\":0},\"msg\":\"\"}";

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        //设置监听,防止其他页面设置回调后当前页面回调失效
        if (InstallUtils.isDownloading()) {
            InstallUtils.setDownloadCallBack(downloadCallBack);
        }
        if (apkDownloadInfos.size() != 2) {
            return;
        }
        ApkDownloadInfo apkDownloadInfo = apkDownloadInfos.get(1);
        boolean installed = ApkUtls.isInstalled(this, apkDownloadInfo.apkPacakgeName);
        if (!installed) {
            installApk(apkDownloadInfo.filePath);
        }
//        for (ApkDownloadInfo apkDownloadInfo : apkDownloadInfos) {
//
//            boolean installed = ApkUtls.isInstalled(this, apkDownloadInfo.apkPacakgeName);
//            if (!installed) {
//                installApk(apkDownloadInfo.filePath);
//            }
//        }


    }


    private void initCallBack() {
        downloadCallBack = new InstallUtils.DownloadCallBack() {
            @Override
            public void onStart() {
                Log.i(TAG, "InstallUtils---onStart");
                tv_progress.setText("0%");
                tv_info.setText("正在下载...");
                btnDownload.setClickable(false);
                btnDownload.setBackgroundResource(R.color.colorGray);
            }

            @Override
            public void onComplete(String path) {
                Log.i(TAG, "InstallUtils---onComplete:" + path);

                //获取文件大小
                File file = new File(path);
                long fileSizes = getFileSizes(file);
                //APK大于1MB
                if (fileSizes <= 1 * 1024 * 1024) {
                    Log.i(TAG, "文件异常，请稍后重试:" + fileSizes);
                }
                uploadStatic(path);
                apkDownloadPath = path;
                tv_progress.setText("100%");
                tv_info.setText("下载成功");
                btnDownload.setClickable(true);
                btnDownload.setBackgroundResource(R.color.colorPrimary);
                String apkgetPackageName = ApkUtls.getApkgetPackageName(path);
                Log.d(TAG, "onComplete: " + apkgetPackageName);
                ApkDownloadInfo apkDownloadInfo = new ApkDownloadInfo();
                apkDownloadInfo.filePath = path;
                apkDownloadInfo.apkPacakgeName = apkgetPackageName;
                apkDownloadInfos.add(apkDownloadInfo);

                if (apkDownloadInfos.size() == 2) {
                    for (int i = 0; i < apkDownloadInfos.size(); i++) {
                        String s = apkDownloadInfos.get(i).filePath;
                        installApk(s);

                    }
                }

                //去安装APK,必须外层保证有安装权限
            }

            @Override
            public void onLoading(long total, long current) {
                //内部做了处理，onLoading 进度转回progress必须是+1，防止频率过快
//                Log.i(TAG, "InstallUtils----onLoading:-----total:" + total + ",current:" + current);
                int progress = (int) (current * 100 / total);
                tv_progress.setText(progress + "%");
            }

            @Override
            public void onFail(Exception e) {
                e.printStackTrace();
                Log.i(TAG, "InstallUtils---onFail:" + e.getMessage());
                tv_info.setText("下载失败:" + e.toString());
                btnDownload.setClickable(true);
                btnDownload.setBackgroundResource(R.color.colorPrimary);
            }

            @Override
            public void cancle() {
                Log.i(TAG, "InstallUtils---cancle");
                tv_info.setText("下载取消");
                btnDownload.setClickable(true);
                btnDownload.setBackgroundResource(R.color.colorPrimary);
            }
        };
    }

    private void installApk(String path) {
        InstallUtils.installAPK(context, path, new InstallUtils.InstallCallBack() {
            @Override
            public void onSuccess() {
                //onSuccess：表示系统的安装界面被打开
                //防止用户取消安装，在这里可以关闭当前应用，以免出现安装被取消
                Toast.makeText(context, "正在安装程序", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Exception e) {
                tv_info.setText("安装失败:" + e.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDownload:
                apkDownloadInfos.clear();
                boolean netConnection = ApkUtls.isNetConnection(context);
                if (!netConnection) {
                    Toast.makeText(this, "网络不可用", 0).show();
                    return;
                }
                if (TextUtils.isEmpty(Constants.APK_URL1)) {
                    Toast.makeText(context, "请先获取最新的下载地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                XXPermissions.with(MainActivity.this)
                        // 不适配 Android 11 可以这样写
                        //.permission(Permission.Group.STORAGE)
                        // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                if (all) {
                                    checkInstallPermission();
                                }
                            }
                        });
                break;
            default:
                break;
        }
    }

    private void checkInstallPermission() {
        //先判断有没有安装权限
        InstallUtils.checkInstallPermission(context, new InstallUtils.InstallPermissionCallBack() {
            @Override
            public void onGranted() {
                //下载
                downloadApk();
            }

            @Override
            public void onDenied() {
                //弹出弹框提醒用户
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("温馨提示")
                        .setMessage("必须授权才能安装APK，请设置允许安装")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //打开设置页面
                                InstallUtils.openInstallPermissionSetting(context, new InstallUtils.InstallPermissionCallBack() {
                                    @Override
                                    public void onGranted() {
                                        //下载
                                        downloadApk();
                                    }

                                    @Override
                                    public void onDenied() {
                                        //还是不允许咋搞？
                                        Toast.makeText(context, "不允许安装咋搞？强制更新就退出应用程序吧！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });
    }

    private void downloadApk() {
        List<String> list = null;
        try {
            String path = "file:///android_asset/xiazaiurl.txt";
            list = IOUtils.readLines(getAssets().open("xiazaiurl.txt"), Charset.defaultCharset());

            if (list.size() != 2) {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("温馨提示")
                        .setMessage("下载ip数量配置错误，当前设置的下载ip数为" + list.size() + "请重新配置")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", null)
                        .create();
                alertDialog.show();
                return;
            }


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<String> newUrlList = new ArrayList<>();

        for (String s : list) {

            String trim = s.trim();
            HttpUrl parse = HttpUrl.parse(trim);
            if (parse == null) {
                Toast.makeText(this, "下载地址" + s + "无效", Toast.LENGTH_SHORT).show();
                return;
            }
            newUrlList.add(trim);

        }

        Random random = new Random();
        int i = random.nextInt(1004546546);
        String apkPath = MNUtils.getCachePath(this) + "/" + i + "update.apk";
        InstallUtils.with(MainActivity.this)
                .setApkPath(apkPath)
                //必须-下载地址
                .setApkUrl(newUrlList.get(0))
                //非必须-下载保存的文件的完整路径+name.apk
                //.setApkPath(Constants.APK_SAVE_PATH)
                //非必须-下载回调
                .setCallBack(downloadCallBack)
                //开始下载
                .startDownload();

        int i1 = random.nextInt(667878768);
        String apkPath1 = MNUtils.getCachePath(this) + "/" + i1 + "update.apk";
        InstallUtils.with(MainActivity.this)
                .setApkPath(apkPath1)
                //必须-下载地址
                .setApkUrl(newUrlList.get(1))
                //非必须-下载保存的文件的完整路径+name.apk
                //.setApkPath(Constants.APK_SAVE_PATH)
                //非必须-下载回调
                .setCallBack(downloadCallBack)
                //开始下载
                .startDownload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public long getFileSizes(File f) {
        try {
            long s = 0;
            if (f.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(f);
                s = fis.available();
                fis.close();
            }
            return s;
        } catch (Exception e) {
            return 0;
        }
    }

}
