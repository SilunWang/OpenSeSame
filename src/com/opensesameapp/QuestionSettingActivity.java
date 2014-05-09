package com.opensesameapp;

//不同的Activity是否可以访问名字相同的SharedPreferences？
//替换锁屏程序待完成

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class QuestionSettingActivity extends Activity 
{
	private String[] questionList = new String[]{"我的出生地是哪里？", "我的生日是什么时候？", "我爸爸的生日是什么时候？", "我妈妈的生日是什么时候？", "我最喜欢的人是谁？", "我的身份证号码是多少？"};
	private String questionSelected = null;
	private String answerToQuestion = null;
	private Activity context;
	private EditText answerText = null;
	private Button confirmButton = null;
	
	private boolean isReset;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        context = this;
        isReset = isReset();
        
        setContentView(R.layout.question_setting);
        
        ArrayAdapter<String> questionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, questionList);
        Spinner questionSpinner = (Spinner)findViewById(R.id.questionSpinner);
        questionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionSpinner.setAdapter(questionAdapter);
        questionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() 
        {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int i, long arg3) 
			{
				questionSelected = questionList[i];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{		
			}
		});
        
        answerText = (EditText)findViewById(R.id.answerText);
        
        confirmButton = (Button)findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() 
        {			
			@Override
			public void onClick(View v) 
			{
				answerToQuestion = answerText.getText().toString();
				saveQuestionAndAnswer();
				
				if (isReset == false)
				{
					replaceLockingApp();
					saveLockingStatus();
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, "摇晃指纹学习完成，锁屏程序开启！", duration);
					toast.show();
					
					Intent intent = new Intent(context, InitialActivity.class);
					context.startActivity(intent);
				}
				else
				{
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, "问题设置成功！", duration);
					toast.show();
				}
				context.finish();
				return;
			}
			
			public void saveLockingStatus()
			{
		    	SharedPreferences openSesamePref = getSharedPreferences(Basic.OpenSesamePreference, 0);
		    	Editor editor = openSesamePref.edit();
		    	editor.putBoolean("isLockingOpened", true);	
		    	boolean isSuccess = editor.commit();
			}
			
			public void saveQuestionAndAnswer()
			{
		    	SharedPreferences openSesamePref = context.getSharedPreferences(Basic.OpenSesamePreference, 0);
		    	Editor editor = openSesamePref.edit();
		    	editor.putString("question", questionSelected);	
		    	editor.putString("answer", answerToQuestion);
		    	boolean isSuccess = editor.commit();				
			}
			
			public void replaceLockingApp()
			{
				Intent intent = new Intent(context, LockService.class);
				context.startService(intent);
				EnableSystemKeyguard(false);				
			}
			
			public void EnableSystemKeyguard(boolean bEnable)
			{
		    	KeyguardManager mKeyguardManager=null;
		    	KeyguardLock mKeyguardLock=null; 
		    	
		    	mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);  
		    	mKeyguardLock = mKeyguardManager.newKeyguardLock(""); 
		    	if(bEnable)
		    	{
		    		mKeyguardLock.reenableKeyguard();
		    	}
		    	else
		    	{
		    		mKeyguardLock.disableKeyguard();
		    	}
		    }
		});
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_initial, menu);
        return true;
    }
    
    private boolean isReset()
    {
    	Intent intent = context.getIntent();
    	String isReset = intent.getStringExtra("isReset");
    	if (isReset == null)
    	{    	
    		return false;
    	}
    	else
    	{
    		return true;
    	}
    }
    
}
