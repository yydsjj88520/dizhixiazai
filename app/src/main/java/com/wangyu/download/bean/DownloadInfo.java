package com.wangyu.download.bean;

/**
 * Created by WYU on 2021/5/10.
 * 下载信息
 */

public class DownloadInfo {
    public static final long TOTAL_ERROR = -1;//获取进度失败
    private String url;
    private long total;
    private long progress;
    private String fileName;
    private String loaclPath;
    private String loaclUrl;

    public DownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public void setLoaclUrl(String loaclUrl) {
        this.loaclUrl = loaclUrl;
    }

    public String getLoaclUrl() {
        return loaclUrl;
    }

    public void setLoaclPath(String loaclPath) {
        this.loaclPath = loaclPath;
    }

    public String getLoaclPath() {
        return loaclPath;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "url='" + url + '\'' +
                ", total=" + total +
                ", progress=" + progress +
                ", fileName='" + fileName + '\'' +
                ", loaclPath='" + loaclPath + '\'' +
                ", loaclUrl='" + loaclUrl + '\'' +
                '}';
    }
}
