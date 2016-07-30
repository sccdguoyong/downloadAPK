package net.sunniwell.download;

import java.io.File;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private EditText mEtPath;
	private Button mBtnStart;
	private Button mBtnPause;
	private Button mBtnStop;
	/** 进度条 */
	private ProgressBar mProBar;
	/** 下载路径 */
	private String url;
	/** 广播接受者 */
	private MyReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mEtPath = (EditText) findViewById(R.id.et_path);
		mBtnStart = (Button) findViewById(R.id.btn_start);
		mBtnPause = (Button) findViewById(R.id.btn_pause);
		mBtnStop = (Button) findViewById(R.id.btn_stop);
		mProBar = (ProgressBar) findViewById(R.id.progress);

		url = mEtPath.getText().toString().trim();
		if (DownloadService.getInstance() != null) {
			mProBar.setProgress(DownloadService.getInstance().getProgress());
		}

		receiver = new MyReceiver();
		mBtnStart.setOnClickListener(this);
		mBtnPause.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
	}

	/**
	 * 功能：界面可见时注册广播接受者。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 * 
	 */
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Contants.ACTION_DOWNLOAD_PROGRESS);
		filter.addAction(Contants.ACTION_DOWNLOAD_FAIL);
		filter.addAction(Contants.ACTION_DOWNLOAD_SUCCESS);
		registerReceiver(receiver, filter);
	}

	/**
	 * 功能 ：界面不可见，取消注册。
	 *
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 * 
	 */
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start:
			start();
			break;
		case R.id.btn_pause:
			pause();
			break;
		case R.id.btn_stop:
			stop();
			break;
		default:
			break;
		}
	}

	/**
	 * 功能 : 开始下载的方法。开启service。
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 */
	private void start() {
		if (DownloadService.getInstance() != null && DownloadService.getInstance().getFlag() != Contants.Flag_Init) {
			Toast.makeText(this, "已经在下载", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("flag", "start");
		intent.putExtra("url", url);
		startService(intent);
	}

	/**
	 * 功能 暂停下载的方法。
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 */
	private void pause() {
		String flag = null;
		int f = DownloadService.getInstance().getFlag();
		if (DownloadService.getInstance() != null) {
			// 如果当前已经暂停，则恢复
			if (f == Contants.Flag_Pause) {
				flag = "resume";
			} else if (f == Contants.Flag_Down) {
				flag = "pause";
			} else {
				return;
			}
		}
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("flag", flag);
		startService(intent);
	}


	/**
	 * 功能 : 停止下载的方法。
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 */
	private void stop() {
		Intent intent = new Intent(this, DownloadService.class);
		intent.putExtra("flag", "stop");
		startService(intent);
		mProBar.setProgress(0);
	}

	/**
	 * 功能 ： 广播接收者
	 *
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 * 
	 */
	class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(Contants.ACTION_DOWNLOAD_PROGRESS)) {
				int pro = intent.getExtras().getInt("progress");
				mProBar.setProgress(pro);

			}  else if (action.equals(Contants.ACTION_DOWNLOAD_SUCCESS)) {
				Toast.makeText(MainActivity.this, getResources().getString(R.string.down_success), Toast.LENGTH_SHORT).show();
				
//				File f = (File) intent.getExtras().getSerializable("file");
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.setDataAndType(Uri.fromFile(f),
//						"application/vnd.android.package-archive");
//				startActivity(i);

			} else if (action.equals(Contants.ACTION_DOWNLOAD_FAIL)) {

				Toast.makeText(MainActivity.this, getResources().getString(R.string.down_fail), Toast.LENGTH_SHORT).show();

			}
		}
	}

	/**
	 * 功能 : 下载中或者暂停的时候，监听返回键。
	 * 
	 * @author 作者 郭勇 创建时间：2016年7月28日
	 */
	@Override
	public void onBackPressed() {
		final int f = DownloadService.getInstance().getFlag();
		if (f == Contants.Flag_Down || f == Contants.Flag_Pause ) {
			new AlertDialog.Builder(this).setTitle("确定退出程序？").setMessage("你有未完成的下载任务").setNegativeButton("取消下载", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					stop();
					MainActivity.super.onBackPressed();
				}
			}).setPositiveButton("后台下载", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					if (f == Contants.Flag_Pause) {
						Intent it = new Intent(MainActivity.this, DownloadService.class);
						it.putExtra("flag", "resume");
						startService(it);
					}

					MainActivity.super.onBackPressed();
				}
			}).create().show();
			return;
		}

		if (DownloadService.getInstance() != null)
			DownloadService.getInstance().stopSelf();
		super.onBackPressed();
	}
}