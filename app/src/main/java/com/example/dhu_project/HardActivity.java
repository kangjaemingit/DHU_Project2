package com.example.dhu_project;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
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

public class HardActivity extends AppCompatActivity implements View.OnClickListener {
    private VideoView vv;
    private String str_videoUrl = "";
            //"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    private TextView tv_time_mm;
    private TextView tv_time_ss;
    private TextView tv_time_pp;
    private TextView tv_bpm;

    private Button btn_end;

    CountDownTimer countDownTimer;
    private static Handler bpm_handler;

    RelativeLayout relativeLayout;
    LinearLayout linearLayout;

    //파이어 베이스 데이터베이스 사용
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("video/hard");
    DatabaseReference clear = database.getReference();
    Map<String, Object> task = new HashMap<String, Object>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hard);
        // TextView 연결
        tv_time_mm = findViewById(R.id.time_mm);
        tv_time_ss = findViewById(R.id.time_ss);
        tv_time_pp = findViewById(R.id.time_pp);
        tv_bpm = findViewById(R.id.bpm);

        // 레이아웃 연결
        relativeLayout = findViewById(R.id.relativelayout);
        linearLayout = findViewById(R.id.end_BoxLayout);

        // 버튼 연결
        btn_end = findViewById(R.id.btn_end);

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
                int random_value = (int)(random * 40) + 80;
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


        // CountDown
        countDownTimer = new CountDownTimer(461*1000,10) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 시간을 분, 초, 밀리초 단위로 보여주게 하기 0.01 초 단위
                tv_time_mm.setText(""+millisUntilFinished/(60*1000));
                millisUntilFinished = millisUntilFinished%(60*1000);
                tv_time_ss.setText(":"+millisUntilFinished/(1000));
                millisUntilFinished = millisUntilFinished%(1000);
                tv_time_pp.setText("."+millisUntilFinished/(10));
            }

            @Override
            public void onFinish() {
                // 끝났다는 메시지 띄우기
                // 숨겨놓은 리니어 레이아웃 보이기
                linearLayout.setVisibility(View.VISIBLE);
                btn_end.setOnClickListener(HardActivity.this);
                vv.pause();
                tv_bpm.setText("0");
            }
        };


        // VideoView 연결
        vv = findViewById(R.id.videoVideo_hard);
        //vv.setVideoURI(Uri.parse(str_videoUrl));

        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                vv.start();
                countDownTimer.start();
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
        //
        switch (v.getId()){
            case R.id.btn_end:
                clear.child("timer/hard/clear").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int value = snapshot.getValue(int.class);
                        int int_value = value+1;
                        task.put("timer/hard/clear", int_value);
                        clear.updateChildren(task);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
        }

        if (vv.isPlaying()) vv.pause();
        else {
            vv.start();
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
