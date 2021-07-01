package com.shu.messystem.component;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/4/29.
 */
    /*
//获取通用信息 例如 设备标识
 */
public class GetGeneralInfomation {
    private static String IP = "";
    private static final int equipment = 2;

    private GetGeneralInfomation() {
    }

    //获取设备标识
    public static int getEquipment() {
        return equipment;
    }
    public static int getAppLocalVersionCode(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo info = context.getApplicationContext().getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        Log.e("SoftewareLocalVersion", String.valueOf(info.versionCode));
        return info.versionCode;

    }
    public static String getAppLocalVersionName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo info = context.getApplicationContext().getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        Log.e("SoftewareLocalversionName", String.valueOf(info.versionName));
        return info.versionName;

    }
    //获取IP地址
    public static String getIPAddress(Context context) {
        NetworkInfo info = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            info = connectivityManager.getActiveNetworkInfo();
        }
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                Log.e("ipaddress", inetAddress.getHostAddress());
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                // String ip = GetNetIp();
                // if (ip == null) {//以下为获取的内网ip 若不能获取真实IP，则获取本地ip
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = null;
                if (wifiManager != null) {
                    wifiInfo = wifiManager.getConnectionInfo();
                    Log.e("ipaddress", intIP2StringIP(wifiInfo.getIpAddress()));
                    return intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                }


                // } else {
                //   Log.e("ipaddress", ip);
                //   return ip;
                // }

            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return "";
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    private static String GetNetIp() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String address = "http://ip.taobao.com/service/getIpInfo2.php?ip=myip";
                    URL url = new URL(address);
                    HttpURLConnection connection;
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setUseCaches(false);
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream in = connection.getInputStream();      // 将流转化为字符串
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        String tmpString = "";
                        StringBuilder retJSON = new StringBuilder();
                        while ((tmpString = reader.readLine()) != null) {
                            retJSON.append(tmpString).append("\n");
                        }
                        JSONObject jsonObject = new JSONObject(retJSON.toString());
                        String code = jsonObject.getString("code");
                        if (code.equals("0")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            //IP = data.getString("ip") + "(" + data.getString("country") + data.getString("area") + "区" + data.getString("region") + data.getString("city") + data.getString("isp") + ")";
                            IP = data.getString("ip");
                            Log.e("提示", "您的IP地址是：" + IP);
                        } else {
                            IP = null;
                            Log.e("提示", "IP接口异常，无法获取IP地址！");
                        }
                    } else {
                        IP = null;
                        Log.e("提示", "网络连接异常，无法获取IP地址！");
                    }
                } catch (Exception e) {
                    IP = null;
                    Log.e("提示", "获取IP地址时出现异常，异常信息是：" + e.toString());
                }
            }
        }).start();
        return IP;
    }

    public static String getCurrDateAndTime() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));//当前日期
        int year = calendar.get(Calendar.YEAR);//年
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);//月        0-11表示12月
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));//日
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));//时
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));//分
        String second = String.valueOf(calendar.get(Calendar.SECOND));//秒
        String milliSecond = String.valueOf(calendar.get(Calendar.MILLISECOND));
        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        if (second.length() == 1) {
            second = "0" + second;
        }
        Log.e("currDate", year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + milliSecond);
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second + "." + milliSecond;
    }

    public static String getCurrDateByDayBegin() {//一天的开始
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));//当前日期
        int hour = calendar.get(Calendar.HOUR_OF_DAY);//时
        if (hour >= 0 && hour < 8) {//前一天的8点
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        int year = calendar.get(Calendar.YEAR);//年
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);//月        0-11表示12月
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));//日
        Log.e("currDateBegin", year + "-" + month + "-" + day + " 08:00:00");
        return year + "-" + month + "-" + day + " 08:00:00";
    }

    public static String getCurrDateByDayEnd() {//一天的结束
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));//当前日期
        int hour = calendar.get(Calendar.HOUR_OF_DAY);//时
        if (hour >= 8) {//前一天的8点
            calendar.add(Calendar.DAY_OF_MONTH, +1);
        }
        int year = calendar.get(Calendar.YEAR);//年
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);//月        0-11表示12月
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));//日
        Log.e("currDateEnd", year + "-" + month + "-" + day + " 08:00:00");
        return year + "-" + month + "-" + day + " 08:00:00";
    }

    public static String md5(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(str.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    // 计算文件的 MD5 值
    public static String md5(File file) {
        Log.e("FileExists",file.exists()+"");
        if (file == null || !file.isFile() || !file.exists()) {
            return "";
        }
        FileInputStream in = null;
        StringBuilder result = new StringBuilder();
        byte buffer[] = new byte[8192];
        int len;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            byte[] bytes = md5.digest();

            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("LocalFileMD5",result.toString());
        return result.toString();
    }
    public static boolean deteDateFormat(String str,String formatStr){
        //判断时间格式
        SimpleDateFormat format=new SimpleDateFormat(formatStr);
        try{
            format.setLenient(false);
            format.parse(str);
        }catch(ParseException e){
            return false;
        }
        return true;
    }

}
