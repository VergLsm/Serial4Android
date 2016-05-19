package vis.util.NetHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.util.Log;

public class Stream4Socket implements Runnable {

	private final String TAG = Stream4Socket.class.getSimpleName();

	private ServerSocket ss;
	// private Message message;
	// private Bundle bundle;
	public boolean isListen;
	public String endFlag;
	private Socket client = null;
	private PrintWriter out;
	private BufferedReader in;

	private int port;

	private Thread thread;

	// Synchronized by 'this'
	private Listener mListener;

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

	public Stream4Socket(Context context, Listener listener) {
		this.mListener = listener;
	}

	@Override
	public void run() {

		try {
			this.ss = new ServerSocket(port);
			// returnMsg("socket setup.", (byte) 0);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

		while (isListen) {
			// client = listener(ss);
			// streamer(client, "end");
			Log.d(TAG, "start to listen.");
			mListener.onNewMsg("start to listen.");
			// returnMsg("start to listen.", (byte) 0);
			try {
				client = ss.accept();
				Log.d(TAG, "sth connected.");
				mListener.onNewMsg("sth connected.");
				// returnMsg("sth connected.", (byte) 0);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
				break;
			}
			// if(isListen){
			// break;
			// }
			if (!client.isClosed()) {
				try {
					out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(client.getOutputStream())),
							true);
					in = new BufferedReader(new InputStreamReader(
							client.getInputStream()));
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
					break;
				}

				receive(); // 阻塞方法

				try {
					out.close();
					out = null;
					in.close();
					in = null;
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}

				try {
					client.close();
					Log.d(TAG, "closed the Client.");
					mListener.onNewMsg("closed the Client.");
					// returnMsg("closed the Client.", (byte) 0);
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}

				// returnMsg("00", (byte) 1);
				// close(client);
			}
		}
	}

	public synchronized Listener getListener() {
		return mListener;
	}

	public boolean send(String str) {
		if (null == out) {
			return false;
		} else {
			out.println(str);
			out.flush();
			return true;
		}
	}

	public void receive() {
		while (!client.isClosed()) {
			String str = null;
			try {
				str = in.readLine();
				if (str.equals(endFlag)) {// 流出错，输入end，结束
					send(endFlag);
					break;
				}
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			if (ss.isClosed()) {
				// out.println(endFlag);
				break;
			}
			// send(str);
			mListener.onNewData(str);
			System.out.println(str);
			// returnMsg(str, (byte) 1); // 向Handler发送消息,更新UI
		}
	}

	// protected boolean returnMsg(String data, byte type) {
	// Message message = new Message();
	// Bundle bundle = new Bundle();// 存放数据
	// bundle.putByte("type", type);
	// bundle.putString("data", data);
	// message.setData(bundle);
	// // ((SerialConsoleActivity) context).myHandler.sendMessage(message);
	// ((MainActivity) context).myHandler.sendMessage(message);
	// return true;
	// }

	public void beginListen(int port, String endFlag) {
		// TODO 自动生成的方法存根
		this.port = port;
		this.endFlag = endFlag;
		isListen = true;
		thread = new Thread(this);
		thread.start();
	}

	public void close() {
		send(endFlag);
	}

	public void closeAll() {
		isListen = false;
		if (client.isConnected()) {
			close();
		}
		// thread.interrupt();
		try {
			ss.close();
			Log.d(TAG, "closed the ServerSocket.");
			mListener.onNewMsg("closed the ServerSocket.");
			// returnMsg("closed the ServerSocket.", (byte) 0);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
}
