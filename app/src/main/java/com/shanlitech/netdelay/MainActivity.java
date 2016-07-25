package com.shanlitech.netdelay;

import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                //TCP client and server (Client will automatically send welcome message after setup and server will respond)
                new com.shanlitech.netdelay.tcp.Server("localhost", 7000);
                new com.shanlitech.netdelay.tcp.Client("localhost", 7000);

                //UDP client and server (Here the client explicitly sends a message)
                new com.shanlitech.netdelay.udp.Server("localhost", 7001);
                new com.shanlitech.netdelay.udp.Client("localhost", 7001).send("Hello World");
                return null;
            }
        }.execute();
    }
}
