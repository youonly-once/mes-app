package com.shu.messystem.update_component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InstallApkBroadCastReceiver extends BroadcastReceiver {
private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
    	//Log.e("Receiver", "这是静态的广播接受者（优先级999）---》");
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packagename="";
            try{
                packagename=intent.getDataString();
                if(packagename!=null){
                    open(packagename.substring(8));
                }
            }catch(NullPointerException e){
                e.printStackTrace();
            }

            Log.e("监听到系统广播添加安装","监听到系统广播添加"+packagename);
        }

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
        	Log.e("监听到系统广播移除安装","监听到系统广播移除"+intent.getDataString());

        }

        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            String packagename=intent.getDataString().substring(8);
            Log.e("监听到系统广播替换安装","监听到系统替换安装"+packagename);
            //升级后打开
            open(packagename);
        }
    }
    private void open(String packagename){ //升级后打开
        if(packagename.equals("com.shu.messystem")){
            Intent newIntent=new Intent();
            newIntent.setClassName(packagename,packagename+".SplashActivity");
            newIntent.setAction("android.intent.action.MAIN");
            newIntent.addCategory("android.intent.category.LAUNCHER");
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.startActivity(newIntent);
        }
    }

}
