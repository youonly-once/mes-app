package com.shu.messystem.main_login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.shu.messystem.*;
import com.shu.messystem.LoginActivity;
import com.shu.messystem.component.CustomProgressDialog;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.operator_database.InsertRecord;
import com.shu.messystem.result_bean.GetUserInfoBean;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class LoginUserpasFragment extends Fragment {
    private Activity context;
    private View rootView;

    EditText mobileEdit;//账号
    EditText codeEdit;//密码
    Button loginBt;//登录按钮
    CustomProgressDialog progressDialog;
    public static final String Login_Info_SHARED="LoginInfo";
    private SharedPreferences sharedLogin;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_foruserpas_fragment, container, false);
        context = getActivity();
        this.rootView = rootView;
        initBtListener();
        progressDialog = new CustomProgressDialog(context, "登录中，请稍后");
        return rootView;
    }

    private void initBtListener() {

        mobileEdit = rootView.findViewById(R.id.login_user);
        codeEdit = rootView.findViewById(R.id.login_pws);

        loginBt = rootView.findViewById(R.id.login_bt);
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        sharedLogin = context.getSharedPreferences(Login_Info_SHARED, MODE_PRIVATE);
        mobileEdit.setText(sharedLogin.getString("userid", ""));
        codeEdit.setText(sharedLogin.getString("pwd", ""));
        mobileEdit.setSelection(mobileEdit.getText().length());// 光标默认在末尾
    }


    private void login() {
        progressDialog.show();
        HashMap<String, String> queryMap=new HashMap<>();
        String username=mobileEdit.getText().toString();
        String pws=codeEdit.getText().toString();
        if (username.isEmpty()||pws.isEmpty()){
            popupTipInfo("请输入账号或密码");
        }
        queryMap.put("userId",username);
        queryMap.put("pws",pws);
        Call<GetUserInfoBean> request = ManageRetrofit.getRetroInter(context).getUserInfo(queryMap);
        request.enqueue(new Callback<GetUserInfoBean>() {
            @Override
            public void onResponse(Call<GetUserInfoBean> call, Response<GetUserInfoBean> response) {
                if (response.body()!=null){


                    String msg = response.body().getMsg();
                    String usernameSer=response.body().getUsername();
                    String realnameSer=response.body().getRealname();
                    String pwsSer=response.body().getUpword();
                    Toast.makeText(context, msg , Toast.LENGTH_SHORT).show();
                    if ("success".equals(msg)) {//密码正确

                        saveLoginInfo(usernameSer,realnameSer,pwsSer);

                        progressDialog.hide();

                    } else {
                        InsertRecord.getInstance().insertLoginRecord(context,username,"","password_error");

                        popupTipInfo(msg);

                    }
                }else{
                    InsertRecord.getInstance().insertLoginRecord(context,username,"","password_error");

                    popupTipInfo("网络错误，请重试！");
                }
            }

            @Override
            public void onFailure(Call<GetUserInfoBean> call, Throwable t) {
                InsertRecord.getInstance().insertLoginRecord(context,username,"","登录错误"+t.getMessage());
                popupTipInfo("登录错误"+t.getMessage());
                t.printStackTrace();
            }
        });
    }
    private void saveLoginInfo(String userId,String userName,String pwd_server){
        //获得用户姓名
        SharedPreferences.Editor edit = sharedLogin.edit();// 编辑 该对象
        edit.putString("userid", userId);// 存放名为user 值为user的数据
        edit.putString("username", userName);
        //edit.putString("pwd", GetGeneralInfomation.md5(pwd_server));//加密存储密码
        edit.putString("pwd", pwd_server);
        edit.apply();                                        // 保存数据
        logined(userId, userName);// 登录进入主界面
        errorProcess();
        InsertRecord.getInstance().insertLoginRecord(context,userId,userName,"success");
    }
    // 登录成功后
    private void logined(String userId, String userName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("userId", userId);// 传递工号和姓名
        intent.putExtra("userName", userName);
        this.startActivity(intent);
        progressDialog.dismiss();
        context.finish();
    }

    private void errorProcess() {
      //  handler.post(new Runnable() {

           // public void run() {
                loginBt.setEnabled(true);
                //loginBt.setBackgroundColor(Color.rgb(0, 191, 255));
                progressDialog.hide();
           // }
       // });
    }

    private void popupTipInfo(final String tipInfo) {
        // 用handler 将子线程 加入到 主线程的 消息队列 等待执行 更新主界面GUI
        //handler.post(()->{
                    errorProcess();
                    Toast.makeText(context, tipInfo, Toast.LENGTH_SHORT).show();// 新建提示对话框

               // }
        //);
    }


}