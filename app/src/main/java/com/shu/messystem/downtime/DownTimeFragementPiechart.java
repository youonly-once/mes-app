package com.shu.messystem.downtime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.shu.messystem.ConnectServer;
import com.shu.messystem.LoginActivity;
import com.shu.messystem.MainActivity;
import com.shu.messystem.R;
import com.shu.messystem.component.TopTips.util.TipUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class DownTimeFragementPiechart extends Fragment implements OnTouchListener, Runnable,  OnClickListener, OnCheckedChangeListener, android.widget.CompoundButton.OnCheckedChangeListener {
	private  AppCompatActivity parentActivity;//获取父Activity
	private Thread queryThread;//查询数据的线程
	private TextView dateBegin,dateEnd;//日期选择框
	private TextView dateTextSet;  //用于间接设置日期框值
	private DatePickerDialog dateChoiceDialog;//日期选择对话框 点击dateBegin弹出
	private Calendar currentDate;//保存当前时间

	private TextView banciData;//班次列表
	private String[] banciList = {"白班(8:00)","夜班(20:00)"};
	private boolean[] banciIsChoice = {true,false};//保存线体选择状态  ，会随着选择 变化
	private AlertDialog banciChoiceDialog;//班次选择对话框

	private TextView xiantiData;//线体列表
	private String[] xiantiList = {"PH","PJ","PK","TN"};
	private boolean[] xiantiIsChoice = {true,false,false,false};//保存线体选择状态  ，会随着选择 变化
	private AlertDialog xiantiChoiceDialog;//线体选择对话框

	private Button query_bt;//查询按钮
	private int queryBtStat=1;//防止查询太频繁


	private boolean queryTableData=true;//点击汇总按钮时 不更新表格数据

	private int collectType=0;//保存汇总类型
	//汇总类型  责任人 部门 线体  一级分类原因
	private String collectString[]=new String[]{"SolveMan","DepartMent","FLineNo","StopTypeFist"};


	private boolean showLabel=false;//不显示标签
	private PieChart pieChart;//柱状图对象


	private ArrayList<TableRow> rowList=new ArrayList<>(); //数据表格的24行
	private ArrayList<TextView> columnList=new ArrayList<>();//数据表格的存储单元格 24*3
	private TableLayout tableData; //存放数据的TableLayout

	private String[][] queryDataDetail=new String[100][7]//保存每一条停机数据
			,queryDataCollect=new String[50][2];//保存分类汇总数据
	private int stopTimeCount=0;//保存每次查询的停机 数量 元祖个数

	private Connection con = null; //保存连接服务器后的对象
	private Statement statement=null;
	private SharedPreferences shared;//获取 存储对象  用于查找存储在本地的服务器地址
	
	Handler handler=new Handler();//用于子线程刷新主界面
	private View rootView;
	private DisplayMetrics dm ;//获取屏幕宽高
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView=inflater.inflate(R.layout.downtime_fragement_piechart, container,false);
		this.rootView=rootView;
		//获取上次 保存的值
		
		String dateBeginCurr,dateEndCurr,banciText="大白班(8:00)",xiantiText="PH";
		if(savedInstanceState!=null) {
			dateBeginCurr=savedInstanceState.getString("DATE_BEGIN");
			dateEndCurr=savedInstanceState.getString("DATE_END");
			
			banciIsChoice=savedInstanceState.getBooleanArray("BANCI_ISCHOICE");
			banciText=savedInstanceState.getString("BANCI_TEXT");
			
			xiantiIsChoice=savedInstanceState.getBooleanArray("XIANTI_ISCHOICE");
			xiantiText=savedInstanceState.getString("XIANTIDATA_TEXT");
		}else {
			currentDate=Calendar.getInstance();
			dateBeginCurr=currentDate.get(Calendar.YEAR)+"-"+(currentDate.get(Calendar.MONTH)+1)+"-"+currentDate.get(Calendar.DAY_OF_MONTH);
			currentDate.add(Calendar.DAY_OF_MONTH, 1);//1天后
			dateEndCurr=currentDate.get(Calendar.YEAR)+"-"+(currentDate.get(Calendar.MONTH)+1)+"-"+currentDate.get(Calendar.DAY_OF_MONTH);
		}
		
		initCreateView(dateBeginCurr,dateEndCurr,banciText,xiantiText);

		//initDetailTable();//创建显示详细信息的表格
		
		queryThread();
		
		Log.e("OutputPerHourFragement","onCreateView()");
		return rootView;
	}
	@SuppressLint("ClickableViewAccessibility")
	private void initCreateView(String dateBeginCurr, String dateEndCurr, String banciText, String xiantiText) {
		parentActivity=(AppCompatActivity)getActivity();//获取Acticity
		
		dateBegin= rootView.findViewById(R.id.downtime_datebegin);
		dateBegin.setOnClickListener(this);
		dateBegin.setText(dateBeginCurr);//设置为当前日期
		
		dateEnd= rootView.findViewById(R.id.downtime_dateend);
		dateEnd.setOnClickListener(this);
		dateEnd.setText(dateEndCurr);//设置为当前日期
		
		banciData= rootView.findViewById(R.id.downtime_banci);
		banciData.setOnClickListener(this);
		banciData.setText(banciText);
		
		xiantiData= rootView.findViewById(R.id.downtime_xiantidata);
		xiantiData.setOnClickListener(this);
		xiantiData.setText(xiantiText);
		
		query_bt= rootView.findViewById(R.id.downtime_query_bt);
		query_bt.setOnTouchListener(this);//按钮触摸事件



		RadioGroup collectRadioGroup= rootView.findViewById(R.id.collect_radiogroup);
		collectRadioGroup.setOnCheckedChangeListener(this);//单选按钮选择事件
		CheckBox showLabelCheckBox;//标签隐藏与显示按钮
		showLabelCheckBox= rootView.findViewById(R.id.show_label_check);
		showLabelCheckBox.setOnCheckedChangeListener(this);//单选按钮选择事件
		
		
		pieChart= rootView.findViewById(R.id.downtime_piechart);
		
		tableData= rootView.findViewById(R.id.downtime_table_data);//存放数据
		
		
		
		//获取屏幕宽度 /高度
		dm = new DisplayMetrics();
		parentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
	}

	//第一次启动时 和选择完日期是 用该线程查询数据
	public void queryThread() {
		//暂时关闭此功能 20190820
		//queryThread=new Thread(this);
		//queryThread.start();
	}
	public void run() {
		if(Thread.currentThread()==queryThread) {
			
		changeQueryBtStat("查询中,请稍后",false);//按钮不可用
		
				if(queryData()) {
					handler.post(()-> {
							updateCollectData();
							if(queryTableData) {
								updateDetailData();
							}
							}
						);
				}
				
		changeQueryBtStat("查询",true);//按钮可用
		}
		
	}

	//查询汇总停机数据
	private synchronized boolean queryData() {
		//获取选择的班次
			currentDate = Calendar.getInstance();//当前时间
			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			String dateBeginText,dateEndText;
			dateBeginText = dateBegin.getText().toString()+" 8:00";// 开始时间
			int year,month,day,hour,addCount=0;
			try {
				currentDate.setTime(sd.parse(dateBeginText));
			} catch (ParseException e1) {
				popupTipInfo("时间初始化失败",0);
				Log.e("DownTimeParseException", e1.getMessage() + dateBeginText);
				return false;
			}
			if(banciIsChoice[0] && banciIsChoice[1]) {//所有
				addCount=24;	
			}else if(banciIsChoice[0]) {//白班
				addCount=12;
			}else if(banciIsChoice[1]) {//夜班
				dateBeginText = dateBegin.getText().toString()+" 20:00";// 开始时间
			}
			currentDate.add(Calendar.HOUR_OF_DAY, addCount);// 增加24小时
			year =currentDate.get(Calendar.YEAR);
			month=currentDate.get(Calendar.MONTH)+1;//月从0开始
			day=currentDate.get(Calendar.DAY_OF_MONTH);
			hour=currentDate.get(Calendar.HOUR_OF_DAY);
			dateEndText  = year + "-" + month + "-" + day + " " + hour+ ":00";	

		// 获取选择的线体线体
			String line1 = "", line2 = "", line3 = "", line4 = "";
			if (xiantiIsChoice[0]) {
				line1 = "重庆滚筒总装一线";
			}
			if (xiantiIsChoice[1]) {
				line2 = "重庆滚筒总装二线";
			}
			if (xiantiIsChoice[2]) {
				line3 = "重庆滚筒总装三线";
			}
			if (xiantiIsChoice[3]) {
				line4 = "重庆滚筒总装五线";
			}
		
		String collectStr=collectString[collectType];//获取汇总类型;
		ResultSet result ;// 保存查询结果
		String sql ;//保存查询语句
		try {
			if (!detectionConnect()) {
				return false;
			}
			
			//查询汇总数据
			sql = "select "+collectStr+",SUM(TimeForStop) as collectStopTime from ReportForStopTime where "
					+ "OfflineTimeCurrent>'"+ dateBeginText + "' "
					+ "and OfflineTimeInfo<='" + dateEndText + "' "
					+ "and (FLineNo='" + line1+"' "
						+ "OR FLineNo='" + line2 + "'  "
						+ "OR FLineNo='" + line3 + "' "
					+ "OR FLineNo='" + line4 + "') "
					+ " GROUP BY "+collectStr;
			result = statement.executeQuery(sql);
			int k = 0;
			for (; result.next(); k++) {
				queryDataCollect[k][0] = result.getString(collectStr);// 汇总 字符串
				queryDataCollect[k][1] = result.getString("collectStopTime"); // 汇总时间
			}

			for (int l = k; l < 50; l++) {// 清除 上次 查询 的值
				queryDataCollect[l][0] = null;
				queryDataCollect[l][1] = null;
			}
			if(queryTableData) {//
				//查询每条停机时数据
				sql = "select Top 100 FLineNo,OfflineTimeInfo,OfflineTimeCurrent,TimeForStop,StopTypeFist,StopReason,SolveMan from ReportForStopTime where "
						+ "OfflineTimeInfo>'"+ dateBeginText + "' "
						+ "and OfflineTimeInfo<='" + dateEndText + "' "
						+ "and (FLineNo='" + line1+"' "
							+ "OR FLineNo='" + line2 + "'  "
						+ "OR FLineNo='" + line3 + "' "
						+ "OR FLineNo='" + line4 + "') "
						+ "ORDER BY TimeForStop DESC";

				result = statement.executeQuery(sql);
				String xianti;
				for (stopTimeCount=0; result.next();) {
					xianti=result.getString("FLineNo");
					if(xianti.equals("重庆滚筒总装一线")) {// 线体
						xianti="PH";
					}
					if(xianti.equals("重庆滚筒总装二线")) {// 线体
						xianti="PJ";
					}
					if(xianti.equals("重庆滚筒总装三线")) {// 线体
						xianti="PK";
					}
					if(xianti.equals("重庆滚筒总装五线")) {// 线体
						xianti="TN";
					}
					int stoptime=Integer.parseInt(result.getString("TimeForStop"))/60;
					if(stoptime<5){
						continue;
					}
					queryDataDetail[stopTimeCount][0] = xianti;
					queryDataDetail[stopTimeCount][1] = String.valueOf(stoptime); // 停机时间
					queryDataDetail[stopTimeCount][2] = result.getString("StopTypeFist");// 停机一级类别
					queryDataDetail[stopTimeCount][3] = result.getString("SolveMan");// 责任人
					queryDataDetail[stopTimeCount][4] = result.getString("StopReason"); // 停机原因
					queryDataDetail[stopTimeCount][5] = result.getString("OfflineTimeInfo"); // 停机开始时间
					queryDataDetail[stopTimeCount][6] = result.getString("OfflineTimeCurrent");// 停机结束时间
					stopTimeCount++;
				}
			}
			return true;

		} catch (SQLException e) {
			popupTipInfo("查询过程出错",0);
			Log.e("DownTimeSQLException", e.getMessage() + dateBeginText);
		}
		try {
			if(con!=null) {
				con.close();
			}
			if(statement!=null){
				statement.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}
	private void initDetailTable() {
		//建立数据表格的行和列
		TableRow row;//行
		TextView columnText ;//单元格
		for(int i=rowList.size()-1;i<stopTimeCount;i++) {
			row=new TableRow(parentActivity);
			row.setBackgroundColor(Color.parseColor("#FFFFFF"));
			rowList.add(row);//添加进 行 队列
			for(int j=0;j<7;j++) {
				columnText=new TextView(parentActivity);
				columnText.setGravity(Gravity.CENTER);
				columnText.setWidth(dm.widthPixels/3);
				if(i%2==1)
					columnText.setBackgroundColor(Color.parseColor("#E0FFFF"));
				row.addView(columnText);//单元格 加入行
				columnList.add(columnText);//保存 单元格对象
			}
			tableData.addView(row,new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));//行加入表格
		}
	}
	//汇总选项单选按钮组  从1开始
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(group.getId() == R.id.collect_radiogroup) {
			switch (checkedId) {
			case 1:  //责任人
				collectType=0;
				break;
			case 2:	//部门
				collectType=1;
				break;
			case 3: //线体
				collectType=2;
				break;
			case 4: //原因
				collectType=3;
				break;
			}
			queryTableData=false;
			queryThread();
		}
		
	}
	//标签显示 复选按钮
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView.getId() == R.id.show_label_check) {
			showLabel=isChecked;//false隐藏饼图文字 true显示饼图文字
			queryThread();
		}
		
	}
	private void updateDetailData() {
		//填充表格数据   需用handler才能更新主线程界面
		initDetailTable();
		handler.post(()-> {
				int textCount=0;
				for(int i=0;i<stopTimeCount;i++) { 
					for(int j=0;j<7;j++) {
						columnList.get(textCount++).setText(queryDataDetail[i][j]);//改变单元格值
					}
				}
				
			}	
		);
	
	}
	private void updateCollectData() {
		//更新图标数据
		pieChart.setMinimumHeight(500);//最小高度
		pieChart.animateXY(3000, 3000);//动画
		Description pieDescription=new Description();
		    pieDescription.setText(xiantiData.getText().toString()+banciData.getText().toString());//设置 描述
			pieDescription.setTextColor(Color.RED);
		pieChart.setDescription(pieDescription);
		//pieChart.setDrawBarShadow(true);//阴影
		pieChart.setNoDataText("loading,please wait.");//无数据时显示内容
		pieChart.setDrawHoleEnabled(true);//设置为饼图，不是环形图
	       //是否显示圆盘中间文字，默认显示
        pieChart.setDrawCenterText(true);
        
        //设置圆盘中间文字
        pieChart.setCenterText(xiantiData.getText().toString()+banciData.getText().toString());
        //设置图列位置
		 Legend pieLegend = pieChart.getLegend();  
		 pieLegend.setWordWrapEnabled(true); //设置比例块换行...
		 pieLegend.setForm(Legend.LegendForm.CIRCLE);//设置比例块形状为圆形，默认为方块 
		 pieLegend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT); 
		 //pieLegend.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);//图列位置
		 pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);//左边
		 pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//顶部
		pieChart.setData(createPieCollectData());
		pieChart.setDrawEntryLabels(showLabel);//设置是否显示标签
		pieChart.setEntryLabelColor(Color.BLACK);//标签颜色
		pieChart.setEntryLabelTextSize(8f);//标签大小
	}
	private PieData createPieCollectData() {
		//生成 一个 柱状图数据
		ArrayList<PieEntry> pieDataList=new ArrayList<>();//饼图 上的的数据
		ArrayList<Integer> pieColors = new ArrayList<>();//饼图每一块显示的颜色
		Random random=new Random();//生成随机颜色
		float x;
		for(int i=0;i<50;i++) { 
			if(queryDataCollect[i][1]!=null) {
				if(queryDataCollect[i][0]==null) {
					queryDataCollect[i][0]="经营体(默认)";//默认为经营体责任
					pieColors.add(Color.RED);
				}else {
					pieColors.add(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
				}
				x=Float.parseFloat(queryDataCollect[i][1])/60; //获得分钟数
				x=new BigDecimal(x).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();//四舍五入取整
				pieDataList.add(new PieEntry(x,queryDataCollect[i][0])); //第一个参数为数据，第二个为标签
				
			}
		}
		//加入图表中
		PieDataSet pieDataSet=new PieDataSet(pieDataList,"");
		pieDataSet.setColors(pieColors);//图表 块颜色
	    //设置 数据由一根线 向外伸出显示
	    pieDataSet.setValueLinePart1OffsetPercentage(80.f);
	    pieDataSet.setValueLinePart1Length(0.3f);
	    pieDataSet.setValueLinePart2Length(0.4f);
	    pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
	    pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
	    
	    PieData pieData=new PieData(pieDataSet);
	    pieData.setValueTextColors(pieColors); //设置每一块数据的颜色
	    pieChart.setCenterText("总计："+pieData.getYValueSum()+" 分钟");//环中间显示内容
	   // pieData.setValueFormatter(new PercentFormatter());//显示百分比符号
		return pieData;
			
	}
	private boolean detectionConnect() throws SQLException {
		if(shared == null) {//shared传递 给connectServer 用户查找存储在手机上的服务器地址 
			shared = parentActivity.getSharedPreferences(LoginActivity.Login_Info_SHARED,Context.MODE_PRIVATE);//建立 名为 LoginInfo 且私有的  数据存储对象
		}
		try {
			con= ConnectServer.connectServer(parentActivity);
			if((statement=con.createStatement())==null){
				return false;
			}
			
		} catch (ClassNotFoundException e) {
			popupTipInfo("ClassNotFoundException",0);
		}
		return true;
		
	}
	//弹出日期选择器
	public void onClick(View v) {
		int id=v.getId();
		if(id == R.id.downtime_datebegin ||id == R.id.downtime_dateend ) {
			if(R.id.downtime_dateend == id) {
				dateTextSet=dateEnd;
			}else {
				dateTextSet=dateBegin;
			}
			createDateChoiceDialog();
		}else if(id == R.id.downtime_banci) {
				createBanciChoiceDialog();
		}else if(id == R.id.downtime_xiantidata) {
				createXiantiChoiceDialog();
		}
		
	}
	private void createDateChoiceDialog() {
		currentDate=Calendar.getInstance();
		if(dateChoiceDialog == null) {
			dateChoiceDialog = new DatePickerDialog(parentActivity,
					(DatePicker view, int year, int month, int dayOfMonth) ->{//创建日期选择对话框
					currentDate=Calendar.getInstance();
					int currentYear=currentDate.get(Calendar.YEAR);
					int currentMonth=currentDate.get(Calendar.MONTH);
					String dateString=String.format(getResources().getString(R.string.date_string),year,(month+1),dayOfMonth);
					if(year<currentYear) {
						dateTextSet.setText(dateString);
						queryTableData=true;
						queryBtStat=1;//选择日期后允许查询
						return;
					}else if(year==currentYear) {
						if(month<currentMonth) {
							dateTextSet.setText(dateString);
							queryTableData=true;
							queryBtStat=1;//选择日期后允许查询
							return;
						}
						else if(month==currentMonth){
							if(dayOfMonth<=currentDate.get(Calendar.DAY_OF_MONTH)) {
								dateTextSet.setText(dateString);
								queryTableData=true;
								queryBtStat=1;//选择日期后允许查询
								return;
							}
						}
							
					}
					popupTipInfo("只能选择当天或以前的日期",0);
				
			},currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH),currentDate.get(Calendar.DAY_OF_MONTH));
		}
		dateChoiceDialog.show();
	}
	private void createBanciChoiceDialog() {
		Builder builder;
		if(banciChoiceDialog == null) {
			builder=new AlertDialog.Builder(parentActivity);
			builder.setTitle("班次");
			builder.setMultiChoiceItems(banciList , banciIsChoice,
			(DialogInterface dialog, int which, boolean isChecked)-> {
							StringBuffer s =new StringBuffer();
							for(int i=0;i<2;i++) {
								if(banciIsChoice[i]) {
									s.append(banciList[i]).append(" ");
								}
							}
							banciData.setText(s);
							queryTableData=true;
					}
			);
			banciChoiceDialog=builder.create();
		}
		queryBtStat=1;//选择班次后允许查询
		banciChoiceDialog.show();
	}
	private void createXiantiChoiceDialog() {
		Builder builder;
		if(xiantiChoiceDialog == null) {
			builder=new AlertDialog.Builder(parentActivity);
			builder.setTitle("线体");
			builder.setMultiChoiceItems( xiantiList , xiantiIsChoice,
					(DialogInterface dialog, int which, boolean isChecked)-> {
							StringBuffer s=new StringBuffer();
							for(int i=0;i<4;i++) {
								if(xiantiIsChoice[i]) {
									s.append(xiantiList[i]).append(" ");
								}
							}
							xiantiData.setText(s);
							queryTableData=true;
				}
					
			);
			xiantiChoiceDialog=builder.create();
		}
		queryBtStat=1;//选择线体后允许查询
		xiantiChoiceDialog.show();
		//query();//选择线体后 查询
	}


	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.downtime_query_bt) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) { //手指按下 加深背景色
				v.performClick();//解决 与 单击事件的冲突  添加该语句后 有 Touch与Click二个事件
				if(queryBtStat==1) {
					//定时器防止频繁刷新
					queryBtStat=0;
				    new Handler().postDelayed(()-> {
				    	queryBtStat=1;
						queryTableData=true;
				    }, 1000*60*5); //5分后刷新按钮可用
					queryThread=new Thread(this);//创建线程查询数据
					queryThread.start();
				}else {
					popupTipInfo("查询太频繁,请5分钟后再试",0);
				}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){ // 手指松开  恢复背景色

			}
			
		}
		return false;
	}

	private void changeQueryBtStat(final String text,final boolean enable) {//改变查询按钮状态
		// 需用handler才能更新主线程界面 按钮颜色和字体
		handler.post(() ->{
				query_bt.setText(text);
				query_bt.setEnabled(enable);
		});
	}
	private void popupTipInfo(final String tip,int status) {
		handler.post(()-> {
			new TipUtil().showTips(parentActivity, MainActivity.toolbar,tip,status);
		});
	}
	@SuppressWarnings("deprecation")
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.e("DowntimePieChart","onAttach()");
	}
	public void onActivityCreated(Bundle SavedFragmentState) {
		super.onActivityCreated(SavedFragmentState);
		Log.e("DowntimePieChart","onActivityCreated()");

	}
	public void onStart() {
		super.onStart();
		Log.e("DowntimePieChart","onStart()");

	}
	public void onResume() {
		super.onResume();
		Log.e("DowntimePieChart","onResume()");
	}
	public void onPause() {
		super.onPause();
		Log.e("DowntimePieChart","onPause()");
	}
	public void onStop() {
		super.onStop();
		Log.e("DowntimePieChart","onStop()");
	}
	public void onDestroyView() {
		super.onDestroyView();
		Log.e("DowntimePieChart","onDestroyView()");
	}
	public void onDestroy() {
		super.onDestroy();
		Log.e("DowntimePieChart","onDestory()");
	}
	public void onDetach() {
		super.onDetach();
		Log.e("DowntimePieChart","onDetach()");
	}
	public void onSaveInstanceState(Bundle outState) {
		Log.e("MainActivity","onSaveInstanceState()");
		
		//保存当前选择的日期 、线体   以便锁屏后  旋转屏幕后  onCreate重建时恢复显示 上次的数据
		outState.putString("DATE_BEGIN", dateBegin.getText().toString());
		outState.putString("DATE_END", dateEnd.getText().toString());
		
		outState.putString("BANCI_TEXT",banciData.getText().toString());
		outState.putBooleanArray("BANCI_ISCHOICE", banciIsChoice);
		
		outState.putString("XIANTIDATA_TEXT",xiantiData.getText().toString());
		outState.putBooleanArray("XIANTI_ISCHOICE", xiantiIsChoice);
		super.onSaveInstanceState(outState);
	}
}
	
