package com.shu.messystem;
 //启动界面

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.shu.messystem.operator_database.InsertRecord;

public class SplashActivity extends Activity {

    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        this.setContentView(R.layout.splash_layout);
        Handler handler=new Handler();
        SharedPreferences sharedLogin= getSharedPreferences(LoginActivity.Login_Info_SHARED,MODE_PRIVATE);
        handler.postDelayed(()-> {
                String userId=sharedLogin.getString("userid","null");
                String userName=sharedLogin.getString("username","null");
                String pwd=sharedLogin.getString("pwd","null");
                if(userId.equals("null") ||userName.equals("null")||pwd.equals("null") ) {
                    startActivity(new Intent(SplashActivity.this, com.shu.messystem.main_login.LoginActivity.class));
                }else{//已经登录，直接进入主界面
                    InsertRecord.getInstance().insertLoginRecord(SplashActivity.this,userId,userName,"direct_login");
                    Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                    intent.putExtra("userId", userId);// 传递工号和姓名
                    intent.putExtra("userName", userName);
                    startActivity(intent);
                }
                SplashActivity.this.finish();
            },1000);
        }
    }
