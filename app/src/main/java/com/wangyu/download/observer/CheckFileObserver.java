package com.wangyu.download.observer;

import com.wangyu.download.bean.CheckFileInfo;
import com.wangyu.download.bean.DownloadInfo;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by WYU on 2021/5/10.
 */

public abstract class CheckFileObserver implements Observer<CheckFileInfo> {
    private Disposable d;//可以用于取消注册的监听者
    private CheckFileInfo checkFileInfo;

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(CheckFileInfo downloadInfo) {
        this.checkFileInfo = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
        if (checkFileInfo == null) {
            onFileNotExist(null);
        } else {
            if (!checkFileInfo.isFileExist()) {
                onFileNotExist(checkFileInfo.getDownloadInfo());
            } else if (checkFileInfo.isFileCompleted()) {
                onExistAndCompleted(checkFileInfo.getDownloadInfo());
            } else {
                onExistAndIncomplete(checkFileInfo.getDownloadInfo());
            }
        }

    }

    // 文件不存在
    protected abstract void onFileNotExist(DownloadInfo downloadInfo);

    // 文件存在但是下载不完整
    protected abstract void onExistAndIncomplete(DownloadInfo downloadInfo);

    // 文件存在并且下载完整
    protected abstract void onExistAndCompleted(DownloadInfo downloadInfo);
}
