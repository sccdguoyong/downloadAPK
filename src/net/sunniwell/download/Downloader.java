package net.sunniwell.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 功能 ： 下载类。
 *
 * @author 作者 郭勇 创建时间：2016年7月26日
 * 
 */
public class Downloader {
	/** 下载地址 */
	private String urlStr;
	/** 文件路径 */
	private String filePath;
	/** 文件名称 */
	private String fileName;
	/** 下载监听对象 */
	private DownloadListener downloadListener;
	/** 是否暂停 */
	private boolean mIsPause;
	/** 是否停止 */
	private boolean mIsCancel;

	public void setDownloadListener(DownloadListener listener) {
		this.downloadListener = listener;
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param url
	 *            下载路径
	 * @param filePath
	 *            文件保存路径
	 * @param fileName
	 *            文件名
	 */
	public Downloader(Context context, String url, String filePath, String fileName) {
		this.urlStr = url;
		this.filePath = filePath;
		this.fileName = fileName;
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param url
	 *            下载路径
	 * @param fileName
	 *            文件名
	 */
	public Downloader(Context context, String url, String fileName) {
		this(context, url, "/download/", fileName);
	}

	/**
	 * 功能 ： 开始下载。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月26日
	 */
	public void start() {
		URL url = null;
		try {
			url = new URL(urlStr);
			HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
			// 设置是否从httpUrlConnection读入，默认情况下是true;
			urlCon.setDoInput(true);
			urlCon.setRequestMethod("GET");
			// 设定传送的内容类型是可序列化的java对象
			urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

			// 建立连接
			urlCon.connect();
			int length = urlCon.getContentLength();
			downloadListener.onStart(length);

			if (urlCon.getResponseCode() == 200) {

				File dir = Environment.getExternalStoragePublicDirectory(filePath);
				if (!dir.exists()) {
					dir.mkdir();
				}
				if (!urlStr.endsWith(".apk")) {
					fileName = fileName + ".apk";
				}
				File file = new File(dir, fileName);
				if (!file.exists()) {
					file.createNewFile();
				}
				BufferedInputStream is = new BufferedInputStream(urlCon.getInputStream());
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
				byte[] buffer = new byte[1024 * 1024];
				int len = 0;
				int receivedBytes = 0;
				lable: while (true) {
					// 这里如果暂停下载，并没有真正的销毁线程，而是处于等待状态
					// 但如果这时候用户退出了，要做处理，比如取消任务；或做其他处理

					if (mIsPause)
						downloadListener.onPause();
					if (mIsCancel) {
						downloadListener.onCancel();
						break lable;
					}
					// 这里是让广播有时间更新，同步加锁
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						Log.e(Contants.TAG, Log.getStackTraceString(e));
					}

					while (!mIsPause && (len = is.read(buffer)) > 0) {
						out.write(buffer, 0, len);
						receivedBytes += len;
						downloadListener.onProgress(receivedBytes);
						if (receivedBytes == length) {
							downloadListener.onSuccess(file);
							break lable;
						}
						if (mIsCancel) {
							downloadListener.onCancel();
							file.delete();
							break lable;
						}
					}
				}

				is.close();
				out.close();
			} else {
				Log.e(Contants.TAG, "httpurlconnection连接失败");
			}

		} catch (Exception e) {
			Log.e(Contants.TAG, Log.getStackTraceString(e));
			downloadListener.onFail();
		}
	}

	/**
	 * 功能 ： 暂停下载。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 * 
	 */
	public void pause() {
		mIsPause = true;
	}

	/**
	 * 功能 ： 正在下载。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 * 
	 */
	public void resume() {
		mIsPause = false;
		mIsCancel = false;
		downloadListener.onResume();
	}

	/**
	 * 功能 ： 取消或者停止下载。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 * 
	 */
	public void cancel() {
		mIsCancel = true;
	}
}
