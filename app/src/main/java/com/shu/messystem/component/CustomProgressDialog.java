package com.shu.messystem.component;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by 01405645 on 2018-04-26.
 */

public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context, String msg) {
        super(context);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置圆形进度条
        setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        setCancelable(false);// 设置是否可以通过点击Back键取消
        setMessage(msg);
    }
}
