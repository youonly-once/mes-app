package com.shu.messystem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shu.messystem.component.ClearEditText;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.operator_database.InsertRecord;
import com.shu.messystem.result_bean.GetUserInfoBean;
import com.shu.messystem.update_component.UpdateServices;
import com.shu.messystem.update_component.UpdateVersion;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity implements OnTouchListener, Runnable, OnClickListener {
	@SuppressLint("HandlerLeak")
	Handler  handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (msg.what==1){
				loginBt.setEnabled(true);
			}else if (msg.what==0){
				loginBt.setEnabled(false);
			}
			super.handleMessage(msg);


		}
	};
	public static final String Login_Info_SHARED="LoginInfo";
	private SharedPreferences sharedLogin;
	// 建立 名为 LoginInfo 且私有的 数据存储对象;// 建立 名为 LoginInfo 且私有的 数据存储对象
	private Button loginBt;
	private ClearEditText userEditText;
	private ClearEditText pwdEditText;
	private ProgressDialog loginingDialog ;// 正在登录弹框
	private Thread loginThread;

	private UpdateVersion updateVersion;

	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.startService(new Intent(this,UpdateServices.class));
		//背景图覆盖状态栏
		backgroundFullScreen();//需在加载内容前
		setContentView(R.layout.login_main);// 加载布局

		//检查更新
		updateVersion=UpdateVersion.getInstance(this,handler);
		updateVersion.startUpdate();

		//初始化登录框相关信息
		initLoginInfo();
		//请求读取外存权限
		requestPower();
		Log.e("LoginActivity", "onCreate()");
	}
	private void backgroundFullScreen(){
		// 背景图覆盖状态栏
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {// 当前android版本大于等于 代码支持版本
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
		}
		Log.e("AndroidSDKVersion", VERSION.SDK_INT + "");
		Log.e("AndroidCodeVersion", VERSION_CODES.LOLLIPOP + "");

	}
	private void initLoginInfo(){
		//获取状态栏高度
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		// "设置"图片按钮事件
		ImageView settingImage = findViewById(R.id.login_setting);
		settingImage.setOnClickListener(this);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {// 当前android版本大于等于 代码支持版本
			// 设置 标题在状态栏下方 即设置标题上边距
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) settingImage.getLayoutParams();
			lp.topMargin = result;
			settingImage.setLayoutParams(lp);
		}
		// 注册 长按 快捷菜
		userEditText = findViewById(R.id.edittext_user);
		pwdEditText =  findViewById(R.id.edittext_password);
		//this.registerForContextMenu(userEditText);
		// 注册 按钮 点击事件
		loginBt = findViewById(R.id.ok);
		loginBt.setEnabled(false);//初始化时不可用
		loginBt.setOnTouchListener(this);
		String[] s = new String[] {
				// "01405645","01405646"
		};
		ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, s);
		//userEditText.setAdapter(adp);
		// 将历史登录账户 放入登录框
		sharedLogin = getSharedPreferences(Login_Info_SHARED, MODE_PRIVATE);
		userEditText.setText(sharedLogin.getString("userid", ""));
		pwdEditText.setText(sharedLogin.getString("pwd", ""));
		userEditText.setSelection(userEditText.getText().length());// 光标默认在末尾
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


	private void initLoginingDialog() {
		loginingDialog = new ProgressDialog(this);
		loginingDialog .setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置圆形进度条
		loginingDialog .setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
		loginingDialog .setCancelable(false);// 设置是否可以通过点击Back键取消
		loginingDialog.setMessage("正在登录，请稍后");
	}

	// 重写 创建菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.login_menu, menu);
		return true;
	}

	// 重写 菜单项 选择事件
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	// 重写 声明快捷菜单
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final int Context = Menu.FIRST;
		menu.add(0, Context, 0, "粘贴");
	}

	// 触摸时改变 按钮背景颜色
	public boolean onTouch(View v, MotionEvent event) {
		if(loginThread!=null&&loginThread.isAlive()){
			return false;
		}
		if (v.getId() == R.id.ok) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) { // 手指按下 加深背景色
				v.performClick();// 解决 与 单击事件的冲突 添加该语句后 有 Touch与Click二个事件
				loginBt.setEnabled(false);// 按下后按钮不可用
				//loginBt.setBackgroundColor(Color.rgb(30, 144, 255));// 按下改变按钮背景色
				if (userEditText.getText().toString().trim().equals("")) {
					loginBt.setEnabled(true);
					Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();// 新建提示对话框
					return false;
				}
				if (pwdEditText.getText().toString().trim().equals("")) {
					loginBt.setEnabled(true);
					Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();// 新建提示对话框
					return false;
				}
				if (loginingDialog  == null) {
					initLoginingDialog();
				}
				loginingDialog .show();// 显示正在登录对话框
				loginThread = new Thread(this);// 网络访问需要在子线程
				loginThread.start();
			} else if (event.getAction() == MotionEvent.ACTION_UP) { // 手指松开 恢复背景色
				//loginBt.setBackgroundColor(Color.rgb(0, 191, 255));
			}

		}
		return false;
	}


	private void login() {
/*
		String userId = userEditText.getText().toString();// 工号
		String userName ; // 姓名
		String pws = pwdEditText.getText().toString(); // 密码 用户输入的密码为明文，或者读取的存储密码，MD5密文
		try {
			// shared传递 给connectServer 用户查找存储在手机上的服务器地址 第二个参数用于出错时弹出提示框
			//SharedPreferences shared = getSharedPreferences(LoginActivity.Login_Info_SHARED, Context.MODE_PRIVATE);
			Connection con = ConnectServer.connectServer(LoginActivity.this);
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery("select upword,realname from users where username='" + userId + "'");
			if (rs.next()) {
				String pwd_server=rs.getString("upword").trim();
				userName = rs.getString("realname").trim();
				Log.e("111",pws);
				Log.e("222",GetGeneralInfomation.md5(pwd_server));
				//pwd_server= GetGeneralInfomation.md5(pwd_server);
				if (pwd_server.equals(pws) || pws.equals(GetGeneralInfomation.md5(pwd_server))) {//密码正确

					saveLoginInfo(userId,userName,pwd_server);

				} else {
					InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,userId,"","password_error");
					popupTipInfo("用户名或密码错误");
				}
			} else {
				InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,userId,"","user_notfound");
				popupTipInfo("用户不存在");
			}
			try {
				con.close();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,userId,"","login_error_SQLexception");
			popupTipInfo("登录失败");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,userId,"","login_error_ClassNotFoundException");
			popupTipInfo("ClassNotFoundException");
			e.printStackTrace();
		}*/


	}
	private void loginPost() {
		//String userId = ;// 工号
		String userName ; // 姓名
	//	String pws = ; // 密码 用户输入的密码为明文，或者读取的存储密码，MD5密文
		HashMap<String, String> queryMap=new HashMap<>();
		String username=userEditText.getText().toString();
		String pws=pwdEditText.getText().toString();
		queryMap.put("userId",username);
		queryMap.put("pws",pws);
		Call<GetUserInfoBean> request = ManageRetrofit.getRetroInter(this).getUserInfo(queryMap);
		request.enqueue(new Callback<GetUserInfoBean>() {
			@Override
			public void onResponse(Call<GetUserInfoBean> call, Response<GetUserInfoBean> response) {
				if (response.body()!=null){


					String msg = response.body().getMsg();
					String usernameSer=response.body().getUsername();
					String realnameSer=response.body().getRealname();
					String pwsSer=response.body().getUpword();
					Toast.makeText(LoginActivity.this, msg , Toast.LENGTH_SHORT).show();
					if ("success".equals(msg)) {//密码正确

						saveLoginInfo(usernameSer,realnameSer,pwsSer);

					} else {
						InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,username,"","password_error");
						popupTipInfo(msg);
					}
				}else{
					InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,username,"","password_error");
					popupTipInfo("网络错误，请重试！");
				}
			}

			@Override
			public void onFailure(Call<GetUserInfoBean> call, Throwable t) {
				InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,username,"","登录错误");
				popupTipInfo("登录错误");
				t.printStackTrace();
			}
		});
	}
	private void saveLoginInfo(String userId,String userName,String pwd_server){
		//获得用户姓名
		Editor edit = sharedLogin.edit();// 编辑 该对象
		edit.putString("userid", userId);// 存放名为user 值为user的数据
		edit.putString("username", userName);
		edit.putString("pwd", GetGeneralInfomation.md5(pwd_server));//加密存储密码
		edit.apply();                                        // 保存数据
		logined(userId, userName);// 登录进入主界面
		errorProcess();
		InsertRecord.getInstance().insertLoginRecord(LoginActivity.this,userId,userName,"success");
	}
	// 登录成功后
	private void logined(String userId, String userName) {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("userId", userId);// 传递工号和姓名
		intent.putExtra("userName", userName);
		this.startActivity(intent);
		loginingDialog .dismiss();
		this.finish();
	}

	private void errorProcess() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				loginBt.setEnabled(true);
				//loginBt.setBackgroundColor(Color.rgb(0, 191, 255));
				loginingDialog .hide();
			}
		});
	}

	private void popupTipInfo(final String tipInfo) {
		// 用handler 将子线程 加入到 主线程的 消息队列 等待执行 更新主界面GUI
		handler.post(()->{
				errorProcess();
				Toast.makeText(LoginActivity.this, tipInfo, Toast.LENGTH_SHORT).show();// 新建提示对话框

			}
		);
	}

	public void run() {
		if (Thread.currentThread() == loginThread) {
			synchronized (this) {// 不能有多个线程登录
				//login();
				loginPost();
			}
		}

	}

	// 点击设置图片
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		if (v.getId() == R.id.login_setting) {
			// 跳转到 设置界面
			Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
			this.startActivity(intent);
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

		Log.e("LoginActivity", "onDestory()");
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==UpdateVersion.SetInstallApkPermi){
			updateVersion.setInstallApkPermiResult();
		}

	}

}


