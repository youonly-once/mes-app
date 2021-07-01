package com.shu.messystem.main_login;

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
import com.shu.messystem.component.TitleBar;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginOtherActivity extends AppCompatActivity {
    private int identity;
    private CustomProgressDialog progressDialog;

    private EditText mobileEdit;//账号
    private EditText codeEdit;//密码
    private Button codeSendBt;//发送验证码
    private String phone;
    private String code;
    private Button loginBt;//登录按钮
    private int otherIdentity;//其他登录身份
    private Handler handler = new Handler();//刷新验证码延时
    private int sec = 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_forphone_fragment);
        initTitleBar();
        progressDialog = new CustomProgressDialog(this, "登录中，请稍后");
        initControlView();
        //状态栏字体和背景
        ChangeStatusBgColor.SetStatusBgColor(this, R.color.white);
    }

    private void setLoginType() {

    }

    private void initTitleBar() {
        TitleBar titleBar = findViewById(R.id.login_title_bar);
        identity = getIntent().getIntExtra("identity", 0);
        String title = "";
        switch (identity) {
            case 1:
                title = getResources().getString(R.string.jiamengs) + "登录";
                break;
            case 2:
                title = getResources().getString(R.string.hehuoren) + "登录";
                break;
            case 3:
                title = getResources().getString(R.string.kuaisongy) + "登录";
                break;
            case 4:
                title = getResources().getString(R.string.guanliyuan) + "登录";
                break;
        }
        //中间文字
        // 中间文字根据左右控件始终居中显示，自动排版
        titleBar.setTitle(title);
        //显示titlebar
        titleBar.setVisibility(View.VISIBLE);
        //隐藏其他登录
        findViewById(R.id.other_login).setVisibility(View.GONE);
        findViewById(R.id.other_login_tip).setVisibility(View.GONE);
    }

    private void initControlView() {
        loginBt = findViewById(R.id.login_bt);//登录按钮
        codeSendBt = findViewById(R.id.send_code);//发送验证码
        loginBt = findViewById(R.id.login_bt);//登录
        codeSendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getServerCode();
            }
        });
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!detectionInput()){
                    return;
                }
                switch (identity){
                    case 1://加盟商登录，商家
                        //loginForSeller();
                        break;
                    case 2://合伙人

                        break;
                    case 3://快递员

                        break;
                    case 4://管理员
                       // loginForAdmin();
                        break;
                }
            }
        });

        mobileEdit = findViewById(R.id.login_phone);//输入手机号
        codeEdit = findViewById(R.id.login_code);//输入验证码
    }


    private void getServerCode() {
        phone= mobileEdit.getText().toString();
        Log.e("mobileNumber", phone);

        if ( phone.length()!=11) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "已发送", Toast.LENGTH_SHORT).show();
        //发送后 60秒倒计时
        codeTimer();

 /*       Call<LoginReceptionCode> request = ManageRetrofit.getRetroInter(this).getSendStatus( phone);
        request.enqueue(new Callback<LoginReceptionCode>() {
            @Override
            public void onResponse(Call<LoginReceptionCode> call, Response<LoginReceptionCode> response) {
                try {
                    Log.e("MSG", response.body().msg);
                    Toast.makeText(LoginOtherActivity.this, response.body().msg, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(LoginOtherActivity.this, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<LoginReceptionCode> call, Throwable t) {
                Toast.makeText(LoginOtherActivity.this, "发送失败:"+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void codeTimer() {
        codeSendBt.setEnabled(false);
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
                            codeSendBt.setText("获取验证码");
                            codeSendBt.setEnabled(true);
                        }
                    });

                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            codeSendBt.setText(sec + "秒后重新获取");
                        }
                    });
                }

            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    private boolean detectionInput(){
        phone=mobileEdit.getText().toString();
        if (phone.length()!=11){
            Toast.makeText(LoginOtherActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return false;
        }
        code=codeEdit.getText().toString();
        //测试阶段 关闭
 /*       if (code.length()!=6){
            Toast.makeText(LoginOtherActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return false;
        }*/
        return true;
    }

   /* private void loginForSeller() {
        progressDialog.show();
        Call<LoginReceptionSellerInfo> request = ManageRetrofit.getRetroInter(this).getSellerInfo(phone,code);
        request.enqueue(new Callback<LoginReceptionSellerInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionSellerInfo> call, Response<LoginReceptionSellerInfo> response) {
                try {
                    Toast.makeText(LoginOtherActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    if (response.body().getMsg().equals("success")) {
                        //完成登录后的相关处理
                        progressDialog.dismiss();
                        Logined.saveSellerLoginInfo(LoginOtherActivity.this,response.body().getData());
                    }else{
                        progressDialog.hide();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(LoginOtherActivity.this, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Log.e("Error", e.getMessage());
                }finally {

                }
            }
            @Override
            public void onFailure(Call<LoginReceptionSellerInfo> call, Throwable t) {
                Toast.makeText(LoginOtherActivity.this, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });
    }
    private void loginForAdmin() {
        progressDialog.show();
        Call<LoginReceptionAdminInfo> request = ManageRetrofit.getRetroInter(this).getAdminInfo(phone,code, GetGeneralInfomation.getEquipment()+"");
        request.enqueue(new Callback<LoginReceptionAdminInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionAdminInfo> call, Response<LoginReceptionAdminInfo> response) {
                try {
                    Toast.makeText(LoginOtherActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    if (response.body().getMsg().equals("success")) {
                        //完成登录后的相关处理
                        progressDialog.dismiss();
                        Logined.saveAdminLoginInfo(LoginOtherActivity.this,response.body().getData());
                    }else{
                        progressDialog.hide();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(LoginOtherActivity.this, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Log.e("Error", e.getMessage());
                }finally {

                }
            }
            @Override
            public void onFailure(Call<LoginReceptionAdminInfo> call, Throwable t) {
                Toast.makeText(LoginOtherActivity.this, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });
    }*/
    //点击返回
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}