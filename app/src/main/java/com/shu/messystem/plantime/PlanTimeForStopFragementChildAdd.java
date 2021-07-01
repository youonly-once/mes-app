package com.shu.messystem.plantime;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.shu.messystem.result_bean.GetLineBean;
import com.shu.messystem.result_bean.GetUserInfoBean;
import com.shu.messystem.ManageRetrofit;
import com.shu.messystem.component.ClearEditText;
import com.shu.messystem.MainActivity;
import com.shu.messystem.R;
import com.shu.messystem.component.TopTips.util.TipUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanTimeForStopFragementChildAdd extends Fragment implements View.OnClickListener, Runnable {
    private AppCompatActivity parentActivity;// 获取父Activity

    private ClearEditText name;// 线体列表
    private AlertDialog nameChioDialog;// 线体选择对话框
    //private String[] xiantiList = new String[4];//4个都必须赋值，否则加载到对话框中时会报错
    private String[] xiantiList;//4个都必须赋值，否则加载到对话框中时会报错
    private ArrayList<String> xiantiCodeList = new ArrayList<>();
    private String xiantiCodeChoos;//当前选择的线体编码
    private int initValue = 0;//初始值

    private EditText dept;//计划列表
    private String[] planList;
    private AlertDialog planChoiceDialog;//班次选择对话框
    private boolean initFinish=false;
    private ClearEditText ip;//时间
    private ClearEditText mac;//时间
    private ClearEditText computer;//时间
    private ClearEditText type;//时间
    private Calendar currentDate;// 保存当前时间
    private TextView currentTimeView;//当前是谁点击了 日期选择框
    private TimePickerDialog timeChoiceDialog;
    private SmartRefreshLayout mRefreshLayout;
    private Button saveBt;
    private Thread saveThread;
    Handler handler = new Handler();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.plantimeforstop_fragement_childadd, container, false);
        parentActivity = (AppCompatActivity) getActivity();// 获取Acticity

        name = (ClearEditText) rootView.findViewById(R.id.name);
        name.setOnClickListener(this);

        dept = (ClearEditText) rootView.findViewById(R.id.dept);
        dept.setOnClickListener(this);

        ip =  rootView.findViewById(R.id.ip);
        ip.setOnClickListener(this);

        mac = rootView.findViewById(R.id.mac);
        mac.setOnClickListener(this);

        computer = rootView.findViewById(R.id.computer);
        computer.setOnClickListener(this);

        type = rootView.findViewById(R.id.type);
        type.setOnClickListener(this);

        saveBt = (Button) rootView.findViewById(R.id.save);
        saveBt.setText("添加");
        saveBt.setOnClickListener(this);
        initRefreshControl(rootView);
       // mRefreshLayout.autoRefresh();
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
                //读取线体数据
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
    private void initData() {
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(parentActivity).getLine();
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean> call, Response<GetLineBean> response) {
                if (response.body()!=null) {
                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {
                        ArrayList<String> arrayListXinti = new ArrayList<>();
                        List<GetLineBean.DataBean> data = response.body().getData();
                        for (int i = 0; i < data.size(); i++) {
                            //保持线体名称
                            arrayListXinti.add(data.get(i).getLinename());
                            //保存线体编码
                            xiantiCodeList.add(data.get(i).getLid());
                        }
                        xiantiList = arrayListXinti.toArray(new String[]{});

                    } else {

                        topTip("初始化失败，请重试", 0);
                    }
                }else{
                    topTip("初始化失败，请重试", 0);
                }
                initFinish=true;
                mRefreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(Call<GetLineBean> call, Throwable t) {
                topTip("初始化失败，请重试",0);
                initFinish=true;
                mRefreshLayout.finishRefresh();
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
        if (nameChioDialog == null) {
            builder = new Builder(parentActivity);
            builder.setTitle("线体");
            builder.setSingleChoiceItems(xiantiList, initValue, (DialogInterface dialog, int which) -> {
                        String s = xiantiList[which];
                        xiantiCodeChoos = xiantiCodeList.get(which);
                        name.setText(s);
                        nameChioDialog.hide();
                    }
            );
            nameChioDialog = builder.create();
        }

        nameChioDialog.show();
    }

    // 创建时间选择对话框
    private void createTimePickerDialog() {
        if (timeChoiceDialog == null) {
            currentDate = Calendar.getInstance();
            // int currentHour=currentDate.get(Calendar.HOUR_OF_DAY);
            // int currentMinute=currentDate.get(Calendar.MINUTE);
            timeChoiceDialog = new TimePickerDialog(parentActivity,
                    (TimePicker view, int hourOfDay, int minute) -> {// 创建日期选择对话框
                        SimpleDateFormat sd = new SimpleDateFormat("HH:mm", Locale.getDefault());

                        try {
                            currentDate.setTime(sd.parse(hourOfDay + ":" + minute));
                            String hour = currentDate.get(Calendar.HOUR_OF_DAY) + "";
                            String min = currentDate.get(Calendar.MINUTE) + "";
                            if (currentDate.get(Calendar.HOUR_OF_DAY) / 10 < 1) {
                                hour = "0" + currentDate.get(Calendar.HOUR_OF_DAY);
                            }
                            if (currentDate.get(Calendar.MINUTE) / 10 < 1) {
                                min = "0" + currentDate.get(Calendar.MINUTE);
                            }
                            String timeDisplay = String.format(getResources().getString(R.string.time_string), hour, min);
                            currentTimeView.setText(timeDisplay);
                        } catch (ParseException e) {
                            topTip("时间设置失败，请重试",0);
                            e.printStackTrace();
                        }

                        timeChoiceDialog.hide();

                    }, 12, 0, true);// true代表24小时
        }
        timeChoiceDialog.show();

    }

    public void run() {
        if (Thread.currentThread() == saveThread) {
            addData();
        }
    }

    private void addData() {
        changeQueryBtStat("添加中,请稍后", false);// 按钮不可用
       // SharedPreferences shared = parentActivity.getSharedPreferences("query", Context.MODE_PRIVATE);
        String sql;
        String name = this.name.getText().toString();
        if (name.length() == 0) {
            topTip("请输入姓名",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }
        String dept = this.dept.getText().toString();
        if (dept.length() == 0) {
            topTip("请输入部门",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }

        String ipValue = this.ip.getText().toString();
        if (ipValue.length() == 0) {
            topTip("请输入IP",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }
        String macValue = this.mac.getText().toString();
        if (macValue.length() == 0) {
            topTip("请输入MAC",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }
        String computerValue = this.computer.getText().toString();
        if (computerValue.length() == 0) {
            topTip("请输入姓名",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }
        String typeValue = this.type.getText().toString();
        if (typeValue.length() == 0) {
            topTip("请输入部门",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }
//判断时间格式
        //判断时间格式
/*        if(!GetGeneralInfomation.deteDateFormat(start,"HH:mmm")
                ||!GetGeneralInfomation.deteDateFormat(start,"HH:mmm")){
            topTip("时间格式不正确",0);
            changeQueryBtStat("添加", true);// 按钮可用
            return;
        }*/
        String gonghao = MainActivity.gonghao;
        String idname = MainActivity.name;





        HashMap<String,String> map=new HashMap<>();
        //map.put("xiantiCodeChoos",xiantiCodeChoos);
        map.put("name",name);
        map.put("dept",dept);
        map.put("ip",ipValue);
        map.put("mac",macValue);
        map.put("computer",computerValue);
        map.put("type",typeValue);
        map.put("gonghao",gonghao);
        map.put("idname",idname);
        Log.e("=========",map.toString());
        Call<GetUserInfoBean> request = ManageRetrofit.getRetroInter(parentActivity).addIpMac(map);
        request.enqueue(new Callback<GetUserInfoBean>() {
            @Override
            public void onResponse(Call<GetUserInfoBean > call, Response<GetUserInfoBean > response) {
                if (response.body()!=null){

                     String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        topTip("已添加",1);
                        PlanTimeForStopFragementChildAdd.this.name.setText("");
                        PlanTimeForStopFragementChildAdd.this.dept.setText("");
                        ip.setText("");
                        mac.setText("");
                        computer.setText("");
                        type.setText("");
                        //设置共享变量，通知删除页面下次打开时刷新
                        PlanTimeForStopFragementMain.modirefresh = true;
                        PlanTimeForStopFragementMain.delrefresh = true;

                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("添加失败，请重试",0);
                }
                changeQueryBtStat("添加", true);// 按钮可用
            }

            @Override
            public void onFailure(Call<GetUserInfoBean > call, Throwable t) {
                topTip("添加失败，请重试",0);
                changeQueryBtStat("添加", true);// 按钮可用
            }

        });

    }

    private void changeQueryBtStat(final String text, final boolean enable) {// 改变查询按钮状态
        // 需用handler才能更新主线程界面 按钮颜色和字体
        handler.post(() -> {
                    saveBt.setText(text);
                    //saveBt.setBackgroundColor(Color.parseColor(color));
                    saveBt.setEnabled(enable);
                }
        );
    }

    private void topTip(final String tip,int status) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                new TipUtil().showTips(parentActivity,MainActivity.toolbar,tip,status);
            }
        });
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.name) {
           // createXiantiChoiceDialog();
        } else if (id == R.id.ip) {
            this.currentTimeView = this.ip;
          //  createTimePickerDialog();
        } else if (id == R.id.mac) {
            this.currentTimeView = this.mac;
           // createTimePickerDialog();
        } else if (id == R.id.save) {
            saveThread = new Thread(this);
            saveThread.start();
        }

    }

    //父fragment 反射调用，标题栏刷新
    public void queryThread() {
        mRefreshLayout.autoRefresh();
    }

}
