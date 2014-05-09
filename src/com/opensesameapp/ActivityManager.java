package com.opensesameapp;

import java.util.Stack;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;

public class ActivityManager {
	private static Stack<Activity> activityStack;
	private static ActivityManager instance;
	private static KeyguardLock mKeyguardLock;

	private ActivityManager() {
	}

	public static void setKeyGuardLock(KeyguardLock lock) {
		mKeyguardLock = lock;
	}

	public static KeyguardLock getKeyGuardLock() {
		return mKeyguardLock;
	}

	public static ActivityManager getScreenManager() {
		if (instance == null) {
			instance = new ActivityManager();
		}
		return instance;
	}

	// �˳�ջ��Activity
	public void popActivity(Activity activity) {
		if (activity != null) {
			// �ڴ��Զ��弯����ȡ����ǰActivityʱ��Ҳ������Activity�Ĺرղ���
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}

	// ��õ�ǰջ��Activity
	public Activity currentActivity() {
		Activity activity = null;
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		if (!activityStack.empty())
			activity = activityStack.lastElement();
		return activity;
	}

	// ����ǰActivity����ջ��
	public void pushActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	// �˳�ջ������Activity
	public void popAllActivityExceptOne(Class cls) {
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			if (activity.getClass().equals(cls)) {
				break;
			}
			popActivity(activity);
		}
	}

	// �˳�ջ������Activity
	public void popAllActivity() {
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			popActivity(activity);
		}
	}
}