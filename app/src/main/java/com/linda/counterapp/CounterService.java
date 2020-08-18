package com.linda.counterapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class CounterService extends Service {

    private static final String TAG = "CounterService";
    private int count;
    private boolean isStop = false;
    private int startCount;


    ICounterService.Stub binder = new ICounterService.Stub(){

        //전역변수를 리턴 - 인터페이스 ICounterService에 똑같은 이름의 함수가 등록되어있다.
        @Override
        public int getCount() throws RemoteException {
            return count;
        }
    };

    public CounterService() {
        Log.d(TAG, "CounterService: 생성자 실행됨");
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "onCreate: 서비스 시작");
        Thread counterThread = new Thread(new Counter());
        counterThread.start();
    }

    //activity와 생명주기를 같이 합니다.
    @Override
    public IBinder onBind(Intent intent) { //다시 start 할때
        startCount = intent.getIntExtra("startCount", 0);
        Log.d(TAG, "onBind: count : "+count);

       return binder;
    }


    @Override
    public boolean onUnbind(Intent intent) { //stop 할 때
        Log.d(TAG, "onUnbind: count : "+count);
        isStop = true; //동기화
        return super.onUnbind(intent);
    }

    //내부 클래스로
    class Counter implements Runnable{

        @Override
        public void run() {

            for(count=startCount; count<20; count++){

                if(isStop){
                    break;
                }
                try {
                    Thread.sleep(1000);
                    Log.d(TAG, "run: count: "+count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isStop = true;
        Log.d(TAG, "onDestroy: 서비스 파괴");
    }
}