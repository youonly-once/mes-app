package com.shu.messystem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.shu.messystem.component.ChangeStatusBgColor;
import com.shu.messystem.component.ClearEditText;
import com.shu.messystem.component.TitleBar;

public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sharedLogin;
    //建立 名为 LoginInfo 且私有的  数据存储对象
    private ClearEditText server_addr;

    private ClearEditText  updateAddr;
    private ClearEditText  updateinfoFilename;

    public  static String serAddr="http://10.139.114.219/";
    public  static String updaAddr="http://10.139.114.219/mesapp/MesSystem/";
    public  static String updaFileName="JSONMesApp.json";
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        initConfig();
        initTitleBar();
        ChangeStatusBgColor.SetStatusBgColor(this, R.color.white);

    }
    private void initConfig(){
        //加载 设置 信息

        server_addr =  findViewById(R.id.seradd_edittext);
/*        server_user = (EditText) findViewById(R.id.seruser_edittext);
        server_pws = (EditText) findViewById(R.id.serpas_edittext);*/
        updateAddr = findViewById(R.id.update_addr);
        updateAddr.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        updateAddr.setSingleLine(false);
        updateAddr.setHorizontallyScrolling(false);
        updateinfoFilename = findViewById(R.id.updateinfo_filename);
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });
/*        phjia = (EditText) findViewById(R.id.ph_jia_09);
        phyi = (EditText) findViewById(R.id.ph_yi_09);
        phbing = (EditText) findViewById(R.id.ph_bing_09);
        pjjia = (EditText) findViewById(R.id.pj_jia_09);
        pjyi = (EditText) findViewById(R.id.pj_yi_09);
        pjbing = (EditText) findViewById(R.id.pj_bing_09);
        pkjia = (EditText) findViewById(R.id.pk_jia_09);
        pkyi = (EditText) findViewById(R.id.pk_yi_09);
        pkbing = (EditText) findViewById(R.id.pk_bing_09);
        tnjia = (EditText) findViewById(R.id.tn_jia_09);
        tnyi = (EditText) findViewById(R.id.tn_yi_09);
        tnbing = (EditText) findViewById(R.id.tn_bing_09);*/

        sharedLogin = getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
        server_addr.setText(sharedLogin.getString("server_addr", serAddr));
 /*       server_user.setText(sharedLogin.getString("server_user", "mesapp"));
        server_pws.setText(sharedLogin.getString("server_pws", "Haier@Mes"));*/

        updateAddr .setText(sharedLogin.getString("update_addr", updaAddr));
        updateinfoFilename .setText(sharedLogin.getString("updateinfo_filename", updaFileName));
 /*       phjia  .setText(sharedLogin.getString("ph_jia", "01262191"));
        phyi .setText(sharedLogin.getString("ph_yi", "01408556"));
        phbing .setText(sharedLogin.getString("ph_bing", ""));
        pjjia  .setText(sharedLogin.getString("pj_jia", "01426242"));
        pjyi .setText(sharedLogin.getString("pj_yi", "01432832"));
        pjbing  .setText(sharedLogin.getString("pj_bing", ""));
        pkjia .setText(sharedLogin.getString("pk_jia", "01420355"));
        pkyi .setText(sharedLogin.getString("pk_yi", "01405612"));
        pkbing  .setText(sharedLogin.getString("pk_bing", ""));
        tnjia .setText(sharedLogin.getString("tn_jia", "01460616"));
        tnyi.setText(sharedLogin.getString("tn_yi", ""));
        tnbing  .setText(sharedLogin.getString("tn_bing", ""));*/
    }
    private void initTitleBar() {
        TitleBar titleBar = findViewById(R.id.login_title_bar);
            titleBar.addAction(new TitleBar.TextAction("保存") {
                @Override
                public void performAction(View view) {
                    saveConfig();
                }
            });

    }
    private void saveConfig(){
        //保存设置信息

        Editor edit = sharedLogin.edit();//编辑 该对象
        edit.putString("server_addr", server_addr.getText().toString());
/*        edit.putString("server_user", server_user.getText().toString());
        edit.putString("server_pws", server_pws.getText().toString());*/
        edit.putString("update_addr", updateAddr.getText().toString());
        edit.putString("updateinfo_filename", updateinfoFilename.getText().toString());
/*        edit.putString("ph_jia", phjia.getText().toString());
        edit.putString("ph_yi", phyi.getText().toString());
        edit.putString("ph_bing", phbing.getText().toString());

        edit.putString("pj_jia", pjjia.getText().toString());
        edit.putString("pj_yi", pjyi.getText().toString());
        edit.putString("pj_bing", pjbing.getText().toString());

        edit.putString("pk_jia", pkjia.getText().toString());
        edit.putString("pk_yi", pkyi.getText().toString());
        edit.putString("pk_bing", pkbing.getText().toString());


        edit.putString("tn_jia", tnjia.getText().toString());
        edit.putString("tn_yi", tnyi.getText().toString());
        edit.putString("tn_bing", tnbing.getText().toString());*/

        boolean saveState = edit.commit();//保存数据
        //跳转到 登录界面  模拟返回键功能,有点卡顿
             /* Runtime runtime = Runtime.getRuntime();
		      try {
		           runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
		       } catch (IOException e) {
		            e.printStackTrace();
		       }*/
        if (saveState) {
            Toast.makeText(SettingActivity.this, "保存成功,重新打开软件生效", Toast.LENGTH_SHORT).show();//新建提示对话框
        } else {
            Toast.makeText(SettingActivity.this, "保存失败", Toast.LENGTH_SHORT).show();//新建提示对话框
        }
        finish();//终结该Activity
    }
    //重写  创建菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    //重写  菜单项 选择事件
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.backAndSave) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStart() {
        super.onStart();
        Log.e("SettingActivity", "onStart()");
    }

    public void onResume() {
        super.onResume();
        Log.e("SettingActivity", "onResume()");
    }

    public void onPause() {
        super.onPause();
        Log.e("SettingActivity", "onPause()");
    }

    public void onStop() {
        super.onStop();
        Log.e("SettingActivity", "onStop()");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("SettingActivity", "onDestory()");
    }
}
