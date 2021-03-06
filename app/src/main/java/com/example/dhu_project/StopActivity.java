package com.example.dhu_project;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class StopActivity extends AppCompatActivity implements View.OnClickListener {
    private VideoView vv;
    private String str_videoUrl = "";


    private TextView tv_bpm;

    private Button btn_end;
    private Button btn_watchstart;
    private Button btn_watchpause;
    private Button btn_watchreset;

    private long stopTime = 0;
    private Chronometer chronometer;
    private static Handler bpm_handler;

    RelativeLayout relativeLayout;
    LinearLayout linearLayout;

    //파이어 베이스 데이터베이스 사용
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("video/stopwatch");
    DatabaseReference clear = database.getReference();
    Map<String, Object> task = new HashMap<String, Object>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop);
        // TextView 연결
        tv_bpm = findViewById(R.id.bpm);

        // 레이아웃 연결
        relativeLayout = findViewById(R.id.relativelayout);
        linearLayout = findViewById(R.id.end_BoxLayout);

        // 버튼 연결
        btn_end = findViewById(R.id.btn_end);
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        btn_watchstart = (Button)findViewById(R.id.btn_start);
        btn_watchpause = (Button)findViewById(R.id.btn_pause);
        btn_watchreset = (Button)findViewById(R.id.btn_reset);

        //파이어 베이스 동영상 URl받아오기
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                str_videoUrl = value;
                vv.setVideoURI(Uri.parse(str_videoUrl));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // BPM 랜덤값 띄우기(1초당)
        bpm_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                double random = Math.random();
                int random_value = (int)(random * 100) + 60;
                //random_value = (int)(random * 150) + 60;
                tv_bpm.setText(String.valueOf(random_value));
            }
        };

        class BPM_Runnable implements Runnable{
            @Override
            public void run(){
                while(true){
                    try{
                        Thread.sleep(2000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    bpm_handler.sendEmptyMessage(0);
                }
            }
        }

        BPM_Runnable bpm = new BPM_Runnable();
        Thread bpm_thread = new Thread(bpm);
        bpm_thread.start();

        // 스톱워치 구현
        // 시작버튼 클릭시
        btn_watchstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime() + stopTime);
                chronometer.start();
                btn_watchstart.setVisibility(View.GONE);
                btn_watchpause.setVisibility(View.VISIBLE);

                vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        vv.start();
                        mp.setLooping(true);
                    }

                });

                if (!vv.isPlaying()) vv.start();

            }
        });

        // 일시정지 버튼 클릭 시
        btn_watchpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTime = chronometer.getBase() - SystemClock.elapsedRealtime();
                chronometer.stop();
                btn_watchstart.setVisibility(View.VISIBLE);
                btn_watchpause.setVisibility(View.GONE);

                if (vv.isPlaying()) vv.pause();

            }
        });

        // 초기화 버튼 클릭 시
        btn_watchreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 종료 시 시간 데이터베이스 저장
                long current = SystemClock.elapsedRealtime() - chronometer.getBase();
                int time = (int) (current / 1000);

                clear.child("timer/stopwatch").push().setValue(time);

                // 타이머 정지
                chronometer.setBase(SystemClock.elapsedRealtime());
                stopTime = 0;
                chronometer.stop();
                btn_watchstart.setVisibility(View.VISIBLE);
                btn_watchpause.setVisibility(View.GONE);

                // 끝내기 화면 띄우기, 영상 정지지
                linearLayout.setVisibility(View.VISIBLE);
                btn_end.setOnClickListener(StopActivity.this);
                vv.pause();
                tv_bpm.setText("0");
            }
        });


        // VideoView 연결
        vv = findViewById(R.id.videoVideo_stop);
        //str_videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"; // 테스트영상
        //vv.setVideoURI(Uri.parse(str_videoUrl));



        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vv.start();
            }
        });

        // 비디오뷰 클릭시 이벤트
        vv.setOnClickListener(this);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_end:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(vv != null && vv.isPlaying()) vv.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(vv != null) vv.stopPlayback();

    }

}
