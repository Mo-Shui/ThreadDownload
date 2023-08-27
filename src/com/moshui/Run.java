package com.moshui;

import com.moshui.core.Downloader;
import com.moshui.core.DownloaderWithDownloaderTask;
import com.moshui.util.LogUtils;

import java.util.Scanner;

public class Run {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LogUtils.info("请输入下载地址");
        String url = scanner.next();

//        Downloader downloader = new Downloader();
//        downloader.download(url);
        DownloaderWithDownloaderTask downloaderWithDownloaderTask = new DownloaderWithDownloaderTask();
        downloaderWithDownloaderTask.download(url);
    }

}
