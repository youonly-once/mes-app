package com.shu.messystem.main_register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.shu.messystem.R;
import com.shu.messystem.component.ChangeStatusBgColor;
import com.shu.messystem.component.CustomProgressDialog;
import com.shu.messystem.component.GetGeneralInfomation;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2018/4/22.
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText registerPhone; //输入手机号
    EditText registerCode;//输入验证码
    Button getCodeBt;//获取验证码按钮
    EditText registerPws;//输入密码
    Button registerBt;//注册按钮
    int sec = 60;//验证码获取倒计时
    Handler handler = new Handler();//刷新 倒计时
    CustomProgressDialog progressDialog;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_forhome_activity);
        //初始化 自定义titlebar

        //初始化界面的控件
        initControlView();
        //手机状态栏 黑色字体
        ChangeStatusBgColor.SetStatusBgColor(this, R.color.white);
        progressDialog = new CustomProgressDialog(this, "注册中，请稍后");
    }



    private void initControlView() {
        registerPhone = findViewById(R.id.register_phone);
        registerCode = findViewById(R.id.register_code);
        registerPws = findViewById(R.id.register_pws);
        getCodeBt = findViewById(R.id.get_codebt);
        registerBt = findViewById(R.id.register_bt);
        getCodeBt.setOnClickListener(this);
        registerBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_codebt:
                getServerCode();
                break;
            case R.id.register_bt:
                register();
                break;
        }
    }

    //获得验证码
    private void getServerCode() {
        String mobileNumber = registerPhone.getText().toString();
        if (mobileNumber.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(RegisterActivity.this, "已发送", Toast.LENGTH_SHORT).show();
        codeTimer();
        /*Call<LoginReceptionCode> request = ManageRetrofit.getRetroInter(this).getSendStatus(mobileNumber);
        request.enqueue(new Callback<LoginReceptionCode>() {
            @Override
            public void onResponse(Call<LoginReceptionCode> call, Response<LoginReceptionCode> response) {
                try {
                    String msg = response.body().msg;
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(RegisterActivity.this, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<LoginReceptionCode> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "发送失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    //
    private void codeTimer() {
        getCodeBt.setEnabled(false);
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sec--;
                if (sec == 0) {
                    timer.cancel();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getCodeBt.setText("获取验证码");
                            getCodeBt.setEnabled(true);
                        }
                    });

                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            getCodeBt.setText(sec + "秒后重新获取");
                        }
                    });
                }

            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    //注册
    private void register() {
        HashMap<String, String> registerMap = getRegisterInfo();
        if (registerMap == null) {
            Toast.makeText(this, "请输入", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
       /* Call<RegisterReception> request = ManageRetrofit.getRetroInter(this).userRegister(registerMap);
        request.enqueue(new Callback<RegisterReception>() {
            @Override
            public void onResponse(Call<RegisterReception> call, Response<RegisterReception> response) {
                try {
                    String msg = response.body().msg;
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    if (msg.equals("注册成功")) {
                        finish();
                        Log.i(">>>>>>>>>>>Login", "注册成功:" + msg);
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(RegisterActivity.this, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
                } finally {
                    progressDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<RegisterReception> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "注册失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });*/
    }

    //得到用户输入信息
    private HashMap<String, String> getRegisterInfo() {
        String clientip;  //ip
        String equipment = "2"; //用户设备标识 1iOS 2Android 3 h5
        String mobileNumber;//手机号
        String codeNumber;//验证码
        String pwd;//密码
        clientip = GetGeneralInfomation.getIPAddress(this);
        mobileNumber = registerPhone.getText().toString();
        codeNumber = registerCode.getText().toString();
        pwd = registerPws.getText().toString();

        if (mobileNumber.isEmpty() || codeNumber.isEmpty() || pwd.isEmpty()) {
            return null;
        }
        HashMap<String, String> registerMap = new HashMap<String, String>();
        registerMap.put("clientip", clientip);
        registerMap.put("equipment", equipment);
        registerMap.put("code", codeNumber);
        registerMap.put("mobile", mobileNumber);
        registerMap.put("password", pwd);
        return registerMap;

    }

    /*private void login() {
        Call<LoginReceptionUserInfo> request = ManageRetrofit.getRetroInter(this).getUserInfo(null);
        request.enqueue(new Callback<LoginReceptionUserInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionUserInfo> call, Response<LoginReceptionUserInfo> response) {
//                status = response.body().status;
                String msg = response.body().msg;
                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (msg.equals("success")) {
                    response.body().insertMineInfo(RegisterActivity.this, "mineInfo_db", null, 1);
                    Intent intent = getIntent();
                    //Bundle bundle=new Bundle();
                    //通知主界面登陆完成，请刷新数据
                    setResult(1, intent);
                    SharedPreferences sharedPreferences = getSharedPreferences("token", MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("token", response.body().getId());
                    edit.apply();
                    finish();
                    Log.i(">>>>>>>>>>>Login", "登录成功" + msg + response.body().getId());
                }

            }

            @Override
            public void onFailure(Call<LoginReceptionUserInfo> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
   // }
}
