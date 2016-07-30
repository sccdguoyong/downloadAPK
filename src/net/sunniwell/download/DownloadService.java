package net.sunniwell.download;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 功能
 * 
 * @author 作者 郭勇 创建时间：2016年7月25日
 */
public class DownloadService extends Service {
	/** 下载地址 */
	private String url;
	/** 下载进度 */
	private int progress = 0;
	/** 下载状态标志 */
	private int flag;
	private DownThread mThread;
	private Downloader downloader;
	/** DownloadService单例对象 */
	private static DownloadService sInstance;

	/**
	 * 功能 ： 获取DownloadService对象。
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月25日
	 */
	public static DownloadService getInstance() {
		return sInstance;
	}

	/**
	 * 功能 : 获取下载进度。
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月25日
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * 功能 ：获取下载标志
	 *
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月25日
	 */
	public int getFlag() {
		return flag;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(Contants.TAG, "service.........onCreate");
		sInstance = this;
		flag = Contants.Flag_Init;
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String msg = intent.getExtras().getString("flag");
		url = intent.getExtras().getString("url");
		if (mThread == null) {
			mThread = new DownThread();
		}
		if (downloader == null && !TextUtils.isEmpty(url)) {
			downloader = new Downloader(this, url, url.substring(url.lastIndexOf("/") + 1));
		}
		downloader.setDownloadListener(downListener);

		if (msg.equals("start")) {
			startDownload();
		} else if (msg.equals("pause")) {
			downloader.pause();
		} else if (msg.equals("resume")) {
			downloader.resume();
		} else if (msg.equals("stop")) {
			downloader.pause();
			downloader.cancel();
			// 停止服务
			stopSelf();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 功能 ： 开始下载的方法。
	 *
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月25日
	 */
	private void startDownload() {
		if (flag == Contants.Flag_Init || flag == Contants.Flag_Pause) {
			if (mThread != null && !mThread.isAlive()) {
				mThread = new DownThread();
			}
			mThread.start();
		}
	}

	@Override
	public void onDestroy() {
		Log.e(Contants.TAG, "service...........onDestroy");
		try {
			flag = 0;
			mThread.join();
		} catch (InterruptedException e) {
			Log.e(Contants.TAG, Log.getStackTraceString(e));
		}
		mThread = null;
		super.onDestroy();
	}

	/**
	 * 功能 ：开始下载的任务类。
	 *
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月25日
	 */
	class DownThread extends Thread {

		@Override
		public void run() {

			if (flag == Contants.Flag_Init || flag == Contants.Flag_Done) {
				flag = Contants.Flag_Down;
			}

			downloader.start();
		}
	}

	private DownloadListener downListener = new DownloadListener() {
		/** 文件的大小 */
		int fileSize;
		/** 意图对象 */
		Intent intent = new Intent();

		/**
		 * 功能： 回调成功的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onSuccess(File file) {
			intent.setAction(Contants.ACTION_DOWNLOAD_SUCCESS);
			intent.putExtra("progress", 100);
			intent.putExtra("file", file);
			sendBroadcast(intent);
		}

		/**
		 * 功能： 回调开始下载的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onStart(int fileByteSize) {
			fileSize = fileByteSize;
			flag = Contants.Flag_Down;
		}

		/**
		 * 功能： 回调正在下载的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onResume() {
			flag = Contants.Flag_Down;
		}

		/**
		 * 功能： 回调更新进度的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onProgress(int receivedBytes) {
			if (flag == Contants.Flag_Down) {
				progress = (int) ((receivedBytes / (float) fileSize) * 100);
				intent.setAction(Contants.ACTION_DOWNLOAD_PROGRESS);
				intent.putExtra("progress", progress);
				sendBroadcast(intent);

				if (progress == 100) {
					flag = Contants.Flag_Done;
				}
			}
		}

		/**
		 * 功能： 回调暂停的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onPause() {
			flag = Contants.Flag_Pause;
		}

		/**
		 * 功能： 回调失败的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onFail() {
			intent.setAction(Contants.ACTION_DOWNLOAD_FAIL);
			sendBroadcast(intent);
			flag = Contants.Flag_Init;
		}

		/**
		 * 功能： 回调停止的方法。
		 * 
		 * @author 作者 郭勇 创建时间：2016年7月26日
		 */
		@Override
		public void onCancel() {
			progress = 0;
			intent.setAction(Contants.ACTION_DOWNLOAD_PROGRESS);
			intent.putExtra("progress", progress);
			sendBroadcast(intent);
			flag = Contants.Flag_Init;
		}
	};

}
