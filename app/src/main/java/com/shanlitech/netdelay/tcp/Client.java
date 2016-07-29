package com.shanlitech.netdelay.tcp;

import android.os.Handler;
import android.os.Message;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
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
import java.util.Date;

/**
 * Created by reweber on 12/20/14.
 */
public class Client {

    private String host;
    private int port;

    Handler handler;
    FileOutputStream outputStream = null;

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

        socket.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {

                //byte[] msg = bb.getAllByteArray();
                String msg = getString(bb.getAll());

                System.out.println(new Date().toString() + "[Client] Received Message " + msg);

                try {
                    outputStream.write((new Date().toString() + msg + " receive \n").getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Message msgMessage = handler.obtainMessage(1, new String(bb.getAllByteArray()));
                handler.sendMessageDelayed(msgMessage, 5000);


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
}
