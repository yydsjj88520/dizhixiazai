package com.wangyu.download.bean;

/**
 * Created by WYU on 2021/5/10.
 * 检查文件信息
 */

public class CheckFileInfo {
    private String url;
    private boolean fileExist;
    private boolean fileCompleted;
    private DownloadInfo downloadInfo;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setFileExist(boolean fileExist) {
        this.fileExist = fileExist;
    }

    public boolean isFileExist() {
        return fileExist;
    }

    public void setFileCompleted(boolean fileCompleted) {
        this.fileCompleted = fileCompleted;
    }

    public boolean isFileCompleted() {
        return fileCompleted;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }
}
