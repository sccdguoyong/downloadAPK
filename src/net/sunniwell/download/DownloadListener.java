package net.sunniwell.download;

import java.io.File;

/**
 * 功能 ： 下载的回调接口。
 *
 * @author 作者 郭勇 创建时间：2016年7月26日
 * 
 */
public interface DownloadListener {
	/**
	 * 功能 : 开始下载的回调。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onStart(int fileByteSize);

	/**
	 * 功能 : 暂停下载的回调。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onPause();

	/**
	 * 功能 : 正在下载状态的回调。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onResume();

	/**
	 * 功能 ： 更新进度的回调
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onProgress(int receivedBytes);

	/**
	 * 功能 : 下载失败的回调
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onFail();

	/**
	 * 功能 ： 下载成功的回调。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onSuccess(File file);

	/**
	 * 功能 ： 停止或者取消下载的回调
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 * 
	 */
	void onCancel();
}
