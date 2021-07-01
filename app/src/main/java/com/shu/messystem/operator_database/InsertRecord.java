package com.shu.messystem.operator_database;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.shu.messystem.ConnectServer;
import com.shu.messystem.MainActivity;
import com.shu.messystem.component.GetGeneralInfomation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 服务器 on 2018-8-17.
 */

public class InsertRecord {
    private static InsertRecord getCommonUserInfoMation = new InsertRecord();

    public static InsertRecord getInstance() {
        return getCommonUserInfoMation;
    }
    public void insertUpdateRecord(Activity myActivity, int beforeVer, int afterVer, String appName, String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String gonghao = MainActivity.gonghao;
                String name = MainActivity.name;
                String ip = GetGeneralInfomation.getIPAddress(myActivity.getBaseContext());
                //SharedPreferences shared = myActivity.getPreferences(MODE_PRIVATE);
                try {
                    Connection con = ConnectServer.connectServer(myActivity);
                    Statement statement = con.createStatement();
                    String sql = "INSERT INTO MesSystemUpdateInfo (update_before_version,update_after_version,ip,gonghao,name,appname,type) VALUES ('" + beforeVer + "','" + afterVer + "','" + ip + "','" + gonghao + "','" + name + "','" + appName + "','" + type + "')";
                    Log.e("SQL", sql);
                    Log.e("SQL", String.valueOf(statement.executeUpdate(sql)));
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void insertLoginRecord(Activity myActivity, String gonghao,String name, String type) {//Type表示是直接登录还是输入用户名登录
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = GetGeneralInfomation.getIPAddress(myActivity.getBaseContext());
                int ver = 0;
                try {
                    ver = GetGeneralInfomation.getAppLocalVersionCode(myActivity);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
              //  SharedPreferences shared = myActivity.getPreferences(MODE_PRIVATE);
                try {
                    Connection con = ConnectServer.connectServer(myActivity);
                    Statement statement = con.createStatement();
                    String sql = "INSERT INTO MesSystemLoginInfo (login_version,ip,gonghao,name,type) VALUES ('" + ver + "','" + ip + "','" + gonghao + "','" + name + "','"  + type + "')";
                    Log.e("SQL", sql);
                    Log.e("SQL", String.valueOf(statement.executeUpdate(sql)));
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
