package net.sunniwell.download;

import android.os.Environment;

/**
 * 功能 : 存储常量的bean。
 * 
 * @author 作者 郭勇 创建时间：2016年7月25日
 */
public class Contants {
	public static final String TAG = "Download";
	/** 更新进度条的广播的Action */
	public static final String ACTION_DOWNLOAD_PROGRESS = "ACTION_DOWNLOAD_PROGRESS";
	/** 下载成功时广播的Action */
	public static final String ACTION_DOWNLOAD_SUCCESS = "ACTION_DOWNLOAD_SUCCESS";
	/** 下载失败时广播的Action */
	public static final String ACTION_DOWNLOAD_FAIL = "ACTION_DOWNLOAD_FAIL";

	/** 初始状态 */
	public static final int Flag_Init = 0;
	/** 正在下载状态 */
	public static final int Flag_Down = 1;
	/** 暂停状态 */
	public static final int Flag_Pause = 2;
	/** 完成状态 */
	public static final int Flag_Done = 3;
}
