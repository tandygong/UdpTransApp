package net.zyc.ss.udptransapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private EditText etPort;
    private RadioGroup rbGroup;
    private EditText et_server_ip;
    private Button bt_port_action;
    private boolean isWorking;
    private DatagramSocket mDatagramSocket;

    private DatagramPacket mDatagramPacket;
    private TextView txt_receive;
    private TextView btn_send_data;
    private byte[] buffer = new byte[1024 * 20];
    private TextView et_send_data;
    private LinkedBlockingDeque<byte[]> sendQueue = new LinkedBlockingDeque<>();
    private String clientIP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etPort = findViewById(R.id.et_port);
        rbGroup = findViewById(R.id.rb_group);
        et_server_ip = findViewById(R.id.et_server_ip);
        bt_port_action = findViewById(R.id.btn_port_action);
        txt_receive = findViewById(R.id.txt_receive);
        btn_send_data = findViewById(R.id.btn_send_data);
        et_send_data = findViewById(R.id.et_send_data);
        setListener();
        startSendThread();
    }

    private void startSendThread() {
        ThreadPoolManager.getInstance().executeLongTask(new Runnable() {
            @Override
            public void run() {
                while (isWorking) {
                    byte[] bytes = sendQueue.removeFirst();

                    if (mDatagramSocket != null && !mDatagramSocket.isClosed()) {
                        try {
                            DatagramPacket datagramPacket;
                            if (rbGroup.getCheckedRadioButtonId() == R.id.rb_startServer) {
                                datagramPacket = new DatagramPacket(bytes, bytes.length);
                            }else{
                                InetAddress inetAddress = InetAddress.getByName(et_server_ip.getText().toString());
                                datagramPacket = new DatagramPacket(bytes, bytes.length,inetAddress,Integer.parseInt(etPort.getText().toString()));
                            }

                            mDatagramSocket.send(datagramPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
    }

    private MyHandler myHandler = new MyHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (rbGroup.getCheckedRadioButtonId()==R.id.rb_startServer){
                        bt_port_action.setText("停止Server");
                    }else{
                        bt_port_action.setText("断开连接");
                    }

                    break;
                case 2:
                    Bundle data = msg.getData();
                    if (data != null) {
                        byte[] receiveData = data.getByteArray("receiveData");
                        if (receiveData != null) {
                            String hexArrayString = ZOLogUtil.getHexArrayString(receiveData);
                            if (hexArrayString.length() > 100) {
                                hexArrayString = hexArrayString.substring(0, 100) + "···";
                            }
                            addText(txt_receive, hexArrayString);
                        }

                    }
                    break;
            }
            return false;
        }
    });


    private void setListener() {
        rbGroup.setOnCheckedChangeListener(this);
        bt_port_action.setOnClickListener(this);
        btn_send_data.setOnClickListener(this);
    }


    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_startClient:
                et_server_ip.setVisibility(View.VISIBLE);
                bt_port_action.setText("连接Server");
                shutDown();
                sendQueue.clear();
                break;
            case R.id.rb_startServer:
                et_server_ip.setVisibility(View.GONE);
                bt_port_action.setText("开启Server");
                shutDown();
                sendQueue.clear();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_port_action:
                if (rbGroup.getCheckedRadioButtonId() == R.id.rb_startServer) {//server
                    if (isWorking) {
                        Toast.makeText(this, "正在停止Server", Toast.LENGTH_SHORT).show();
                        shutDown();
                        bt_port_action.setText("开启Server");
                    } else {
                        Toast.makeText(this, "正在开启Server", Toast.LENGTH_SHORT).show();
                        createSocket(Integer.parseInt(etPort.getText().toString()));
                    }
                } else { //client
                    if (isWorking) {
                        Toast.makeText(this, "正在断开连接", Toast.LENGTH_SHORT).show();
                        shutDown();
                    } else {
                        Toast.makeText(this, "正在连接", Toast.LENGTH_SHORT).show();
                        createSocket(Integer.parseInt(etPort.getText().toString()));
                    }
                }
                break;

            case R.id.btn_send_data:
                if (rbGroup.getCheckedRadioButtonId() == R.id.rb_startServer) {//server
                    if (isWorking) {
                        if (mDatagramSocket != null) {
                            String str = et_send_data.getText().toString();
                            byte[] strBytes = str.getBytes();
                            sendQueue.add(strBytes);
                        }
                    }
                } else {

                }
                break;
        }
    }

    private void shutDown() {
        isWorking = false;
        if (mDatagramPacket != null) {
            mDatagramPacket = null;
        }
        if (mDatagramSocket != null && !mDatagramSocket.isClosed()) {
            mDatagramSocket.close();
            mDatagramSocket = null;
        }

    }

    private void createSocket(final int port) {
        ThreadPoolManager.getInstance().executeShortTask(new Runnable() {
            @Override
            public void run() {
                Log.e("createSocket", "port=" + port);
                try {
                    mDatagramSocket = new DatagramSocket(port);
                    mDatagramPacket = new DatagramPacket(buffer, buffer.length);
                    isWorking = true;
                    myHandler.sendEmptyMessage(1);
                    while (isWorking) {
                        try {
                            mDatagramSocket.receive(mDatagramPacket);
                            int length = mDatagramPacket.getLength();
                            if (length > 0) {
                                byte[] data = new byte[length];
                                System.arraycopy(mDatagramPacket.getData(), 0, data, 0, length);
                                ZOLogUtil.printHexArray_e("receiveData", data);
                                Message obtain = Message.obtain();
                                obtain.what = 2;
                                Bundle bundle = new Bundle();
                                bundle.putByteArray("receiveData", data);
                                obtain.setData(bundle);
                                myHandler.sendMessage(obtain);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
