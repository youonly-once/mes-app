package com.shu.messystem.component.TopTips.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import java.util.Timer;

/**
 * 自定义RelativeLayout  显示提示信息,显示时有动画效果(从上面弹出,然后改变透明度慢慢隐藏)
 * @author ansen
 * @create time 2015-10-20
 */
public class TipRelativeLayout extends RelativeLayout{
	private static final int START_TIME=200;//动画显示时间
	private static final int END_TIME=200;//动画移出时间
	private static final int SHOW_TIME=1000;//动画显示时间

	private AnimationEndCallback animationEnd;
	private int titleHeight=100;//标题栏默认的高度设置成100

	public TipRelativeLayout(Context context) {
		super(context);
	}
	
	public TipRelativeLayout(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
	}

	public TipRelativeLayout(Context context, AttributeSet paramAttributeSet,int paramInt) {
		super(context, paramAttributeSet, paramInt);
	}
	
	public void showTips(){

		setVisibility(View.VISIBLE);
		
		//向下移动动画
		TranslateAnimation downTranslateAnimation=new TranslateAnimation(0,0,0,titleHeight);
		downTranslateAnimation.setDuration(START_TIME);
		downTranslateAnimation.setFillAfter(true);
		
		startAnimation(downTranslateAnimation);
		
		//动画监听
		downTranslateAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation){//向下移动动画结束
				topTranslateAnimation();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
	}
	
	private void topTranslateAnimation(){
		new Handler().postDelayed(new Runnable() {//延时1秒之后再向上移动
			@Override
			public void run(){
				
				//向上移动动画
				TranslateAnimation topTranslateAnimation=new TranslateAnimation(0,0,titleHeight,0);
				topTranslateAnimation.setDuration(END_TIME);
				topTranslateAnimation.setFillAfter(true);
				
				//改变透明度
				AlphaAnimation alphaAnimation=new AlphaAnimation(1,0);
				alphaAnimation.setDuration(END_TIME);
				
				//两个动画添加到动画集合中
				AnimationSet animationSet=new AnimationSet(true);
				animationSet.addAnimation(topTranslateAnimation);
				animationSet.addAnimation(alphaAnimation);
				
				startAnimation(animationSet);//开启动画
				
				animationSet.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationRepeat(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation){//动画结束隐藏提示的TextView
						setVisibility(View.GONE);
						if(animationEnd!=null){
							animationEnd.onAnimationEnd();
						}
					}
				});
			}
		},SHOW_TIME);
	}

	/**
	 * 设置标题栏高度
	 * @param titleHeight
	 */
	public void setTitleHeight(int titleHeight) {
		this.titleHeight = titleHeight;
	}
	
	public void setAnimationEnd(AnimationEndCallback animationEnd) {
		this.animationEnd = animationEnd;
	}
	
	/**
	 * 动画结束监听函数
	 * @author apple
	 */
	public interface AnimationEndCallback{
		public void onAnimationEnd();
	}
}
