package com.example.soundscapeapplication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class WeatherData {

    private String weather = "", temperature = "";

    public interface WeatherCallback {
        void onWeatherDataReceived(String weather, String temperature);
    }

    public void getWeatherData(String baseDate, String time, String nx, String ny, WeatherCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = lookUpWeather(baseDate, time, nx, ny, callback);
                    notifyCallback(callback, weather, temperature);
                } catch (IOException e) {
                    Log.e("WeatherData", "Error: " + e.getMessage());
                    notifyCallback(callback, "날씨 정보를 가져오지 못했습니다.","");
                }
            }
        }).start();
    }

    private void notifyCallback(WeatherCallback callback, String weather, String temperature) {
        // UI 업데이트를 위해 핸들러를 사용하여 메인 스레드에서 콜백 호출
        new Handler() {
            public void post(Runnable runnable) {
            }

            @Override
            public void publish(LogRecord logRecord) {

            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        }.post(new Runnable() {
            @Override
            public void run() {
                callback.onWeatherDataReceived(weather, temperature);
            }
        });
    }


    public String lookUpWeather(String baseDate, String time, String nx, String ny, WeatherCallback callback) throws UnsupportedEncodingException {
        Log.i("날씨", "찾는중..");

        String baseTime = timeChange(time);
        String type = "JSON";
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
        String serviceKey = "%2BAENO2kECMtl7G%2FjWRO1rPr8C4pfiRlhC2%2Bx9jr%2BLF5MS6M4GE1RAYEiC0zN6XKUsicTH71Un9eyzPOFUDByQw%3D%3D";
        String pageNo = "3";

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", "application/json");

                    BufferedReader rd;
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    rd.close();
                    conn.disconnect();
                    String result = sb.toString();

                    // response 키를 가지고 데이터를 파싱
                    JSONObject jsonObj_1 = new JSONObject(result);
                    String response = jsonObj_1.getString("response");

                    // response 로 부터 body 찾기
                    JSONObject jsonObj_2 = new JSONObject(response);
                    String body = jsonObj_2.getString("body");

                    // body 로 부터 items 찾기
                    JSONObject jsonObj_3 = new JSONObject(body);
                    String items = jsonObj_3.getString("items");
                    Log.i("ITEMS", items);

                    // items로 부터 itemlist 를 받기
                    JSONObject jsonObj_4 = new JSONObject(items);
                    JSONArray jsonArray = jsonObj_4.getJSONArray("item");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObj_4 = jsonArray.getJSONObject(i);
                        String fcstValue = jsonObj_4.getString("fcstValue");
                        String category = jsonObj_4.getString("category");

                        if (category.equals("SKY")) {
                            weather = "";
                            if (fcstValue.equals("1")) {
                                weather += "맑음";
                            } else if (fcstValue.equals("2")) {
                                weather += "비";
                            } else if (fcstValue.equals("3")) {
                                weather += "구름 많음";
                            } else if (fcstValue.equals("4")) {
                                weather += "흐림";
                            }
                        }

                        if (category.equals("T3H") || category.equals("T1H")) {
                            temperature = fcstValue;
                        }

                        Log.i("지금 날씨는", weather + temperature);
                    }

                    Log.i("WeatherData", "날씨 정보: " + weather + temperature);

                    // 날씨 정보를 받은 후에 콜백 메소드 호출
                    callback.onWeatherDataReceived(weather, temperature);


                } catch (IOException | JSONException e) {
                    Log.e("WeatherData", "Error: " + e.getMessage());

                    // 에러가 발생한 경우에도 콜백 메소드 호출
                    callback.onWeatherDataReceived("날씨 정보를 가져오지 못했습니다.", "");
                }
            }

        }).start();
        return weather + temperature;
    }

    private String timeChange(String time) {
        switch (time) {
            case "0200":
            case "0300":
            case "0400":
                return "0200";
            case "0500":
            case "0600":
            case "0700":
                return "0500";
            case "0800":
            case "0900":
            case "1000":
                return "0800";
            case "1100":
            case "1200":
            case "1300":
                return "1100";
            case "1400":
            case "1500":
            case "1600":
                return "1400";
            case "1700":
            case "1800":
            case "1900":
                return "1700";
            case "2000":
            case "2100":
            case "2200":
                return "2000";
            default:
                return "2300";
        }
    }
}
