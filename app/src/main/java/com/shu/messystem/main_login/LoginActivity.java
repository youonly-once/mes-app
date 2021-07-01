package com.shu.messystem.main_login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.shu.messystem.R;
import com.shu.messystem.SettingActivity;
import com.shu.messystem.component.ChangeStatusBgColor;
import com.shu.messystem.component.PagerSlidingTabStrip;
import com.shu.messystem.component.TitleBar;
import com.shu.messystem.main_register.RegisterActivity;
import com.shu.messystem.update_component.UpdateServices;
import com.shu.messystem.update_component.UpdateVersion;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1){
                //loginBt.setEnabled(true);
            }else if (msg.what==0){
                //loginBt.setEnabled(false);
            }
            super.handleMessage(msg);


        }
    };
    private ImageView mCollectView;
    private PagerSlidingTabStrip pst;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments;
    //声明pst的标题
    private String[] titles = { "账号密码登录","手机号快捷登录"};
    private int identity=0;//登录类型
    static final int otherLoginResult=100;
    private UpdateVersion updateVersion;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //背景图覆盖状态栏
        //backgroundFullScreen();//需在加载内容前
        setContentView(R.layout.login_main);// 加载布局

        Log.e("LoginActivity", "onCreate()");

        setContentView(R.layout.login_formain_activity);
        initTitleBar();
        initPst();
        ChangeStatusBgColor.SetStatusBgColor(this, R.color.bg1);

        //请求读取外存权限
        requestPower();

        this.startService(new Intent(this,UpdateServices.class));
        //检查更新
        updateVersion= UpdateVersion.getInstance(this,handler);
        updateVersion.startUpdate();

    }
    private void requestPower() {
        String []grant = new String[]{
                //Manifest.permission.VIBRATE
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // ,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
        //,Manifest.permission.CHANGE_WIFI_STATE
        // , Manifest.permission.ACCESS_WIFI_STATE};

        if (//ContextCompat.checkSelfPermission(this,Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //|| ContextCompat.checkSelfPermission(this,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS) != PackageManager.PERMISSION_GRANTED
            //|| ContextCompat.checkSelfPermission(this,Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
            //|| ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) { //判断是否已经赋予权限

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) { //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
                //Toast.makeText(this, "您需要同意申请权限才能使用进行二维码扫描、存储历史记录、分享", Toast.LENGTH_LONG).show();
                //这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
            }
            //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
            ActivityCompat.requestPermissions(this,grant, 1);

        }
    }


    private void initTitleBar() {
        TitleBar titleBar = findViewById(R.id.login_title_bar);
        identity = getIntent().getIntExtra("identity", 0);
        if(identity==0){
            titleBar.addAction(new TitleBar.TextAction("设置") {
                @Override
                public void performAction(View view) {
                    //打开注册 界面
                    Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                    startActivity(intent);
                }
            });
        }
        String title = "登录";
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
    }

    private void initPst() {
        pst = findViewById(R.id.pst);
        viewPager = findViewById(R.id.pager);

        fragments = new ArrayList<>();
        LoginPhoneFragment houFragment = new LoginPhoneFragment();
        LoginUserpasFragment panFragment = new LoginUserpasFragment();
        //向Fragment传递登录类型
        Bundle bundle=new Bundle();
        bundle.putInt("identity",identity);
        houFragment.setArguments(bundle);
        panFragment.setArguments(bundle);
        //添加fragment到集合中时注意顺序
        fragments.add(panFragment);
        fragments.add(houFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new LoginActivity.MyPagerAdapter(fragmentManager, titles, fragments));
        //首次显示那个Fragment
        viewPager.setCurrentItem(0);
        //当ViewPager的onPagerChangeListener回调时，PagerSlidingTabStrip也一起随之变动
        //具体做法都已封装到了PagerSlidingTabStrip.setViewPager()方法里，使用时调用实例如下
        pst.setViewPager(viewPager);
        //字体颜色
        pst.setTextColor(R.color.font_color1);
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        private String[] titles;
        ArrayList<Fragment> fragments;

        //根据需求定义构造方法，方便外部调用
        MyPagerAdapter(FragmentManager fm, String[] titles, ArrayList<Fragment> fragments) {
            super(fm);
            this.titles = titles;
            this.fragments = fragments;
        }

        //设置每页的标题
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        //设置每一页对应的fragment
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        //fragment的数量
        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("LoginActivity", "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateVersion.startUpdate();
        Log.e("LoginActivity", "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("LoginActivity", "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("LoginActivity", "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateVersion.detectionUpdateDialog!=null){
            updateVersion.detectionUpdateDialog.dismiss();
        }
        if (updateVersion.downloadingDialog!=null){
            updateVersion.downloadingDialog.dismiss();
        }
        Log.e("LoginActivity", "onDestory()");
    }
}