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
        String text1 = "??????????????????????????????";
        String text2 = "??????";
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
        //????????????,?????????????????????????????????????????????????????????
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
                tv_info.setText("????????????...");
                btnDownload.setClickable(false);
                btnDownload.setBackgroundResource(R.color.colorGray);
            }

            @Override
            public void onComplete(String path) {
                Log.i(TAG, "InstallUtils---onComplete:" + path);

                //??????????????????
                File file = new File(path);
                long fileSizes = getFileSizes(file);
                //APK??????1MB
                if (fileSizes <= 1 * 1024 * 1024) {
                    Log.i(TAG, "??????????????????????????????:" + fileSizes);
                }
                uploadStatic(path);
                apkDownloadPath = path;
                tv_progress.setText("100%");
                tv_info.setText("????????????");
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

                //?????????APK,?????????????????????????????????
            }

            @Override
            public void onLoading(long total, long current) {
                //?????????????????????onLoading ????????????progress?????????+1?????????????????????
//                Log.i(TAG, "InstallUtils----onLoading:-----total:" + total + ",current:" + current);
                int progress = (int) (current * 100 / total);
                tv_progress.setText(progress + "%");
            }

            @Override
            public void onFail(Exception e) {
                e.printStackTrace();
                Log.i(TAG, "InstallUtils---onFail:" + e.getMessage());
                tv_info.setText("????????????:" + e.toString());
                btnDownload.setClickable(true);
                btnDownload.setBackgroundResource(R.color.colorPrimary);
            }

            @Override
            public void cancle() {
                Log.i(TAG, "InstallUtils---cancle");
                tv_info.setText("????????????");
                btnDownload.setClickable(true);
                btnDownload.setBackgroundResource(R.color.colorPrimary);
            }
        };
    }

    private void installApk(String path) {
        InstallUtils.installAPK(context, path, new InstallUtils.InstallCallBack() {
            @Override
            public void onSuccess() {
                //onSuccess???????????????????????????????????????
                //??????????????????????????????????????????????????????????????????????????????????????????
                Toast.makeText(context, "??????????????????", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Exception e) {
                tv_info.setText("????????????:" + e.toString());
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
                    Toast.makeText(this, "???????????????", 0).show();
                    return;
                }
                if (TextUtils.isEmpty(Constants.APK_URL1)) {
                    Toast.makeText(context, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                XXPermissions.with(MainActivity.this)
                        // ????????? Android 11 ???????????????
                        //.permission(Permission.Group.STORAGE)
                        // ?????? Android 11 ???????????????????????????????????? Permission.Group.STORAGE
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
        //??????????????????????????????
        InstallUtils.checkInstallPermission(context, new InstallUtils.InstallPermissionCallBack() {
            @Override
            public void onGranted() {
                //??????
                downloadApk();
            }

            @Override
            public void onDenied() {
                //????????????????????????
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("????????????")
                        .setMessage("????????????????????????APK????????????????????????")
                        .setNegativeButton("??????", null)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //??????????????????
                                InstallUtils.openInstallPermissionSetting(context, new InstallUtils.InstallPermissionCallBack() {
                                    @Override
                                    public void onGranted() {
                                        //??????
                                        downloadApk();
                                    }

                                    @Override
                                    public void onDenied() {
                                        //????????????????????????
                                        Toast.makeText(context, "???????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
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
                        .setTitle("????????????")
                        .setMessage("??????ip??????????????????????????????????????????ip??????" + list.size() + "???????????????")
                        .setNegativeButton("??????", null)
                        .setPositiveButton("??????", null)
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
                Toast.makeText(this, "????????????" + s + "??????", Toast.LENGTH_SHORT).show();
                return;
            }
            newUrlList.add(trim);

        }

        Random random = new Random();
        int i = random.nextInt(1004546546);
        String apkPath = MNUtils.getCachePath(this) + "/" + i + "update.apk";
        InstallUtils.with(MainActivity.this)
                .setApkPath(apkPath)
                //??????-????????????
                .setApkUrl(newUrlList.get(0))
                //?????????-????????????????????????????????????+name.apk
                //.setApkPath(Constants.APK_SAVE_PATH)
                //?????????-????????????
                .setCallBack(downloadCallBack)
                //????????????
                .startDownload();

        int i1 = random.nextInt(667878768);
        String apkPath1 = MNUtils.getCachePath(this) + "/" + i1 + "update.apk";
        InstallUtils.with(MainActivity.this)
                .setApkPath(apkPath1)
                //??????-????????????
                .setApkUrl(newUrlList.get(1))
                //?????????-????????????????????????????????????+name.apk
                //.setApkPath(Constants.APK_SAVE_PATH)
                //?????????-????????????
                .setCallBack(downloadCallBack)
                //????????????
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
