package com.moshui.learn;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池的创建
 */

public class PoolTest3 {

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = null;
        try {
            //创建线程池对象
            threadPoolExecutor = new ThreadPoolExecutor(2,   //核心线程数为2
                    3,  //总线程数为3，非核心线程为总线程数 - 核心线程数，这里非核心线程数为1
                    1, TimeUnit.MINUTES,    //设定存活时间，1表示具体的数量，TimeUnit.MINUTES表示这个数量的单位，这里表示1分钟
                    new ArrayBlockingQueue<>(2) //工作队列，容量为2，表示最多只能有两个线程同时工作
            );

            //创建任务
            Runnable r = () -> System.out.println(Thread.currentThread().getName());

            //将任务提交给线程池中的线程工厂（ThreadFactory）执行
            //测试用的循环
            for (int x = 0; x < 5; x++)
                threadPoolExecutor.execute(r);
        } finally {
            /*
             * 关闭线程池
             * 有两种状态：
             * 1、shutdown：温和，不会接收新的任务，但会等待工作队列的任务执行结束后再关闭
             * 2、stop：暴力，不会接收新的任务，并且会将工作队列的任务全部取消后关闭
             * */
            if (threadPoolExecutor != null) {
                //关闭线程池，状态改为shutdown
                threadPoolExecutor.shutdown();

                /*
                    等待1分钟后，如果线程池还没有关闭，则将状态改为stop
                    threadPoolExecutor.awaitTermination方法：如果线程池已关闭，返回true，否则返回false
                 */
                if (!threadPoolExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                    threadPoolExecutor.shutdownNow();
                }
            }
        }
    }

}
