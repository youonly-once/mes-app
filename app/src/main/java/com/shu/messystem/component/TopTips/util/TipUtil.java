package com.shu.messystem.component.TopTips.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.NotificationCompatBase;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shu.messystem.R;
import com.shu.messystem.component.TopTips.view.TipRelativeLayout;


/**
 * 封装了显示Popupwindow的方法.
 * @author ansen
 * @create time 2015-10-27
 */
public class TipUtil implements TipRelativeLayout.AnimationEndCallback {
	private PopupWindow reportVideoPopwindow;
	private static boolean isShow=true;
	public void showTips(Activity activity, Toolbar actionbar, String tip,int status){
		//定时器，不允许重复显示提示

		if (!isShow){
			return;
		}
		new Handler().postDelayed(()-> {
			isShow=true;

		}, 1500);
		isShow=false;

		int translateHeight=(int) dip2px(activity,52);
		View parent = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
		View popView = LayoutInflater.from(activity).inflate(R.layout.plantimeforstop_fragement_toptip_layout, null);
		int statusBar=getStatusBarHeight(activity);
		reportVideoPopwindow = new PopupWindow(popView,LayoutParams.MATCH_PARENT,translateHeight*2);
		reportVideoPopwindow.showAtLocation(actionbar,Gravity.TOP, 0, -translateHeight*2);
		TipRelativeLayout tvTips=(TipRelativeLayout) popView.findViewById(R.id.rl_tips);

		//消息内容
		TextView msg=tvTips.findViewById(R.id.tip_msg);
		msg.setText(tip);
		if(status==1){//成功消息，不显示图标  背景蓝色
			ImageView tipImg=tvTips.findViewById(R.id.tip_img);
			tipImg.setVisibility(View.GONE);
			tvTips.setBackgroundColor(Color.GREEN);
			//tvTips.setBackgroundColor(Color.GREEN);
			//颜色与状态栏颜色相同时，多下拉一点，区分
			tvTips.setTitleHeight(translateHeight+5);//移动状态栏的高度
		}else{
			tvTips.setTitleHeight(translateHeight);//移动状态栏的高度
		}

		tvTips.setAnimationEnd(this);//设置动画结束监听函数
		tvTips.showTips();//显示提示RelativeLayout,移动动画.
	}
	
	public int getStatusBarHeight(Context context) {
		  int result = 0;
		  int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		  if (resourceId > 0) {
		      result = context.getResources().getDimensionPixelSize(resourceId);
		  }
		  return result;
	}
	
	private  float dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		float result = dpValue * scale + 0.5f;
		return result;
	}
	
	@Override
	public void onAnimationEnd() {
		reportVideoPopwindow.dismiss();//动画结束，隐藏popupwindow
	}
}
