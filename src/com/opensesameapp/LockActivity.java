package com.opensesameapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.opensesameapp.R.id;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

public class LockActivity extends Activity implements SensorEventListener {
	final int DEFAULT_PANELTY_TIME = 5;
	final int DEFAULT_REMAIN_TIME = 3;
	private SensorManager sensorManager = null;
	private ArrayList<Tuple> sensorData = new ArrayList<Tuple>();
	ImageButton Backdoor;
	Handler ReminderHandler;
	Handler homeHandler;
	int RemainTime;
	TextView RemindTextView;
	int PaneltyTime;
	public MovingImg hand;
	public ImageView denyimg;
	Context context;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock);
		ActivityManager.getScreenManager().pushActivity(this);
		// 其他参数
		RemainTime = DEFAULT_REMAIN_TIME;
		context = this;
		// 后门按钮，按完之后直接退出
		Backdoor = (ImageButton) findViewById(R.id.backdoor);
		OnClickListener Backdoor_Listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorData.clear();
				Intent intent = new Intent();
				intent.setClass(LockActivity.this, AnswerQuestionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		};
		Backdoor.setOnClickListener(Backdoor_Listener);

		// 提示栏信息
		RemindTextView = (TextView) findViewById(R.id.Reminder);
		if (getIntent().getSerializableExtra("panelty") != null) {
			Object o = getIntent().getSerializableExtra("panelty");
			PaneltyTime = (Integer) (getIntent()
					.getSerializableExtra("panelty"));
			RemindTextView.setText(PaneltyTime + "秒后重试");
		} else {
			if (RemainTime == DEFAULT_REMAIN_TIME) {
				RemindTextView.setText("");
			} else {
				RemindTextView.setText("晃动不被认可，你还有" + RemainTime + "次尝试机会");
			}
		}

		// handeler
		ReminderHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x1111111) {
					
					RemindTextView.setText("");
					redrawReminder();
				} else if (msg.what == Basic.ILLEGAL_SAMPLE) {
					
					sensorData.clear();
					Analysis.startAnotherShake();
					Toast.makeText(context, "TIME TOO SHORT", 500).show();
					TipHelper.Vibrate(LockActivity.this, 300);
					RemindTextView.setText("您的摇晃时间太短，请重新摇晃");
					
				}else if (msg.what == Basic.UNMATCHED) {
					sensorData.clear();
					Analysis.reduction();
					RemainTime--;
					TipHelper.Vibrate(LockActivity.this, 300);
					Analysis.startAnotherShake();
					if (RemainTime == 0) 
					{
						RemindTextView.setText(DEFAULT_PANELTY_TIME + "秒后重试");
						// 关闭其他功能，不能监听摇一摇
						new Thread() {
							int Panelty = DEFAULT_PANELTY_TIME;
							public void run() 
							{
								while (Panelty > 0) 
								{
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Panelty--;
									PaneltyTime = Panelty;
									Message message = new Message();
									message.what = 0x1111111;
									ReminderHandler.sendMessage(message);
								}
								RemainTime = DEFAULT_REMAIN_TIME;
								// 打开其他的功能，开始监听摇一摇
								Message message = new Message();
								message.what = 0x1111111;
								ReminderHandler.sendMessage(message);
								sensorData.clear();
							}
						}.start();
						alertDialogInLock();

					} else {
						denyimg.setVisibility(0x00000000);
						redrawReminder();
					}
				} else if (msg.what == Basic.MATCHED) {
					sensorData.clear();
					RemainTime = 0;
					PaneltyTime = 0;
					ActivityManager.getScreenManager().popAllActivity();
					Analysis.finalizeAnalyze();
				}
				
			}
		};
		Handler handler = new Handler() {
		    public void handleMessage(Message msg) {
		    	if (msg.what == 1111) {
		    		Toast.makeText(context, "HomeKey is pressed", 2000).show();
				}
		    };  
		};
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		hand = new MovingImg((ImageView) findViewById(R.id.handOnLock), dm.widthPixels, dm.heightPixels);
		denyimg = (ImageView)findViewById(id.denyicon);
		denyimg.setVisibility(0x00000004);
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		Analysis.init(this, ReminderHandler);
		Analysis.initSamples();
		//listenerHome();
	}

	private void redrawReminder() {
		// 提示栏信息
		if (RemainTime <= 0) {
			if (PaneltyTime <= 0) 
				RemindTextView.setText("请重新摇晃");
			else
				RemindTextView.setText(PaneltyTime + "秒后重试");
		} else if(RemainTime < 3){
			RemindTextView.setText("密码错误！还有" + RemainTime + "次机会");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_lock, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		//Analysis.init(this, ReminderHandler);
		//Analysis.initSamples();
		sensorData.clear();
	}

	@Override
	public void onWindowFocusChanged(boolean paramBoolean) {
		super.onWindowFocusChanged(paramBoolean);
		try {
			Object localObject = getSystemService("statusbar");
			Class.forName("android.app.StatusBarManager")
					.getMethod("collapse", new Class[0])
					.invoke(localObject, new Object[0]);
			return;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}
	/*@Override 
	protected void onUserLeaveHint() {
		Intent i = new Intent(this, LockActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		super.onUserLeaveHint(); 
	}*/

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent e) {
		if (this.PaneltyTime > 0)
			return;
		float x = e.values[sensorManager.DATA_X];
		float y = e.values[sensorManager.DATA_Y];
		float z = e.values[sensorManager.DATA_Z];

		float[] tuple = new float[3];
		tuple[0] = x;
		tuple[1] = y;
		tuple[2] = z;
		hand.move(x, y);
		sensorData.add(new Tuple(x, y, z));

		if (sensorData.size() == 300) {
			denyimg.setVisibility(0x00000004);
			Analysis.receiveData(sensorData, 1);
			sensorData.clear();
		}
	}
	
	private void alertDialogInLock() {
		Dialog alertDialog = new AlertDialog.Builder(LockActivity.this)
				.setTitle("晃动不匹配")
				.setMessage("是否生病了？")
				.setPositiveButton("重试", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setNegativeButton("下一步",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent();
								intent.setClass(LockActivity.this, AnswerQuestionActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
								startActivity(intent);
							}
						}).create();
		alertDialog.show();
	}

	@Override
	public void onAttachedToWindow() {
		//屏蔽home键，仅适用于Android 2.3以下
		//this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);        
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			return true;
		case KeyEvent.KEYCODE_BACK:
			return true;
		case KeyEvent.KEYCODE_CALL:
			return true;
		case KeyEvent.KEYCODE_SYM:
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			return true;
		case KeyEvent.KEYCODE_STAR:
			return true;
		case KeyEvent.KEYCODE_MENU:
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void listenerHome() {  
        new Thread(new Runnable() {  
            public void run() {  
                Process mLogcatProc = null;
                BufferedReader reader = null;  
                try {  
                    /* 获取logcat信息 
                     * logcat:日志类型为logcat 
                     * ActivityManager日志Tag为ActivityManager 
                     * I:<span style="font-size:16px;">logcat的标识分为：（V</span> ：Verbose<span style="font-size:16px;"> D </span>：Debug<span style="font-size:16px;"> I</span> ：Info<span style="font-size:16px;"> W</span> ：Warning<span style="font-size:16px;"> E</span> ：Error<span style="font-size:16px;">  
                                    * F</span> ：Fatal<span style="font-size:16px;"> S</span> ：Silent）这里I代表Info。 
                     * *:S:日志的所有内容 
                     */  
                    mLogcatProc = Runtime.getRuntime().exec(  
                            new String[] { "logcat", "ActivityManager:I *:S" });  
  
                    reader = new BufferedReader(new InputStreamReader(  
                            mLogcatProc.getInputStream()));  
                      
                    String line;                    
                    while ((line = reader.readLine()) != null) {  
                        if (line.indexOf("android.intent.category.HOME") > 0) {  
                        		Message msg = new Message();
                        		msg.what = 1111;
                                homeHandler.sendMessage(msg);
                                Runtime.getRuntime().exec("logcat -c");  
                                break;  
                        }  
                    }  
  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }).start();  
    }
}
