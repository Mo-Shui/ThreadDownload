package com.moshui.core;

import com.moshui.constant.Constant;

import java.util.concurrent.atomic.LongAdder;

/**
 * 展示下载信息
 */

public class DownloadInfoThread implements Runnable{

    //文件总大小（单位：字节）
    private long httpFileContentLength;

    //TODO 用这个变量做断点下载
    //本地已下载文件的大小
//    public double finishedSize;
    //方便操作，添加static，但是在一个用户下载一个文件才可以
    public static LongAdder finishedSize = new LongAdder();

    //本次累积下载的大小
//    public volatile double downSize;
    //方便操作，添加static，但是在一个用户下载一个文件才可以
    public static volatile LongAdder downSize = new LongAdder();

    //前一次下载的大小
    public double preSize;

    public DownloadInfoThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
    }

    @Override
    public void run() {
        /*
         * 计算文件总大小
         * 文件总大小变量的单位是字节，所以要除以MB，才能变为MB单位，再保留2位小数
         */
        String httpFileSize = String.format("%.2f", httpFileContentLength / Constant.MB);

        //计算每秒下载速度，downSize和preSize单位为字节，除以1024即得单位为KB
        int speed = (int)((downSize.doubleValue() - preSize) / 1024D);
        preSize = downSize.doubleValue();

        //计算剩余文件的大小（此处用到的变量的单位为字节）
        //TODO finishedSize暂未用到，可忽略
        double remainSize = httpFileContentLength - finishedSize.doubleValue() - downSize.doubleValue();

        /*
        * 估算剩余时间
        * 为何处以1024？因为remainSize的单位是字节，要与速度speed变量相除的话，需要将单位变为相同，即KB
        * */
        String remainTime = String.format("%.1f", (remainSize / 1024D) / speed);

        //剩余时间可能为无限大
        if("Infinity".equalsIgnoreCase(remainTime)){
            remainTime = "---";
        }

        /*
        * 计算已下载大小
        * TODO finishedSize暂未用到，可忽略
        * */
        String currentFileSize = String.format("%.2f", (downSize.doubleValue() - finishedSize.doubleValue()) / Constant.MB);

        String downInfo = String.format("已下载 %sMb/%sMb，速度 %sKb/s，剩余时间 %ss",
                                        currentFileSize, httpFileSize, speed, remainTime);

        System.out.print("\r");
        System.out.print(downInfo);
    }

}
