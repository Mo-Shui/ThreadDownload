package com.moshui.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类
 */

public class LogUtils {

    /**
     * info等级的日志
     * @param message 递过来的信息，如输出的信息有带参数，则使用%s表示
     * @param args 参数
     */
    public static void info(String message,Object... args){
        print(message,"info",args);
    }

    /**
     * error等级的日志
     * @param message 递过来的信息，如输出的信息有带参数，则使用%s表示
     * @param args 参数
     */
    public static void error(String message,Object... args){
        print(message,"error",args);
    }

    /**
     * 打印日志方法
     * @param message 传递过来的信息，如输出的信息有带参数，则使用%s表示
     * @param level 日志等级
     * @param args 参数
     */
    private static void print(String message,String level,Object... args){
        if (args != null && args.length > 0){
            message = String.format(message,args);
        }

        String threadName = Thread.currentThread().getName();
        String timeFormat = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));

        System.out.print("\r");
        System.out.print(timeFormat + "===" + threadName + "===" + level + ": " + message);
    }

}
