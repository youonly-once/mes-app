package com.shu.messystem.update_component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;

import com.shu.messystem.LoginActivity;
import com.shu.messystem.R;
import com.shu.messystem.SettingActivity;
import com.shu.messystem.component.GetGeneralInfomation;
import com.shu.messystem.component.TipDialogForIos;
import com.shu.messystem.operator_database.InsertRecord;

import static android.content.Context.MODE_PRIVATE;

public class UpdateVersion {
    private DownloadManager downloadManager;// 下载文件对象
    private TimerTask task;// 检测文件下载进度任务
    public ProgressDialog downloadingDialog; // 下载进度对话框
    public ProgressDialog detectionUpdateDialog;//检测更新进度
    private long bytes_downloaded; // 已下载文件字节数
    private long bytes_total; // 字节总数
    private long downloadId;//下载进程的ID
    private String updateTitle; // 更新标题 从JSON获得
    private String updateDesc;// 更新描述 从JSON获得
    private String appName;// 更新文件名 从JSON获得
    private String serverVersionName;//最新版本名  从JSON获得
    private String localVersionName;//本地版本名
    private String fileSize;//最新版本文件大小  从JSON获得
    private String  fileMD5;//最新版本文件MD5
    private Activity myActivity;// 主ACtivity
    public Handler handler;// 向主Activity发送消息
    private static UpdateVersion updateVersion;
    //
    private String updateAddr;
    private String updateinfoFilename;
    private int beforeVer;
    private int afterVer;
    private File downloadFile;//下载的文件
    public static final int SetInstallApkPermi=110;
    private UpdateVersion(Activity myActivity, Handler handler) {
        this.myActivity = myActivity;
        this.downloadManager=(DownloadManager) myActivity.getSystemService(LoginActivity.DOWNLOAD_SERVICE);
        //this.downloadManager = downloadManager;
        this.handler = handler;
 /*       try {
            if (GetGeneralInfomation.getAppLocalVersionCode(myActivity)<=33){
                updateAddr="http://cqgtweixin.natapp1.cc/mesapp/MesSystem/";
            }else{
                updateAddr=sharedPreferences.getString("update_addr", "http://cqgtweixin.natapp1.cc/mesapp/MesSystem/");
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }*/
    }

    public void startUpdate() {
        new Thread(() -> {
            // 比较版本 下载更新
            try {
                //弹出检测更新对话框
                handler.post(()->{
                        if (detectionUpdateDialog == null) {
                            detectionUpdateDialog();
                        }

                        detectionUpdateDialog.show();
                        detectionUpdateDialog.setMessage("正在检查新版本,请稍后");
                        detectionUpdateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                        detectionUpdateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    }
                );
                //检测到更新 则提示用户
                beforeVer=GetGeneralInfomation.getAppLocalVersionCode(myActivity);
                localVersionName=GetGeneralInfomation.getAppLocalVersionName(myActivity);
                afterVer=getAppServerVersion();
                if ( beforeVer< afterVer) {
                    handler.post(()-> {
                            detectionUpdateDialog.setMessage("当前版本：" + localVersionName
                                    + "\n最新版本：" + serverVersionName
                                    + "\n更新内容：" + updateDesc
                                    + "\n文件大小：" + fileSize
                                    + "\n\n是否更新？");
                            detectionUpdateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    );
                    //保存提示更新的记录
                    InsertRecord.getInstance().insertUpdateRecord(myActivity,beforeVer,afterVer,appName,"tipupdate");
                } else {//未检测到更新 则消除对话框
    /*                try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }*/
                    Message message=Message.obtain();
                    message.what=1;//登录按钮可用
                    handler.sendMessage(message);
                    handler.post(new Runnable() {//创建dialog是在主线程，关闭也应该在主线程。否则主线程还没创建完毕，子线程判断NULL，最终关不掉
                        @Override
                        public void run() {
                            if (detectionUpdateDialog != null) {
                                detectionUpdateDialog.dismiss();
                            }
                        }
                    });

                }

            } catch (IOException e) {
                Log.e("ERROR", "IOException");
                detctionUpdateError("更新失败，请检查网络");
                //	errorTip("更新失败，请检查网络");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("ERROR", "JSONException");
                detctionUpdateError("获取最新版本失败，请联系系统管理员");
                //	errorTip("获取最新版本失败，请检查网络");
                e.printStackTrace();
            } catch (NameNotFoundException e) {
                Log.e("ERROR", "NameNotFoundException");
                detctionUpdateError("获取本地软件版本失败，请检查软件权限");
                //errorTip("获取本地软件版本失败，请检查软件权限");
                e.printStackTrace();
            }
        }
        ).start();
    }

    private void detctionUpdateError(final String tip) {
        handler.post(()->{

                detectionUpdateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                detectionUpdateDialog.setMessage(tip);
            }
        );
    }

    private void detectionUpdateDialog() {
        detectionUpdateDialog = new ProgressDialog(myActivity);
        detectionUpdateDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置圆形进度条
        detectionUpdateDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        detectionUpdateDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        detectionUpdateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "设置",
                (DialogInterface dialog, int which)->  {
                        //System.exit(0);
                    // 跳转到 设置界面
                    Intent intent = new Intent(myActivity, SettingActivity.class);
                    myActivity.startActivity(intent);
                    }
                );
        detectionUpdateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "重试",
                (DialogInterface dialog, int which)-> {
                        startUpdate();
                    }
                );
        //点击更新 则弹出下载文件对话框 关闭 检测更新对话框
        detectionUpdateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新",
                (DialogInterface dialog, int which)-> {
                    downloadFile = new File(myActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), appName);// 下载文件存放路径
                    Log.e("ServerFileMD5",fileMD5);
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

