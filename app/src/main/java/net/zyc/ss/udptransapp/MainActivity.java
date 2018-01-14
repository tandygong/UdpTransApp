package net.zyc.ss.udptransapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    private String desIp;


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
        rbGroup.check(R.id.rb_startClient);
    }

    private void startSendThread() {
        ThreadPoolManager.getInstance().executeLongTask(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("sendThread");
                while (true) {
                    if (isWorking) {
                        byte[] bytes ;
                        try {
                            bytes = sendQueue.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            continue;
                        }

                        if (mDatagramSocket != null && !mDatagramSocket.isClosed()) {
                            try {
                                DatagramPacket datagramPacket;
                                if (rbGroup.getCheckedRadioButtonId() == R.id.rb_startServer) {
                                    InetAddress inetAddress = InetAddress.getByName(desIp);
                                    datagramPacket = new DatagramPacket(bytes, bytes.length,inetAddress,Integer.parseInt(etPort.getText().toString()));
                                } else {
                                    InetAddress inetAddress = InetAddress.getByName(et_server_ip.getText().toString());
                                    datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, Integer.parseInt(etPort.getText().toString()));
                                }
                                InetAddress address = datagramPacket.getAddress();
                                if (address != null) {
                                    ZOLogUtil.printHexArray_e("sendData:" +address.getHostAddress(), bytes);
                                }
                                mDatagramSocket.send(datagramPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        });
    }

    private MyHandler myHandler = new MyHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case 0:
                    // txt_receive.setText("");
                    receiveStrList.clear();
                    break;
                case 1:
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    if (rbGroup.getCheckedRadioButtonId() == R.id.rb_startServer) {
                        bt_port_action.setText("停止Server");
                        edit.putString("serverListenPort", etPort.getText().toString());
                    } else {
                        bt_port_action.setText("断开连接");
                        edit.putString("destServerIp", et_server_ip.getText().toString());
                        edit.putString("destServerPort", etPort.getText().toString());
                    }
                    edit.apply();
                    break;
                case 2:

                    if (data != null) {
                        byte[] receiveData = data.getByteArray("receiveData");
                        if (receiveData != null) {
                            //String hexArrayString = ZOLogUtil.getHexArrayString(receiveData);
                            String hexArrayString = new String(receiveData);
                            if (hexArrayString.length() > 100) {
                                hexArrayString = hexArrayString.substring(0, 100) + "···";
                            }
                            addText(txt_receive, hexArrayString);
                        }

                    }
                    break;
                case 5:
                    if (data != null) {
                        String reason = data.getString("reason");
                        Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
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


    ArrayList<String> receiveStrList = new ArrayList<>();

    private void addText(@NonNull TextView textView, String content) {
        int maxLine = textView.getHeight() / textView.getLineHeight();
        if (textView.getLineCount() <= maxLine && receiveStrList.size() > 0) {
            receiveStrList.add(content);
            textView.append(content);
            textView.append("\n");
            int offset = textView.getLineCount() * textView.getLineHeight();
            if (offset > textView.getHeight()) {
                textView.scrollTo(0, offset - textView.getHeight());
            }
        } else {
            if (receiveStrList.size() > 0) {
                receiveStrList.remove(0);
            } else {
                textView.scrollTo(0, 0);
            }
            receiveStrList.add(content);
            String str = "";
            for (int i = 0; i < receiveStrList.size(); i++) {
                str += receiveStrList.get(i) + "\n";
            }
            textView.setText(str);
        }

      /*  textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }*/
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        switch (checkedId) {
            case R.id.rb_startClient:
                etPort.setText("10128");
                String destServerIp = config.getString("destServerIp", "192.168.1.1");
                String destServerPort = config.getString("destServerPort", "10128");
                et_server_ip.setText(destServerIp);
                etPort.setText(destServerPort);

                et_server_ip.setVisibility(View.VISIBLE);
                bt_port_action.setText("连接Server");
                shutDown();
                sendQueue.clear();
                break;
            case R.id.rb_startServer:
                String serverListenPort = config.getString("serverListenPort", "10100");
                etPort.setText(serverListenPort);
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
                        //  Toast.makeText(this, "正在停止Server", Toast.LENGTH_SHORT).show();
                        shutDown();
                        bt_port_action.setText("开启Server");
                    } else {
                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(this, "请输入端口号", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Toast.makeText(this, "正在开启Server", Toast.LENGTH_SHORT).show();
                        createSocket(Integer.parseInt(etPort.getText().toString()));
                    }
                } else { //client
                    if (isWorking) {
                        // Toast.makeText(this, "正在断开连接", Toast.LENGTH_SHORT).show();
                        shutDown();
                        bt_port_action.setText("连接Server");
                    } else {
                        if (TextUtils.isEmpty(etPort.getText().toString())) {
                            Toast.makeText(this, "请输入端口号", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //  Toast.makeText(this, "正在连接", Toast.LENGTH_SHORT).show();
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
                            try {
                                sendQueue.put(strBytes);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    if (isWorking) {
                        if (mDatagramSocket != null) {
                            String str = et_send_data.getText().toString();
                            byte[] strBytes =str.getBytes();
                            try {
                                sendQueue.put(strBytes);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
        }
    }

    private void shutDown() {
        isWorking = false;
        if (mDatagramSocket != null && !mDatagramSocket.isClosed()) {
            mDatagramSocket.close();
            mDatagramSocket = null;
        }

    }

    private void createSocket(final int port) {
        ThreadPoolManager.getInstance().executeShortTask(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("receiveThread");
                Log.e("createSocket", "port=" + port);
                try {
                    mDatagramSocket = new DatagramSocket(port);
                    if (rbGroup.getCheckedRadioButtonId() == R.id.rb_startClient) {

                    }
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
                                desIp=mDatagramPacket.getAddress().getHostAddress();
                                Message obtain = Message.obtain();
                                obtain.what = 2;
                                Bundle bundle = new Bundle();
                                bundle.putByteArray("receiveData", data);
                                obtain.setData(bundle);
                                myHandler.sendMessage(obtain);
                            }
                            Thread.sleep(1000);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SocketException e) {
                    String str = ":" + e.getMessage();
                    str = rbGroup.getCheckedRadioButtonId() == R.id.rb_startServer ?
                            "监听失败" + str : "连接失败" + str;
                    Message obtain = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("reason", str);
                    obtain.setData(bundle);
                    obtain.what = 5;
                    myHandler.sendMessage(obtain);
                    e.printStackTrace();
                }
                myHandler.sendEmptyMessage(0);//不直接清除receiveList,保证在消息队列的最后一条清除
            }
        });

    }
}
