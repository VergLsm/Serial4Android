
package vis.util.NetHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class Datagram4Socket {

    private final String TAG = Datagram4Socket.class.getSimpleName();
    private DatagramSocket dsocket;
    private InetAddress target;
    private int port;

    private Listener mListener;
    private boolean isReceived;

    // private String endFlag;

    public interface Listener {
        /**
         * Called when new incoming data is available.
         */
        public void onNewData(String data);

        public void onNewMsg(String msg);

        /**
         * Called when {@link SerialInputOutputManager#run()} aborts due to an
         * error.
         */
        public void onRunError(Exception e);
    }

    public Datagram4Socket(Listener listener) {
        this.mListener = listener;
        isReceived = false;
    }

    class receive implements Runnable {

        @Override
        public void run() {
            // TODO 自动生成的方法存根
            mListener.onNewMsg("Receiving");
                    Log.d(TAG, "Receiving");
            byte[] buffer = new byte[8192];
            try {
                while (isReceived) {
                    DatagramPacket incomming = new DatagramPacket(buffer,
                            buffer.length);
                    dsocket.receive(incomming);
                    String data = new String(incomming.getData(), 0,
                            incomming.getLength());
                    mListener.onNewData(data);
                    Log.d(TAG, "receive:" + data);
                }
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                Log.e(TAG, e.getMessage());
                mListener.onNewMsg(e.getMessage());
                // e.printStackTrace();
            } finally {
                if (null != dsocket) {
                    dsocket.close();
                    Log.d(TAG, "EndReceive");
                    mListener.onNewMsg("EndReceive");
                    dsocket = null;
                }
            }
        }

    }

    class send implements Runnable {

        private String data;

        public send(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            // TODO 自动生成的方法存根
            if (null == dsocket) {
                mListener.onNewMsg("DatagramSocker is null");
                Log.d(TAG, "DatagramSocker is null");
                return;
            }
            byte[] dataByte = data.getBytes();
            DatagramPacket outgoing = new DatagramPacket(dataByte,
                    dataByte.length, target, port);
            try {
                dsocket.send(outgoing);
                mListener.onNewMsg("sent");
                Log.d(TAG, "sent");
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                Log.e(TAG, e.getMessage());
                mListener.onNewMsg(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public void send(String data) {
        new Thread(new send(data)).start();
    }

    public void setTarget(String target, int port) {
        try {
            this.target = InetAddress.getByName(target);
            this.port = port;
            Log.d(TAG, "setValues");
            mListener.onNewMsg("setValues");
        } catch (UnknownHostException e) {
            // TODO 自动生成的 catch 块
            Log.e(TAG, e.getMessage());
            mListener.onNewMsg(e.getMessage());
            e.printStackTrace();
        }
    }

    public void beginReceive(int port) {

        this.port = port;

        try {
            this.dsocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // this.endFlag = endFlag;
        this.isReceived = true;
        new Thread(new receive()).start();
    }

    public void close() {
        isReceived = false;
        // send(endFlag);
        if (dsocket != null) {
            dsocket.close();
            Log.d(TAG, "close()");
        }
        dsocket = null;
    }

}
