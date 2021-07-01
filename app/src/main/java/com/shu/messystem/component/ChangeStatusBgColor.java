package com.shu.messystem.component;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2018/4/19.
 */

public class ChangeStatusBgColor {
    //设置状态栏成白色的背景，字体颜色为。
    public static void SetStatusBgColor(Activity context, int color) {
        //1.对于android6.0，但是小米魅族不适配（见2，3）

        //设置状态栏成白色的背景，字体颜色为黑色。

        if (Build.VERSION.SDK_INT >= 23) {
            Window window = context.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(context.getResources().getColor(color));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //2.对于miui
        //设置成白色的背景，字体颜色为黑色。
        if (OSUtils.getRomType().equals("MIUI")) {
            Class<? extends Window> clazz = context.getWindow().getClass();
            try {
                int darkModeFlag = 0;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(context.getWindow(), 0, darkModeFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //3.对于flyme:
        //设置成白色的背景，字体颜色为黑色。
        if (OSUtils.getRomType().equals("FLYME")) {
            boolean dark = true;
            try {
                WindowManager.LayoutParams lp = context.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                context.getWindow().setAttributes(lp);
            } catch (Exception e) {
            }
        }
    }
}
