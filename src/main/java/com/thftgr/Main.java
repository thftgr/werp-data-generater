package com.thftgr;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static Random rnd = new Random();


    public static void main(String[] args){
        if (args[0].length() != 36) {
            System.out.println("data length not match");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        long startTime = System.currentTimeMillis();
        Proxy proxy;
        AtomicInteger dataCount = new AtomicInteger();

        File file = new File("setting/list.txt");
        while (true) {
//            System.gc();

            if (dataCount.get() != 0)
                System.out.println(((System.currentTimeMillis() - startTime) / dataCount.get()) + "ms/GB | Total data:" + dataCount + "GB\n");
            try {

                BufferedReader bufReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufReader.readLine()) != null) {
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(line.substring(0, line.indexOf(":")), Integer.parseInt(line.substring(line.indexOf(":") + 1))));
                    Proxy finalProxy = proxy;
                    new Thread(() -> {

                        try {
                            Thread.sleep(rnd.nextInt(2000));
//                            if (generator("7e2757b7-badc-4303-8cd0-2eefa9d78e3b", finalProxy)) {
                            if (generator(args[0], finalProxy)) {
                                dataCount.addAndGet(1);
                            }
                        } catch (Exception ignored) {
                        }
                    }).start();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.println(new Date().toString() + "============================================queue / 1min");
//            Thread.sleep(100);
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    static boolean generator(String referrer_id, Proxy proxy) throws IOException {
        if (proxy == null) return false;
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .proxy(proxy)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String installId = rand(11, false);

        JsonObject data = new JsonObject();
        data.addProperty("key", (rand(43, false) + "="));
        data.addProperty("install_id", installId);
        data.addProperty("fcm_token", installId + ":APA91b" + rand(134, false));
        data.addProperty("referrer", referrer_id);
        data.addProperty("warp_enabled", false);
        data.addProperty("tos", IOSDate());
        data.addProperty("type", "Android");
        data.addProperty("locale", "ko-KR");

        Request request = new Request.Builder()
                .url("https://api.cloudflareclient.com/v0a" + rand(3, true) + "/reg")
                .addHeader("'Content-Type'", " 'application/json; charset=UTF-8',")
                .addHeader("'Host'", " 'api.cloudflareclient.com',")
                .addHeader("'Connection'", " 'Keep-Alive',")
                .addHeader("'Accept-Encoding'", " 'gzip',")
                .addHeader("'User-Agent'", " 'okhttp/4.8.1'")
                .post(RequestBody.create(data.toString(),MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        assert response.body() != null;
        response.body().close();
        response.close();
        client.connectionPool().evictAll();
        client.dispatcher().cancelAll();
        client.dispatcher().executorService().shutdown();
        return response.code() == 200;
    }

    static String rand(int length, boolean intOnly) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int rIndex = rnd.nextInt(3);
            if (intOnly) rIndex = 2;
            switch (rIndex) {
                case 0:
                    temp.append((char) (rnd.nextInt(26) + 97));// a-z
                    break;
                case 1:
                    temp.append((char) (rnd.nextInt(26) + 65));// A-Z
                    break;
                case 2:
                    temp.append(rnd.nextInt(10));// 0-9
                    break;
            }
        }
        return temp.toString();
    }

    static String IOSDate() {
        String IOSTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        return IOSTime.substring(0, IOSTime.length() - 1) + "+09:00";
    }

}