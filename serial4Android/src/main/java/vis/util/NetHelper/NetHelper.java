package vis.util.NetHelper;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetHelper {


	public NetHelper(Context context) {
		// TODO 自动生成的构造函数存根

	}

	public static String getLocalIpAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(android.content.Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		Log.i("IP", String.valueOf(ipAddress));
//		try {
//			 return InetAddress.getByName(
//			 String.format("%d.%d.%d.%d", (ipAddress & 0xff),
//			 (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
//			 (ipAddress >> 24 & 0xff))).toString();
//		} catch (UnknownHostException e) {
//			// TODO 自动生成的 catch 块
//			e.printStackTrace();
//		}
		return String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff)).toString();

//		return null;
	}
}
