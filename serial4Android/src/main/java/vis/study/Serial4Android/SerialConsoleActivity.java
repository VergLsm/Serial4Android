/* Copyright 2011 Google Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: http://code.google.com/p/usb-serial-for-android/
 */

package vis.study.Serial4Android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import vis.util.ConfigHelper.ConfigEntity;
import vis.util.ConfigHelper.ConfigService;
import vis.util.NetHelper.Datagram4Socket;
import vis.util.NetHelper.NetHelper;
import vis.util.SerialHelper.SerialConsole;
import vis.util.VideoHelper.CVideoWriter;

/**
 * Monitors a single {@link UsbSerialDriver} instance, showing all data
 * received.
 * 
 * @author mike wakerly (opensource@hoho.com)
 */
public class SerialConsoleActivity extends Activity {

    private final String TAG = SerialConsoleActivity.class.getSimpleName();

    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialDriver)}.
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialDriver sDriver = null;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;
    private TextView tvIP;
    private EditText etData;
    private Button btnSend;

    // private Server4Serial server;
    private Datagram4Socket d4s;
    private SerialConsole serialConsole;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SerialConsoleActivity.this.updateUIData("receive", data);
                        }
                    });
                }
            };

    // private final Stream4Socket.Listener s4sListener = new
    // Stream4Socket.Listener() {
    //
    // @Override
    // public void onRunError(Exception e) {
    // Log.d(TAG, "Runner stopped.");
    // }
    //
    // @Override
    // public void onNewMsg(final String msg) {
    // // TODO 自动生成的方法存根
    // SerialConsoleActivity.this.runOnUiThread(new Runnable() {
    // @Override
    // public void run() {
    // SerialConsoleActivity.this.updateSocketReceivedMsg(msg);
    // }
    // });
    // }
    //
    // @Override
    // public void onNewData(final String data) {
    // // TODO 自动生成的方法存根
    // SerialConsoleActivity.this.runOnUiThread(new Runnable() {
    // @Override
    // public void run() {
    // serialConsole.wirte(data);
    // // MainActivity.this.updateReceivedData(data);
    // }
    // });
    // }
    // };

    private final Datagram4Socket.Listener d4sListener = new Datagram4Socket.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewMsg(final String msg) {
            // TODO 自动生成的方法存根
            SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SerialConsoleActivity.this.updateSocketReceivedMsg(msg);
                }
            });
        }

        @Override
        public void onNewData(final String data) {
            // TODO 自动生成的方法存根
            SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // serialConsole.wirte(data);
                    SerialConsoleActivity.this.translateCommand(data);
                }
            });
        }
    };

    private ConfigEntity configEntity;

    private EditText etControlPhoneIP;

    private EditText etControlPhoneVideoPort;

    private Button btnSetVideo;

    private SurfaceView mSurfaceview = null;

    private CVideoWriter cw;

    private boolean ipWillCome = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);

        Log.d(TAG, "onCreate");

        configEntity = ConfigService.LoadConfig(this);

        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        tvIP = (TextView) findViewById(R.id.tvIP);
        etData = (EditText) findViewById(R.id.etData);
        btnSend = (Button) findViewById(R.id.btnSend);
        etControlPhoneIP = (EditText) findViewById(R.id.etControlPhoneIP);
        etControlPhoneVideoPort = (EditText) findViewById(R.id.etControlPhoneVideoPort);
        btnSetVideo = (Button) findViewById(R.id.btnSetVideoPort);
        mSurfaceview = (SurfaceView) findViewById(R.id.surfaasdasdas);

        etControlPhoneIP.setText(configEntity.controlPhoneIp);
        etControlPhoneVideoPort.setText("" + configEntity.carPhoneAudioPort);

        cw = new CVideoWriter(mSurfaceview, configEntity.controlPhoneIp,
                configEntity.carPhoneAudioPort,
                getApplicationContext());

        btnSend.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO 自动生成的方法存根
                SerialConsoleActivity.this.wirteSerial(String.valueOf(etData.getText()));
            }
        });

        btnSetVideo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 自动生成的方法存根
                Button btn = (Button) v;
                btn.setEnabled(false);
                if (btn.getText().toString().equals("Set")) {
                    configEntity.controlPhoneIp = etControlPhoneIP.getText().toString();
                    configEntity.carPhoneAudioPort = Integer.parseInt(etControlPhoneVideoPort
                            .getText().toString());
                    cw.setUrl(configEntity.controlPhoneIp, configEntity.carPhoneAudioPort);
                    cw.startWrtier();
                    btn.setText("Stop");
                } else {
                    cw.stopWriter();
                    btn.setText("Set");
                }
                btn.setEnabled(true);
            }
        });
        // myHandler = new MyHandler();
        // server = new Server4Serial(this, socketListener);
        d4s = new Datagram4Socket(d4sListener);
        serialConsole = new SerialConsole(sDriver, mListener);
        init();
    }

    @Override
    protected void onDestroy() { // onPause()
        super.onDestroy();

        serialConsole.close(); // 关闭串口
        cw.endWriter(); // 停止视频
        d4s.close(); // 关闭网络
        // server.closeAll();
        // finish();

    }

    @Override
    protected void onPause() {
        // TODO 自动生成的方法存根
        super.onPause();

        wirteSerial(0); // 保险起见，退出前先发送停止命令

        configEntity.controlPhoneIp = etControlPhoneIP.getText().toString();
        configEntity.controlPhoneVideoPort = Integer.parseInt(etControlPhoneVideoPort.getText()
                .toString());
        ConfigService.SaveConfig(this, configEntity);
    }

    // @Override
    protected void init() { // onResume()
        // super.onResume();

        Log.d(TAG, "Resumed, sDriver=" + sDriver);
        boolean isOpen = serialConsole.open();

        if (!isOpen) {
            mTitleTextView.setText("No serial device.");
        } else {
            tvIP.setText(NetHelper.getLocalIpAddress(this) + ":5550");
            d4s.beginReceive(5550);
            mTitleTextView.setText("Serial device: " + sDriver.getClass().getSimpleName());
        }
    }

    /**
     * 接收到socket传来的信息，自动调用这个方法。
     * 
     * @author Vision_lsm
     * @param msg
     */
    protected void updateSocketReceivedMsg(String msg) {
        // TODO 自动生成的方法存根
        // serialConsole.wirte(data);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * String先转成Int，判断是否在[0,255] 再转成byte[]，但byte[]里面只有一个元素。 最后写到串口。
     * 
     * @author Vision_lsm
     */
    public void wirteSerial(String str) {
        int i = 0;
        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        wirteSerial(i);
        // Toast.makeText(this, i + "", Toast.LENGTH_SHORT).show();
        // switch(){}
        // serialConsole.wirte(b);
    }

    public void wirteSerial(int i) {
        byte[] b = {
                0x00
        };
        if (i > 255 || i < 0) { // 溢出时
            return;
        }
        b[0] = (byte) (i & 0xFF);
        serialConsole.wirte(b);
        updateUIData("send", b);
    }

    /**
     * 接受到串口传来的数据，自动调用这个方法。
     * 
     * @author Vision_lsm
     * @param data
     */
    private void updateUIData(String type, byte[] data) {
        // final String message = "Read " + data.length + " bytes: \n"
        // + HexDump.dumpHexString(data) + "\n\n";
        final String message = "serial -> " + type + " -> "
                + HexDump.toHexString(data)
                + "\n\n";
        // server.send(HexDump.toHexString(data));
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }

    /**
     * 将收到的数据，翻译成实际需要执行的操作。
     * 
     * @author Vision_lsm
     * @param data
     */
    private void translateCommand(String data) {
        if (ipWillCome) {
            configEntity.controlPhoneIp = data;
            etControlPhoneIP.setText(data);
            ipWillCome = false;
        }
        int i;
        try {
            i = Integer.parseInt(data);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4: { // 各种方向
                wirteSerial(i);
                break;
            }
            case 5: { // 设置IP
                ipWillCome = true;
                break;
            }
            case 6: { // 开关视频
                if (btnSetVideo.getText().toString().equals("Set")) {
                    configEntity.controlPhoneIp = etControlPhoneIP.getText().toString();
                    configEntity.carPhoneAudioPort = Integer.parseInt(etControlPhoneVideoPort
                            .getText().toString());
                    cw.setUrl(configEntity.controlPhoneIp, configEntity.carPhoneAudioPort);
                    cw.startWrtier();
                    btnSetVideo.setText("Stop");
                } else {
                    cw.stopWriter();
                    btnSetVideo.setText("Set");
                }
                break;
            }
            default: {
                wirteSerial(0); // 其它信号转成0
            }
        }
        // updateUIData("receive",null);
    }

    /**
     * Starts the activity, using the supplied driver instance.
     * 
     * @param context
     * @param driver
     */
    static void show(Context context, UsbSerialDriver driver) {
        Log.d("show()", "start");
        sDriver = driver;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
        Log.d("show()", "end");
    }
    //
    // class MyHandler extends Handler {
    // public MyHandler() {
    // }
    //
    // public MyHandler(Looper L) {
    // super(L);
    // }
    //
    // // 子类必须重写此方法,接受数据
    // @Override
    // public void handleMessage(Message msg) {
    // // TODO Auto-generated method stub
    // Log.d("MyHandler", "handleMessage......");
    // super.handleMessage(msg);
    // // 此处可以更新UI
    // Bundle b = msg.getData();
    // byte type = b.getByte("type");
    // String data = b.getString("data");
    // // MyHandlerActivity.this.button.append(color);
    //
    // switch (type) {
    // case 0:
    // Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT)
    // .show();
    // break;
    // case 1:
    // Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT)
    // .show();
    // serialConsole.wirte(data);
    // break;
    // }
    // }
    // }
}
