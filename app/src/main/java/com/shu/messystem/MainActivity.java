package com.shu.messystem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.client.android.CaptureActivity;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.component.TipDialogForIos;
import com.shu.messystem.downtime.DownTimeFragementMain;
import com.shu.messystem.downtime_edit.DowntimeEditFragment;
import com.shu.messystem.outputperhour.OutputPerHourFragementMain;
import com.shu.messystem.plantime.PlanTimeForStopFragementMain;
import com.shu.messystem.result_bean.GetUserInfoBean;
import com.shu.messystem.update_component.UpdateServices;
import com.shu.messystem.update_component.UpdateVersion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout = null;
    private FrameLayout includeCehuaLayout = null;
    public static Toolbar toolbar = null;
    private FragmentTransaction ft = null;
    private List<String> parent = null;
    private Map<String, List<String>> map = null;
    public Fragment currentFragment = null;//保存当前Fragement 以备刷新
    final private String mainFlag = "MainFlag";//保存打开软件时显示哪个Fragement
    private int refreshStat = 1;//防止刷新频繁
    private Handler handler = null;
    private String grantString;//拥有的权限代码
    private String currentFragmentTag = "01";//主界面
    public static String gonghao = "null";//PlanTimeForStopFragment会调用
    public static String name = "null";//PlanTimeForStopFragment会调用
    private ServiceConnection conne;
    private ActionBarDrawerToggle drawerListener;
    private UpdateServices updateServices;
    private boolean isReplaceFragment=false;//侧滑菜单关闭时，判断用户是否点击fragment 是则切换fragment

