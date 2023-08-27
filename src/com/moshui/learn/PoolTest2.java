package com.moshui.learn;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池的创建
 */

public class PoolTest2 {

    public static void main(String[] args) throws InterruptedException {
        //创建线程池对象
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,   //核心线程数为2
                3,  //总线程数为3，非核心线程为总线程数 - 核心线程数，这里非核心线程数为1
                1, TimeUnit.SECONDS,    //设定存活时间，1表示具体的数量，TimeUnit.MINUTES表示这个数量的单位，这里表示1分钟
                new ArrayBlockingQueue<>(2) //工作队列，容量为2，表示最多只能有两个线程同时工作
        );

        //创建任务
        Runnable r = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName());
        };

        //将任务提交给线程池中的线程工厂（ThreadFactory）执行
        //测试用的循环
        for (int x = 0; x < 5; x++)
            threadPoolExecutor.execute(r);

        System.out.println(threadPoolExecutor);

        TimeUnit.SECONDS.sleep(5);

        System.out.println(threadPoolExecutor);
    }

}
