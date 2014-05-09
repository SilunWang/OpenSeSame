package com.opensesameapp;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

public class AnswerQuestionActivity extends Activity {

	EditText Question;
	EditText Answer;
	Button Unlock;
	SharedPreferences sharedperferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_answer_question);
		ActivityManager.getScreenManager().pushActivity(this);
        
		sharedperferences = getSharedPreferences("SETTING_Infos", 0);
		Question = (EditText) findViewById(R.id.question);
		String question = sharedperferences.getString("question", "你在哪儿住啊？");
		Question.setText(question);

		Answer = (EditText) findViewById(R.id.anserfield);
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				if (Answer.getText().toString() != "") {
					Unlock.setClickable(true);
				} else {
					Unlock.setClickable(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
		};
		Answer.addTextChangedListener(watcher);

		Unlock = (Button) findViewById(R.id.answer_unlock);
		OnClickListener Unlock_Listener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String answer = sharedperferences.getString("answer", "68665880");
				if (Answer.getText().toString().equals(answer)) {
					ActivityManager.getScreenManager().popAllActivity();
				} else {
					alertDialogInAnswer();
				}
			}

			private void alertDialogInAnswer() {
				Dialog alertDialog = new AlertDialog.Builder(
						AnswerQuestionActivity.this)
						.setTitle("回答不正确")
						.setMessage("点击“确定”并重试")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).create();
				alertDialog.show();
			}
		};
		Unlock.setClickable(false);
		Unlock.setOnClickListener(Unlock_Listener);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_answer_question, menu);
		return true;
	}
	
	@Override 
    public void onBackPressed() { 
        super.onBackPressed(); 
        ActivityManager.getScreenManager().popActivity(this); 
    } 
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
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
}
