package com.opensesameapp;

import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class LockService extends Service {
	private Intent LockIntent = null;
	public static KeyguardLock mKeyguardLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		IntentFilter onFilter = new IntentFilter(
				"android.intent.action.SCREEN_ON");
		this.registerReceiver(onReceiver, onFilter);
		IntentFilter offFilter = new IntentFilter(
				"android.intent.action.SCREEN_OFF");
		this.registerReceiver(offReceiver, offFilter);
	}

	private BroadcastReceiver onReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
				Log.i("SSSSS",
						"----------------- android.intent.action.SCREEN_ON------");
			}
		}

	};

	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(onReceiver);
		unregisterReceiver(offReceiver);
	}

	private BroadcastReceiver offReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// TODO Auto-generated method stub
			if (action.equals("android.intent.action.SCREEN_OFF")
					|| action.equals("android.intent.action.SCREEN_ON")) {
				try {
					ActivityManager.getScreenManager().popAllActivity();
					LockIntent = new Intent();
					LockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					LockIntent.setClass(context, LockActivity.class);
					context.startActivity(LockIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

}