    static public UpdateVersion getInstance(Activity myActivity, Handler handler) {
        if (updateVersion != null) {
            updateVersion = null;
            System.gc();
        }

        updateVersion = new UpdateVersion(myActivity, handler);
        return updateVersion;

    }

    private int getAppServerVersion() throws IOException, JSONException {

        //获得更新地址
        SharedPreferences sharedPreferences= myActivity.getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
        String newVer=sharedPreferences.getString("newVer", "1");
        updateAddr=sharedPreferences.getString("update_addr", "http://10.139.114.219/mesapp/MesSystem/");

        updateinfoFilename=sharedPreferences.getString("updateinfo_filename", "JSONMesApp.json");String JSONString = updateAddr+updateinfoFilename;
       ByteArrayOutputStream outStream = new ByteArrayOutputStream();
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
        Log.e("Upd_SoftewareServerVersion", String.valueOf(serverVersionCode));
        return serverVersionCode;// 返回服务器版本号
    }



    private void downloadingDialog() {

        downloadingDialog = new ProgressDialog(myActivity);
        downloadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        downloadingDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        downloadingDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        downloadingDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        downloadingDialog.setIcon(R.drawable.download);
        downloadingDialog.setProgressNumberFormat("%1d K/%2d K");//显示格式
        downloadingDialog.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
                (DialogInterface dialog, int which)->{
                        task.cancel();//取消下载进度检测 的定时器
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
        if (downloadFile.exists()){
            downloadFile.delete();
        }
        //保存正在更新的记录
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
        request.setDestinationUri(Uri.fromFile(downloadFile));
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,appName);
        // 指定在WIFI状态下，执行下载操作。
      //  request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI|DownloadManager.Request.NETWORK_MOBILE);
        // 指定在MOBILE状态下，执行下载操作
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);

        request.setAllowedOverRoaming(true);// 是否允许漫游状态下，执行下载操作
        request.setAllowedOverMetered(true); // 默认是允许的 // 是否允许“计量式的网络连接”执行下载操作
        request.setTitle(updateTitle); // 设置通知栏的标题和描述
        request.setDescription(updateDesc);
        // 设置Notification的显示，Notification显示，下载进行时，和完成之后都会显示。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");// 下载文件类型
        request.setVisibleInDownloadsUi(true);
        downloadId = downloadManager.enqueue(request);// 开始下载
        final DownloadManager.Query query = new DownloadManager.Query();// 检测下载进度
        task = new TimerTask() {
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
                        task.cancel();//取消检测任务
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
        timer.schedule(task, 0, 1000);// 1秒钟检测一次下载状态
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
        Log.e("",currentStatusString);
        //InsertRecord.getInstance().insertUpdateRecord(myActivity,beforeVer,afterVer,appName,"downloadapk_status:"+currentStatusString);
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
        closeDownloadDialog();
    }
    //android8.0设置权限后 回调
    public void setInstallApkPermiResult(){
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
                toInstallPermissionSetting();
                tipDialogForIos.getAlertDialog().dismiss();
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
        closeDownloadDialog();//没有这句会窗体泄露
       // myActivity.finish();//结束当前APP
        InsertRecord.getInstance().insertUpdateRecord(myActivity,beforeVer,afterVer,appName,"installapk");
    }
    private void closeDownloadDialog(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (downloadingDialog!=null){
                    downloadingDialog.dismiss();//没有这句会窗体泄露
                }
            }
        });
    }
}
