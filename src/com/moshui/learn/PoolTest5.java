package com.moshui.learn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * jdk提供的便捷创建线程池的方式
 * 阿里巴巴的开发文档中不建议使用jdk提供的方式，建议使用原生方式，因为可以对这些实现不熟悉从而导致问题的出现
 */

public class PoolTest5 {

    public static void main(String[] args) {
        //只有核心线程
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

        //只有非核心线程
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        //只创建一个核心线程
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }

}
