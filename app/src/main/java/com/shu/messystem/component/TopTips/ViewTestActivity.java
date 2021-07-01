package com.shu.messystem.component.TopTips;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.shu.messystem.R;
import com.shu.messystem.component.TopTips.view.TipTextView;


/**
 * 
 * @author ansen
 * @create time 2015-10-20
 */
public class ViewTestActivity extends Activity implements OnGlobalLayoutListener{
	private TipTextView tvTips;//提示
	private TextView tvTitle;//标题
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_test);
		
		//tvTips=(TipTextView) findViewById(R.id.tv_tips);
		//tvTitle=(TextView) findViewById(R.id.tv_title);
		
		//tvTitle在一个视图树中的焦点状态发生改变时，注册回调接口来获取标题栏的高度
		ViewTreeObserver vto = tvTitle.getViewTreeObserver();   
		vto.addOnGlobalLayoutListener(this);
		
		//findViewById(R.id.btn_show_tip).setOnClickListener(clickListener);
	}
	
	private OnClickListener clickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			//case R.id.btn_show_tip://显示提示
			//	tvTips.showTips();
			//	break;
			}
		}
	};
	
	@SuppressLint("NewApi")
	@Override
	public void onGlobalLayout() {
		tvTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);//删除监听
		tvTips.setTitleHeight(tvTitle.getHeight());//把标题栏的高度赋值给自定义的TextView
	}
}
