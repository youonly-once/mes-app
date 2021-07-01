package com.shu.messystem.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shu.messystem.R;

/**
 * Created by Administrator on 2018/5/27.
 */

public class TipDialogForIos extends AlertDialog.Builder {
    private View contentView;



    private AlertDialog alertDialog;
    private Context context;
    private TextView dialogtitle;
    private TextView dialogmsg;
    private TextView cancelBt;
    private TextView connfirmBt;
    private TipDialogForIos(@NonNull final Context context
            , String title
            , String msg
            , String cancelText
            , String confirmText
            ,View.OnClickListener cancelClickListener
            ,View.OnClickListener confirmClickListener
            ,int msgSize) {
        super(context);
        this.context = context;
        contentView = LayoutInflater.from(context).inflate(R.layout.tip_dialog_forios, null);
        cancelBt = contentView.findViewById(R.id.cancel);
        connfirmBt = contentView.findViewById(R.id.goto_setting_addr);
        dialogtitle = contentView.findViewById(R.id.dialog_title);
        dialogmsg = contentView.findViewById(R.id.dialog_msg);
        setTitle(title);
        setMsg(msg);
        setMsgSize(msgSize);
        setCancelText(cancelText);
        setConfirmText(confirmText);
        setCancelListener(cancelClickListener);
        setConfirmListener(confirmClickListener);
        alertDialog = create();
        alertDialog.setView(contentView);
        setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }
    public AlertDialog getAlertDialog() {
        return alertDialog;
    }
    private void setTitle(String title) {
        if(title==null){
            dialogtitle.setVisibility(View.GONE);
            dialogmsg.setPadding(0,20,0,0);
        }
        dialogtitle.setText(title);
    }

    private void setMsg(String msg) {

        dialogmsg.setText(msg);

    }

    private void setMsgSize(int sizesp) {

       // dialogmsg.setTextSize(DensityUtils.sp2px(context,sizesp));
        dialogmsg.setTextSize(sizesp);

    }

    private void setCancelText(String cancelText) {

        cancelBt.setText(cancelText);

    }

    private void setConfirmText(String confirmText) {

        connfirmBt.setText(confirmText);

    }
    public void setConfirmListener(View.OnClickListener clickListener){

        if(clickListener==null){
            clickListener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            };
        }
        connfirmBt.setOnClickListener(clickListener);
    }
    public void setCancelListener(View.OnClickListener clickListener){
        if(clickListener==null){
            clickListener=new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            };
        }
        cancelBt.setOnClickListener(clickListener);
    }
    public void showTip(){
        alertDialog.show();
    }

    public static class builder {
        private String title="提示";
        private String msg="";
        private int msgSize=15;
        private String cancelText="取消";
        private String confirmText="确认";
        private View.OnClickListener cancelClickListener=null;
        private View.OnClickListener confirmClickListener=null;
        private Context context;
        public builder(Context context){
            this.context=context;
        }
        public builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public builder setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public builder setMsgSize(int sizesp) {

            this.msgSize=msgSize;
            return this;

        }
        public builder setCancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }

        public builder setConfirmText(String confirmText) {
            this.confirmText = confirmText;
            return this;
        }
        public builder setConfirmListener(View.OnClickListener clickListener){
            this.confirmClickListener=clickListener;
            return this;
        }
        public  builder setCancelListener(View.OnClickListener clickListener){
            this.cancelClickListener=clickListener;
            return this;
        }
        public TipDialogForIos create() {
            return new TipDialogForIos(
                    context
                    , title
                    , msg
                    , cancelText
                    , confirmText
                    ,cancelClickListener
                    ,confirmClickListener
                    ,msgSize);
        }
    }

}
