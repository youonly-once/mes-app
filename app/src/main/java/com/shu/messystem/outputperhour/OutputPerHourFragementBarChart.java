package com.shu.messystem.outputperhour;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.shu.messystem.ConnectServer;
import com.shu.messystem.LoginActivity;
import com.shu.messystem.MainActivity;
import com.shu.messystem.R;
import com.shu.messystem.component.TopTips.util.TipUtil;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class OutputPerHourFragementBarChart extends Fragment implements OnTouchListener, Runnable, OnClickListener {
    private AppCompatActivity parentActivity;// 获取父Activity
    private Thread queryThread;// 查询数据的线程

    private TextView dateBegin, timeBegin;// 日期选择框
    private DatePickerDialog dateChoiceDialog;// 日期选择对话框 点击dateBegin弹出
    private TimePickerDialog timeChoiceDialog;
    private Calendar currentDate;// 保存当前时间
    private int hour; // 当前小时

    private TextView xiantiData;// 线体列表
    private AlertDialog xiantiChoiceDialog;// 线体选择对话框
    private String[] xiantiList = {"PH", "PJ", "PK","TN"};
    private boolean[] xiantiIsChoice = {true, false, false,false};// 保存线体选择状态 ，会随着选择 变化

    private Button query_bt;// 查询按钮
    private int queryBtStat = 1;// 防止查询太频繁

    private BarChart barChart;// 柱状图对象
    private List<IBarDataSet> barDataSets = new ArrayList<>();//保存每一组柱状图数据
    private TableLayout tableData; // 存放数据的TableLayout
private final int LINE_COUNT=4;
    private String[][] queryTableData = new String[24][3]
            , queryDataPH = new String[24][3]
            , queryDataPJ = new String[24][3]
            , queryDataPK = new String[24][3]
            , queryDataTN = new String[24][3];// 保存查询数据
    private ArrayList<TableRow> rowList = new ArrayList<>(); // 数据表格的24行
    private ArrayList<TextView> columnList = new ArrayList<>();// 数据表格的存储单元格 24*3
    private int totalData = 0;//保存查询总产量
    private Connection con = null; // 保存连接服务器后的对象
    private Statement statement = null;
    private SharedPreferences shared;// 获取 存储对象 用于查找存储在本地的服务器地址
    private Handler handler = new Handler();// 用于子线程刷新主界面

    private DisplayMetrics dm;// 获取屏幕宽高

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.outputperhour_fragement_barchart, container, false);

        // 获取上次 保存的值

        String dateCurr, timeCurr = "08:00", xiantiText = "PH";
        if (savedInstanceState != null) {
            dateCurr = savedInstanceState.getString("DATE");
            timeCurr = savedInstanceState.getString("TIME");
            xiantiIsChoice = savedInstanceState.getBooleanArray("XIANTI_ISCHOICE");
            xiantiText = savedInstanceState.getString("XIANTIDATA_TEXT");
        } else {
            currentDate = Calendar.getInstance();
            dateCurr = currentDate.get(Calendar.YEAR) + "-" + (currentDate.get(Calendar.MONTH) + 1) + "-"
                    + currentDate.get(Calendar.DAY_OF_MONTH);
        }

        initCreateView(rootView, dateCurr, timeCurr, xiantiText);

        initTableData();

        queryThread();

        Log.e("OutputPerHourFragement", "onCreateView()");
        return rootView;
    }
    private void initCreateView(View rootView, String dateCurr, String timeCurr, String xiantiText) {
        parentActivity = (AppCompatActivity) getActivity();// 获取Acticity

        query_bt = (Button) rootView.findViewById(R.id.query_bt);
        query_bt.setOnTouchListener(this);// 按钮触摸事件

        dateBegin = (TextView) rootView.findViewById(R.id.datebegin);
        dateBegin.setOnClickListener(this);
        dateBegin.setText(dateCurr);

        timeBegin = (TextView) rootView.findViewById(R.id.timebegin);
        timeBegin.setOnClickListener(this);
        timeBegin.setText(timeCurr);

        xiantiData = (TextView) rootView.findViewById(R.id.xiantidata);
        xiantiData.setOnClickListener(this);
        xiantiData.setText(xiantiText);

        tableData = (TableLayout) rootView.findViewById(R.id.table_data);// 存放数据

        barChart = (BarChart) rootView.findViewById(R.id.outputperhour_barchart);

        // 获取屏幕宽度 /高度
        dm = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    private void initTableData() {
        // 建立数据表格的行和列
        TableRow row;// 行
        TextView columnText;// 单元格
        for (int i = 0; i < 48; i++) {
            row = new TableRow(parentActivity);
            row.setBackgroundColor(Color.parseColor("#FFFFFF"));
            rowList.add(row);// 添加进 行 队列
            for (int j = 0; j < 3; j++) {
                columnText = new TextView(parentActivity);
                columnText.setGravity(Gravity.CENTER);
                columnText.setWidth(dm.widthPixels / 3);
                if (i % 2 == 1)
                    columnText.setBackgroundColor(Color.parseColor("#E0FFFF"));
                row.addView(columnText);// 单元格 加入行
                columnList.add(columnText);// 保存 单元格对象
            }
            tableData.addView(row, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));// 行加入表格
        }
    }

    // 第一次启动时 和选择完日期是 用该线程查询数据
    public void queryThread() {
       // queryThread = new Thread(this);
        //queryThread.start();
    }

    public void run() {
        if (Thread.currentThread() == queryThread) {

            changeQueryBtStat("查询中,请稍后", false);// 按钮不可用

            if (queryData()) {
                handler.post(()-> {
                        updateData();
                    }
                );
            }

            changeQueryBtStat("查询", true);// 按钮可用
        }
    }

    // 查询数据
    private synchronized boolean queryData() {
        String id1="";
        String id2="";
        String id3="";
        SharedPreferences sharedPreferences= parentActivity.getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
        String dateBeginText = dateBegin.getText().toString();// 选择时间
        String timeBeginText = timeBegin.getText().toString();// 选择时间
        String beginDateTime = dateBeginText + " " + timeBeginText;
        String sql;
        ResultSet result;
        totalData = 0;
        currentDate = Calendar.getInstance();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        //String date1 = currentDate.get(Calendar.YEAR) + "-" + (currentDate.get(Calendar.MONTH) + 1) + "-"
        //		+ currentDate.get(Calendar.DAY_OF_MONTH) + " 00:00";// 当前时间
        try {
            if (!detectionConnect()) {
                return false;
            }
            try {
                //if (sd.parse(date1).getTime() > sd.parse(beginDateTime).getTime()) {// 昨天或以前则查询 24小时数据 当天则查询现在到0点之间数据
                hour = 11;
                //} else {
                //	hour = currentDate.get(Calendar.HOUR_OF_DAY);// 当前小时
                //}
                int year, month, day, hourCurr, hourAdd1;
                String begin, end;
                String query[][]=null;
                for (int j = 0; j < LINE_COUNT; j++) {
                    if (!xiantiIsChoice[j]) {
                        continue;
                    }
                    if (j == 0) {//一线员工号
                        id1 = sharedPreferences.getString("ph_jia","");//
                        id2 = sharedPreferences.getString("ph_yi","");//
                        id3 = sharedPreferences.getString("ph_bing","");//
                        query = queryDataPH;
                    } else if (j == 1) {// 二线员工号
                        id1 = sharedPreferences.getString("pj_jia","");//
                        id2 = sharedPreferences.getString("pj_yi","");//
                        id3 = sharedPreferences.getString("pj_bing","");//
                        query = queryDataPJ;
                    } else if (j == 2) {
                        // 三线员工号
                        id1 = sharedPreferences.getString("pk_jia","");//
                        id2 = sharedPreferences.getString("pk_yi","");//
                        id3 = sharedPreferences.getString("pk_bing","");//
                        query = queryDataPK;
                    }else if (j == 3) {
                        // 三线员工号
                        id1 = sharedPreferences.getString("tn_jia","");//
                        id2 = sharedPreferences.getString("tn_yi","");//
                        id3 = sharedPreferences.getString("tn_bing","");//
                        query = queryDataTN;
                    }
                    //每次查询完一条线体后   end和 houradd1 已经叠加多次  需要 将 时间初始化为开始时间
                    end = beginDateTime;
                    currentDate.setTime(sd.parse(beginDateTime));
                    hourAdd1 = currentDate.get(Calendar.HOUR_OF_DAY);

                    for (int i = 0; i < hour + 1; i++) {
                        begin = end;
                        hourCurr = hourAdd1; //显示 8-9 9-10

                        currentDate.add(Calendar.HOUR_OF_DAY, 1);// 增加一个小时
                        year = currentDate.get(Calendar.YEAR);
                        month = currentDate.get(Calendar.MONTH) + 1;//月从0开始
                        day = currentDate.get(Calendar.DAY_OF_MONTH);
                        hourAdd1 = currentDate.get(Calendar.HOUR_OF_DAY);

                        end = year + "-" + month + "-" + day + " " + hourAdd1 + ":00";
                        sql = "select count(*) as hourCount from z_comwcb where " +
                                "(yg_id='" + id1 + "' OR yg_id='" + id2+ "' OR yg_id='" + id3
                                + "') and realdate >'" + begin + "' and realdate <='" + end + "'";
                        Log.e("SQL",sql);
                        result = statement.executeQuery(sql);
                        if (result.next()) {
                            query[i][0] = hourCurr + "~" + (hourAdd1);
                            query[i][1] = xiantiList[j];
                            query[i][2] = result.getString("hourCount");
                            totalData += Integer.parseInt(query[i][2]);
                        }
                    }
                }
                return true;
            } catch (ParseException e1) {
                popupTipInfo("时间初始化失败",0);
                Log.e("OutPutParseException", e1.getMessage() + beginDateTime);

            }

        } catch (SQLException e) {
            popupTipInfo("查询过程出错",0);
            Log.e("OutPutSQLException", e.getMessage() + beginDateTime);
        }
        try {
            con.close();
            statement.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;

    }

    private boolean detectionConnect() throws SQLException {
        if (shared == null) {// shared传递 给connectServer 用户查找存储在手机上的服务器地址
           // shared = parentActivity.getSharedPreferences(LoginActivity.Login_Info_SHARED, Context.MODE_PRIVATE);
            // 建立 名为 LoginInfo 且私有的 数据存储对象
        }
        try {
            con = ConnectServer.connectServer(parentActivity);
            if ((statement = con.createStatement()) == null) {
                return false;
            }

        } catch (ClassNotFoundException e) {
            popupTipInfo("ClassNotFoundException",0);
        }
        return true;

    }

    private void updateTableData() {
        // 填充表格数据 需用handler才能更新主线程界面
        handler.post(()-> {
                int textCount = 0;
                for (int i = hour; i >= 0; i--) {
                    for (int j = 0; j < 3; j++) {
                        columnList.get(textCount++).setText(queryTableData[i][j]);// 改变单元格值

                    }
                }
            for (int i = hour+1; i <=(hour+hour); i++) {
                for (int j = 0; j < 3; j++) {
                    columnList.get(textCount++).setText(queryDataPJ[i][j]);// 改变单元格值

                }
            }
 /*           for (int i = hour+hour; i >= hour+hour; i--) {
                for (int j = 0; j < 3; j++) {
                    columnList.get(textCount++).setText(queryDataPK[i][j]);// 改变单元格值

                }
            }
            for (int i = hour+hour; i >= hour+hour+hour; i--) {
                for (int j = 0; j < 3; j++) {
                    columnList.get(textCount++).setText(queryDataTN[i][j]);// 改变单元格值

                }
            }*/
  /*              for (int i = hour + 1; i < 48; i++) {
                    for (int j = 0; j < 3; j++) {
                        columnList.get(textCount++).setText("");// 清空上次查询单元格值
                    }
                }*/

            }

        );

    }

    private void updateData() {

        // 更新图标数据

        barChart.setData(getChartData());
        barChart.setMinimumHeight(500);// 最小高度
        barChart.animateXY(3000, 3000);// 动画

        // 图列位置
        Legend barLegend = barChart.getLegend();
        barLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);//左边
        barLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//顶部
        //barChart.setDrawBarShadow(true);// 阴影
        barChart.setNoDataText("loading,please wait.");// 无数据时显示内容

        //描述
        Description description = new Description();
        description.setText("累计：" + totalData + " 台");
        description.setTextColor(Color.RED); // 描述文字颜色
        barChart.setDescription(description);
        //Y轴 右标签
        YAxis yaxis = barChart.getAxisRight();
        yaxis.setDrawLabels(false);

        // 隐藏网格线
        XAxis xaxis = barChart.getXAxis();
        xaxis.setDrawGridLines(false);
        xaxis.setPosition(XAxisPosition.BOTH_SIDED);// X轴在下方和上方显示

        barChart.invalidate();
        // 更新表格数据
        updateTableData();


    }

    private BarData getChartData() {
        // 柱状图数量
        /*
		 * int barCount=24; if(dm.widthPixels>=1440) { barCount=24; }else
		 * if(dm.widthPixels>=1080) { barCount=24; }else if (dm.widthPixels>=720) {
		 * barCount=24; }else if (dm.widthPixels>=480) { barCount=24; }
		 */
        //清除元素
        for (int i = barDataSets.size() - 1; i >= 0; i--) {
            barDataSets.remove(i);
        }
        int count = 0;
        if (xiantiIsChoice[0]) {
            createChartBarData(queryDataPH, Color.BLUE);
            queryTableData = queryDataPH;
            count++;
        }
        if (xiantiIsChoice[1]) {
            createChartBarData(queryDataPJ, Color.parseColor("#9900FF"));
            queryTableData = queryDataPJ;
            count++;
        }
        if (xiantiIsChoice[2]) {
            createChartBarData(queryDataPK, Color.parseColor("#FF00FF"));
            queryTableData = queryDataPK;
            count++;
        }
        if (xiantiIsChoice[3]) {
            createChartBarData(queryDataTN, Color.parseColor("#00FF00"));
            queryTableData = queryDataTN;
            count++;
        }
        //当有多组数据时设置 组距离

        // 倒序显示
		/*
		 * for(int i=hour,k=0;i>=0 && i>hour-barCount;i--,k++) { phList.add(new
		 * BarEntry(Float.parseFloat(queryData[i][2]),k)); xList.add(queryData[i][0]); }
		 */
        // 设置 x轴数据、柱状图数据
        BarData barData = new BarData(barDataSets);
        if (count == 2) {
            float groupSpace = 0.2f;//表示组之间的距离
            float barSpace = 0f;//表示组内柱子之间的距离
            float barWidth = (((1 - groupSpace) / count) - barSpace);
            barData.setBarWidth(barWidth);
            barData.groupBars(0f, groupSpace, barSpace);
        } else if (count == 3) {
            float groupSpace = 0.2f;//表示组之间的距离
            float barSpace = 0f;//表示组内柱子之间的距离
            float barWidth = (((1 - groupSpace) / count) - barSpace);
            barData.setBarWidth(barWidth);
            barData.groupBars(0f, groupSpace, barSpace);
        } else if (count == 4) {
            float groupSpace = 0.2f;//表示组之间的距离
            float barSpace = 0f;//表示组内柱子之间的距离
            float barWidth = (((1 - groupSpace) / count) - barSpace);
            barData.setBarWidth(barWidth);
            barData.groupBars(0f, groupSpace, barSpace);
        }

        return barData;
    }

    private void createChartBarData(String queryData[][], int color) {
        // 生成 一个 柱状图数据
        List<BarEntry> barList = new ArrayList<>();
        for (int i = 0; i <= hour; i++) {
            barList.add(new BarEntry(i, Float.parseFloat(queryData[i][2])));//第一个参数为横坐标,第二个为纵坐标
        }
        // 加入柱状图中
        BarDataSet barDataSet = new BarDataSet(barList, queryData[0][1]); //第二个参数为该柱状图的描述 图列的文本 PH,PJ,PK
        barDataSet.setValueFormatter(
                (float value, Entry entry, int dataSetIndex,ViewPortHandler viewPortHandler)-> {
                DecimalFormat df = new DecimalFormat("#");  //设置柱状图上的数据 没有小数点
                return "" + df.format(value);  //

        });
        //设置柱状图颜色
        barDataSet.setColor(color);
        // 加入图标中
        barDataSets.add(barDataSet);

    }

    // 弹出日期选择器
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.datebegin) {
            createDatePickerDialog();
        } else if (id == R.id.timebegin) {
            createTimePickerDialog();
        } else if (id == R.id.xiantidata) {
            createXiantiChoiceDialog();
        }

    }

    // 创建日期选择对话框
    private void createDatePickerDialog() {
        if (dateChoiceDialog == null) {
            currentDate = Calendar.getInstance();
            dateChoiceDialog = new DatePickerDialog(parentActivity,
                    (DatePicker view, int year, int month, int dayOfMonth) ->{// 创建日期选择对话框
                    currentDate = Calendar.getInstance();
                    int currentYear = currentDate.get(Calendar.YEAR);
                    int currentMonth = currentDate.get(Calendar.MONTH);
                    String dateDisplay = String.format(getResources().getString(R.string.date_string), year, (month + 1), dayOfMonth);
                    if (year < currentYear) {
                        dateBegin.setText(dateDisplay);
                        queryBtStat = 1;
                        return;
                    } else if (year == currentYear) {
                        if (month < currentMonth) {
                            dateBegin.setText(dateDisplay);
                            queryBtStat = 1;
                            return;
                        } else if (month == currentMonth) {
                            if (dayOfMonth <= currentDate.get(Calendar.DAY_OF_MONTH)) {
                                dateBegin.setText(dateDisplay);
                                queryBtStat = 1;
                                return;
                            }
                        }

                    }
                    popupTipInfo("只能选择当天或以前的日期",0);

            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

        }
        dateChoiceDialog.show();
    }

    // 创建时间选择对话框
    private void createTimePickerDialog() {
        if (dateChoiceDialog == null) {
            currentDate = Calendar.getInstance();
            // int currentHour=currentDate.get(Calendar.HOUR_OF_DAY);
            // int currentMinute=currentDate.get(Calendar.MINUTE);
            timeChoiceDialog = new TimePickerDialog(parentActivity,
                    (TimePicker view, int hourOfDay, int minute)-> {// 创建日期选择对话框
                    String timeString =String.format(getResources().getString(R.string.time_string),String.valueOf(hourOfDay),String.valueOf(minute));
                    timeBegin.setText(timeString);
                    queryBtStat = 1;

            }, 8, 0, true);// true代表24小时
        }
        timeChoiceDialog.show();

    }

    // 创建线体选择对话框
    private void createXiantiChoiceDialog() {
        if (xiantiChoiceDialog == null) {
            Builder builder = new AlertDialog.Builder(parentActivity);
            builder.setTitle("线体");
            builder.setMultiChoiceItems(xiantiList, xiantiIsChoice,(DialogInterface dialog, int which, boolean isChecked)-> {
                    StringBuffer s =new StringBuffer();
                    for (int i = 0; i < LINE_COUNT; i++) {
                        if (xiantiIsChoice[i]) {
                            s.append( xiantiList[i] + " ");
                        }
                    }
                    xiantiData.setText(s);
                    queryBtStat = 1;
                }
            );
            xiantiChoiceDialog = builder.create();
        }

        xiantiChoiceDialog.show();
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.query_bt) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) { // 手指按下 加深背景色
                v.performClick();// 解决 与 单击事件的冲突 添加该语句后 有 Touch与Click二个事件
                if (queryBtStat == 1) {
                    // 定时器防止频繁刷新
                    queryBtStat = 0;
                    new Handler().postDelayed(()->{
                                queryBtStat = 1;
                    }, 1000 * 60 * 5); // 5分后刷新按钮可用
                    queryThread = new Thread(this);// 创建线程查询数据
                    queryThread.start();
                } else {
                    popupTipInfo("查询太频繁,请5分钟后再试",0);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) { // 手指松开 恢复背景色

            }

        }
        return false;
    }

    private void changeQueryBtStat(final String text,final boolean enable) {// 改变查询按钮状态
        // 需用handler才能更新主线程界面 按钮颜色和字体
        handler.post(()->{
                query_bt.setText(text);
                query_bt.setEnabled(enable);
            }
        );
    }

    private void popupTipInfo(final String tip,int status) {
        handler.post(() ->{
            new TipUtil().showTips(parentActivity, MainActivity.toolbar,tip, status);
        });
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.e("OutputPerHourFragement", "onAttach()");
    }

    public void onActivityCreated(Bundle SavedFragmentState) {
        super.onActivityCreated(SavedFragmentState);
        Log.e("OutputPerHourFragement", "onActivityCreated()");

    }

    public void onStart() {
        super.onStart();
        Log.e("OutputPerHourFragement", "onStart()");

    }

    public void onResume() {
        super.onResume();
        Log.e("OutputPerHourFragement", "onResume()");
    }

    public void onPause() {
        super.onPause();
        Log.e("OutputPerHourFragement", "onPause()");
    }

    public void onStop() {
        super.onStop();
        Log.e("OutputPerHourFragement", "onStop()");
    }

    public void onDestroyView() {
        super.onDestroyView();
        Log.e("OutputPerHourFragement", "onDestroyView()");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("OutputPerHourFragement", "onDestory()");
    }

    public void onDetach() {
        super.onDetach();
        Log.e("OutputPerHourFragement", "onDetach()");
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.e("OutputPerHourFragement", "onSaveInstanceState()");

        // 保存当前选择的日期 、线体 以便锁屏后 旋转屏幕后 onCreate重建时恢复显示 上次的数据
        outState.putString("DATE", dateBegin.getText().toString());
        outState.putString("TIME", timeBegin.getText().toString());
        outState.putString("XIANTIDATA_TEXT", xiantiData.getText().toString());
        outState.putBooleanArray("XIANTI_ISCHOICE", xiantiIsChoice);
        super.onSaveInstanceState(outState);
    }
}
