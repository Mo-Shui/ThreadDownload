package com.moshui.core;

import com.moshui.constant.Constant;
import com.moshui.util.HttpUtils;
import com.moshui.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * 分块下载任务类
 * 使用Callable是因为需要返回值来表示下载成功或失败
 */

public class DownloaderTask implements Callable<Boolean> {

    private String url;
    private long startPos;
    private long endPos;

    private int part;   //标识当前下载的是第几块，同时作为临时文件的文件名

    private CountDownLatch countDownLatch;

    public DownloaderTask(String url, long startPos, long endPos, int part,CountDownLatch countDownLatch) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Boolean call() throws Exception {
        String httpFileName = HttpUtils.getHttpFileName(url);
        //拼接分块的文件名
        httpFileName = httpFileName + ".temp" + part;
        //拼接下载路径
        httpFileName = Constant.PATH + httpFileName;

        //获取分块下载的链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);

        try(
                InputStream is = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                //TODO 可用于断点下载的类
                RandomAccessFile accessFile = new RandomAccessFile(httpFileName,"rw");
        ){
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            int length = -1;
            while((length = bis.read(buffer)) != -1){
                //统计一秒内下载数据之和，通过原子类添加
                DownloadInfoThread.downSize.add(length);

                accessFile.write(buffer,0,length);
            }
        }catch (FileNotFoundException e){
            LogUtils.error("下载文件不存在：%s",url);

            return false;
        }catch (Exception e){
            LogUtils.error("下载错误");

            return false;
        }finally {
            httpURLConnection.disconnect();

            //用于计数、阻塞
            countDownLatch.countDown();
        }

        return true;
    }
}
