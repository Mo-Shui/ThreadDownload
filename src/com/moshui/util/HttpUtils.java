package com.moshui.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Http协议的工具类
 */

public class HttpUtils {

    /**
     * 获取下载文件的大小
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static long getHttpFileContentLength(String url) throws IOException {
        HttpURLConnection httpURLConnection = null;
        int contentLength = 0;
        try {
            httpURLConnection = getHttpURLConnection(url);
            contentLength = httpURLConnection.getContentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }

        return contentLength;
    }

    /**
     * 分块下载
     *
     * @param url      要下载的文件的url
     * @param startPos 当前下载的数据块的起始位置
     * @param endPos   当前下载的数据块的结束位置
     * @return 返回HttpURLConnection对象
     */
    public static HttpURLConnection getHttpURLConnection(String url, long startPos, long endPos) throws IOException {
        HttpURLConnection httpURLConnection = getHttpURLConnection(url);
        System.out.println("下载的区间是：" + startPos + "-" + endPos);

        /*
         * 多个分块中，第一块和最后一块比较特殊
         * 第一块不需要知道起始位置，因为肯定是0
         * 最后一块不需要知道结束位置，因为肯定是整个文件的大小
         * */
        if (endPos != 0) {
            //结束位置不为0，表示是中间或第一块分块
            httpURLConnection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
        } else {
            //结束位置为0，表示是最后一块
            httpURLConnection.setRequestProperty("Range", "bytes=" + startPos + "-");
        }

        return httpURLConnection;
    }

    /**
     * 获取HttpURLConnection对象
     *
     * @param url 要下载的文件的url
     * @return 返回HttpURLConnection对象
     * @throws IOException
     */
    public static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        URL httpURL = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpURL.openConnection();

        //模拟浏览器，向服务器发送标识
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1");

        return httpURLConnection;
    }

    /**
     * 返回url对应的文件名（包括后缀）
     *
     * @param url 要下载的文件的url
     * @return 返回url对应的文件名（包括后缀）
     */
    public static String getHttpFileName(String url) {
        int lastIndexOf = url.lastIndexOf("/");
        return url.substring(lastIndexOf + 1);
    }

}
