package com.shu.messystem.plantime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog.Builder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.shu.messystem.MainActivity;
import com.shu.messystem.ManageRetrofit;
import com.shu.messystem.R;
import com.shu.messystem.component.ClearEditText;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.component.TopTips.util.TipUtil;
import com.shu.messystem.result_bean.GetLineBean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanTimeForStopFragementChildModi extends Fragment implements android.view.View.OnClickListener, Runnable{
	private AppCompatActivity parentActivity;// 获取父Activity

	private TextView xiantiData;// 线体列表
	private AlertDialog xiantiChoiceDialog;// 线体选择对话框
	//private String[] xiantiList = new String[4];//4个都必须赋值，否则加载到对话框中时会报错
	private String[] xiantiList;//4个都必须赋值，否则加载到对话框中时会报错
	private int initValue=0;//初始值
	private boolean isRefreshXiantiDialogData=false;//当数据改变时 需要重新创建对话框
	private boolean isRefreshPlanDialogData=false;
	private boolean isDelFromModi=false;
/*	private TextView banciData;//班次列表
	private String[] banciList ;
	private AlertDialog banciChoiceDialog;//班次选择对话框*/

	private TextView planData;//计划列表
	private String[] planList ;
	private AlertDialog planChoiceDialog;//班次选择对话框

	private ClearEditText timeBegin;//时间
	private ClearEditText timeEnd;//时间
	private String oldtimeBegin="";
	private String oldtimeEnd="";
	private Calendar currentDate;// 保存当前时间
	private TextView currentTimeView;//当前是谁点击了 日期选择框
	private TimePickerDialog timeChoiceDialog;
	private SmartRefreshLayout mRefreshLayout;
	private boolean initFinish=false;
	private Button saveBt;
	private Thread saveThread;
	Handler handler = new Handler();
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.plantimeforstop_fragement_child, container, false);
		parentActivity = (AppCompatActivity) getActivity();// 获取Acticity

		xiantiData = (TextView) rootView.findViewById(R.id.name);
		xiantiData.setOnClickListener(this);

		/*banciData = (TextView) rootView.findViewById(R.id.plantime_bancidata);
		banciData.setOnClickListener(this);*/

		planData = (TextView) rootView.findViewById(R.id.dept);
		planData.setOnClickListener(this);

		timeBegin = rootView.findViewById(R.id.ip);
		timeBegin  .setOnClickListener(this);

		timeEnd =  rootView.findViewById(R.id.mac);
		timeEnd .setOnClickListener(this);

		saveBt = (Button) rootView.findViewById(R.id.save);
		saveBt .setOnClickListener(this);
		//读取已存在的信息
		initRefreshControl(rootView);
		mRefreshLayout.autoRefresh();
		isDelFromModi();
		return rootView;
	}
	private void initRefreshControl(View rootView) {
		mRefreshLayout = (SmartRefreshLayout) rootView.findViewById(R.id.refreshLayout);
		//设置 Header 为 Material风格
		//mRefreshLayout.setRefreshHeader(new MaterialHeader(this).setShowBezierWave(true));
		//设置 Footer 为 球脉冲
		mRefreshLayout.setRefreshFooter(new BallPulseFooter(parentActivity).setSpinnerStyle(SpinnerStyle.Scale));

		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh(RefreshLayout refreshlayout) {

				refreshlayout.finishRefresh(20000);//延迟20s
				initData();

				Log.e("load", "refresh");
			}

		});
		mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
			@Override
			public void onLoadmore(RefreshLayout refreshlayout) {
				Log.e("load", "more");
				refreshlayout.finishLoadmore(20000);//延迟20s
			}
		});
	}
	private void initData(){
/*		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//SharedPreferences shared = parentActivity.getSharedPreferences("query", Context.MODE_PRIVATE);
					Connection con = ConnectServer.connectServer(parentActivity);
					Statement statement=con.createStatement();
					String sql="SELECT  LineName ,Bc_Name,Mark FROM PlanTimeForStop";
					ResultSet resultSet=statement.executeQuery(sql);
					HashSet<String> arrayListXinti=new HashSet<String>();//HashSet元素不会重复，自动去掉重复元素
					//HashSet<String> arrayListBanci=new HashSet<String>();
					HashSet<String> arrayListPlan=new HashSet<String>();
					while(resultSet.next()){
						String line=resultSet.getString("LineName");
						String banci=resultSet.getString("Bc_Name");
						String plan=resultSet.getString("Mark");
						if(line!=null){
							arrayListXinti.add(line);
						}
*//*						if(banci!=null){
                        	arrayListBanci.add(banci);
						}*//*
						if(plan!=null){
                       	 arrayListPlan.add(plan);
						}
					}
					xiantiList=arrayListXinti.toArray(new String[]{});
					//banciList=arrayListBanci.toArray(new String[]{});
					planList=arrayListPlan.toArray(new String[]{});

					Log.e("查询成功", "查询成功");
				} catch (ClassNotFoundException | SQLException e) {
					topTip("初始化失败，请重试",0);
					Log.e("SQL", e.getMessage());
					e.printStackTrace();
				}finally {
					initFinish=true;
					mRefreshLayout.finishRefresh();
				}
			}
		}).start();
		PlanTimeForStopFragementMain.modirefresh=false;

		isRefreshPlanDialogData=true;
		isRefreshXiantiDialogData=true;//通知对话框刷新数据*/


		Call<GetLineBean> request = ManageRetrofit.getRetroInter(parentActivity).getPlantime();
		request.enqueue(new Callback<GetLineBean>() {
			@Override
			public void onResponse(Call<GetLineBean> call, Response<GetLineBean> response) {
				if(response.body()!=null){


				String msg = response.body().getMsg();

				if ("success".equals(msg)) {//密码正确
					HashSet<String> arrayListXinti=new HashSet<String>();//HashSet元素不会重复，自动去掉重复元素
					//HashSet<String> arrayListBanci=new HashSet<String>();
					HashSet<String> arrayListPlan=new HashSet<String>();
					List<GetLineBean.DataBean> data = response.body().getData();
					for (int i = 0; i < data.size(); i++) {
						String line=data.get(i).getLineName();
						String banci=data.get(i).getBc_Name();
						String plan=data.get(i).getMark();
						if(line!=null){
							arrayListXinti.add(line);
						}
/*						if(banci!=null){
                        	arrayListBanci.add(banci);
						}*/
						if(plan!=null){
							arrayListPlan.add(plan);
						}
					}
					xiantiList=arrayListXinti.toArray(new String[]{});
					//banciList=arrayListBanci.toArray(new String[]{});
					planList=arrayListPlan.toArray(new String[]{});

					Log.e("查询成功", "查询成功");

				} else {
					topTip("初始化失败，请重试",0);

				}
				}else{
					topTip("初始化失败，请重试",0);
				}
				initFinish=true;
				mRefreshLayout.finishRefresh();

			}

			@Override
			public void onFailure(Call<GetLineBean> call, Throwable t) {
				topTip("初始化失败，请重试",0);
				Log.e("SQL", t.getMessage());
				t.printStackTrace();
				initFinish=true;
				mRefreshLayout.finishRefresh();
			}

		});
		PlanTimeForStopFragementMain.modirefresh=false;

		isRefreshPlanDialogData=true;
		isRefreshXiantiDialogData=true;//通知对话框刷新数据
	}

	private void topTip(final String tip,int status) {
		handler.post(new Runnable() {
			@Override
			public void run() {

				new TipUtil().showTips(parentActivity,MainActivity.toolbar,tip,status);
			}
		});
	}
	// 创建线体选择对话框
	private void createXiantiChoiceDialog() {
		if(!initFinish){
			topTip("正在初始化，请稍后",0);
			return;
		}
		Builder builder;
		if (xiantiChoiceDialog == null ||isRefreshXiantiDialogData ) {
			isRefreshXiantiDialogData=false;
			builder = new AlertDialog.Builder(parentActivity);
			builder.setTitle("线体");
			builder.setSingleChoiceItems(xiantiList, initValue, (DialogInterface dialog, int which) ->{
					String s = xiantiList[which];
					xiantiData.setText(s);
					xiantiChoiceDialog.hide();
					queryCurrentPlanTime();
				}
			);
			xiantiChoiceDialog = builder.create();
		}

		xiantiChoiceDialog.show();
	}
