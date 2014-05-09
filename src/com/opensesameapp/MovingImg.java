package com.opensesameapp;

import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MovingImg {

	public ImageView img;
	public double speedx;
	public double speedy;
	public int x;
	public int y;
	public int width;
	public int height;
	public int window_w, window_h;

	public MovingImg(ImageView img, int w, int h) {
		this.img = img;
		this.speedx = 0;
		this.speedy = 0;
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
		this.window_w = w;
		this.window_h = h;
	}

	public void move(double x, double y) {
		MarginLayoutParams source = (MarginLayoutParams) img.getLayoutParams();
		double ReP = 0.1;
		double OutP = 0.5;
		double[] Re = { (window_w / 2-img.getWidth()/2 - source.leftMargin) * ReP,
				(window_h / 2 -img.getHeight()/2- source.topMargin) * ReP };
		double[] Out = { x * OutP, (y - 9.8) * OutP };
		speedx += Re[0] + Out[0];
		speedy += Re[1] - Out[1];
		speedx = zuni(speedx);
		speedy = zuni(speedy);
		this.x += 3*speedx;
		this.y += 3*speedy;
		source.setMargins((int) this.x, (int) this.y, 0, 0);
		RelativeLayout.LayoutParams paras = new RelativeLayout.LayoutParams(source);
		img.setLayoutParams(paras);
	}
	
	private double zuni(double speed){
		return speed*0.9;
//		double f = 2;
//		if(speed>0){
//			speed-=f;
//			if(speed<0) speed=0;
//		}else if(speed<0){
//			speed+=f;
//			if(speed>0) speed=0;
//		}
//		return speed;
	}
}
