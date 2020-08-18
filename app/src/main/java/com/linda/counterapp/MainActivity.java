package com.linda.counterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main_Activity";

    private TextView tvCount;
    private Button btnStart, btnStop;
    private ICounterService binder;
    private boolean running =true;
    private Handler handler = new Handler();
    private int startCount = 0;

    ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) { //이 바인더는 서비스에서부터 콜벡온 거임.
            binder = ICounterService.Stub.asInterface(service);
           /* try{
                Log.d(TAG, "onClick: binder count : "+ binder.getCount());
            }catch (RemoteException e){
                e.printStackTrace();
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    }; // end of connection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initObject();
        initListener();

    }

    private void initObject(){
        tvCount = findViewById(R.id.tv_count);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
    }

    //BIND_AUTO_CREATE : Component와 연결되어있는 동안 비정상적으로 종료시 자동으로 다시 시작.
    //BIND_DEBUG_UNBIND : 비정상적으로 연결이 끊어지명 로그를 남긴다.(디버깅용)
    //BIND_NOT_FOREGROUND : 백그라운드로만 동작한다. 만약 Activity에서 생성한 경우.
    private void initListener(){
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CounterService.class);
                intent.putExtra("startCount", startCount);
                bindService(intent, connection, BIND_AUTO_CREATE); //얘가 실행이 언제 다 되었는지
                //intent로 넘어온 count값으로 for문을 초기화 시키기.


                //bind를 여기서 바로 콜백받으면 null이니까 쉬어야한다.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        running = true;
                        while(running){
                            if(binder != null){
                                //그럼 언제 binder가 0이 아니냐면 unbind()호출되면.
                                //새로운 스레드에서 ui접근할 수 없으니 3가지 방법 중에 간단한거는 handler 필요하다.
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            tvCount.setText(binder.getCount()+"");
                                            Log.d(TAG, "run: 그림그려짐");
                                            if(binder.getCount() == 20){
                                                running = false;
                                            }
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                try {
                                    Thread.sleep(500); //그림 두번 그림.
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } //end of while
                    }
                }).start();
              //  Log.d(TAG, "onClick: "+binder.getCount());
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startCount = binder.getCount();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                unbindService(connection);
                running = false; //while문 안돌게 그림 안그려도 되니까
            }
        });
    }//end of initListener
}