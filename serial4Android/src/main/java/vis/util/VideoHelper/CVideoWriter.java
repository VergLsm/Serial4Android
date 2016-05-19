package vis.util.VideoHelper;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

// CVideoWrtier 以SurfaceView实例对象构造一个Writer
public class CVideoWriter implements Camera.PreviewCallback {

	private final String TAG = CVideoWriter.class.getSimpleName();

	private SurfaceView m_surface = null;
	private SurfaceHolder m_holder = null;

	// 控制缓存PreViewFrame发送
	private boolean control = false;

	// Camera实例对象
	private Camera mCamera = null;

	// 视频参数
	private int VideoWidth = 480;
	private int VideoHeight = 320;
	private int VideoQuality = 12;

	// 服务端地址
	private String serverUrl;
	// 服务端端口
	private int serverPort;

	// 应用程序上下文
	private final Context current_context;

	private int bln = 0;

	DatagramSocket s = null;

	public CVideoWriter(SurfaceView m_surface, String ip, int Port,
			Context current) {
		serverUrl = ip;
		serverPort = Port;
		this.m_surface = m_surface;
		this.current_context = current;
		m_holder = m_surface.getHolder();
		m_holder.addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
					int arg3) {
				Log.d(TAG, "surfaceChanged()");
				// surfaceView有新内容时候
				if (mCamera != null) {
					Camera.Parameters parameters = mCamera.getParameters();
					parameters.setPreviewFormat(ImageFormat.NV21);
					parameters.setPictureFormat(ImageFormat.JPEG);
					// parameters.setPreviewFrameRate(1);
					// parameters.setPreviewFpsRange(100000, 200000);

					Size size = parameters.getPreviewSize();
					// parameters.set("", "");
					mCamera.setParameters(parameters);
					VideoWidth = size.width;
					VideoHeight = size.height;
					mCamera.startPreview();
				}

			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "surfaceCreated()");
				// 完成surfaceView构造时候的初始化工作
				// 打开摄像头，获取摄像头对象
				// 为摄像头对象mCamera设置回调函数
				// 启动摄像头，开始
				mCamera = Camera.open();

				if (mCamera != null) {
					mCamera.setDisplayOrientation(90);
					mCamera.setPreviewCallback(CVideoWriter.this);
					try {
						mCamera.setPreviewDisplay(m_holder);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					Toast s = Toast.makeText(current_context,
							"摄像头启动失败，请确认摄像头权限", Toast.LENGTH_LONG);
					s.show();
				}
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "surfaceDestroyed()");
				if (mCamera != null) {
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();
					mCamera.release();
				}
			}
		});

	}

	// 开始发送实时数据
	public void startWrtier() {
		Log.d(TAG, "startWrtier()");
		if (null != s) {
			return;
		}
		control = true;
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			Toast s1 = Toast.makeText(current_context, "网络打开失败，请确认网络权限",
					Toast.LENGTH_LONG);
			s1.show();
		}
	}

	// 停止发送实时数据
	public void stopWriter() {
		Log.d(TAG, "stopWriter()");
		control = false;
		// 收尾操作
		if (s != null) {
			s.close();
			s = null;
			Log.d(TAG, "socket = null");
		}
	}

	// endWriter用于 终止线程，在程序退出时执行
	public void endWriter() {
		Log.d(TAG, "endWriter()");
		control = false;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPreviewFrame()");
		// control标志量，控制是否发送图片数据
		if (!control) {
			return;
		}

		if (bln == 7) {
			bln = 0;
		} else {
			bln++;
		}

		if (bln != 3) {
			return;
		}
		// YuvImage解析data数据，重新生成新的图片数据
		YuvImage image = new YuvImage(data, ImageFormat.NV21, VideoWidth,
				VideoHeight, null);
		if (image != null) {
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();

			// 图片压缩Jpeg 并放入outstream中
			image.compressToJpeg(new Rect(0, 0, VideoWidth, VideoHeight),
					VideoQuality, outstream);
			try {
				outstream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			new MySendFileThread(outstream).start();

		}
	}

	// 更改目标ip地址和端口
	public void setUrl(String ip, int port) {
		if (!ip.isEmpty()) {
			control = false;// PV锁操作

			serverUrl = ip;
			serverPort = port;

			control = true;
		}

	}

	// 更改图像的质量
	public void setQuality(int temp) {
		control = false; // 临界资源PV锁定操作

		VideoQuality = temp;
		if (temp > 80)
			VideoQuality = 80;

		control = true;// 临界资源PV锁定操作
	}

	// 发送outstream数据流线程
	public class MySendFileThread extends Thread {

		// private String ipname;
		// private int port;

		private ByteArrayOutputStream data;

		public MySendFileThread(ByteArrayOutputStream data) {

			// this.ipname = ipname;
			// this.port = port;
			this.data = data;

		}

		public void run() {

			try {
				// 将图像数据通过Socket发送出去

				DatagramPacket dPacket = new DatagramPacket(data.toByteArray(),
						data.size(), InetAddress.getByName(serverUrl),
						serverPort);// /
				s.send(dPacket);
				Log.d(TAG, "send()");
			} catch (Exception e) {

				e.printStackTrace();

			}

		}
	}
}
