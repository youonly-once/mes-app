package com.shu.messystem.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.shu.messystem.R;

/**
 * Created by Administrator on 2018/4/29.
 */

public class ClearEditText extends android.support.v7.widget.AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {
    Drawable[] drawables;
    Drawable drawableTextRight;//保存 XML设置的图片 密码框保存的不可见图标
    Drawable drawablePwdVisi;//保存密码框 可见图标
    boolean isFocus;

    public ClearEditText(Context context) {
        super(context);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        //光标位于末尾
        setSelection(getText().length());
    }

    private void init() {
        drawables = getCompoundDrawables();//获取drawableEnd获取不到，但能获取drawableRight？？？？
        drawableTextRight = drawables[2];
        if (drawableTextRight == null) {
            if(getInputType() == 129){
                drawablePwdVisi=getResources().getDrawable(R.drawable.pwd_visible_3x);
            }else{
                drawableTextRight=getResources().getDrawable(R.drawable.clear_gray_3x);
            }
           // return;
        }
        if(getInputType() == 129){
            drawablePwdVisi=getResources().getDrawable(R.drawable.pwd_visible_3x);
            drawablePwdVisi.setBounds(0, 0, drawablePwdVisi.getMinimumWidth(), drawablePwdVisi.getMinimumHeight());
        }//else{
            setCompoundDrawables(drawables[0], drawables[1], null, drawables[3]);
       // }

        drawableTextRight.setBounds(0, 0, drawableTextRight.getMinimumWidth(), drawableTextRight.getMinimumHeight());
        // 设置焦点改变的监听
        setOnFocusChangeListener(this);
        // 设置输入框里面内容发生改变的监听
        // addTextChangedListener(this);
        setSelection(length());//设置光标位置在末尾
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    public void onFocusChange(View v, boolean hasFocus) {
        isFocus = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right ;
        if(getInputType()==129){//129密码可见
            right = visible ? drawableTextRight : null;

        }else if(getInputType() == 144){//144密码不可见
            right = visible ? drawablePwdVisi : null;
        }else{
            right = visible ? drawableTextRight : null;

        }
        setCompoundDrawables(drawables[0], drawables[1], right, drawables[3]);

    }


    public void afterTextChanged(Editable s) {

    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isFocus) {
            setClearIconVisible(s.length() > 0);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawableTextRight == null) {//如果没有设置图片
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float x = event.getX();
                float y = event.getY();
                int p = getPaddingRight();//获取输入框的右pading 也就是图标与右边的间距
                int dp = getCompoundDrawablePadding();//获得图片的padding
                int drawableW = drawableTextRight.getBounds().width() + p + dp;//图片区域的宽
                int drawableH = drawableTextRight.getBounds().height() + p + dp;//图片区域的高
                int editW = getWidth();//输入框的宽

                Log.e("inputtype",getInputType()+"");
                if (x > editW - drawableW && x < editW) {
                    if(length()>0) {
                        if (getInputType() == 129) { //不可见时 改为可见并切花图标  129代表inputType="textPassword"
                            setCompoundDrawables(drawables[0], drawables[1], drawablePwdVisi,drawables[3]);
                            setInputType(144);
                        } else if (getInputType()==144) {
                            setCompoundDrawables(drawables[0], drawables[1], drawableTextRight,drawables[3]);
                            setInputType(129);
                        }else{
                            setText("");
                        }
                        setSelection(length());//设置光标位置在末尾
                    }
                    event.setAction(MotionEvent.ACTION_CANCEL);//点击输入框后会调出输入法，这句模拟用户取消，不会弹出输入法
                }

                break;
            }
        return super.onTouchEvent(event);
    }
    private void setPWdVisi(){

    }
}
