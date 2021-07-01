package com.shu.messystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;



import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Administrator on 2018/5/1.
 */

public class ManageRetrofit {

    private static String cookieStr="";
    private static RetrofitInterface retrofitInterface;
    private static final long TIMOUT=20000;



    private Context context;    /**
     * 初始化 OkHttpClient
     *
     * @return
     */
    private static OkHttpClient getClient(final Context context) {
        OkHttpClient.Builder client = new OkHttpClient().newBuilder();

        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 获取 Cookie

                    Response resp = chain.proceed(chain.request());
                    List<String> cookies = resp.headers("Set-Cookie");
                    if (cookies != null && cookies.size() > 0) {
                        for (int i = 0; i < cookies.size(); i++) {
                            cookieStr += cookies.get(i);
                        }
                        //返回的 cookie: JSESSIONID=AB09BDCE87CA3DA25EA5C97CF0D2254B; Path=/; HttpOnly
                        //需要截取 JSESSIONID=AB09BDCE87CA3DA25EA5C97CF0D2254B
                        if(cookieStr.length()>=43){
                            cookieStr=cookieStr.substring(0,43);
                        }

                        Log.e("readServerCookie",cookieStr);
                    }
                    //Log.e("X",cookies.toString());
                return resp;
            }
        });
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 设置 Cookie
                //读取保存的 cookie
                SharedPreferences sharedPreferences=context.getSharedPreferences("cookies",Context.MODE_PRIVATE);
                String cookie=sharedPreferences.getString("cookie","null");
                if(cookie.equals("null")) {
                    Log.e("获取本地cookie失败","请重新登录");
                }else{
                    Log.e("readLocalCookie",cookie);
                    return chain.proceed(chain.request().newBuilder().header("Cookie", cookie).build());
                }
                return chain.proceed(chain.request());
            }
        });
        client.connectTimeout(TIMOUT, TimeUnit.MILLISECONDS);
        client.writeTimeout(TIMOUT, TimeUnit.MILLISECONDS);
        client.readTimeout(TIMOUT, TimeUnit.MILLISECONDS);
        return client.build();
    }
    public static RetrofitInterface getRetroInter(Context context){
        //获得更新地址
        SharedPreferences sharedPreferences= context.getSharedPreferences(LoginActivity.Login_Info_SHARED, MODE_PRIVATE);
        String url=sharedPreferences.getString("server_addr", SettingActivity.serAddr);
        Log.e("ser",url);
        if (retrofitInterface == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient(context))
                    .build();
            retrofitInterface = retrofit.create(RetrofitInterface.class);
        }
        return retrofitInterface;
    }
    //登录后调用保存该 cookie
    public static void saveLoginCookie(Context context){
        //保存 cookie
        SharedPreferences sharedPreferences=context.getSharedPreferences("cookies",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=sharedPreferences.edit();
        edit.putString("cookie",cookieStr);
        edit.apply();
        Log.e("saveCookie","cookie:"+cookieStr);
    }

}
