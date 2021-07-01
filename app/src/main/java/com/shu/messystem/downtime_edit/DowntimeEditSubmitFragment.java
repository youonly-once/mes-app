package com.shu.messystem.downtime_edit;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shu.messystem.ConnectServer;
import com.shu.messystem.MainActivity;
import com.shu.messystem.ManageRetrofit;
import com.shu.messystem.R;
import com.shu.messystem.component.ClearEditText;
import com.shu.messystem.component.CustomProgressDialog;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.component.SingleOptionPopup;
import com.shu.messystem.component.TopTips.util.TipUtil;
import com.shu.messystem.plantime.PlanTimeForStopFragementChildAdd;
import com.shu.messystem.plantime.PlanTimeForStopFragementMain;
import com.shu.messystem.result_bean.GetLineBean;
import com.shu.messystem.result_bean.GetUserInfoBean;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 服务器 on 2018-8-24.
 */

public class DowntimeEditSubmitFragment extends Fragment implements View.OnClickListener {

    private TextView mCancel;
    private TextView mSubmit;
    private TextView mDel;

    private MainActivity mActivity;
    private Handler handler=new Handler();
    private CustomProgressDialog processDia;

    private String currChoDept =null;
    private String currChoName =null;
    private String currChoCom=null;
    private String currChoType =null;

