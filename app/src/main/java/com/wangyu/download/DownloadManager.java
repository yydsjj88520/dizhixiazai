package com.wangyu.download;

import android.util.Log;

import com.wangyu.download.bean.CheckFileInfo;
import com.wangyu.download.bean.DownloadInfo;
import com.wangyu.download.listener.CheckFileListener;
import com.wangyu.download.listener.DownLoadListener;
import com.wangyu.download.observer.CheckFileObserver;
import com.wangyu.download.observer.DownLoadObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by WYU on 2021/5/10.
 */

public class DownloadManager {
    private HashMap<String, Call> downCalls;//用来存放各个下载的请求,如果已经在下载任务中，则不在重复下载
    private OkHttpClient mClient;//OKHttpClient;

    {
        RxJavaPlugins.setErrorHandler(throwable -> {
            Log.d("DownloadManager", "MNDevConfigManager RxJavaPlugins   throw " + throwable.getMessage());
        });
    }

    private static class Factory {
        private static DownloadManager INSTANCE = new DownloadManager();
    }

    //获得一个单例类，不是真正的单利
    public static DownloadManager getInstance() {
        return Factory.INSTANCE;
    }

    private DownloadManager() {
        downCalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    public synchronized void checkFileExist(String url, String savePath, CheckFileListener listener) {
        Observable.create((ObservableOnSubscribe<CheckFileInfo>) emitter -> {
            // 测试用的，获取设备基本信息，用的时候 改回来
            try {
                CheckFileInfo checkFileInfo = new CheckFileInfo();
                checkFileInfo.setUrl(url);
                DownloadInfo downloadInfo = createDownInfo(url, savePath);

                long downloadLength = 0, contentLength = downloadInfo.getTotal();
                File file = new File(downloadInfo.getLoaclUrl());
                if (file.exists()) {
                    //找到了文件,代表已经下载过,则获取其长度
                    downloadLength = file.length();
                    downloadInfo.setProgress(downloadLength);
                    checkFileInfo.setFileExist(true);
                }
                // 已下载部分文件大小与源文件大小比较，看是否是完整的文件
                if (downloadLength >= contentLength) {
                    checkFileInfo.setFileCompleted(true);
                } else {
                    checkFileInfo.setFileCompleted(false);
                }
                checkFileInfo.setDownloadInfo(downloadInfo);

                emitter.onNext(checkFileInfo);
                emitter.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CheckFileObserver() {

                    @Override
                    protected void onFileNotExist(DownloadInfo downloadInfo) {
                        if (listener != null) {
                            listener.onFileNotExist(downloadInfo);
                        }
                    }

                    @Override
                    protected void onExistAndIncomplete(DownloadInfo downloadInfo) {
                        if (listener != null) {
                            listener.onExistAndIncomplete(downloadInfo);
                        }
                    }

                    @Override
                    protected void onExistAndCompleted(DownloadInfo downloadInfo) {
                        if (listener != null) {
                            listener.onExistAndCompleted(downloadInfo);
                        }
                    }
                });
    }

    /**
     * 开始下载
     *
     * @param url              下载请求的网址
     * @param downLoadListener 用来回调的接口
     */
    public synchronized void download(String url, String savePath, DownLoadListener downLoadListener) {
        Observable.just(url)
                .filter(s -> !downCalls.containsKey(s))//call的map已经有了,就证明正在下载,则这次不下载
                .flatMap(s -> Observable.just(createDownInfo(s, savePath)))
                .map(this::getRealFileName)//检测本地文件夹,生成新的文件名
                .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))//下载
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io())//在子线程执行
                .subscribe(new DownLoadObserver() {

                    @Override
                    protected void onProgress(DownloadInfo downloadInfo) {
                        if (downLoadListener != null) {
                            downLoadListener.onProgress(downloadInfo);
                        }
                    }

                    @Override
                    protected void onComplete(DownloadInfo downloadInfo) {
                        if (downLoadListener != null) {
                            downLoadListener.onComplete(downloadInfo);
                        }
                    }
                });//添加观察者
    }


    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    public void delete(String filePath) {
        File file = new File(filePath);
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url, String savePath) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        long contentLength = getContentLength(url);//获得文件大小
        downloadInfo.setTotal(contentLength);
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        downloadInfo.setFileName(fileName);
        File loaclUrl = new File(savePath, fileName);
        downloadInfo.setLoaclPath(savePath);
        downloadInfo.setLoaclUrl(loaclUrl.getPath());
        return downloadInfo;
    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        long downloadLength = 0, contentLength = downloadInfo.getTotal();
        File file = new File(downloadInfo.getLoaclUrl());
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        String fileName = file.getName();

        //之前下载过,需要重新来一个文件
//        int i = 1;
//        while (downloadLength >= contentLength) {
//            int dotIndex = fileName.lastIndexOf(".");
//            String fileNameOther;
//            if (dotIndex == -1) {
//                fileNameOther = fileName + "(" + i + ")";
//            } else {
//                fileNameOther = fileName.substring(0, dotIndex)
//                        + "(" + i + ")" + fileName.substring(dotIndex);
//            }
//            File newFile = new File(downloadInfo.getLoaclPath(), fileNameOther);
//            file = newFile;
//            downloadLength = newFile.length();
//            i++;
//        }
        //设置改变过的文件名/大小
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getProgress();//已经下载好的长度
            long contentLength = downloadInfo.getTotal();//文件的总长度
            //初始进度信息
            e.onNext(downloadInfo);

            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            File file = new File(downloadInfo.getLoaclUrl());
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fileOutputStream.flush();
                downCalls.remove(url);
            } finally {
                //关闭IO流
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
            e.onComplete();//完成
        }
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DownloadInfo.TOTAL_ERROR;
    }


}
