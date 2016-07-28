package com.shanlitech.netdelay;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FileOutputStream outputStream = null;
    private PowerManager.WakeLock mWakelock;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                outputStream.write((new Date().toString() + ((String) msg.obj) +   "savelocal").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
//            releaseWakeLock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                //TCP client and server (Client will automatically send welcome message after setup and server will respond)
//                new com.shanlitech.netdelay.tcp.Server("localhost", 7000);
//                new com.shanlitech.netdelay.tcp.Client("localhost", 7000);
//
//                //UDP client and server (Here the client explicitly sends a message)
//                new com.shanlitech.netdelay.udp.Server("localhost", 7001);
//                new com.shanlitech.netdelay.udp.Client("localhost", 7001).send("Hello World");
//                return null;
//            }
//        }.execute();

        new Thread(new Runnable() {

            @Override
            public void run() {
                File file = new File("/sdcard/testlog-lock.txt");
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }

                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                }
                try {

                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("119.254.211.165", 10061));
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader inputStream2 = new BufferedReader(new InputStreamReader(
                            inputStream));
                    String lineString;
                    while ((lineString = inputStream2.readLine()) != null) {
//                        acquireWakeLock();
                        outputStream.write((new Date().toString() + lineString +  "receive").getBytes());
                        Message msgMessage = handler.obtainMessage(1, lineString);
                        handler.sendMessageDelayed(msgMessage, 5000);
                    }
                } catch (UnknownHostException e) {
                    try {
                        outputStream.write(e.getMessage().getBytes());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    try {
                        outputStream.write(e.getMessage().getBytes());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void acquireWakeLock() {
        if (mWakelock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
        }
        mWakelock.acquire();
    }

    private void releaseWakeLock() {
        if (mWakelock != null && mWakelock.isHeld()) {
            mWakelock.release();
        }
        mWakelock = null;
    }
}
