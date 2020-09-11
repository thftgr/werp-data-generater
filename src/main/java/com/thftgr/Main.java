package com.thftgr;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static Random rnd = new Random();
    static AtomicInteger threadCount = new AtomicInteger();

    public static void main(String[] args) {
        String[] account;
        if (args.length == 0) {
            account = new String[1];
            account[0] = "7e2757b7-badc-4303-8cd0-2eefa9d78e3b";
        } else {
            account = args;
            if (args[0].length() != 36) account[0] = "7e2757b7-badc-4303-8cd0-2eefa9d78e3b";
        }

        long startTime = System.currentTimeMillis();

        AtomicInteger dataCount = new AtomicInteger();
        HashSet<Proxy> proxyHashSet = new Main().readProxy();
        HashSet<Thread> threadHashSet = new HashSet<>();

        new Thread(() -> {
            while (true) {
                new Main().delay(5000);
                if (dataCount.get() != 0) {
                    System.out.println("Alive Thread : " + threadCount);
                    System.out.println(((System.currentTimeMillis() - startTime) / dataCount.get()) + "ms/GB | Total data:" + dataCount + "GB");
                }
            }
        }).start();


        while (true) {
            threadHashSet.clear();
            for (Proxy proxy : proxyHashSet) {
                threadHashSet.add(new Thread(() -> {
                    threadCount.addAndGet(1);
                    if (new Main().generator(account[rnd.nextInt(account.length)], proxy)) dataCount.addAndGet(1);
                    threadCount.addAndGet(-1);
                }));

            }

            long runtime = System.currentTimeMillis();

            for (Thread thread : threadHashSet) {
                thread.start();
                new Main().delay(1);
            }

            while (threadCount.get() > 0 | (System.currentTimeMillis() - runtime) < 60000) {
                new Main().delay(1000);
            }
        }
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

    void delay(long i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HashSet<Proxy> readProxy() {
        HashSet<Proxy> proxyHashSet = new HashSet<>();
        try {
            File file = new File("setting/list.txt");
            BufferedReader bufReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufReader.readLine()) != null) {
                String ip = line.substring(0, line.indexOf(":"));
                int port = Integer.parseInt(line.substring(line.indexOf(":") + 1));
                proxyHashSet.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
            }
        } catch (Exception e) {
            return null;
        }
        return proxyHashSet;
    }

    String IOSDate() {
        String IOSTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        return IOSTime.substring(0, IOSTime.length() - 1) + "+09:00";
    }

    boolean generator(String referrer_id, Proxy proxy) {
        if (proxy == null) return false;
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .proxy(proxy)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
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
                .post(RequestBody.create(data.toString(), MediaType.parse("application/json")))
                .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();
            if (response.body() != null) {
                String responseBody = response.body().string();
                response.body().close();
                return responseBody.contains(referrer_id) && response.code() == 200;
            }

//            response.close();
            client.connectionPool().evictAll();
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdown();
        } catch (Exception e) {

            return false;
        }
        return false;
    }


}