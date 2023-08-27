package com.moshui.core;

import com.moshui.constant.Constant;
import com.moshui.util.FileUtils;
import com.moshui.util.HttpUtils;
import com.moshui.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * 下载器
 */

public class DownloaderWithDownloaderTask {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    //线程池对象
    public ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM,
                                                                        Constant.THREAD_NUM,
                                                                        0,
                                                                        TimeUnit.SECONDS,
                                                                        new ArrayBlockingQueue<>(Constant.THREAD_NUM));

    //用于计数、阻塞
    private CountDownLatch countDownLatch = new CountDownLatch(Constant.THREAD_NUM);

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

            //切分任务
            ArrayList<Future> futureArrayList = new ArrayList<>();
            split(url,futureArrayList);

            //用于阻塞，等到分块全部下载完成再往下走
//            futureArrayList.forEach(future -> {
//                try {
//                    future.get();
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
//                }
//            });
            //使用CountDownLatch类来代替上述代码
            countDownLatch.await();

            //到这里说明分块下载完成，可以合并和删除临时文件了
            if(merge(filePath)){
                clearTemp(filePath);
            }
        } catch (IOException e) {
            LogUtils.error("获取HttpURLConnection失败%s",url);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.print("\r");
            System.out.print("下载完成");

            httpURLConnection.disconnect();

            scheduledExecutorService.shutdownNow();

            threadPoolExecutor.shutdown();
        }
    }

    /**
     * 文件切分
     * @param url
     * @param futureArrayList
     */
    public void split(String url, ArrayList<Future> futureArrayList){
        try {
            //获取下载文件的大小
            long fileContentLength = HttpUtils.getHttpFileContentLength(url);

            //计算切分后的文件大小
            long size = fileContentLength / Constant.THREAD_NUM;

            //计算分块个数
            for (int x = 0;x < Constant.THREAD_NUM;x++){
                //计算下载起始位置
                long startPos = x * size;

                //计算下载结束位置
                long endPos;
                if (x == Constant.THREAD_NUM - 1){
                    //说明是最后一块分块，为0是因为在HttpUtils类中对最后一块有处理
                    endPos = 0;
                }else{
                    endPos = startPos + size;
                }

                //如果不是第一块，起始位置要加一
                if (startPos != 0){
                    startPos++;
                }

                //创建任务
                DownloaderTask downloaderTask = new DownloaderTask(url, startPos, endPos,x,countDownLatch);

                /*
                * 将任务提交到线程池中
                * submit方法源码中仍然调用了execute方法，只不过包装了一下
                * */
                Future<Boolean> booleanFuture = threadPoolExecutor.submit(downloaderTask);

                futureArrayList.add(booleanFuture);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件合并
     * @param fileName
     * @return
     */
    public boolean merge(String fileName){
        System.out.println("开始合并文件" + fileName);

        byte[] buffer = new byte[Constant.BYTE_SIZE];
        int length = -1;
        try(
                RandomAccessFile randomAccessFile = new RandomAccessFile(fileName,"rw");

                ){
            for (int x = 0;x < Constant.THREAD_NUM;x++){
                try(
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".temp" + x));
                        ){
                    while((length = bis.read(buffer)) != -1){
                        randomAccessFile.write(buffer,0,length);
                    }
                }
            }

            System.out.println("文件合并完毕" + fileName);
        }catch (Exception e){
            e.printStackTrace();

            return false;
        }

        return true;
    }

    /**
     * 清除临时文件
     * @param fileName
     * @return
     */
    public boolean clearTemp(String fileName){
        for(int x = 0;x < Constant.THREAD_NUM;x++){
            File file = new File(fileName + ".temp" + x);
            file.delete();
        }

        return true;
    }

}
