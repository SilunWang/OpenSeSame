package com.opensesameapp;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SampleShakingActivities extends Activity implements
		SensorEventListener {
	//每次传送给Analyzer的数据长度
	private static final int DEFAULT_TRASNSMIT_DATA_AMOUNT = 150;
	private SampleShakingActivities context;
	private SensorManager sensorManager = null;
	private ArrayList<Tuple> sensorData = new ArrayList<Tuple>();
	private TextView dataGroupText;
	private AnalyzingDialog dialog;
	private TextView tipsText;
	private int groupNum = 0;
	private int sampleStatus = Basic.InitialSampling;
	private boolean isSampling = false;
	public MovingImg hand;
	boolean firstSample = true;//初次摇晃

	private Handler msgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//摇晃成功，但还没结束
			if (msg.what == Basic.SUCCESS_AND_CONTINUE) {
				Toast.makeText(context, "SUCCESS_AND_CONTINUE", Toast.LENGTH_SHORT).show();
				groupNum = 0;
				alertDialogForFix();
				isSampling = false;
				Analysis.startAnotherShake();
				sensorData.clear();
				if(firstSample)
					tipsText.setText("摇晃采集成功，请继续摇动");
				else
					tipsText.setText("采集仍未结束，请重新摇动。");
				firstSample = false;
			}
			else if (msg.what == Basic.UNMATCHED) {
				Toast.makeText(context, "DATA_UNMATCHED", Toast.LENGTH_SHORT).show();
				TipHelper.Vibrate(SampleShakingActivities.this, 250);
				alertDialogForUnmatched();
				isSampling = false;
				sensorData.clear();
				Analysis.startAnotherShake();
				Analysis.reduction();
				tipsText.setText("与上一次摇晃差异较大，请重新摇动");
			}
			else if (msg.what == Basic.ILLEGAL_SAMPLE) {
				Toast.makeText(context, "ILLEGAL_SAMPLE", Toast.LENGTH_SHORT).show();
				TipHelper.Vibrate(SampleShakingActivities.this, 250);
				groupNum = 0;
				alertDialogForILLegal();
				isSampling = false;
				Analysis.startAnotherShake();
				sensorData.clear();
				firstSample = false;
			}
			else if (msg.what == Basic.SUCCESS_AND_OVER) {
				Toast.makeText(context, "SUCCESS_AND_OVER", Toast.LENGTH_SHORT).show();
				alertDialogForCont();
				isSampling = false;
				sensorManager.unregisterListener(context);
				sensorData.clear();
				tipsText.setText("采集数据成功，数据匹配");
				firstSample = true;
			}
		}
	};
	private void alertDialogForILLegal() {
		Dialog alertDialog = new AlertDialog.Builder(context).setTitle("晃动无效可能时间太短")
				.setMessage("请继续摇晃采集")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//记录数据
						isSampling = true;
					}
				}).create();
		alertDialog.show();
	}
	private void alertDialogForFix() {
		Dialog alertDialog = new AlertDialog.Builder(context).setTitle("晃动有效但还没结束")
				.setMessage("请继续摇晃采集")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//记录数据
						isSampling = true;
					}
				}).create();
		alertDialog.show();
	}
	
	private void alertDialogForUnmatched() {
		Dialog alertDialog = new AlertDialog.Builder(context).setTitle("与此前摇晃数据不匹配")
				.setMessage("请重新采集")
				.setPositiveButton("放弃本次数据", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//记录数据
						isSampling = true;
						Analysis.finalData_length--;
					}
				})
				.setNegativeButton("清空所有数据",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								isSampling = true;
								ArrayList<Tuple> listTemp = Analysis.finalData[Analysis.finalData_length - 1];
								Analysis.finalData_length = 1;
								Analysis.finalData[0] = listTemp;
							}
						}).create();
		alertDialog.show();
	}
	
	private void alertDialogForCont() {
		Dialog alertDialog = new AlertDialog.Builder(context)
				.setTitle("晃动有效且匹配").setMessage("是否记录这个样本？")
				.setNegativeButton("下一步",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//写入preference的XML文件中
								Analysis.storeCurrentPDF();
								Analysis.writeToShare();
								Intent intent = new Intent(context, QuestionSettingActivity.class);
								context.startActivity(intent);
								context.finish();
							}
						}).create();
		alertDialog.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.sample_shaking);

		dataGroupText = (TextView) findViewById(R.id.dataGroupText);
		tipsText = (TextView) findViewById(R.id.tipsText);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		hand = new MovingImg((ImageView) findViewById(R.id.hand), dm.widthPixels, dm.heightPixels);

		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);

		Analysis.init(context, msgHandler);
		isSampling = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_initial, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent e) {
		if (!isSampling)
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

		if (sensorData.size() == DEFAULT_TRASNSMIT_DATA_AMOUNT) {
			sendDataToAnalyzer();
			sensorData.clear();
		}
	}

	private void sendDataToAnalyzer() {
		Analysis.receiveData(sensorData, sampleStatus);
	}

	@Override
	public void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Analysis.finalData_length != 3) {
			Toast.makeText(getApplicationContext(), "Pause-采集尚未结束", 1000).show();
			Analysis.initSamples();
		}
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);

		Analysis.init(this, msgHandler);
		sensorData.clear();
	}
}
