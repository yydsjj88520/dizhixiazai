package com.wangyu.download.listener;

import com.wangyu.download.bean.DownloadInfo;

/**
 * @WYU-WIN
 * @date 2021/6/9
 * @description
 */
public interface DownLoadListener {
    void onProgress(DownloadInfo downloadInfo);

    void onComplete(DownloadInfo downloadInfo);
}
