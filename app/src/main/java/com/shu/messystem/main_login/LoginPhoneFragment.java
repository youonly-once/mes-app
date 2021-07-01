package com.shu.messystem.main_login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.shu.messystem.ManageRetrofit;
import com.shu.messystem.R;
import com.shu.messystem.RetrofitInterface;
import com.shu.messystem.component.CustomProgressDialog;
import com.shu.messystem.component.GetGeneralInfomation;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPhoneFragment extends Fragment implements View.OnClickListener {
    private Activity context;
    private View rootView;
    private String status;//发送验证码状态
    private String msg;//发送验证码 返回的消息
    private EditText mobileEdit;//账号
    private EditText codeEdit;//密码
    private Button codeSendBt;//发送验证码
    private Timer timer = new Timer();
    private Handler handler = new Handler();//刷新验证码延时
    private int sec = 60;
    private RetrofitInterface retrofitInter;
    private CustomProgressDialog progressDialog;
    private int currIdentity = 0;//当前登录类型 用户，合伙人，加盟商等，默认用户
    //提交登录所需信息
    private String phone;
    private String code;
    private String equipment;
    private String ip;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_forphone_fragment, container, false);
        context = getActivity();
        this.rootView = rootView;
        progressDialog = new CustomProgressDialog(context, "登录中，请稍后");
        retrofitInter = ManageRetrofit.getRetroInter(context);
        Bundle bundle = getArguments();
        currIdentity = bundle.getInt("identity", currIdentity);
        //初始化控件
        initBtListener();

        return rootView;
    }

    private void initBtListener() {
        codeSendBt = rootView.findViewById(R.id.send_code);
        Button loginBt = rootView.findViewById(R.id.login_bt);
        codeSendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getServerCode();
            }
        });
        //其他登录里面不显示 其他登录
        if(currIdentity!=0){
            rootView.findViewById(R.id.other_login_tip).setVisibility(View.GONE);
            rootView.findViewById(R.id.other_login).setVisibility(View.GONE);
        }
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!detectionInput()) {//输入信息不正确
                    return;
                }
                switch (currIdentity) {
                    case 0://用户登录
                        loginForUser();
                        break;
                    case 1://加盟商登录
                        loginForSeller();
                        break;
                    case 2://合伙人登录
                        loginForPartner();
                        break;
                    case 3://快递员登录
                        loginForCourier();
                        break;
                    case 4://管理员登录
                        loginForAdmin();
                        break;
                }

            }
        });
        mobileEdit = rootView.findViewById(R.id.login_phone);
        codeEdit = rootView.findViewById(R.id.login_code);

        TextView jiamengs = rootView.findViewById(R.id.jiamengshang);
        TextView hehuor = rootView.findViewById(R.id.hehuoren);
        TextView kuaisongy = rootView.findViewById(R.id.kuaidiyuan);
        TextView guanliyuan = rootView.findViewById(R.id.guanliyuan);
        jiamengs.setOnClickListener(this);
        hehuor.setOnClickListener(this);
        kuaisongy.setOnClickListener(this);
        guanliyuan.setOnClickListener(this);

    }

    public void onClick(View v) {
        int otherIdentity = -1;//其他登录身份
        int id = v.getId();
        switch (id) {
            case R.id.jiamengshang:
                otherIdentity = 1;
                break;
            case R.id.hehuoren:
                otherIdentity = 2;
                break;
            case R.id.kuaidiyuan:
                otherIdentity = 3;
                break;
            case R.id.guanliyuan:
                otherIdentity = 4;
                break;
        }
        // 跳转到其他类型登录界面
        //   Intent intent = new Intent(context, LoginOtherActivity.class);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra("identity", otherIdentity);
        context.startActivityForResult(intent, LoginActivity.otherLoginResult);
    }

    private void getServerCode() {
        String mobileNumber = mobileEdit.getText().toString();
        Log.e("mobileNumber", mobileNumber);
        if (mobileNumber.isEmpty()) {
            Toast.makeText(context, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "已发送", Toast.LENGTH_SHORT).show();
        Log.e("click", "已发送");
        codeTimer();
        /*Call<LoginReceptionCode> request = retrofitInter.getSendStatus(mobileNumber);
        request.enqueue(new Callback<LoginReceptionCode>() {
            @Override
            public void onResponse(Call<LoginReceptionCode> call, Response<LoginReceptionCode> response) {
                try {
                    status = response.body().status;
                    msg = response.body().msg;
                    Log.e("MSG", msg);
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    Toast.makeText(context, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<LoginReceptionCode> call, Throwable t) {
                msg = "发送失败:";
                Toast.makeText(context, msg + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void codeTimer() {
        sec = 60;
        codeSendBt.setEnabled(false);
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
                            codeSendBt.setText(sec + getString(R.string.getcode));
                        }
                    });
                }

            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    //用户登录
    private void loginForUser() {
        progressDialog.show();
        /*Call<LoginReceptionUserInfo> request = retrofitInter.getUserInfo(getUserLoginInfo());
        request.enqueue(new Callback<LoginReceptionUserInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionUserInfo> call, Response<LoginReceptionUserInfo> response) {
                try {
                    msg = response.body().msg;
                    if (msg.equals("success")) {
                        //登录成功后的相关处理
                        progressDialog.dismiss();
                        Logined.saveUserLoginInfo(context, response.body());
                        context.finish();

                    } else {
                        progressDialog.hide();
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException e) {
                    progressDialog.hide();
                    Toast.makeText(context, "登录失败，请重试", Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
                } finally {

                }

            }

            @Override
            public void onFailure(Call<LoginReceptionUserInfo> call, Throwable t) {
                msg = "登录失败:" + t.getMessage();
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });*/
    }

    private void loginForSeller() {
        progressDialog.show();
        /*Call<LoginReceptionSellerInfo> request = ManageRetrofit.getRetroInter(context).getSellerInfo(phone, code);
        request.enqueue(new Callback<LoginReceptionSellerInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionSellerInfo> call, Response<LoginReceptionSellerInfo> response) {
                try {
                    Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    if (response.body().getMsg().equals("success")) {
                        //完成登录后的相关处理
                        progressDialog.dismiss();
                        Logined.saveSellerLoginInfo(context, response.body().getData());
                    } else {
                        progressDialog.hide();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(context, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Log.e("Error", e.getMessage());
                } finally {

                }
            }

            @Override
            public void onFailure(Call<LoginReceptionSellerInfo> call, Throwable t) {
                Toast.makeText(context, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });*/
    }
    private void loginForPartner() {
        progressDialog.show();
        /*Call<LoginReceptionPartnerInfo> request= ManageRetrofit.getRetroInter(context).getPartnerInfo(phone,code);
        request.enqueue(new Callback<LoginReceptionPartnerInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionPartnerInfo> call, Response<LoginReceptionPartnerInfo> response) {
                try {
                    Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    if (response.body().getMsg().equals("success")) {
                        //完成登录后的相关处理
                        progressDialog.dismiss();
                        Logined.savePartnerLoginInfo(context, response.body().getData());
                    } else {
                        progressDialog.hide();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(context, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Log.e("Error", e.getMessage());
                } finally {

                }
            }
            @Override
            public void onFailure(Call<LoginReceptionPartnerInfo> call, Throwable t) {
                Toast.makeText(context, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });*/
    }
    private void loginForCourier() {
        progressDialog.show();
       /* Call<LoginReceptionCourierInfo> request = ManageRetrofit.getRetroInter(context).getCourierInfo(phone, code);
        request.enqueue(new Callback<LoginReceptionCourierInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionCourierInfo> call, Response<LoginReceptionCourierInfo> response) {
                try {
                    Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    if (response.body().getMsg().equals("success")) {
                        //完成登录后的相关处理
                        progressDialog.dismiss();
                        Logined.saveCourierLoginInfo(context, response.body().getData());
                    } else {
                        progressDialog.hide();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(context, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Log.e("Error", e.getMessage());
                } finally {

                }
            }

            @Override
            public void onFailure(Call<LoginReceptionCourierInfo> call, Throwable t) {
                Toast.makeText(context, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });*/
    }
    private void loginForAdmin() {
        Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
        /* progressDialog.show();

       Call<LoginReceptionAdminInfo> request = ManageRetrofit.getRetroInter(context).getAdminInfo(phone, code, equipment);
        request.enqueue(new Callback<LoginReceptionAdminInfo>() {
            @Override
            public void onResponse(Call<LoginReceptionAdminInfo> call, Response<LoginReceptionAdminInfo> response) {
                try {
                    Toast.makeText(context, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    if (response.body().getMsg().equals("success")) {
                        //完成登录后的相关处理
                        progressDialog.dismiss();
                        Logined.saveAdminLoginInfo(context, response.body().getData());
                    } else {
                        progressDialog.hide();
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(context, "出现错误，请重试", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Log.e("Error", e.getMessage());
                } finally {

                }
            }

            @Override
            public void onFailure(Call<LoginReceptionAdminInfo> call, Throwable t) {
                Toast.makeText(context, "登录失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        });*/
    }

    private boolean detectionInput() {
        phone = mobileEdit.getText().toString();
        code = codeEdit.getText().toString();
        if (phone.length() != 11) {
            Toast.makeText(context, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return false;
        }/*else  if (code.length()!=6) {
            Toast.makeText(context, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
            return false;
        }*/
        ip = GetGeneralInfomation.getIPAddress(context);//ip
        equipment = GetGeneralInfomation.getEquipment() + "";//用户设备标识 1iOS 2Android 3 h5
        return true;
    }

    private HashMap<String, String> getUserLoginInfo() {
        String logintype = "1"; //1 登录状态 1 手机验证码  2 账户密码
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("clientip", ip);
        queryMap.put("equipment", equipment);
        queryMap.put("logintype", logintype);
        queryMap.put("mobile", phone);
        queryMap.put("password", code);
        return queryMap;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("LoginPhoneFragment", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("LoginPhoneFragment", "onDestroy");
        timer.cancel();
        progressDialog.dismiss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("LoginPhoneFragment", "onDetach");
    }
}