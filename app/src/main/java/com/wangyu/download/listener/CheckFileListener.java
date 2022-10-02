package com.wangyu.download.listener;

import com.wangyu.download.bean.DownloadInfo;

/**
 * @WYU-WIN
 * @date 2021/6/9
 * @description
 */
public interface CheckFileListener {
    // 文件不存在
    void onFileNotExist(DownloadInfo downloadInfo);

    // 文件存在但是下载不完整
    void onExistAndIncomplete(DownloadInfo downloadInfo);

    // 文件存在并且下载完整
    void onExistAndCompleted(DownloadInfo downloadInfo);
}
