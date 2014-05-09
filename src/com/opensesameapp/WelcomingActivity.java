package com.opensesameapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class WelcomingActivity extends Activity {

	private static final int TO_THE_END = 0;// 到达最后一张
	private static final int LEAVE_FROM_END = 1;// 离开最后一张

	private int[] ids = { R.drawable.welcome1, R.drawable.welcome2,
			R.drawable.welcome3, R.drawable.welcome4};
			
	private List<View> guides = new ArrayList<View>();
	private ViewPager pager;
	private ImageView open;
	private ImageView curDot;
	private int offset;// 位移量
	private int curPos = 0;// 记录当前的位置
	
	private Activity context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		context = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);

		for (int i = 0; i < ids.length; i++) 
		{
			ImageView iv = new ImageView(this);
			iv.setImageResource(ids[i]);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT);
			iv.setLayoutParams(params);
			iv.setScaleType(ScaleType.FIT_XY);
			guides.add(iv);
		}
		
		guides.get(3).setOnTouchListener(new View.OnTouchListener() 
		{		
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) 
			{
				Intent intent = new Intent(context, SampleShakingActivities.class);
				context.startActivity(intent);			
				context.finish();
				return false;
			}
		});		

		curDot = (ImageView) findViewById(R.id.cur_dot);
		open = (ImageView) findViewById(R.id.open);
		curDot.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {
					public boolean onPreDraw() {
						offset = curDot.getWidth();
						return true;
					}
				});

		GuidePagerAdapter adapter = new GuidePagerAdapter(guides);
		pager = (ViewPager) findViewById(R.id.contentPager);
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(new OnPageChangeListener() 
		{
			public void onPageSelected(int arg0) 
			{
				moveCursorTo(arg0);
				curPos = arg0;
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			public void onPageScrollStateChanged(int arg0) {
			}
			
		});
	}

	/**
	 * 移动指针到相邻的位置
	 * 
	 * @param position
	 * 		  		指针的索引值
	 * */
	private void moveCursorTo(int position) {
		TranslateAnimation anim = new TranslateAnimation(offset*curPos, offset*position, 0, 0);
		anim.setDuration(300);
		anim.setFillAfter(true);
		curDot.startAnimation(anim);
	}
}