package com.shu.messystem.update_component;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;

import com.shu.messystem.LoginActivity;
import com.shu.messystem.MainActivity;
import com.shu.messystem.R;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.component.TipDialogForIos;
import com.shu.messystem.component.TopTips.util.TipUtil;
import com.shu.messystem.operator_database.InsertRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateServices extends Service {
    private DownloadManager downloadManager;// 下载文件对象
    private TimerTask downloadTask;// 检测文件下载进度任务
    private TimerTask deteUpdateTask;
    private Timer deteUpdatetimer;
    private ProgressDialog downloadingDialog; // 下载进度对话框
    private ProgressDialog detectionUpdateDialog;//检测更新进度
    private long bytes_downloaded; // 已下载文件字节数
    private long bytes_total; // 字节总数
    private long downloadId;//下载进程的ID
    private String updateTitle; // 更新标题 从JSON获得
    private String updateDesc;// 更新描述 从JSON获得
    private String appName;// 更新文件名 从JSON获得
    private String serverVersionName;//最新版本名  从JSON获得
    private String localVersionName;//本地版本名
    private String fileSize;//最新版本文件大小  从JSON获得
    private Activity myActivity;// 主ACtivity
    public Handler handler;// 向主Activity发送消息

    private MyBinder myBinder=new MyBinder();
    private int beforeVer;
    private int afterVer;
    private String updateAddr;
    private String updateinfoFilename;
    private File downloadFile;//下载的文件
    private String  fileMD5;//最新版本文件MD5
    public static final int SetInstallApkPermi=110;
    private boolean isDownloading=false;//是否正在下载软件
    public IBinder onBind(Intent intent) {
        Log.e("Services OnBind", "Services OnBind");
        beginDetUpdate();
        return myBinder;
    }

    public void onCreate() {
        super.onCreate();
        Log.e("Services Oncreate", "Services Oncreate");

       SharedPreferences sharedPreferences= getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
       updateAddr=sharedPreferences.getString("update_addr", "http://10.139.114.219/mesapp/MesSystem/");
  /*     if(myActivity!=null){


        try {
            if (GetGeneralInfomation.getAppLocalVersionCode(myActivity)<=33){
                updateAddr="http://cqgtweixin.natapp1.cc/mesapp/MesSystem/";
            }else{
                updateAddr=sharedPreferences.getString("update_addr", "http://cqgtweixin.natapp1.cc/mesapp/MesSystem/");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
       }*/
        updateinfoFilename=sharedPreferences.getString("updateinfo_filename", "JSONMesApp.json");
    }
    private void beginDetUpdate(){
       deteUpdatetimer = new Timer();
        deteUpdateTask = new TimerTask() {
            @Override
            public void run() {
                //检测到更新 则提示用户
                try {
                    beforeVer = GetGeneralInfomation.getAppLocalVersionCode(UpdateServices.this);
                    localVersionName = GetGeneralInfomation.getAppLocalVersionName(UpdateServices.this);
                    afterVer = getAppServerVersion();
                    if (beforeVer < afterVer) {



                        if (myActivity != null && handler != null) {
                            if (!isDownloading)//如果正在下载则不提示
                            {
                                tipUpdate();
                            }
                            InsertRecord.getInstance().insertUpdateRecord(myActivity, beforeVer, afterVer, appName, "tipupdate");
             /*           deteUpdatetimer.cancel();
                        deteUpdateTask.cancel();*/
                        }

                    }
                }catch (PackageManager.NameNotFoundException e){
                    Log.e("ERROR", "NameNotFoundException");
                    detctionUpdateError("获取本地软件版本失败，请检查软件权限",0);
                    //errorTip("获取本地软件版本失败，请检查软件权限");
                    e.printStackTrace();
                }
            }
        };
        deteUpdatetimer.scheduleAtFixedRate(deteUpdateTask, 0, 60000);
    }
    public class MyBinder extends Binder {
        public void setContext(Activity activity,Handler handler){
            UpdateServices.this.myActivity=activity;
            UpdateServices.this.handler=handler;
            UpdateServices.this.downloadManager= (DownloadManager) getSystemService(MainActivity.DOWNLOAD_SERVICE);
            Log.e("setContext", "setContext");
        }
        public UpdateServices getService(){
            return UpdateServices.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Services StartCommand", "--------->onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    private int getAppServerVersion() {
        String JSONString = updateAddr+updateinfoFilename;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            URL url = new URL(JSONString);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            InputStream in = urlConn.getInputStream();
            byte[] data = new byte[1024];
            int len;
            while ((len = in.read(data)) != -1) {
                outStream.write(data, 0, len);
            }
            JSONArray jsonArray = new JSONArray(new String(outStream.toByteArray()));
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            updateTitle = jsonObject.getString("UpdateTitle");
            appName = jsonObject.getString("AppName");
            updateDesc = jsonObject.getString("UpdateDesc");
            serverVersionName = jsonObject.getString("VersionName");
            fileSize = jsonObject.getString("FileSize");
            fileMD5=jsonObject.getString("MD5");
            int serverVersionCode = jsonObject.getInt("VersionCode");
            Log.e("SoftewareServerVersion", String.valueOf(serverVersionCode));
            return serverVersionCode;// 返回服务器版本号
        } catch (IOException e) {
            Log.e("ERROR", "IOException");
            detctionUpdateError("更新失败，请检查网络",0);
            //	errorTip("更新失败，请检查网络");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("ERROR", "JSONException");
             detctionUpdateError("获取最新版本失败，请联系系统管理员",0);
            //	errorTip("获取最新版本失败，请检查网络");
            e.printStackTrace();
        }
        return -1;
    }

    private int getAppLocalVersion() {
        try {
            PackageInfo info = getApplicationContext().getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            localVersionName = info.versionName;
            Log.e("SerSoftewareLocalVersion", String.valueOf(info.versionCode));
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public void tipUpdate(){

        handler.post(()-> {
                detectionUpdateDialog();
                    detectionUpdateDialog.setMessage("当前版本：" + localVersionName
                            + "\n最新版本：" + serverVersionName
                            + "\n更新内容：" + updateDesc
                            + "\n文件大小：" + fileSize
                            + "\n\n是否更新？");
//                    detectionUpdateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    detectionUpdateDialog.show();
                }
        );
    }
    private void detctionUpdateError(final String tip,int status) {
        if(handler==null){
            return;
        }
        handler.post(()->{
     /*               detectionUpdateDialog();
                    detectionUpdateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                    detectionUpdateDialog.setMessage(tip);*/
            new TipUtil().showTips(myActivity, MainActivity.toolbar,tip,status);
                }
        );
    }

    private void detectionUpdateDialog() {
        if(detectionUpdateDialog!=null){
            return;
        }
        detectionUpdateDialog = new ProgressDialog(myActivity);
        detectionUpdateDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置圆形进度条
        detectionUpdateDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        detectionUpdateDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        detectionUpdateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "退出",
                (DialogInterface dialog, int which)->  {
                    System.exit(0);
                }
        );
        detectionUpdateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "稍后",
                (DialogInterface dialog, int which)-> {

                }
        );
        //点击更新 则弹出下载文件对话框 关闭 检测更新对话框
        detectionUpdateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新",
                (DialogInterface dialog, int which)-> {
                    downloadFile = new File(myActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), appName);// 下载文件存放路径
                    if(GetGeneralInfomation.md5(downloadFile).equals(fileMD5))//本地文件存在且MD5正确，则直接安装
                    {
                        installApk();
                    }else{
                        downloadApk();// 下载安装apk
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                detectionUpdateDialog.dismiss();
                            }
                        });
                    }

                }
        );

        //downloadingDialog.setTitle("正在更新");
    }

    private void downloadingDialog() {
        isDownloading=true;
        downloadingDialog = new ProgressDialog(myActivity);
        downloadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        downloadingDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        downloadingDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        downloadingDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        downloadingDialog.setIcon(R.drawable.download);
        downloadingDialog.setProgressNumberFormat("%1d K/%2d K");//显示格式
        downloadingDialog.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
                (DialogInterface dialog, int which)->{
                    downloadTask.cancel();//取消下载进度检测 的定时器
                    downloadManager.remove(downloadId);//取消当前下载进程
                    downloadApk();// 下载安装apk
                    downloadingDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    //startUpdate();// 重新检测更新
                    //downloadingDialog.dismiss();
                }
        );
        // dialog.setIcon(R.mipmap.ic_launcher);// 设置提示的title的图标，默认是没有的
        downloadingDialog.setTitle("正在更新");
        downloadingDialog.setMax(-1);
        // Window window = progressDialog.getWindow();
        // WindowManager.LayoutParams lp = window.getAttributes();
        // lp.alpha = 0.5f; // 设置透明度为0.5

    }

    private void downloadApk() {
        InsertRecord.getInstance().insertUpdateRecord(myActivity,beforeVer,afterVer,appName,"downloadapk");
    /*	String filePath = myActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/"+ appName;
		if (new File(filePath).exists()) {//已下载则直接安装
			installApk(filePath);// 安装APK
			return;
		}*/
        handler.post(()->{
                    if (downloadingDialog == null) {
                        downloadingDialog();
                    }
                    downloadingDialog.show();
                    downloadingDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                }
        );
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(updateAddr + appName));

        //7.0
        downloadFile = new File(myActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), appName);// 下载文件存放路径
        if (downloadFile.exists()){
            downloadFile.delete();
        }
        request.setDestinationUri(Uri.fromFile(downloadFile));
        // 指定在WIFI状态下，执行下载操作。
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 指定在MOBILE状态下，执行下载操作
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);

        request.setAllowedOverRoaming(true);// 是否允许漫游状态下，执行下载操作
        request.setAllowedOverMetered(true); // 默认是允许的 // 是否允许“计量式的网络连接”执行下载操作
        request.setTitle(updateTitle); // 设置通知栏的标题和描述
        request.setDescription(updateDesc);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);// 设置Notification的显示，Notification显示，下载进行时，和完成之后都会显示。
        request.setMimeType("application/vnd.android.package-archive");// 下载文件类型
        downloadId = downloadManager.enqueue(request);// 开始下载
        final DownloadManager.Query query = new DownloadManager.Query();// 检测下载进度
        downloadTask = new TimerTask() {
            public void run() {
                Cursor cursor = downloadManager.query(query.setFilterById(downloadId));
                if (cursor != null && cursor.moveToFirst()) {
                    // 路径及文件名
                    bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));// 总需下载的字节数
                    bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));// 已经下载的字节数
                    Log.i("downloadApk", bytes_downloaded + "/" + bytes_total);

                    String re=detectionDownloadStatus(cursor);
                    if(re.indexOf("1")==0) {
                        detectionInstallPermission();
                        downloadTask.cancel();//取消检测任务
                    }
                    if(re.indexOf("2")==0) {
                        handler.post(() -> {// 出错时 设置重试按钮可用
                                    downloadingDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                }
                        );
                    }
                    handler.post(()-> {// 刷新进度条

                                downloadingDialog.setTitle(re.substring(1));//标题栏显示当前状态
                                downloadingDialog.setMax((int) (bytes_total / 1024));// 进度条最大值
                                downloadingDialog.setProgress((int) (bytes_downloaded / 1024));// 设置当前进度值
                            }
                    );
                }
                if (cursor!=null) {
                    cursor.close();
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(downloadTask, 0, 1000);// 1秒钟检测一次下载状态
    }

    //检测下载状态
    private String detectionDownloadStatus(Cursor cursor) {
        String currentStatusString = "";//显示错误代码
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        String currentStatus = "0";

        switch (status) {
            case DownloadManager.STATUS_FAILED://下载错误
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        currentStatusString = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        //no external storage device was found. Typically, this is because the SD card is not mounted
                        currentStatusString = "SD卡不可用";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        //the requested destination file already exists (the download manager will not overwrite an existing file)
                        currentStatusString = "文件以及存在";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        //a storage issue arises which doesn't fit under any other error code
                        currentStatusString = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        //an error receiving or processing data occurred at the HTTP level
                        currentStatusString = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE://sd卡满了
                        currentStatusString = "SD卡满";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        currentStatusString = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        //an HTTP code was received that download manager can't handle
                        currentStatusString = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        //he download has completed with an error that doesn't fit under any other error code
                        currentStatusString = "ERROR_UNKNOWN";
                        break;
                }

                currentStatusString = "更新错误:" + currentStatusString;
                currentStatus = "2"+currentStatusString;
                break;
            case DownloadManager.STATUS_PAUSED://下载暂停
                currentStatusString ="";
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        //the download exceeds a size limit for downloads over the mobile network and the download manager is waiting for a Wi-Fi connection to proceed
                        currentStatusString = "等待WIFI接入";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        //the download is paused for some other reason
                        currentStatusString = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        currentStatusString = "等待网络";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        currentStatusString = "网络错误";
                        //the download is paused because some network error occurred and the download manager is waiting before retrying the request
                        break;
                }

                currentStatusString = "更新暂停:" + currentStatusString;
                currentStatus = "2"+currentStatusString;
                break;
            case DownloadManager.STATUS_PENDING:
                //the download is waiting to start
                currentStatusString = "准备更新";
                break;
            case DownloadManager.STATUS_RUNNING:
                //the download is currently running
                currentStatusString = "正在更新";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                //the download has successfully completed
                //currentStatusString="更新成功,准备安装";
                currentStatus = "1"+currentStatusString;
                break;
        }

        InsertRecord.getInstance().insertUpdateRecord(myActivity,beforeVer,afterVer,appName,"downloadapk_status:"+currentStatusString);
        return currentStatus;

    }
    private void detectionInstallPermission() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){//android 8.0以上
            if(!myActivity.getPackageManager().canRequestPackageInstalls()){//没有打开安装未知应用的开关 则弹框提示安装
                handler.post(new Runnable() {
                    @Override
                    public void run() {//弹出对话框提示去设置
                        tipSetInstallApkPermi("需要打开允许安装未知来源软件的权限，请去设置中开启此权限后再升级");
                    }
                });

            }else{
                installApk();
            }
        }else{
            installApk();
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private void toInstallPermissionSetting(){
        Uri packageURI= Uri.parse("package:"+myActivity.getPackageName());
        Intent intent =new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        myActivity.startActivityForResult(intent,SetInstallApkPermi);
        isDownloading=false;
        closeDownloadDialog();
    }
    //android8.0设置权限后 回调
    public  void setInstallApkPermiResult(){
        //如果已打卡，则安装
        if(myActivity.getPackageManager().canRequestPackageInstalls()) {//没有打开安装未知应用的开关 则弹框提示安装
            installApk();
        }else{//如果未打开，继续提示
            tipSetInstallApkPermi("你必须设置为允许才能继续安装更新");
        }

    }
    private void tipSetInstallApkPermi(String str){
        TipDialogForIos.builder builder=new TipDialogForIos.builder(myActivity);
        final TipDialogForIos tipDialogForIos=builder.setMsg(str)
                .setTitle("安装权限")
                .setConfirmText("去设置")
                .create();
        tipDialogForIos.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//去设置权限
                tipDialogForIos.getAlertDialog().dismiss();
                toInstallPermissionSetting();
            }
        });
        tipDialogForIos.setCancelListener(new View.OnClickListener() {//不设置，则退出
            @Override
            public void onClick(View v) {
                closeDownloadDialog();
                tipDialogForIos.getAlertDialog().dismiss();
                myActivity.finish();
            }
        });
        tipDialogForIos.showTip();
    }
    private void installApk() {

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 由于没有在Activity环境下启动Activity,设置下面的标签

        if (Build.VERSION.SDK_INT >= 24) {//版本在7.0以上是不能直接通过uri访问的

            Uri apkUri = FileProvider.getUriForFile(myActivity, "com.shu.messystem.fileprovider", downloadFile);//参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(downloadFile), "application/vnd.android.package-archive");
        }

        myActivity.startActivity(intent);
        closeDownloadDialog();
       // myActivity.finish();//结束当前APP
        InsertRecord.getInstance().insertUpdateRecord(myActivity,beforeVer,afterVer,appName,"installapk");
    }
    private void closeDownloadDialog(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                downloadingDialog.dismiss();//没有这句会窗体泄露
            }
        });
    }
    public void onDestroyActivity(){
        if(downloadingDialog!=null){
            downloadingDialog.dismiss();
        }
        if(detectionUpdateDialog!=null){
            detectionUpdateDialog.dismiss();
        }
    }
}
