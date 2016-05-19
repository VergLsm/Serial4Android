package vis.util.ConfigHelper;

/**
 * 设置变量存放类
 * @author verg
 */
public class ConfigEntity {
//	public static final int VIDEO_MODE_SERVERCONFIG = 0;	// 服务器视频参数配置
//	public static final int VIDEO_MODE_CUSTOMCONFIG = 1;	// 自定义视频参数配置
//	
//	public static final int VIDEO_QUALITY_NORMAL = 2;		// 普通视频质量
//	public static final int VIDEO_QUALITY_GOOD = 3;			// 中等视频质量
//	public static final int VIDEO_QUALITY_BEST = 4;			// 较好视频质量
//	
//	public boolean IsSaveNameAndPw;
//	public String name = "";
//	public String password = "";

	public String controlPhoneIp = "";                     //控制电话IP
	public String carPhoneIP = "";                         //车上电话IP
	
	public int controlPhoneVideoPort;                      //控制电话的视频输入端口
	public int controlPhoneAudioPort;                      //控制电话的音频输入端口
	public int carPhonebasePort;                           //车上电话的基本控制端口
	public int carPhoneAudioPort;                          //车上电话的音频输入端口
	
//	public int configMode = VIDEO_MODE_SERVERCONFIG;
//	public int resolution_width = 0;
//	public int resolution_height = 0;
	
//	public int videoBitrate = 0;
//	public int videoFps = 0;
//	public int videoQuality = VIDEO_QUALITY_GOOD;
//	public int videoPreset = 1;
//	public int videoOverlay = 1;							// 本地视频是否采用Overlay模式
//	public int videorotatemode = 0;							// 本地视频旋转模式
//	public int videoCapDriver = 3;							// 本地视频采集驱动（0 默认， 1 Linux驱动，3 Java驱动
//	public int fixcolordeviation = 0;						// 修正本地视频采集偏色：0 关闭(默认）， 1 开启
	
//	public int enableP2P = 1;
//	public int useARMv6Lib = 0;								// 是否强制使用ARMv6指令集，默认是内核自动判断
//	public int enableAEC = 1;								// 是否使用回音消除功能
//	public int useHWCodec = 0;								// 是否使用平台内置硬件编解码器
//	public int smoothPlayMode = 0;							// 是否使用平滑播放模式（打开时播放更平滑，丢包时会出现马赛克，关闭时不会出现马赛克[默认]，但丢包时画面会短暂卡住）
//	public int videoShowDriver = 5;							// 视频显示驱动（0 默认， 4 Android 2.x兼容模式，5 Java驱动）
//	public int audioPlayDriver = 3;							// 音频播放驱动（0 默认，3 Java驱动）
//	public int audioRecordDriver = 3;						// 音频采集驱动（0默认，3 Java驱动）
}