/*	private void createBanciChoiceDialog() {
		Builder builder;
		if(banciChoiceDialog == null) {
			builder=new AlertDialog.Builder(parentActivity);
			builder.setTitle("班次");
			builder.setSingleChoiceItems(banciList , initValue,
					(DialogInterface dialog, int which) ->{
							String s =banciList[which];
							banciData.setText(s);
							banciChoiceDialog.hide();
							queryCurrentPlanTime();
						}
			);
			banciChoiceDialog=builder.create();
		}
		banciChoiceDialog.show();
	}*/
	private void createPlanChoiceDialog() {
		if(!initFinish){
			topTip("正在初始化，请稍后",0);
			return;
		}
		Builder builder;
		if(planChoiceDialog == null || isRefreshPlanDialogData) {
			isRefreshPlanDialogData=false;
			builder=new AlertDialog.Builder(parentActivity);
			builder.setTitle("计划");
			builder.setSingleChoiceItems(planList , initValue,
					(DialogInterface dialog, int which)-> {
						String s = planList[which];
						planData.setText(s);
						planChoiceDialog.hide();
						queryCurrentPlanTime();
					}
			);
			planChoiceDialog=builder.create();
		}
		planChoiceDialog.show();
	}
	// 创建时间选择对话框
	private void createTimePickerDialog() {
		if (timeChoiceDialog == null) {
			currentDate = Calendar.getInstance();
			// int currentHour=currentDate.get(Calendar.HOUR_OF_DAY);
			// int currentMinute=currentDate.get(Calendar.MINUTE);
			timeChoiceDialog = new TimePickerDialog(parentActivity,
				(TimePicker view, int hourOfDay, int minute)-> {// 创建日期选择对话框
					SimpleDateFormat sd = new SimpleDateFormat("HH:mm", Locale.getDefault());

					try {
						currentDate.setTime(sd.parse(hourOfDay + ":" + minute));
						String hour= currentDate.get(Calendar.HOUR_OF_DAY)+"";
						String min = currentDate.get(Calendar.MINUTE)+"";
						if(currentDate.get(Calendar.HOUR_OF_DAY)/10<1) {
							hour="0"+currentDate.get(Calendar.HOUR_OF_DAY);
						}
						if(currentDate.get(Calendar.MINUTE)/10<1) {
							min="0"+currentDate.get(Calendar.MINUTE);
						}
						String timeDisplay = String.format(getResources().getString(R.string.time_string), hour, min);
						currentTimeView.setText(timeDisplay);
					} catch (ParseException e) {
						errorTip("时间设置失败，请重试",0);
						e.printStackTrace();
					}

					timeChoiceDialog.hide();

			}, 12, 0, true);// true代表24小时
		}
		timeChoiceDialog.show();

	}
	private void queryCurrentPlanTime(){//查询当前计划停机时间
/*		new Thread(()-> {
				changeQueryBtStat("查询中,请稍后", false);// 按钮不可用
				// TODO Auto-generated method stub
				//SharedPreferences shared = parentActivity.getSharedPreferences("query", Context.MODE_PRIVATE);
				String sql ;
				try {
					Connection con = ConnectServer.connectServer(parentActivity);
					Statement statement=con.createStatement();
					sql="SELECT StartTime,EndTime FROM  PlanTimeForStop"
									+" WHERE LineName='"+xiantiData.getText()+"'"
									//+" AND Bc_Name='"+banciData.getText()+"'"
									+" AND Mark='"+planData.getText()+"'";
					ResultSet result=statement.executeQuery(sql);
					if (result.next()) {
						oldtimeBegin=result.getString("StartTime");
						oldtimeEnd=result.getString("EndTime");
						handler.post(()-> {
								timeBegin.setText(oldtimeBegin);
								timeEnd.setText(oldtimeEnd);

							}

						);

					}else{
						handler.post(()-> {
						timeBegin.setText("无");
						timeEnd.setText("无");
								}

						);
					}
				} catch (ClassNotFoundException | SQLException e) {
					errorTip("查询当前停机计划失败",0);
					e.printStackTrace();
				}
			changeQueryBtStat("修改", true);// 按钮可用
			}

		).start();*/


	String where=" WHERE LineName='"+xiantiData.getText()+"'"
			//+" AND Bc_Name='"+banciData.getText()+"'"
			+" AND Mark='"+planData.getText()+"'";
		Call<GetLineBean> request = ManageRetrofit.getRetroInter(parentActivity).getPlantime(where);
		request.enqueue(new Callback<GetLineBean>() {
			@Override
			public void onResponse(Call<GetLineBean> call, Response<GetLineBean> response) {
				if(response.body()!=null){


				String msg = response.body().getMsg();

				if ("success".equals(msg)) {//密码正确

					List<GetLineBean.DataBean> data = response.body().getData();
					if (data.size()<=0){
						handler.post(()-> {
									timeBegin.setText("无");
									timeEnd.setText("无");
								}

						);
					}else{
						for (int i = 0; i < data.size(); i++) {
							oldtimeBegin=data.get(i).getStartTime();
							oldtimeEnd=data.get(i).getEndTime();
							handler.post(()-> {
										timeBegin.setText(oldtimeBegin);
										timeEnd.setText(oldtimeEnd);

									}

							);
						}

					}


				} else {
					errorTip("查询当前停机计划失败",0);


				}
				}else{
					errorTip("查询当前停机计划失败",0);
				}
				changeQueryBtStat("修改", true);// 按钮可用

			}

			@Override
			public void onFailure(Call<GetLineBean> call, Throwable t) {
				errorTip("查询当前停机计划失败",0);
				t.printStackTrace();
				changeQueryBtStat("修改", true);// 按钮可用
			}

		});

	}
	public void run() {
		if (Thread.currentThread() == saveThread) {
			modifyData();
		}
	}
