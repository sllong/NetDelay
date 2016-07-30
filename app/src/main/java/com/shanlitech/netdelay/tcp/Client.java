package com.shanlitech.netdelay.tcp;

import android.os.Handler;
import android.os.Message;
import com.koushikdutta.async.*;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by reweber on 12/20/14.
 */
public class Client {

    private String host;
    private int port;

    Handler handler;
    FileOutputStream outputStream = null;

    AsyncSocket mSocket = null;

    int mCount = 0;

    public Client(Handler handler, FileOutputStream outputStream, String host, int port) {
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.outputStream = outputStream;

        setup();
    }

    private void setup() {
        AsyncServer.getDefault().connectSocket(new InetSocketAddress(host, port), new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, final AsyncSocket socket) {
                handleConnectCompleted(ex, socket);
            }
        });
    }

    /**
     * ByteBuffer 转换 String
     * @param buffer
     * @return
     */
    public static String getString(ByteBuffer buffer)
    {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try
        {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }

    private void handleConnectCompleted(Exception ex, final AsyncSocket socket) {
        if(ex != null) throw new RuntimeException(ex);

//        Util.writeAll(socket, "Hello Server".getBytes(), new CompletedCallback() {
//            @Override
//            public void onCompleted(Exception ex) {
//                if (ex != null) throw new RuntimeException(ex);
//                System.out.println("[Client] Successfully wrote message");
//            }
//        });

        mSocket = socket;
        new Thread(new WorkThread()).start();

        socket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {

                //byte[] msg = bb.getAllByteArray();
                String msg = getString(bb.getAll());

                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");//设置日期格式
                System.out.println(df.format(new Date()) + "[Client] Received Message " + msg);

                try {
                    outputStream.write((df.format(new Date()) + " " + msg + " receive \n").getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Message msgMessage = handler.obtainMessage(1, new String(bb.getAllByteArray()));
                handler.sendMessageDelayed(msgMessage, 1000);


            }
        });

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully closed connection");
            }
        });

        socket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if(ex != null) throw new RuntimeException(ex);
                System.out.println("[Client] Successfully end connection");
            }
        });
    }

    public class WorkThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");//设置日期格式
                    System.out.println(df.format(new Date()) + "[Client] Send Ping ");

                    Util.writeAll(mSocket, ("Ping " + mCount).getBytes(), new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            if (ex != null) throw new RuntimeException(ex);
                            System.out.println("[Client] Successfully wrote message " + mCount);
                        }
                    });

                    try {

                        outputStream.write((df.format(new Date()) + " Ping " + mCount + " send \n").getBytes());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mCount ++;

                    Thread.sleep(60000);// 线程暂停10秒，单位毫秒

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