    private String currRecordId=null;//当前维护记录的ID
    private String currState=null;
    private ArrayList<String> resonTwoList=new ArrayList<>();
    private HashMap<String,String> resonTwoToDepart=new HashMap<>();//二级原因与部门对应关系
    private HashMap<String,String> departToSolvepeople=new HashMap<>();//部门与责任人对应关系
    private TextView modiTime;
    private TextView chooseDept;
    private TextView chooseName;
    private TextView chooseComp;
    private TextView chooseType;
    private ClearEditText name;
    private ClearEditText ip;
    private ClearEditText mac;
    private ClearEditText dept;
    private ClearEditText computer;
    private ClearEditText type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stoptime_fragement_edit_submit, null);
        mActivity = (MainActivity)getActivity();
        processDia=new CustomProgressDialog(mActivity,"处理中");
        initView(rootView);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        //读取传递过来的参数
        readArguments();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {//显示时
            readArguments();
        }
    }

    private void readArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String[] data = bundle.getStringArray(DowntimeEditFragment.argumentsStr);
            if (data != null) {
                //设置控件值
                setViewValue(data);
            }
            currRecordId = bundle.getString("ID");
        }
    }

    private void setViewValue(String[] data) {


        chooseDept.setText(data[2]);
        //情况上次设置的数据
        chooseName.setText(data[1]);
        chooseComp.setText(data[5]);
        chooseType.setText(data[6]);

        ip.setText(data[3]);
        name.setText(data[1]);
        dept.setText(data[2]);
        mac.setText(data[4]);
        computer.setText(data[5]);
        type.setText(data[6]);

        currChoDept =data[2];
        currChoName =data[1];
        currChoCom=data[5];
        currChoType =data[6];

    }

    private void initView(View rootView) {

        modiTime = (TextView) rootView.findViewById(R.id.modi_time);

        chooseDept = (TextView) rootView.findViewById(R.id.chooseDept);
        chooseName = (TextView) rootView.findViewById(R.id.chooseName);
        chooseComp = (TextView) rootView.findViewById(R.id.chooseComp);
        chooseType = (TextView) rootView.findViewById(R.id.chooseType);

        name= (ClearEditText) rootView.findViewById(R.id.name);
       dept = (ClearEditText) rootView.findViewById(R.id.dept);
        ip = (ClearEditText) rootView.findViewById(R.id.ip);
        mac = (ClearEditText) rootView.findViewById(R.id.mac);
        computer = (ClearEditText) rootView.findViewById(R.id.computer);
        type = (ClearEditText) rootView.findViewById(R.id.type);

        mDel = (TextView) rootView.findViewById(R.id.del);
        mCancel = (TextView) rootView.findViewById(R.id.cancel);
        mSubmit = (TextView) rootView.findViewById(R.id.submit);

        chooseDept.setOnClickListener(this);
        chooseName.setOnClickListener(this);
        chooseComp.setOnClickListener(this);
        chooseType.setOnClickListener(this);


        mCancel.setOnClickListener(this);
        mSubmit.setOnClickListener(this);
        mDel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {//取消返回


            case R.id.chooseDept:
                chooseDept();//选择一级原因
                break;
            case R.id.chooseName:
                chooseName();//选择二级原因
                break;
            case R.id.chooseComp:
                chooseComp();//选择部门
                break;
            case R.id.chooseType:
                chooseType();//选择责任人
                break;
            case R.id.cancel:
                //返回列表
                backListViewFragment(false);
                break;
            case R.id.submit:
                //提交
                submitDetec();
                break;
            case R.id.del:
                //提交
                delIpMac();
                break;
        }
    }
    private void chooseDept(){
        processDia.show();
        final ArrayList<String> deptList=new ArrayList();
                Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).getDept();
                request.enqueue(new Callback<GetLineBean>() {
                    @Override
                    public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                        if (response.body()!=null){

                            String msg = response.body().getMsg();

                            if ("success".equals(msg)) {//密码正确"success".equals(msg)

                                List<GetLineBean.DataBean> data = response.body().getData();
                                for (int i = 0; i < data.size(); i++) {
                                    deptList.add(data.get(i).getInfo());
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        SingleOptionPopup.alertBottomWheelOption(mActivity, deptList,chooseDept.getText().toString(), new SingleOptionPopup.OnWheelViewClick() {
                                            @Override
                                            public void onClick(View view, int position) {
                                                currChoDept =deptList.get(position);
                                                chooseDept.setText(currChoDept);
                                                //重新选择二级原因
                                                chooseName.setText("选择责任人");
                                                currChoName =null;
                                            }
                                        });
                                    }
                                });

                            } else {

                                topTip(msg,0);
                            }
                        }else{
                            topTip("查询失败",0);
                        }
                        hideProcessDialog();
                    }

                    @Override
                    public void onFailure(Call<GetLineBean > call, Throwable t) {
                        topTip("查询失败，请重试",0);
                        hideProcessDialog();
                    }


                });


    }
    private void chooseName(){

        if(currChoDept ==null){
            Toast.makeText(mActivity, "请选择部门", Toast.LENGTH_SHORT).show();
            return;
        }
        processDia.show();
        ArrayList<String>  nameList=new ArrayList<String>();

        HashMap<String,String> queryMap=new HashMap<>();
        queryMap.put("dept",currChoDept);
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).getName(queryMap);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        List<GetLineBean.DataBean> data = response.body().getData();
                        for (int i = 0; i < data.size(); i++) {
                            nameList.add(data.get(i).getInfo());
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                SingleOptionPopup.alertBottomWheelOption(mActivity, nameList,chooseName.getText().toString(), new SingleOptionPopup.OnWheelViewClick() {
                                    @Override
                                    public void onClick(View view, int position) {
                                    //保存选择的姓名
                                        currChoName =nameList.get(position);
                                        chooseName.setText(currChoName);
                                    //下一步选择电脑
                                        chooseComp.setText("选择电脑");
                                        currChoCom =null;
                                    }
                                });
                            }
                        });

                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("查询失败",0);
                }
                hideProcessDialog();
            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("查询失败，请重试",0);
                hideProcessDialog();
            }


        });




    }
    private void chooseComp(){
        if(currChoName ==null){
            Toast.makeText(mActivity, "请选择责任人", Toast.LENGTH_SHORT).show();
            return;
        }
        processDia.show();
        //保存部门与责任人对应关系，选择部门后联动显示责任人
       ArrayList<String> computerList=new ArrayList<String>();
        HashMap<String,String> queryMap=new HashMap<>();
        queryMap.put("dept",currChoDept);
        queryMap.put("name",currChoName);
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).getCom(queryMap);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        List<GetLineBean.DataBean> data = response.body().getData();
                        for (int i = 0; i < data.size(); i++) {
                            computerList.add(data.get(i).getInfo());
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SingleOptionPopup.alertBottomWheelOption(mActivity, computerList,chooseComp.getText().toString(), new SingleOptionPopup.OnWheelViewClick() {
                                    @Override
                                    public void onClick(View view, int position) {
                                    currChoCom=computerList.get(position);
                                    chooseComp.setText(currChoCom);

                                        chooseType.setText("选择类型");
                                        currChoType=null;
                                    }
                                });
                            }
                        });

                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("查询失败",0);
                }
                hideProcessDialog();
            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("查询失败，请重试",0);
                hideProcessDialog();
            }


        });




    }
    private void chooseType(){
        if(chooseComp==null){
            Toast.makeText(mActivity, "请选择电脑", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> typeList=new ArrayList();
        processDia.show();
        HashMap<String,String> queryMap=new HashMap<>();
        queryMap.put("dept",currChoDept);
        queryMap.put("name",currChoName);
        queryMap.put("computer",currChoCom);
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).getType(queryMap);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        List<GetLineBean.DataBean> data = response.body().getData();
                        for (int i = 0; i < data.size(); i++) {
                            typeList.add(data.get(i).getInfo());
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                SingleOptionPopup.alertBottomWheelOption(mActivity, typeList,chooseComp.getText().toString(), new SingleOptionPopup.OnWheelViewClick() {
                                    @Override
                                    public void onClick(View view, int position) {
                                        currChoType=typeList.get(position);
                                        chooseType.setText(currChoType);
                                        getDetail();
                                    }
                                });
                            }
                        });

                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("查询失败",0);
                }
                hideProcessDialog();
            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("查询失败，请重试",0);
                hideProcessDialog();
            }


        });



    }
    private void delIpMac(){
        processDia.show();
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).delIpMac(currRecordId);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)
                        backListViewFragment(false);
                        topTip("删除成功",1);
                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("删除失败",0);
                }
                hideProcessDialog();

            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("删除失败，请重试",0);
                hideProcessDialog();
            }


        });
    }
    private void getDetail(){
        HashMap<String,String> queryMap=new HashMap<>();
        queryMap.put("dept",currChoDept);
        queryMap.put("name",currChoName);
        queryMap.put("computer",currChoCom);
        queryMap.put("type",currChoType);
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).getDetail(queryMap);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        List<GetLineBean.DataBean> data = response.body().getData();
                        ip.setText(data.get(0).getIp());
                        name.setText(data.get(0).getName());
                        dept.setText(data.get(0).getDept());
                        mac.setText(data.get(0).getMac());
                        computer.setText(data.get(0).getComputer());
                        type.setText(data.get(0).getType());
                        currRecordId=data.get(0).getId();
                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("查询失败",0);
                }
                hideProcessDialog();
            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("查询失败，请重试",0);
                hideProcessDialog();
            }


        });
    }

    private void hideProcessDialog(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                processDia.hide();
            }
        });
    }
    private void topTip(final String tip, int status) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                new TipUtil().showTips(mActivity, MainActivity.toolbar, tip, status);
            }
        });
    }
    private void backListViewFragment(boolean isSuccess){
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("02");
        FragmentTransaction ft = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_left_in_anim
                        , R.anim.fragment_right_out_anim
                        , R.anim.fragment_left_in_anim
                        , R.anim.fragment_right_out_anim);
        ft.hide(DowntimeEditSubmitFragment.this);
        if (fragment == null) {
            fragment = new DowntimeEditFragment();
            ft.add(R.id.fragement_frame, fragment, "02");
        } else {
            ft.show(fragment);
        }
        Bundle bundle=new Bundle();
        bundle.putBoolean("isSuccess",isSuccess);
        fragment.setArguments(bundle);
        mActivity.currentFragment=fragment;
        ft.commit();
    }
    private void submitDetec() {


        mSubmit.setEnabled(false);
        if(MainActivity.gonghao==null|| TextUtils.isEmpty(MainActivity.gonghao)){
            Toast.makeText(mActivity, "登录过期，请重新登录", Toast.LENGTH_SHORT).show();
            mSubmit.setEnabled(true);
            return;
        }

        if(currRecordId==null){
            Toast.makeText(mActivity, "当前ID为空，请返回重试", Toast.LENGTH_SHORT).show();

            mSubmit.setEnabled(true);
            return;
        }
        if (dept.getText() ==null || TextUtils.isEmpty(dept.getText().toString())) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(dept,null));
            dept.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        if (name.getText() ==null || TextUtils.isEmpty(name.getText().toString())) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(name,null));
            name.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        if (ip.getText() ==null || TextUtils.isEmpty(ip.getText().toString())) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(ip,null));
            ip.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        if (mac.getText() ==null || TextUtils.isEmpty(mac.getText().toString())) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(mac,null));
            mac.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        if (computer.getText() ==null || TextUtils.isEmpty(computer.getText().toString())) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(computer,null));
            computer.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        if (type.getText() ==null || TextUtils.isEmpty(type.getText().toString())) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(chooseType,null));
            type.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        // validate
  /*      String workStation = ip.getText().toString().trim();
        if (TextUtils.isEmpty(workStation)) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(null,mWorkStation));
            ip.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }
        // validate
        String detail = mReasonDetail.getText().toString().trim();
        if (TextUtils.isEmpty(detail)) {//红色的边框，并抖动
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(null,mReasonDetail));
            mReasonDetail.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }

        String solutionString = mSolution.getText().toString().trim();
        if (TextUtils.isEmpty(solutionString)) {
            //抖动动画
            Animation shake= AnimationUtils.loadAnimation(mActivity,R.anim.edittext_error_shake_anim);
            shake.setAnimationListener(new AnimationListener(null,mSolution));
            mSolution.startAnimation(shake);
            mSubmit.setEnabled(true);
            return;
        }*/
        //释放停机时信息
        saveEditInfo();

        // TODO validate success, do something
    }

    private void saveEditInfo() {
        processDia.show();
        HashMap<String,String> queryMap=new HashMap<>();
        queryMap.put("id",currRecordId);
        queryMap.put("dept",dept.getText().toString());
        queryMap.put("name",name.getText().toString());
        queryMap.put("computer",computer.getText().toString());
        queryMap.put("type",type.getText().toString());
        queryMap.put("ip",ip.getText().toString());
        queryMap.put("mac",mac.getText().toString());
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).saveEdit(queryMap);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        topTip("已修改",1);

                    } else {

                        topTip(msg,0);
                    }
                }else{
                    topTip("保存失败",0);
                }
                hideProcessDialog();
                mSubmit.setEnabled(true);
            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("保存失败，请重试",0);
                hideProcessDialog();
                mSubmit.setEnabled(true);
            }


        });

    }

    class AnimationListener implements Animation.AnimationListener {
        private TextView textView;
        private EditText editText;
        public AnimationListener(TextView textView,EditText editText) {
            this.textView=textView;
            this.editText=editText;
        }


        @Override
        public void onAnimationStart(Animation animation) {
            if(textView!=null){
                textView.setTextColor(Color.RED);
            }
            if(editText!=null) {
                editText.setBackground(getResources().getDrawable(R.drawable.edit_bg_red));
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(textView!=null){
                textView.setTextColor(getResources().getColor(R.color.click_color));
            }
            if(editText!=null) {
                editText.setBackground(getResources().getDrawable(R.drawable.edit_bg));
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        processDia.dismiss();
        SingleOptionPopup.closePopupWindow();
    }
}