private void modifyData() {
	String xianti = (String) xiantiData.getText();
	if (xianti.length() == 0) {
		errorTip("请选择线体",0);
		changeQueryBtStat("修改", true);// 按钮可用
		return;
	}
	String plan = planData.getText().toString();
	if (plan.length() == 0) {
		errorTip("请选择计划停机类型",0);
		changeQueryBtStat("修改", true);// 按钮可用
		return;
	}
	String start = timeBegin.getText().toString();
/*	if (start.length() == 0) {
		errorTip("请选择停机开始时间",0);
		changeQueryBtStat("修改", true);// 按钮可用
		return;
	}*/
	String end =  timeEnd.getText().toString();
/*	if (end.length() == 0) {
		errorTip("请选择停机结束时间",0);
		changeQueryBtStat("修改", true);// 按钮可用
		return;
	}*/
	//判断时间格式
	if(!GetGeneralInfomation.deteDateFormat(start,"HH:mmm")
			||!GetGeneralInfomation.deteDateFormat(start,"HH:mmm")){
		topTip("时间格式不正确",0);
		changeQueryBtStat("修改", true);// 按钮可用
		return;
	}
			changeQueryBtStat("修改中,请稍后", false);// 按钮不可用
		//	SharedPreferences shared = parentActivity.getSharedPreferences("query", Context.MODE_PRIVATE);
		//	String sql ;
			//String banci=(String) banciData.getText();
			String gonghao= MainActivity.gonghao;
			String name=MainActivity.name;
/*			try {
				Connection con = ConnectServer.connectServer(parentActivity);
				Statement statement=con.createStatement();
				sql="UPDATE PlanTimeForStop Set StartTime='"+start+"'"
								+" , EndTime='"+end+"'"
								+" WHERE LineName='"+xianti+"'"
								//+" AND Bc_Name='"+banci+"'"
								+" AND Mark='"+plan+"'"
								+" AND StartTime='"+oldtimeBegin+"'"
								+" AND EndTime='"+oldtimeEnd+"'";
				Log.e("SQL", sql);
				Log.e("SQL", String.valueOf(statement.executeUpdate(sql)));
				sql="INSERT INTO PlanTimeForStopModifyRecord"
								+"  (xianti ,  PlanType , gonghao ,name, oldStartTime,oldEndTime,startTime , endTime)"
				+" VALUES ('"+xianti+"','"+plan+"','"+gonghao+"','"+name+"','"+oldtimeBegin+"','"+oldtimeEnd+"','"+start+"','"+end+"')";
				Log.e("SQL", sql);
				Log.e("SQL", String.valueOf(statement.executeUpdate(sql))); //插入修改记录
				errorTip("修改成功",1);
				PlanTimeForStopFragementMain.delrefresh=true;//设置共享变量，通知删除页面下次打开时刷新

			} catch (ClassNotFoundException | SQLException e) {
				errorTip("修改失败，请重试",0);
				Log.e("SQL", e.getMessage());
				e.printStackTrace();
			}finally {

				if(isDelFromModi){//修改完成后切换回删除页面
					returnDelPage();
				}
				changeQueryBtStat("修改", true);// 按钮可用
			}*/

	HashMap<String,String> map=new HashMap<>();
	map.put("oldtimeBegin",oldtimeBegin);
	map.put("oldtimeEnd",oldtimeEnd);
	map.put("xianti",xianti);
	map.put("plan",plan);
	map.put("start",start);
	map.put("end",end);
	map.put("gonghao",gonghao);
	map.put("name",name);
	Call<GetLineBean> request = ManageRetrofit.getRetroInter(parentActivity).modiPlantime(map);
	request.enqueue(new Callback<GetLineBean>() {
		@Override
		public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
			if(response.body()!=null){


			String msg = response.body().getMsg();

			if ("success".equals(msg)) {//密码正确"success".equals(msg)
				errorTip("修改成功",1);
				PlanTimeForStopFragementMain.delrefresh=true;//设置共享变量，通知删除页面下次打开时刷新

			} else {

				errorTip("修改失败，请重试",0);

			}}else{
				errorTip("修改失败，请重试",0);
			}
			if(isDelFromModi){//修改完成后切换回删除页面
				returnDelPage();
			}
			changeQueryBtStat("修改", true);// 按钮可用
		}

		@Override
		public void onFailure(Call<GetLineBean> call, Throwable t) {
			errorTip("修改失败，请重试",0);
			Log.e("SQL", t.getMessage());
			t.printStackTrace();

			if(isDelFromModi){//修改完成后切换回删除页面
				returnDelPage();
			}
			changeQueryBtStat("修改", true);// 按钮可用
		}

	});


	}
	private void returnDelPage(){
		//获取父fragment，然后获得子fragment 使得同层切换fragment
		android.support.v4.app.FragmentManager fragmentManager=getParentFragment().getChildFragmentManager();
		FragmentTransaction ft=fragmentManager.beginTransaction();

		ft.hide(this);//隐藏当前页面
		//返回删除页面
		Fragment fragment=fragmentManager.findFragmentByTag("0");
		if (fragment != null) {
			ft.show(fragment);//显示修改页面
		}else{
			fragment=new PlanTimeForStopFragementChildDel();
			ft.add(fragment,"0");
		}
		PlanTimeForStopFragementMain.currentFragment=fragment;
		ft.commit();

	}
	private void changeQueryBtStat(final String text,  final boolean enable) {// 改变查询按钮状态
		// 需用handler才能更新主线程界面 按钮颜色和字体
		handler.post(()->{
				saveBt.setText(text);
				//saveBt.setBackgroundColor(Color.parseColor(color));
				saveBt.setEnabled(enable);
			}
		);
	}
	private void errorTip(final String tip,int status) {
			handler.post(()-> {
						new TipUtil().showTips(parentActivity,MainActivity.toolbar,tip,status);
					}
			);
	}
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.name) {
			createXiantiChoiceDialog();
		}/*else if (id == R.id.plantime_bancidata) {
			createBanciChoiceDialog();
		}*/else if (id == R.id.dept) {
			createPlanChoiceDialog();
		}else if (id == R.id.ip) {
			this.currentTimeView=this.timeBegin;
			//createTimePickerDialog();
		}else if (id == R.id.mac) {
			this.currentTimeView=this.timeEnd;
			//createTimePickerDialog();
		}else if (id == R.id.save) {
			saveThread = new Thread(this);
			saveThread.start();
		}

	}
//除第一次创建，后面显示会执行的函数 隐藏hidden为false
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		//如果 删除或者添加了数据，则刷新线体显示和计划显示
		if (PlanTimeForStopFragementMain.modirefresh){
			mRefreshLayout.autoRefresh();
		}
		//读取从删除页面点击修改后传过来的值
		if(!hidden){
			isDelFromModi();
		}
		Log.e("hidden",hidden+"");
	}

	//如果是修改，是否为删除页面点击修改 ，读取传过来的值
	private void isDelFromModi(){
		Bundle bundle=getArguments();
		if(bundle==null){
			return;
		}
		isDelFromModi=true;//修改完成后切换回删除页面
		String[] modiInfo=bundle.getStringArray("modiInfo");
		if(modiInfo==null){
			return;
		}
		xiantiData.setText(modiInfo[0]);
		planData.setText(modiInfo[1]);
		timeBegin.setText(modiInfo[2]);
		timeEnd.setText(modiInfo[3]);
		oldtimeBegin=modiInfo[2];
		oldtimeEnd=modiInfo[3];
	}
	//父fragment 反射调用，标题栏刷新
	public void queryThread(){
		mRefreshLayout.autoRefresh();
	}
}
