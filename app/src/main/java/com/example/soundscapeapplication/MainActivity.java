// MainActivity 클래스
package com.example.soundscapeapplication;

import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String x = null;
    private String y = null;
    private String musicId = "";
    private String musicTitle = "";
    private String artist = "";
    private String description = "";
    private GpsTracker gpsTracker;
    private WeatherData weatherData = new WeatherData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textview_address = findViewById(R.id.locationTextView);

        // 위치 받아오기 버튼 클릭 이벤트 처리
        Button ShowLocationButton = findViewById(R.id.buttonGetLocation);
        ShowLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i("위치", "찾는중..");
                Toast.makeText(MainActivity.this, "위치를 불러오는 중입니다! 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();

                gpsTracker = new GpsTracker(MainActivity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String formattedMessage = String.format(Locale.getDefault(), "현재 위치: 위도 %.2f 경도 %.2f", latitude, longitude);

                String address = getCurrentAddress(latitude, longitude);
                textview_address.setText(address);

                Toast.makeText(MainActivity.this, formattedMessage, Toast.LENGTH_LONG).show();

                String[] local = address.split(" ");
                String localName = local[2];
                Log.i("~동", localName);

                readExcel(localName, getAssets()); // 행정시 이름으로 격자값 구하기

                // 현재 위치 기반으로 날씨 가져오기
                try {
                    // 현재 날짜 포맷 설정
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    String date = dateFormat.format(new Date()); // 현재 날짜

                    // 현재 시간 포맷 설정
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH", Locale.getDefault());
                    String time = timeFormat.format(new Date()) + "00"; // 현재 시간

                    Log.i("날짜", date);
                    Log.i("시간", time);

                    // WeatherCallback을 구현한 익명 클래스를 전달하여 날씨 정보 받아오기
                    weatherData.lookUpWeather(date, time, x, y, new WeatherData.WeatherCallback() {
                        @Override
                        public void onWeatherDataReceived(String weather, String temperature) {
                            // 날씨 정보를 받아온 후에 처리할 로직
                            Log.i("현재 날씨", weather + " / 기온: " + temperature);

                            // 음악 추천 창에 출력할 description을 생성
                            generateDescription(
                                    ((EditText) findViewById(R.id.editTextName)).getText().toString(),
                                    ((EditText) findViewById(R.id.editTextAge)).getText().toString(),
                                    ((RadioButton) findViewById(R.id.radioButtonMale)).isChecked(),
                                    weather,
                                    temperature);
                            Log.i("설명", description);
                        }
                    });

                } catch (IOException e) {
                    Log.i("THREE_ERROR1", e.getMessage());
                }
            }
        });

        // Submit 버튼 클릭 이벤트 처리
        Button submitButton = findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트를 사용하여 새로운 화면으로 전환
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("description", description);
                intent.putExtra("musicId", musicId);
                intent.putExtra("musicTitle", musicTitle);
                intent.putExtra("artist", artist);

                startActivity(intent);
            }
        });
    }

    public void readExcel(String localName, AssetManager assetManager) {
        try {
            InputStream is = assetManager.open("local_name.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            if (workbook != null) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);   // 시트 불러오기

                if (sheet != null) {
                    int rowIndexStart = 1; // row 인덱스 시작
                    int rowTotal = sheet.getPhysicalNumberOfRows();

                    for (int rowIdx = rowIndexStart; rowIdx < rowTotal; rowIdx++) {
                        Row row = sheet.getRow(rowIdx);
                        if (row != null) {
                            Cell cell = row.getCell(4); // XX동
                            if (cell != null) {
                                String contents = cell.getStringCellValue();
                                if (contents.equals(localName)) {
                                    x = row.getCell(5).getStringCellValue(); // 격자값 x
                                    y = row.getCell(6).getStringCellValue(); // 격자값 y
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }

        Log.i("격자값", "x = " + x + "  y = " + y);
    }

    public String getCurrentAddress(double latitude, double longitude) {
        // 지오코더: GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            // 네트워크 문제
            Toast.makeText(this, "서비스 사용불가", Toast.LENGTH_LONG).show();
            return "서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    private void generateDescription(String userName, String userAge, boolean isMale, String weather, String temperature) {
        // 설명에 들어갈 정보
        String genderText = isMale ? "남성 " : "여성 ";
        String ageText = "나이 " + userAge + "세 ";
        judgeMusic(Integer.parseInt(temperature), weather);

        // 정보를 결합하여 설명 생성
        String weatherInfo = "현재 날씨는 " + weather + ", 기온은 " + temperature + "도 입니다.\n";
        String userInfo = "날씨에 맞게 " + ageText + genderText + userName + "님께 추천드리고 싶은 노래는 ";
        String musicInfo = artist + "의 " + musicTitle + "입니다.";

        description = weatherInfo + userInfo + musicInfo;
    }

    private void judgeMusic(int tempI, String weather) {
        if (weather.equals("흐림")) {
            if (tempI > 10) {
                musicId =  "no_one_told_me_why";
                musicTitle = "no one told me why";
                artist = "알레프";
            } else {
                musicId = "first_snow";
                musicTitle = "첫눈";
                artist = "EXO";
            }
        } else if (weather.equals("맑음")) {
            if (tempI > 20) {
                musicTitle =  "spicy";
                musicId =  "spicy";
                artist = "에스파";
            } else {
                musicId = "square";
                musicTitle = "square";
                artist = "백예린";
            }
        } else if (weather.equals("비")) {
            musicId = "rain";
            musicTitle = "비";
            artist = "폴킴";
        } else {
            if (tempI > 10) {
                musicId = "life_is_wet";
                musicTitle = "life is wet";
                artist = "카모";
            } else {
                musicId = "peaches";
                musicTitle = "peaches";
                artist = "저스틴 비버";
            }
        }
    }
}
