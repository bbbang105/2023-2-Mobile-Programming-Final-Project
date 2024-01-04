package com.example.soundscapeapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// ResultActivity 클래스
public class ResultActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        String description = intent.getStringExtra("description");
        String musicId = intent.getStringExtra("musicId");
        String musicTitle = intent.getStringExtra("musicTitle");
        String artist = intent.getStringExtra("artist");

        // 가져온 데이터를 화면에 표시
        TextView textViewDescription = findViewById(R.id.textViewDescription);
        textViewDescription.setText(description);

        TextView textViewSong = findViewById(R.id.textViewSong);
        textViewSong.setText(musicTitle);

        TextView textViewArtist = findViewById(R.id.textViewArtist);
        textViewArtist.setText(artist);

        ImageView imageViewAlbum = findViewById(R.id.imageViewAlbum);
        // 앨범 사진 리소스의 ID를 가져오기
        int albumResourceId = getResources().getIdentifier("album_" + musicId, "drawable", getPackageName());
        // 가져온 ID를 사용하여 이미지 설정
        imageViewAlbum.setImageResource(albumResourceId);
        // 노래를 백그라운드에서 실행
        playBackgroundMusic(musicId);

        // buttonBackToMain 버튼에 클릭 이벤트 추가
        Button buttonBackToMain = findViewById(R.id.buttonBackToMain);
        buttonBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 메인 액티비티로 돌아가는 인텐트 생성
                Intent backToMainIntent = new Intent(ResultActivity.this, MainActivity.class);
                // 메인 액티비티로 이동
                startActivity(backToMainIntent);

                // MediaPlayer 해제
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });
    }

    private void playBackgroundMusic(String musicId) {
        // 노래 리소스 ID 가져오기
        int musicResourceId = getResources().getIdentifier(musicId, "raw", getPackageName());

        // MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(this, musicResourceId);

        // 노래 재생
        mediaPlayer.start();
    }
}
