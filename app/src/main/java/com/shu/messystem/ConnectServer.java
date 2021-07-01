package com.shu.messystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ConnectServer{
	private ConnectServer(){ }
	static public Connection connectServer(Activity mActivity) throws SQLException, ClassNotFoundException {
		SharedPreferences shared = mActivity.getSharedPreferences(LoginActivity.Login_Info_SHARED, Context.MODE_PRIVATE);
		String ip=shared.getString("server_addr", SettingActivity.serAddr);
		String db="haier_sbu";
		String server_user=shared.getString("server_user", "mesapp");
		String server_pwd=shared.getString("server_pws", "Haier@Mes");
		Class.forName("net.sourceforge.jtds.jdbc.Driver");
        return  DriverManager.getConnection("jdbc:jtds:sqlserver://" + ip + ":1433/" + db + ";charset=GB2312", server_user, server_pwd);

	}
}