private int currGroupPosition;
private int currChildPosition;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //恢复currentFragment
        if (savedInstanceState != null) {
            restoreFragment(savedInstanceState.getString("TAG", currentFragmentTag));
        } else {//第一次加载时
            //加载 主fragment   supportV4中要用getSupportFragmentManager(),否则用getFragmentManager()
            showMainFragment();
        }

        //初始化各种元素
        initCreateView();
        //初始化 菜单栏
        initActionBar();
        //初始化 侧滑菜单
        initCehuaLayout();
        //初始化配置信息
        initConfig();
        //查询权限信息
        queryGrantString();
        //drawerLayout.openDrawer(includeCehuaLayout);//初始时显示侧滑菜单
        Log.e("MainActivity", "onCreate()");
        beginUpdateServ();
    }
    private void queryGrantString() {

                Call<GetUserInfoBean> request = ManageRetrofit.getRetroInter(MainActivity.this).getUserPermission(gonghao);
                request.enqueue(new Callback<GetUserInfoBean>() {
                    @Override
                    public void onResponse(Call<GetUserInfoBean> call, Response<GetUserInfoBean> response) {
                        if (response.body()!=null){
                            String msg = response.body().getMsg();
                            if ("success".equals(msg)) {//密码正确
                                grantString = response.body().getPermission();

                            } else {
                                grantString="-1"+msg;
                            }
                        }else{
                            grantString="-1网络错误，请重新打开软件尝试";
                        }

                    }

                    @Override
                    public void onFailure(Call<GetUserInfoBean> call, Throwable t) {
                        grantString="-1网络错误，请重新打开软件尝试";
                    }
                });

    }

    private void initConfig() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();//编辑 该对象
        edit.putString("server_addr", sharedPreferences.getString("server_addr", SettingActivity.serAddr));
        edit.putString("update_addr", sharedPreferences.getString("update_addr", SettingActivity.updaAddr));
        edit.putString("updateinfo_filename", sharedPreferences.getString("updateinfo_filename", SettingActivity.updaFileName));

        edit.apply();
    }

    private void beginUpdateServ() {
        conne = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                UpdateServices.MyBinder myBinder = (UpdateServices.MyBinder) service;
                myBinder.setContext(MainActivity.this, handler);
                updateServices=myBinder.getService();
                Log.e("setContext ac", "setContext ac");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                updateServices=null;
            }
        };
        Intent serv = new Intent(MainActivity.this, UpdateServices.class);
        bindService(serv, conne, Service.BIND_AUTO_CREATE);
    }

    private void restoreFragment(String restoreTag) {
        //隐藏所有Fragment
        ft = getSupportFragmentManager().beginTransaction();
        Fragment frag;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 6; j++) {
                frag = getSupportFragmentManager().findFragmentByTag(String.valueOf(i) + String.valueOf(j));
                if (frag != null) {
                    ft.hide(frag);
                }
            }
        }
        ft.commit();

        //显示 上次Activity生命周期内 显示的Fragment

        frag = getSupportFragmentManager().findFragmentByTag(restoreTag);
        if (frag == null) {
            showMainFragment();
        } else {
            currentFragment = frag;
            ft = getSupportFragmentManager().beginTransaction();
            ft.show(frag)
                    .commit();
        }

    }

    private void initCreateView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);//主界面 DrawerLayout布局
        includeCehuaLayout = (FrameLayout) findViewById(R.id.include_cehua_layout);//侧滑菜单
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        parent = new ArrayList<>();
        map = new HashMap<>();
        handler = new Handler();

    }

    private void initActionBar() {
        toolbar.setTitle("MES系统");
        toolbar.setSubtitle("重庆滚筒");
        //toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        //toolbar.setSubtitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();
        if (a != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //侧滑事件
        /*ActionBarDrawerToggle menuAction=new ActionBarDrawerToggle(LoginedMain.this,drawerLayout,toolbar, 0, 0) {
                          @Override
				          public void onDrawerOpened(View drawerView) {
				              super.onDrawerOpened(drawerView);
				               // mAnimationDrawable.stop();
				            }
				           @Override
				           public void onDrawerClosed(View drawerView) {
				                super.onDrawerClosed(drawerView);
				               // mAnimationDrawable.start();
				            }
			 };*/
        //导航按钮点击事件
        toolbar.setNavigationOnClickListener((View v) -> {
                    drawerLayout.openDrawer(includeCehuaLayout);//显示侧滑菜单
                }
        );


    }

    private void initCehuaLayout() {
        //设置工号
        TextView userInfo = ((TextView) findViewById(R.id.cehua_userid));
        gonghao = getIntent().getStringExtra("userId");
        if (userInfo != null) {
            userInfo.setText(gonghao);
        }
        //设置姓名
        name = this.getIntent().getStringExtra("userName");
        userInfo = ((TextView) findViewById(R.id.cehua_username));
        if (userInfo != null) {
            userInfo.setText(name);
        }

        ExpandableListView cehuaLayoutListView = (ExpandableListView) findViewById(R.id.expandablelistview1);
        if (cehuaLayoutListView != null) {
            cehuaLayoutListView.setAdapter(new ExpandAdapter());//添加列表

            cehuaLayoutListView.setOnChildClickListener((ExpandableListView parent, View v, int groupPosition, int childPosition,
                                                         long id) -> {
                        //侧滑 菜单中 列表子项点击事件
                        //第一组groupPosition=  0 ,第一个子项childPosition =   0
                        cehuaOnItemClick(groupPosition, childPosition);
                        return true;
                    }
            );
            //侧滑菜单长按事件
            cehuaLayoutListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View childView, int flatPos, long id) {
                    if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        long packedPos = ((ExpandableListView) parent).getExpandableListPosition(flatPos);
                        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPos);
                        int childPosition = ExpandableListView.getPackedPositionChild(packedPos);

                        if(!isGrant(groupPosition,childPosition)){//无权
                            return false;
                        }
                        String mainStr= GetGeneralInfomation.md5(groupPosition + "" + childPosition);
                        if(TextUtils.isEmpty(mainStr)){
                            Toast.makeText(MainActivity.this, "设置主界面失败.", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        //长按设置主界面初始显示哪个fragement
                        SharedPreferences sharedMainFrag = getSharedPreferences(mainFlag, MODE_PRIVATE);
                        SharedPreferences.Editor edit = sharedMainFrag.edit();
                        edit.putString(mainFlag, mainStr);
                        if (edit.commit()) {
                            Toast.makeText(MainActivity.this, "设置主界面成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "设置主界面失败", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                    return false;
                }

            });
        }
        drawerListener = new ActionBarDrawerToggle(this, drawerLayout,0,0) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.e("侧滑菜单打开","侧滑菜单打开");
            }

            @Override//菜单关闭完成以后再 切换fragment 否则会卡顿
            public void onDrawerClosed(View drawerView) {
                //fragment存在则直接显示
                String tag = String.valueOf(currGroupPosition) + String.valueOf(currChildPosition);//创建fragment标志
                Fragment temp = getSupportFragmentManager().findFragmentByTag(tag);

                Log.e("侧滑菜单关闭","侧滑菜单关闭");
                if(isReplaceFragment){//用户选择后
                    if (temp != null) {
                        replaceFragment(null);
                    }else{
                        showFragment(currGroupPosition,currChildPosition);
                    }
                    isReplaceFragment=false;
                }
                super.onDrawerClosed(drawerView);

            }
        };
        drawerLayout.addDrawerListener(drawerListener);
    }

    private void showMainFragment() {//显示用户设置的主界面
        ft = getSupportFragmentManager().beginTransaction();
        SharedPreferences sharedMainFrag = this.getSharedPreferences(mainFlag, MODE_PRIVATE);
        currentFragmentTag = sharedMainFrag.getString(mainFlag, currentFragmentTag);
        if (currentFragmentTag.equals(GetGeneralInfomation.md5("01"))) {
        } else if (currentFragmentTag.equals(GetGeneralInfomation.md5("01"))) {
            currentFragment = new DownTimeFragementMain();

        } else if (currentFragmentTag.equals(GetGeneralInfomation.md5("02"))) {
            currentFragment = new DowntimeEditFragment();

        } else if (currentFragmentTag.equals(GetGeneralInfomation.md5("03"))) {
            currentFragment = new OutputPerHourFragementMain();

        } else if (currentFragmentTag.equals(GetGeneralInfomation.md5("16"))) {
            requestPower();//请求相机权限

        } else if (currentFragmentTag.equals(GetGeneralInfomation.md5("20"))) {
            currentFragment = new PlanTimeForStopFragementMain();

        }
        if (currentFragment != null) {
            ft.add(R.id.fragement_frame, currentFragment, currentFragmentTag).commit();
        }
    }
    private boolean isGrant(int groupPosition, int childPosition){
        if(grantString==null){
            Toast.makeText(MainActivity.this, "正在初始化，请稍后", Toast.LENGTH_SHORT).show();
 /*           if(!queryGrantThread.isAlive()){//如果查询线程已经died 则开启线程重新查询
                queryGrantString();
            }*/
            return false;
        }
        if (grantString.indexOf("-1")==0){
            Toast.makeText(MainActivity.this, grantString.substring(2), Toast.LENGTH_SHORT).show();
            queryGrantString();
            return  false;
        }
        if ( !grantString.contains(groupPosition + "" + childPosition)) {
            Toast.makeText(MainActivity.this, "请联系管理员添加权限", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void cehuaOnItemClick(int groupPosition, int childPosition) {
        if(!isGrant(groupPosition,childPosition)){//没有权限
            return;
        }
        isReplaceFragment=true;
        currGroupPosition=groupPosition;
        currChildPosition=childPosition;
       drawerLayout.closeDrawer(includeCehuaLayout);//关闭侧滑菜单

//设置titlebar的标题和子标题
        String groupName = parent.get(groupPosition);//组名称
        String childName = map.get(groupName).get(childPosition);//子项名称
        toolbar.setTitle(groupName);   //标题
        toolbar.setSubtitle(childName);//子标题

    }
    private void showFragment(int groupPosition,int childPosition){
        switch (groupPosition) {
            case 0://0 组
                switch (childPosition) {
                    case 0://下线数据
                        break;
                    case 1://停机时数据
                        replaceFragment(new DownTimeFragementMain());
                        break;
                    case 2://停机时维护
                        replaceFragment(new DowntimeEditFragment());
                        break;
                    case 3://单小时产量
                        replaceFragment(new OutputPerHourFragementMain());
                        break;
                    default:
                }
                break;
            case 1://1组
                switch (childPosition) {
                    case 6://二维码扫描
                        requestPower();//请求相机权限
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "升级中", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2: //2组 系统配置管理
                switch (childPosition) {
                    case 0://计划停机维护
                        replaceFragment(new PlanTimeForStopFragementMain());
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "升级中", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
    //申请权限
    private void requestPower() {
        String grant[] = {Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) { //判断是否已经赋予权限

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    ) { //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
                Toast.makeText(this, "您需要同意申请权限才能使用进行二维码扫描、存储历史记录、分享", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this, grant, 1);//申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果

        } else {
            startActivity(new Intent(this, CaptureActivity.class));
        }

    }

    //权限申请结果
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
                    startActivity(new Intent(this, CaptureActivity.class));
                } else { //拒绝权限申请
                    Toast.makeText(this, "你将不能使用相机扫描二维码。", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void replaceFragment(Fragment fragement) {
        //传递参数fragment为NULL时，表示 此点击fragment已存在

        String tag = String.valueOf(currGroupPosition) + String.valueOf(currChildPosition);//创建fragment标志
        if(fragement == null && currentFragment!=null ){//如果准备加载的页面已加载
            Fragment temp=getSupportFragmentManager().findFragmentByTag(tag);
            Log.e("n",temp.getClass().getName());
            Log.e("n",currentFragment.getClass().getName());
            if(temp.getClass().getName().equals(currentFragment.getClass().getName())){//且准备加载的页面与显示的页面相同
                return;
            }

        }

        //隐藏当前fragement
        ft = getSupportFragmentManager().beginTransaction();
      //  ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft .setCustomAnimations(R.anim.fragment_right_in_anim
                ,R.anim.fragment_left_out_anim
                ,R.anim.fragment_left_in_anim
                ,R.anim.fragment_right_out_anim);
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }
        if (fragement != null) {
            ft.add(R.id.fragement_frame, fragement, tag);
            ft.show(fragement);
            currentFragment = fragement;
        } else {//传递参数fragment为NULL时，表示 此点击fragment已存在
            currentFragment = getSupportFragmentManager().findFragmentByTag(tag);
            ft.show(currentFragment);
        }
                ft.commit();//更换主页面内容
    }

    //重写  菜单项 选择事件
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            if (refreshStat == 1) {
                refreshStat = 0;
                try {
                    @SuppressWarnings("rawtypes")

                    Class c = Class.forName(currentFragment.getClass().getName());//获取子类
                    c.getDeclaredMethod("queryThread").invoke(currentFragment);//执行query方法刷新Fraement
                } catch (IllegalAccessException e) {
                    Log.e("MainActivity", "IllegalAccessException");
                } catch (InvocationTargetException e) {
                    Log.e("MainActivity", "InvocationTargetException");
                } catch (NoSuchMethodException e) {
                    Log.e("MainActivity", "NoSuchMethodException");
                } catch (ClassNotFoundException e) {
                    Log.e("MainActivity", "ClassNotFoundException");
                }
                //定时器防止频繁刷新
                new Handler().postDelayed(() -> {
                    refreshStat = 1;
                }, 1000 * 10); //5分后刷新按钮可用

            } else {
                Toast.makeText(MainActivity.this, "刷新太频繁,请10秒后再试", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.exit) {//退出到登陆界面
            startActivity(new Intent(MainActivity.this, com.shu.messystem.main_login.LoginActivity.class));
            //删除用户名
            SharedPreferences sharedLogin = this.getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedLogin.edit();
            edit.remove("username");
            edit.apply();
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("InflateParams")
    class ExpandAdapter extends BaseExpandableListAdapter implements View.OnTouchListener {
        Drawable[][] drawable = new Drawable[3][];
        float mPosX;//触摸时坐标
        float mPosY;
        float mCurPosX;//滑动后坐标
        float mCurPosY;

        ExpandAdapter() {
            initDrawable();

            initList();

        }

        @SuppressWarnings("deprecation")
        private void initDrawable() {
            //子列表 加载图标资源
            if (VERSION.SDK_INT <= 17) {
                drawable[0] = new Drawable[4];
                drawable[0][0] = getResources().getDrawable(R.drawable.search);
                drawable[0][1] = getResources().getDrawable(R.drawable.search);
                drawable[0][2] = getResources().getDrawable(R.drawable.search);
                drawable[0][3] = getResources().getDrawable(R.drawable.search);

                drawable[1] = new Drawable[7];
                drawable[1][0] = getResources().getDrawable(R.drawable.case3d2);
                drawable[1][1] = getResources().getDrawable(R.drawable.anjianyi);
                drawable[1][2] = getResources().getDrawable(R.drawable.book);
                drawable[1][3] = getResources().getDrawable(R.drawable.nengxiaotie);
                drawable[1][4] = getResources().getDrawable(R.drawable.engineererror);
                drawable[1][5] = getResources().getDrawable(R.drawable.washing);
                drawable[1][6] = getResources().getDrawable(R.drawable.search);

                drawable[2] = new Drawable[3];
                drawable[2][0] = getResources().getDrawable(R.drawable.config);
                drawable[2][1] = getResources().getDrawable(R.drawable.user);
                drawable[2][2] = getResources().getDrawable(R.drawable.config);
            } else {//android 4.0不支持此方法
                drawable[0] = new Drawable[4];
                drawable[0][0] = getResources().getDrawable(R.drawable.search, null);
                drawable[0][1] = getResources().getDrawable(R.drawable.search, null);
                drawable[0][2] = getResources().getDrawable(R.drawable.search, null);
                drawable[0][3] = getResources().getDrawable(R.drawable.search);

                drawable[1] = new Drawable[7];
                drawable[1][0] = getResources().getDrawable(R.drawable.case3d2, null);
                drawable[1][1] = getResources().getDrawable(R.drawable.anjianyi, null);
                drawable[1][2] = getResources().getDrawable(R.drawable.book, null);
                drawable[1][3] = getResources().getDrawable(R.drawable.nengxiaotie, null);
                drawable[1][4] = getResources().getDrawable(R.drawable.engineererror, null);
                drawable[1][5] = getResources().getDrawable(R.drawable.washing, null);
                drawable[1][6] = getResources().getDrawable(R.drawable.qrcode_scan, null);

                drawable[2] = new Drawable[3];
                drawable[2][0] = getResources().getDrawable(R.drawable.clock_plan, null);
                drawable[2][1] = getResources().getDrawable(R.drawable.user, null);
                drawable[2][2] = getResources().getDrawable(R.drawable.config, null);
            }
        }

        private void initList() {
            parent.add("生产过程管理");
            parent.add("质量不良管理");
            parent.add("系统配置管理");

            List<String> productionList = new ArrayList<>();
            productionList.add("下线数据查询");
            productionList.add("停机时数据查询");
            productionList.add("A08IP/MAC管理");
            productionList.add("单小时产量查询");
            map.put("生产过程管理", productionList);

            List<String> qualityList = new ArrayList<>();
            qualityList.add("包装箱绑定查询");
            qualityList.add("安检仪检测查询");
            qualityList.add("说明书绑定查询");
            qualityList.add("能耗贴检测查询");
            qualityList.add("工程不良查询");
            qualityList.add("落地条码查询");
            qualityList.add("二维码扫描查询");
            map.put("质量不良管理", qualityList);

            List<String> configList = new ArrayList<>();
            configList.add("办公电脑新增");
            configList.add("用户账号管理");
            configList.add("数据采集权限");
            /*
			configList.add("child3-2");
			configList.add("child3-3");
			*/
            map.put("系统配置管理", configList);
        }

        //获取 组 数量
        public int getGroupCount() {
            // TODO Auto-generated method stub
            return parent.size();
        }

        //获取当前父item下的子item的个数
        public int getChildrenCount(int groupPosition) {
            String key = parent.get(groupPosition);//父
            return map.get(key).size();//子数量
        }

        //获取当前父item的数据
        public Object getGroup(int groupPosition) {
            return parent.get(groupPosition);
        }

        //得到子item需要关联的数据
        public Object getChild(int groupPosition, int childPosition) {
            String key = parent.get(groupPosition);//服编号
            return (map.get(key).get(childPosition));
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.expandable_parent, null);

            }
            TextView tv = (TextView) convertView.findViewById(R.id.parent_textview);
            tv.setText(MainActivity.this.parent.get(groupPosition));
            return convertView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            {
                String key = MainActivity.this.parent.get(groupPosition);
                String info = map.get(key).get(childPosition);
                ViewHolder viewHolder=null;
                if (convertView == null) {
                    viewHolder=new ViewHolder();
                        convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.expandable_children, null);
                        convertView.setTag(viewHolder);
                }else{
                    viewHolder=(ViewHolder)convertView.getTag();
                }
                viewHolder. tv = (TextView) convertView.findViewById(R.id.children_textview);
                viewHolder. tv.setText(info);

                //设置子列表图标
                Drawable drawable_temp = drawable[groupPosition][childPosition];
                //Drawable drawable= getResources().getDrawableForDensity(R.drawable.find, DisplayMetrics.DENSITY_XHIGH, null);
                drawable_temp.setBounds(drawable_temp.getMinimumWidth(), 0, drawable_temp.getMinimumWidth() * 2, drawable_temp.getMinimumHeight());
                viewHolder. tv.setCompoundDrawables(drawable_temp, null, null, null);//设置TextView的drawableleft
                viewHolder. tv.setCompoundDrawablePadding(10);//设置图片和text之间的间距
                //tv.setOnTouchListener(this);//为子项设置触摸监听
                // tv.setOnLongClickListener(this);

                return convertView;
            }
        }
        class ViewHolder{
            TextView tv;

        }
        //触摸滑动
        public boolean onTouch(View v, MotionEvent event) {
            Log.e("POSX", String.valueOf(event.getX()));
            Log.e("POSY", String.valueOf(event.getY()));
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPosX = event.getX();
                    mPosY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurPosX = event.getX();
                    mCurPosY = event.getY();

                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("CHA", String.valueOf(mCurPosY - mPosY));
                    Log.e("CHA", String.valueOf(mCurPosX - mPosX));
                    if (mCurPosY - mPosY > 0) {
                        //向下滑動
                        Log.e("向下滑动", "向下滑动");

                    }
                    if (mCurPosY - mPosY < 0) {
                        //向上滑动
                        Log.e("向上滑动", "向上滑动");
                    }
                    if (mCurPosX - mPosX < 0) {
                        //向上滑动
                        Log.e("向左滑动", "向左滑动");
                    }
                    if (mCurPosX - mPosX > 0) {
                        //向上滑动
                        Log.e("向右滑动", "向右滑动");
                    }

                    break;
            }
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return true;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单文件  
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void onStart() {
        super.onStart();
        Log.e("MainActivity", "onStart()");
    }

    public void onResume() {
        super.onResume();
        if (currentFragment == null) {//从二维码扫描界面回来时空白
            currGroupPosition=0;
            currChildPosition=1;
            replaceFragment(new DownTimeFragementMain());
        }
        Log.e("MainActivity", "onResume()");
    }

    public void onPause() {
        super.onPause();
        Log.e("MainActivity", "onPause()");
    }

    public void onStop() {
        super.onStop();
        Log.e("MainActivity", "onStop()");
    }

    public void onDestroy() {
        super.onDestroy();
        updateServices.onDestroyActivity();
        try {
            unbindService(conne);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        Log.e("MainActivity", "onDestory()");
    }

    protected void onSaveInstanceState(Bundle outState) {
        Log.e("MainActivity", "onSaveInstanceState()");

        //保存当前Activity显示的 Fragment的 标志   以便锁屏后  旋转屏幕后  onCreate重建时恢复显示该Fragment
        if (currentFragment != null) {
            outState.putString("TAG", currentFragment.getTag());
        }

        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== UpdateVersion.SetInstallApkPermi && updateServices!=null){
            //android8.0设置允许apk后 跳转检查
            updateServices.setInstallApkPermiResult();
        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                tipExit();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //退出提示
    private void tipExit(){
        TipDialogForIos.builder builder=new TipDialogForIos.builder(this);
        final TipDialogForIos tipDialogForIos=builder.setMsg("确定退出吗?")
                .setTitle("退出")
                .setConfirmText("退出")
                .create();
        tipDialogForIos.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//去设置权限
                tipDialogForIos.getAlertDialog().dismiss();
                finish();
            }
        });
        tipDialogForIos.setCancelListener(new View.OnClickListener() {//不设置，则退出
            @Override
            public void onClick(View v) {
                tipDialogForIos.getAlertDialog().dismiss();
            }
        });
        tipDialogForIos.showTip();
    }
}
