package com.thftgr;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Main {

    static JsonArray proxyList = new JsonArray();
    static long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        if (args[0].length() != 36) {
            System.out.println("data length not match");
            return;
        }


            try {
                proxyList = (JsonArray) JsonParser.parseReader(new Gson().newJsonReader(new FileReader("setting/proxy_list.json")));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


        int dataCount = 0;
        long timetmp = 0;
        long delay = 0;
        while (true) {
            delay = System.currentTimeMillis();
            if (proxyList.size() == 0) {
                System.out.println("proxy list end");
                return;
            }
            for (int i = 0; i < proxyList.size(); i++) {


                timetmp = System.currentTimeMillis();
                System.out.println("=====================================");

                Proxy proxy = new Main().buildProxy(proxyList.get(i).getAsString());
                System.out.println(proxy.toString());


                try {
//                    if (new Main().generator("cf62e033-9e89-49c6-9830-5e61adc29ac5",proxy)) {
//                    if (new Main().generator("7e2757b7-badc-4303-8cd0-2eefa9d78e3b", proxy)) {
                    if (new Main().generator(args[0], proxy)) {
                        dataCount += 1;
                        System.out.println("generated data : " + dataCount + "GB");
                        System.out.println((System.currentTimeMillis() - timetmp) + "ms\n");
                        System.out.println(((System.currentTimeMillis() - startTime) / dataCount / 1000) + "s/GB\n");
                    } else {
                        System.out.println("generate fail. | data : " + dataCount + "GB");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println();
                    System.out.println("Exception generate fail. | data : " + dataCount + "GB");
                    proxyList.remove(i); //램에서만 삭제
                    if(args.length == 2){
                        if (args[1].equals("-d")) {
                            new Main().settingSave();
                        }
                    }


                }

            }
            // 전체 실행 후 시간이 20초 이하면 남운시간만큼 대기
            if (System.currentTimeMillis() - delay < 20000) {
                try {
                    Thread.sleep((System.currentTimeMillis() - delay));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String rand(int length, boolean intOnly) {
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            int rIndex = rnd.nextInt(3);
            if (intOnly) rIndex = 2;
            switch (rIndex) {
                case 0:
                    // a-z
                    temp.append((char) (rnd.nextInt(26) + 97));
                    break;
                case 1:
                    // A-Z
                    temp.append((char) (rnd.nextInt(26) + 65));
                    break;
                case 2:
                    // 0-9
                    temp.append((rnd.nextInt(10)));
                    break;
            }
        }
        return temp.toString();

    }

    String iosdate() {
        String iostime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        return iostime.substring(0, iostime.length() - 1) + "+09:00";
    }

    boolean generator(String referrer_id, Proxy proxy) throws IOException {
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .proxy(proxy)
                .build();


        String installId = new Main().rand(11, false);

        JsonObject data = new JsonObject();
        data.addProperty("key", (new Main().rand(43, false) + "="));
        data.addProperty("install_id", installId);
        data.addProperty("fcm_token", installId + ":APA91b" + new Main().rand(134, false));
        data.addProperty("referrer", referrer_id);
        data.addProperty("warp_enabled", false);
        data.addProperty("tos", new Main().iosdate());
        data.addProperty("type", "Android");
        data.addProperty("locale", "ko-KR");

        Request request = new Request.Builder()
                .url("https://api.cloudflareclient.com/v0a" + new Main().rand(3, true) + "/reg")
                .addHeader("'Content-Type'", " 'application/json; charset=UTF-8',")
                .addHeader("'Host'", " 'api.cloudflareclient.com',")
                .addHeader("'Connection'", " 'Keep-Alive',")
                .addHeader("'Accept-Encoding'", " 'gzip',")
                .addHeader("'User-Agent'", " 'okhttp/3.12.1'")
                .post(RequestBody.create(MediaType.parse("application/json"), data.toString()))

                .build();

        Response response = client.newCall(request).execute();
        JsonObject resp = (JsonObject) JsonParser.parseString(response.body().string());

        client.connectionPool().evictAll();
        System.out.println("status code : " + response.code());
        return !resp.get("referrer").isJsonNull();
    }


    Proxy buildProxy(String ip_port) {
        String ip = ip_port.substring(0, ip_port.indexOf(":"));
        int port = Integer.parseInt(ip_port.substring(ip_port.indexOf(":") + 1));
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
    }

    void settingSave() {

        try {
            FileWriter fw = new FileWriter("setting/proxy_list.json", false);
            fw.write(Main.proxyList.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}