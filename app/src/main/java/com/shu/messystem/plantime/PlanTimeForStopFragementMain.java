package com.shu.messystem.plantime;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.shu.messystem.MainActivity;
import com.shu.messystem.R;

import java.lang.reflect.InvocationTargetException;

public class PlanTimeForStopFragementMain extends Fragment implements BottomNavigationBar.OnTabSelectedListener {
	private View rootView;
	public static Fragment currentFragment;//保存当前显示的Fragment  删除界面点击修改后会切换到修改页面，currentFragment也会改变
	public static boolean delrefresh=false;//当添加或者修改数据后，删除界面用该共享变量确定是否刷新
	public static boolean modirefresh=false;//删除或添加数据后，修改界面用该共享变量确定是否刷新
	private Fragment modiFragment;
	private Fragment addFragment;
	private Fragment delFragment;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView=inflater.inflate(R.layout.plantimeforstop_fragement_main, container,false);
		this.rootView=rootView;
		createBottomNavigationBar();//创建底部导航栏

		//step2:Activity被系统回收时，会主动调用onSaveInstance()方法来保存视图层（View Hierarchy），然后通过onRestoreInstanceState()方法来恢复视图层.(activity中两个方法的核心代码如下)
		//step3:所以当Activity通过导航再次被重建时，之前被实例化过的Fragment依然会出现在Activity中
		if(savedInstanceState == null) {
			replaceFragment("0");
	/*		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			currentFragment = new PlanTimeForStopFragementChildModi();
			String currentFragmentTag = "0";
			ft.add(R.id.plantimeforstop_childfragement_frame, currentFragment, currentFragmentTag).commit();*/
		}
		return rootView;
	}
	private void createBottomNavigationBar(){
		BottomNavigationBar bottomNavigationBar=(BottomNavigationBar)rootView.findViewById(R.id.plantimeforstop_bottom_navigation_bar);
		bottomNavigationBar.setAutoHideEnabled(true);//自动隐藏
		bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);//可设置背景颜色
		bottomNavigationBar.setBarBackgroundColor(R.color.white);//背景颜色
		//bottomNavigationBar.setInActiveColor(Color.GRAY);//未选中时的颜色
		bottomNavigationBar.setActiveColor(R.color.blue);//选中时的颜色
		bottomNavigationBar
				.addItem(new BottomNavigationItem(R.drawable.delete,"删除"))
				.addItem(new BottomNavigationItem(R.drawable.add,"增加"))
				.addItem(new BottomNavigationItem(R.drawable.edit,"编辑"))
				.initialise();
		bottomNavigationBar.setTabSelectedListener(this);
	}

	@Override
	public void onTabSelected(int position) {
		Log.i("onTabSelected", String.valueOf(position));
		switch (position) {
			case 0:
				replaceFragment("0");
				break;
			case 1:
				replaceFragment("1");
				break;
			case 2:
				replaceFragment("2");
				break;
		}
	}

	@Override
	public void onTabUnselected(int position) {

	}

	@Override
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
					if(modiFragment==null){
						 modiFragment = new PlanTimeForStopFragementChildDel();
						fragment=modiFragment;
					}

					break;
				case "1":
					if(addFragment==null){
						addFragment = new PlanTimeForStopFragementChildAdd();
					}
					fragment=addFragment;
					break;
				case "2":
					if(delFragment==null){
						delFragment =new PlanTimeForStopFragementChildModi();
					}
					fragment=delFragment;
					break;
			}
			ft.add(R.id.plantimeforstop_childfragement_frame, fragment, position);
		}
		currentFragment = fragment;
		ft.commit();

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
	
