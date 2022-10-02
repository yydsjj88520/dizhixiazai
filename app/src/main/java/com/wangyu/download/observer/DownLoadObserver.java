package com.wangyu.download.observer;


import com.wangyu.download.bean.DownloadInfo;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by WYU on 2021/5/10.
 */

public abstract class DownLoadObserver implements Observer<DownloadInfo> {
    private Disposable d;//可以用于取消注册的监听者
    private DownloadInfo downloadInfo;

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
        onProgress(downloadInfo);
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
        onComplete(downloadInfo);
    }

    protected abstract void onProgress(DownloadInfo downloadInfo);

    protected abstract void onComplete(DownloadInfo downloadInfo);

}
