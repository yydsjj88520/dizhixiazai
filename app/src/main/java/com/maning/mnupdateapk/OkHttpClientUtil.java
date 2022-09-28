package com.maning.mnupdateapk;

import com.alibaba.fastjson.JSON;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OkHttpClientUtil {
    private Proxy proxy;
    private String proxyUrl;
    private int proxyPort;
    private String brcpSessionTicket;
    private List<Interceptor> interceptors = new ArrayList<>();
    public static String initUrl;
    public static int initPort;
    SSLSocketFactory sslSocketFactory;

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public boolean isOnlyUseHttp2() {
        return onlyUseHttp2;
    }

    public void setOnlyUseHttp2(boolean onlyUseHttp2) {
        this.onlyUseHttp2 = onlyUseHttp2;
    }

    private boolean onlyUseHttp2;


    public void addInerceptors(Interceptor i) {
        interceptors.add(i);
    }


    public String get_fmdata() {
        return _fmdata;
    }

    public void set_fmdata(String _fmdata) {
        this._fmdata = _fmdata;
    }

    private String _fmdata;

    public String getPhpSession() {
        return phpSession;
    }

    public void setPhpSession(String phpSession) {
        this.phpSession = phpSession;
    }

    public String getCart_goods_num() {
        return cart_goods_num;
    }

    public void setCart_goods_num(String cart_goods_num) {
        this.cart_goods_num = cart_goods_num;
    }

    private String phpSession;
    private String cart_goods_num;

    public String getBrcpSessionTicket() {
        return brcpSessionTicket;
    }

    public void setBrcpSessionTicket(String brcpSessionTicket) {
        this.brcpSessionTicket = brcpSessionTicket;
    }


    public OkHttpClientUtil() {
        init();

    }

    private void init() {
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public Response executeOnce(Request.Builder requestBuilder) {
        OkHttpClient.Builder okHttpClientBuilder = getOkHttpClientBuilder(OkHttpClientContainer.okHttpClient);

        Request build1 = requestBuilder.build();
        try {
            OkHttpClient build = okHttpClientBuilder.build();
            Response execute = build.newCall(build1).execute();
            return execute;

        } catch (IOException e) {
            System.out.println("诶哟，执行请求时报错了,需要再次执行把数据发给服务器,请求参数为" + build1.toString());
            e.printStackTrace();
        }
        return null;
    }

    public Response executeOnceWithoutRedirect(Request.Builder requestBuilder) {
        OkHttpClient.Builder okHttpClientBuilder = getOkHttpClientBuilder(OkHttpClientContainer.okHttpClient);
        okHttpClientBuilder.followSslRedirects(false);
        okHttpClientBuilder.followRedirects(false);
        Request build1 = requestBuilder.build();
        try {
            OkHttpClient build = okHttpClientBuilder.build();
            Response execute = build.newCall(build1).execute();
            return execute;

        } catch (IOException e) {
            System.out.println("诶哟，执行请求时报错了,需要再次执行把数据发给服务器,请求参数为" + build1.toString());
            e.printStackTrace();
        }
        return null;
    }

    public Response execute(Request.Builder requestBuilder) {
        OkHttpClient.Builder okHttpClientBuilder = getOkHttpClientBuilder(OkHttpClientContainer.okHttpClient);

        Request build1 = requestBuilder.build();
        try {
            OkHttpClient build = okHttpClientBuilder.build();
            Response execute = build.newCall(build1).execute();
            if (execute.code() != 200) {
                System.out.println("发送请求时服务器返回了错误" + build1 + ",response code=" + execute.code() + ",response data=" + execute.body().string());
                init();
                return execute(requestBuilder);
            } else {
                return execute;
            }

        } catch (IOException e) {
            System.out.println("诶哟，执行请求时报错了,需要再次执行把数据发给服务器,请求参数为" + build1.toString());
            init();
            e.printStackTrace();
        }
        return execute(requestBuilder);
    }

    public String executePostJsonWithStringResult(String url, Object object) {
        Request.Builder builder = new Request.Builder();
        try {

            builder.url(url);
            if (object instanceof String) {
                builder.post(RequestBody.create(MediaType.parse("application/json"), (String) object));
            } else {
                builder.post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(object)));
            }
            return execute(builder).body().string();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("executeWithStringResult 奔溃了");
        }
        return executeWithStringResult(builder);
    }

    public String executePutJsonWithStringResult(String url, Object object) {
        Request.Builder builder = new Request.Builder();
        try {

            builder.url(url);
            if (object instanceof String) {
                builder.put(RequestBody.create(MediaType.parse("application/json"), (String) object));
            } else {
                builder.put(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(object)));
            }
            return execute(builder).body().string();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("executeWithStringResult 奔溃了");
        }
        return executeWithStringResult(builder);
    }

    public String executePostFormWithStringResult(String url, String data) {
        Request.Builder builder = new Request.Builder();
        try {

            builder.url(url);
            builder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), data));
            return execute(builder).body().string();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("executeWithStringResult 奔溃了");
        }
        return executeWithStringResult(builder);
    }

    public String executePostFormWithStringResult(String userAgent, String url, String data) {
        Request.Builder builder = new Request.Builder();
        try {

            builder.url(url);
            builder.addHeader("User-Agent", userAgent);
            builder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), data));
            return execute(builder).body().string();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("executeWithStringResult 奔溃了");
        }
        return executeWithStringResult(builder);
    }

    public String executeGetWithStringResult(String url) {
        Request.Builder builder = new Request.Builder();
        try {

            builder.url(url);
            return execute(builder).body().string();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("executeWithStringResult 奔溃了");
        }
        return executeWithStringResult(builder);
    }

    public String executeWithStringResult(Request.Builder request) {
        try {
            return execute(request).body().string();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("executeWithStringResult 奔溃了");
        }
        return executeWithStringResult(request);
    }

    public OkHttpClient.Builder getOkHttpClientBuilder(OkHttpClient client) {
        OkHttpClient.Builder builder = client.newBuilder();
        builder.retryOnConnectionFailure(true);
        builder.connectTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(10 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(10 * 1000, TimeUnit.MILLISECONDS);
        if (!interceptors.isEmpty()) {
            for (int i = 0; i < interceptors.size(); i++) {
                Interceptor interceptor = interceptors.get(i);
                builder.addInterceptor(interceptor);
            }

        }
        SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
        if (this.sslSocketFactory != null) {
            sslSocketFactory = this.sslSocketFactory;
        }
        builder.sslSocketFactory(sslSocketFactory, new TrustAllManager());
        builder.hostnameVerifier(new TrustAllHostnameVerifier());
        if (isOnlyUseHttp2()) {
            builder.protocols(Collections.unmodifiableList(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1)));
        }

        return builder;
    }

    public static SSLSocketFactory createSSLSocketFactory() {

        SSLSocketFactory sSLSocketFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return sSLSocketFactory;
    }


    public static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static class OkHttpClientContainer {
        public static OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    }

}
