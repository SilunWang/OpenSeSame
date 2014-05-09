package com.opensesameapp;

//重新学习，修改问题，待添加

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class InitialActivity extends Activity {
	private Activity context;
	private Button restudyButton;
	private Button resetQuestionButton;
	private CheckBox openLockingBox;
	private SeekBar thresholdBar;
	private SeekBar senseBar;
	private boolean isInCreate;
	public static ArrayList<Tuple> copyData[] = new ArrayList[4];
	public static double copyDist[][] = new double[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		boolean isFirstRun = isFirstRun();

		if (isFirstRun == true)
		// if(true)
		{
			Intent intent = new Intent(this, WelcomingActivity.class);
			this.startActivity(intent);
			setFirstRun();
			this.finish();
		} else {
			loadSettingView();
			setLockingStatus();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_initial, menu);
		return true;
	}

	private boolean isFirstRun() {
		SharedPreferences openSesamePref = getSharedPreferences(
				Basic.OpenSesamePreference, 0);
		boolean b = openSesamePref.getBoolean("isFirstRun", true);
		if (b == true) {
			return true;
		}
		return false;
	}

	private boolean isLockingOpened() {
		SharedPreferences openSesamePref = getSharedPreferences(
				Basic.OpenSesamePreference, 0);
		boolean b = openSesamePref.getBoolean("isLockingOpened", false);
		if (b == true) {
			return true;
		} else {
			return false;
		}
	}

	private void setFirstRun() {
		SharedPreferences openSesamePref = getSharedPreferences(
				Basic.OpenSesamePreference, 0);
		Editor editor = openSesamePref.edit();
		editor.putBoolean("isFirstRun", false);
		boolean isSuccess = editor.commit();
		return;
	}

	private void loadSettingView() {
		context.setContentView(R.layout.activity_initial);

		restudyButton = (Button) findViewById(R.id.restudyButton);
		openLockingBox = (CheckBox) findViewById(R.id.openLockingBox);
		resetQuestionButton = (Button) findViewById(R.id.resetQuestionButton);

		resetQuestionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context,
						QuestionSettingActivity.class);
				intent.putExtra("isReset", "true");
				context.startActivity(intent);
			}
		});

		openLockingBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton checkBox,
							boolean checked) {
						boolean isSuccess = registerLockingStatus(checked);
						if (isSuccess) {
							SharedPreferences openSesamePref = getSharedPreferences(
									Basic.OpenSesamePreference, 0);
							Editor editor = openSesamePref.edit();
							editor.putBoolean("isLockingOpened", checked);
							editor.commit();
						} else {
							checkBox.setChecked(!checked);
						}
					}

					public boolean registerLockingStatus(boolean isSet) {
						Intent intent = new Intent(context, LockService.class);
						if (isSet) {
							context.startService(intent);
							EnableSystemKeyguard(false);
						} else {
							// Shut off the screen locking app;
							EnableSystemKeyguard(true);
							context.stopService(intent);
						}
						return true;
					}

					public void EnableSystemKeyguard(boolean bEnable) {
					
						if (ActivityManager.getKeyGuardLock() == null) {
							KeyguardManager mKeyguardManager = null;
							KeyguardLock mKeyguardLock = null;
							mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
							mKeyguardLock = mKeyguardManager
									.newKeyguardLock("");
							ActivityManager.setKeyGuardLock(mKeyguardLock);
						}
						if (bEnable) {
							ActivityManager.getKeyGuardLock()
									.reenableKeyguard();
						} else {
							ActivityManager.getKeyGuardLock().disableKeyguard();
						}
					}
				});

		restudyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, SampleShakingActivities.class);
				intent.putExtra("isRestudy", "true");
				copyData = Analysis.finalData;
				Analysis.finalData_length = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						copyDist[i][j] = DTW.DTW_Dist[i][j];
						DTW.DTW_Dist[i][j] = 0;
					}
				}
				context.startActivity(intent);
				finish();
			}
		});
		thresholdBar = (SeekBar)findViewById(R.id.seekBar1);
		thresholdBar.setMax(50);
		thresholdBar.setProgress((int)(100*Analysis.thresholdCoeff - 70));
		thresholdBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				Analysis.thresholdCoeff = (thresholdBar.getProgress() + 70) /100.0;
				Toast.makeText(context, "Threshold:" + Analysis.thresholdCoeff, 1000).show();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
			}
		});
		senseBar = (SeekBar)findViewById(R.id.SeekBar01);
		senseBar.setMax(50);
		senseBar.setProgress((int)(50 - (Analysis.sensibility - 0.5)*25));
		senseBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				Analysis.sensibility = (50 - senseBar.getProgress()) /25.0 + 0.5;
				Toast.makeText(context, "灵敏度:" + senseBar.getProgress(), 1000).show();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void setLockingStatus() {
		boolean isLockingOpened = isLockingOpened();
		openLockingBox.setChecked(isLockingOpened);
	}
}
