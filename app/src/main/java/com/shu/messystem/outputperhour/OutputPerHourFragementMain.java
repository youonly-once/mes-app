package com.shu.messystem.outputperhour;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.shu.messystem.R;
import com.shu.messystem.plantime.PlanTimeForStopFragementChildAdd;
import com.shu.messystem.plantime.PlanTimeForStopFragementChildDel;
import com.shu.messystem.plantime.PlanTimeForStopFragementChildModi;

import java.lang.reflect.InvocationTargetException;

public class OutputPerHourFragementMain extends Fragment implements BottomNavigationBar.OnTabSelectedListener {
	private View rootView;
	private Fragment currentFragment;
	private Fragment barChartFragment;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView=inflater.inflate(R.layout.outputperhour_fragement_main, container,false);
		this.rootView=rootView;
		createBottomNavigationBar();//创建底部导航栏
		//step2:Activity被系统回收时，会主动调用onSaveInstance()方法来保存视图层（View Hierarchy），然后通过onRestoreInstanceState()方法来恢复视图层.(activity中两个方法的核心代码如下)
		//step3:所以当Activity通过导航再次被重建时，之前被实例化过的Fragment依然会出现在Activity中
		if(savedInstanceState == null) {
			replaceFragment("0");
		}
		return rootView;
	}
	private void createBottomNavigationBar(){
		BottomNavigationBar bottomNavigationBar=(BottomNavigationBar)rootView.findViewById(R.id.outputperhour_bottom_navigation_bar);
		//bottomNavigationBar.setAutoHideEnabled(true);//自动隐藏
		bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);//可设置背景颜色
		bottomNavigationBar.setBarBackgroundColor(R.color.white);//背景颜色
		//bottomNavigationBar.setInActiveColor(R.color.);//未选中时的颜色
		bottomNavigationBar.setActiveColor(R.color.blue);//选中时的颜色
		bottomNavigationBar
				.addItem(new BottomNavigationItem(R.drawable.barchart,"饼图"))
				.addItem(new BottomNavigationItem(R.drawable.piechart2,"柱状图"))
				.addItem(new BottomNavigationItem(R.drawable.tablechart,"明细"))
				.initialise();
		bottomNavigationBar.setTabSelectedListener(this);
	}


	public void onTabSelected(int position) {
		Log.i("onTabSelected", String.valueOf(position));
		switch (position) {
			case 0:
				//replaceFragment("0");
				break;
			case 1:
				//replaceFragment("1");
				break;
			case 2:
				//replaceFragment("2");
				break;
		}
	}


	public void onTabUnselected(int position) {

	}


	public void onTabReselected(int position) {

	}
	public void replaceFragment(String position) {
		FragmentManager fragementM = getChildFragmentManager();
		FragmentTransaction ft = fragementM.beginTransaction();
		Fragment fragment = fragementM.findFragmentByTag(position);
		if (currentFragment != null) {//隐藏当前 fragment
			ft.hide(currentFragment);
		}
		if (fragment != null) {
			ft.show(fragment);
		} else {

			switch (position) {
				case "0":
					if(barChartFragment==null){
						barChartFragment = new OutputPerHourFragementBarChart();
						fragment=barChartFragment;
					}

					break;
				case "1":
					if(barChartFragment==null){
						barChartFragment = new OutputPerHourFragementBarChart();
						fragment=barChartFragment;
					}
					break;
				case "2":
					if(barChartFragment==null){
						barChartFragment = new OutputPerHourFragementBarChart();
						fragment=barChartFragment;
					}
					break;
			}
		}
		currentFragment = fragment;
		ft.add(R.id.outputperhour_childfragement_frame, currentFragment, position).commit();

	}
	//父activity 反射调用，标题栏刷新
	public void queryThread(){
		try {
			@SuppressWarnings("rawtypes")
			Class c = Class.forName(currentFragment.getClass().getName());//获取子类
			c.getDeclaredMethod("queryThread").invoke(currentFragment);//执行query方法刷新Fraement
		} catch (IllegalAccessException e) {
			Log.e("MainActivity","IllegalAccessException");
		} catch (InvocationTargetException e) {
			Log.e("MainActivity","InvocationTargetException");
		} catch (NoSuchMethodException e) {
			Log.e("MainActivity","NoSuchMethodException");
		} catch (ClassNotFoundException e) {
			Log.e("MainActivity","ClassNotFoundException");
		}

	}
}
	
