package com.moshui.core;

import com.moshui.constant.Constant;
import com.moshui.util.FileUtils;
import com.moshui.util.HttpUtils;
import com.moshui.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 下载器
 */

public class Downloader {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public void download(String url) {
        //文件存放路径
        String filePath = Constant.PATH + HttpUtils.getHttpFileName(url);

        //获取本地文件的大小
        long localFileLength = FileUtils.getFileContentLength(filePath);

        HttpURLConnection httpURLConnection = null;
        DownloadInfoThread downloadInfoThread = null;
        try {
            httpURLConnection = HttpUtils.getHttpURLConnection(url);

            //获取下载文件的总大小
            int contentLength = httpURLConnection.getContentLength();

            if(localFileLength >= contentLength){
                //长度相等，文件已下载过
                LogUtils.info("%s已下载过",filePath);

                return;
            }

            //创建获取下载信息的任务对象
            downloadInfoThread = new DownloadInfoThread(contentLength);

            //将任务对象交给线程执行，每隔一秒执行一次
            scheduledExecutorService.scheduleAtFixedRate(downloadInfoThread,1,1, TimeUnit.SECONDS);
        } catch (IOException e) {
            LogUtils.error("获取HttpURLConnection失败%s",url);
            throw new RuntimeException(e);
        }

        //下载操作
        try (
                BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        ) {
            int length = -1;
            //自己构建一个缓冲
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            while ((length = bis.read(buffer)) != -1) {
//                downloadInfoThread.downSize += length;
                DownloadInfoThread.downSize.add(length);

                bos.write(buffer,0,length);
            }
        } catch (IOException e) {
            LogUtils.error("下载文件失败%s",url);
            throw new RuntimeException(e);
        } finally {
            System.out.print("\r");
            System.out.print("下载完成");

            httpURLConnection.disconnect();

            scheduledExecutorService.shutdownNow();
        }
    }

}
