package com.shu.messystem;


import com.shu.messystem.result_bean.GetLineBean;
import com.shu.messystem.result_bean.GetUserInfoBean;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by Administrator on 2018/4/20.
 */

public interface RetrofitInterface {

    // @GET注解的作用:采用Get方法发送网络请求
    // getCall() = 接收网络请求数据的方法
    // 其中返回类型为Call<*>，*是接收数据的类（即上面定义的Translation类）
    // 如果想直接获得Responsebody中的内容，可以定义网络请求返回值为Call<ResponseBody>

/**********************登录注册**********************************/


    /**
     * 用户登录
     */
    //http://www.yambaobao.com/login/user?clientip=10.139.114.1&equipment=2&password=1&mobile=13388921821&logintype=1
    @POST("/mesapp/Login")
    Call<GetUserInfoBean> getUserInfo(@QueryMap HashMap<String, String> queryMap);

    @POST("/mesapp/Permission")
    Call<GetUserInfoBean> getUserPermission(@Query("gonghao") String gonghao );

    @POST("/mesapp/GetLine")
    Call<GetLineBean> getLine();

//这里用@QueryMap会乱码
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/addIpMac")
    Call<GetUserInfoBean> addIpMac(@FieldMap HashMap<String, String> queryMap);


    @POST("/mesapp/GetPlantime")
    Call<GetLineBean> getPlantime();

    @POST("/mesapp/GetScanRecord")
    Call<GetLineBean> queryScanRecord(@Query("code") String code );


    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/GetPlantime")
    Call<GetLineBean> getPlantime(@Field("where") String where);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/DelPlantime")
    Call<GetLineBean> delPlantime(@FieldMap HashMap<String, String> queryMap);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/ModiPlantime")
    Call<GetLineBean> modiPlantime(@FieldMap HashMap<String, String> queryMap);


    @POST("/mesapp/GetDept")
    Call<GetLineBean> getDept();

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/GetName")
    Call<GetLineBean> getName(@FieldMap HashMap<String, String> queryMap);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/GetCom")
    Call<GetLineBean> getCom(@FieldMap HashMap<String, String> queryMap);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/GetType")
    Call<GetLineBean> getType(@FieldMap HashMap<String, String> queryMap);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/GetList")
    Call<GetLineBean> getList(@Field("sql") String sql);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/GetDetail")
    Call<GetLineBean> getDetail(@FieldMap HashMap<String, String> queryMap);


    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/SaveEdit")
    Call<GetLineBean> saveEdit(@FieldMap HashMap<String, String> queryMap);

    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/mesapp/IPMacDel")
    Call<GetLineBean> delIpMac(@Field("id") String id);
}
