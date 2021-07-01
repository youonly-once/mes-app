package com.shu.messystem.plantime;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.shu.messystem.MainActivity;
import com.shu.messystem.ManageRetrofit;
import com.shu.messystem.R;
import com.shu.messystem.component.CustomProgressDialog;
import com.shu.messystem.component.TopTips.util.TipUtil;
import com.shu.messystem.result_bean.GetLineBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 01405645 on 2018-04-16.
 */

public class PlanTimeForStopFragementChildDel extends Fragment {

    private AppCompatActivity parentActivity;// 获取父Activity
    private ArrayList<String> xiantiList = new ArrayList<>();
    private ArrayList<String> planList = new ArrayList<>();
    private ArrayList<String> beginList = new ArrayList<>();
    private ArrayList<String> endList = new ArrayList<>();
    private SmartRefreshLayout mRefreshLayout;
    private SwipeMenuListView swipeMenuListView;
    private AppAdapter appAdapter;
    private boolean isRefresh = false;//第一次加载为false
    private Handler handler = new Handler();
    private View rootView;
    private TextView statusView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.plantimeforstop_fragement_childdel, container, false);

        parentActivity = (AppCompatActivity) getActivity();// 获取Acticity

        initSwipeListView();
        Log.e("onCreateView", "onCreateView");
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("onActivityCreated", "onActivityCreated");
        //第一次查询数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                queryPlanData();
            }
        }).start();

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("onResume", "onResume");
    }

    private void initRefreshControl() {
        mRefreshLayout = (SmartRefreshLayout) rootView.findViewById(R.id.refreshLayout);
        //设置 Header 为 Material风格
        //mRefreshLayout.setRefreshHeader(new MaterialHeader(this).setShowBezierWave(true));
        //设置 Footer 为 球脉冲
        mRefreshLayout.setRefreshFooter(new BallPulseFooter(parentActivity).setSpinnerStyle(SpinnerStyle.Scale));

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh(RefreshLayout refreshlayout) {
                statusView.setText("加载中...");
                //refreshlayout.finishRefresh(20000);//延迟20s
                //查询计划停机数据
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        queryPlanData();
                    }
                }).start();
                Log.e("load", "refresh");
            }

        });
        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                Log.e("load", "more");
                refreshlayout.finishLoadmore(1000);//延迟20s
            }
        });
    }

    synchronized private void queryPlanData() {

     /*      // SharedPreferences shared = parentActivity.getSharedPreferences("query", Context.MODE_PRIVATE);
            Connection con = ConnectServer.connectServer(parentActivity);
            Statement statement = con.createStatement();
            String sql = "SELECT  * FROM PlanTimeForStop order by LineId";
            ResultSet resultSet = statement.executeQuery(sql);
            //清空之前的数据
            xiantiList.clear();
            planList.clear();
            beginList.clear();
            endList.clear();
            while (resultSet.next()) {
                xiantiList.add(resultSet.getString("LineName"));
                planList.add(resultSet.getString("Mark"));
                beginList.add(resultSet.getString("StartTime"));
                endList.add(resultSet.getString("EndTime"));
            }*/


            xiantiList.clear();
            planList.clear();
            beginList.clear();
            endList.clear();
            Call<GetLineBean> request = ManageRetrofit.getRetroInter(parentActivity).getPlantime();
            request.enqueue(new Callback<GetLineBean>() {
                @Override
                public void onResponse(Call<GetLineBean> call, Response<GetLineBean> response) {
                    if (response.body()!=null) {

                        String msg = response.body().getMsg();

                        if ("success".equals(msg)) {//密码正确
                            List<GetLineBean.DataBean> data = response.body().getData();
                            for (int i = 0; i < data.size(); i++) {
                                xiantiList.add(data.get(i).getLineName());
                                planList.add(data.get(i).getMark());
                                beginList.add(data.get(i).getStartTime());
                                endList.add(data.get(i).getEndTime());
                            }


                            //刷新数据
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    if (xiantiList.size() == 0) {
                                        statusView.setText("没有数据");
                                    } else {
                                        appAdapter.notifyDataSetChanged();
                                    }
                                }
                            });

                        } else {
                            //刷新完成
                            topTip("初始化失败，请重试", 0);
                            statusView.setText("加载失败");

                        }
                    }else{
                        //刷新完成
                        topTip("初始化失败，请重试", 0);
                        statusView.setText("加载失败");
                    }
                    operator();
                }

                @Override
                public void onFailure(Call<GetLineBean> call, Throwable t) {
                    //刷新完成
                    topTip("初始化失败，请重试", 0);
                    statusView.setText("加载失败");
                    operator();
                }

            });



    }
    private void operator(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRefresh) {//刷新数据
                    mRefreshLayout.finishRefresh();

                } else {//第一次加载
                    //初始化滑动列表
                    initRefreshControl();
                    PlanTimeForStopFragementChildDel.this.isRefresh = true;
                }

            }
        });
    }

    private void initSwipeListView() {
        //创建listView
        swipeMenuListView = rootView.findViewById(R.id.plan_list);
        appAdapter = new AppAdapter();
        swipeMenuListView.setAdapter(appAdapter);
        //无数据时显示内容
        statusView = rootView.findViewById(R.id.loading);
        swipeMenuListView.setEmptyView(statusView);
        //获得 item高度 让item高度与menu宽度相同
        ListAdapter listAdapter = swipeMenuListView.getAdapter();

        int heightTemp;
        if (xiantiList.size() == 0 || listAdapter == null) {
            heightTemp = 200;
        } else {
            View listItem = listAdapter.getView(0, null, swipeMenuListView);
            listItem.measure(0, 0);
            listItem.getMeasuredHeight();
            heightTemp = listItem.getMeasuredHeight();
        }

        final int height = heightTemp;

        //创建菜单
        SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // 创建“打开”项
                SwipeMenuItem editItem = new SwipeMenuItem(parentActivity.getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                editItem.setWidth(height);
                editItem.setTitle("修改");
                editItem.setTitleSize(18);
                editItem.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(editItem);

                // 创建“删除”项
                SwipeMenuItem deleteItem = new SwipeMenuItem(parentActivity.getApplicationContext());
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(height);
                //deleteItem.setIcon(R.drawable.ic_delete);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(deleteItem);
            }

            ;
        };
        //设置菜单
        swipeMenuListView.setMenuCreator(swipeMenuCreator);
        //设置子项点击事件   点击设置默认值
        //子项内组件设置了 事件 该事件失效
        swipeMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("click_position", position + "");

            }
        });
        //为ListView设置滑动菜单项点击监听器，监听菜单项的点击事件
        swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Log.e("menu_position", position + "");
                Log.e("menu_index", index + "");
                switch (index) {
                    case 0://修改
                        modiPlanData(position);
                        break;
                    case 1://删除
                        delPlanData(position);//删除第position条数据
                        break;

                }
                return false;
            }
        });
    }

    private void modiPlanData(int position) {
        //获取父fragment，然后获得子fragment 使得同层切换fragment
        android.support.v4.app.FragmentManager fragmentManager = getParentFragment().getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.hide(this);//隐藏当前页面
        //显示修改页面
        //查找修改页面的fragment是否创建
        Fragment fragment = fragmentManager.findFragmentByTag("2");
        //传递修改的信息
        Bundle bundle = new Bundle();
        String[] modiInfo = {xiantiList.get(position), planList.get(position), beginList.get(position), endList.get(position)};
        bundle.putStringArray("modiInfo", modiInfo);
        if (fragment != null) {
            fragment.setArguments(bundle);
            ft.show(fragment);//显示修改页面
        } else {
            fragment = new PlanTimeForStopFragementChildModi();
            fragment.setArguments(bundle);
            ft.add(R.id.plantimeforstop_childfragement_frame, fragment, "2");
        }
        PlanTimeForStopFragementMain.currentFragment = fragment;
        ft.commit();

    }

    private void delPlanData(int position) {
        CustomProgressDialog dialog = new CustomProgressDialog(parentActivity, "正在删除");
        dialog.show();
   /*     new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                //    SharedPreferences shared = parentActivity.getSharedPreferences("query", Context.MODE_PRIVATE);
                    Connection con = ConnectServer.connectServer(parentActivity);
                    Statement statement = con.createStatement();
                    String sql = "DELETE FROM PlanTimeForStop WHERE " +
                            "LineName='" + xiantiList.get(position) + "'" +
                            "AND Mark='" + planList.get(position) + "' " +
                            "AND StartTime='" + beginList.get(position) + "' " +
                            "AND EndTime='" + endList.get(position) + "'";
                    Log.e("sql", sql);
                    Log.e("del status", String.valueOf(statement.executeUpdate(sql)));
                    //刷新列表
                    refreshListView();
                    //通知修改界面刷新线体，plan数据
                    PlanTimeForStopFragementMain.modirefresh = true;
                    topTip("已删除", 1);
                    Log.e("删除成功", "删除成功");
                } catch (ClassNotFoundException | SQLException e) {
                    topTip("删除失败，请重试", 0);
                    e.printStackTrace();
                } finally {
                    //加载完成后，关闭提示框
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
            }
        }).start();*/


        HashMap<String,String> map=new HashMap<>();
        map.put("LineName",xiantiList.get(position));
        map.put("Mark",planList.get(position) );
        map.put("StartTime",beginList.get(position));
        map.put("EndTime",endList.get(position));
        Call<GetLineBean> request = ManageRetrofit.getRetroInter(parentActivity).delPlantime(map);
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                if (response.body()!=null) {
                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确

                        //刷新列表
                        refreshListView();
                        //通知修改界面刷新线体，plan数据
                        PlanTimeForStopFragementMain.modirefresh = true;
                        topTip("已删除", 1);
                        Log.e("删除成功", "删除成功");

                    } else {
                        topTip("删除失败，请重试", 0);
                    }
                }else{
                    topTip("删除失败，请重试", 0);
                }
                //加载完成后，关闭提示框
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                topTip("删除失败，请重试", 0);
            }

        });

    }

    private void refreshListView() {

        isRefresh = true;
        mRefreshLayout.autoRefresh();
    }

    class AppAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return xiantiList.size();
        }

        @Override
        public Object getItem(int position) {
            return xiantiList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            // menu type count
            return 4;
        }

        @Override
        public int getItemViewType(int position) {
            // current menu type
            return position % 4;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (xiantiList.size() == 0) {
                // Toast.makeText(AddrManageActivity.this,"该用户没有地址",Toast.LENGTH_SHORT).show();
                return null;
            }
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(parentActivity, R.layout.plantimeforstop_fragement_childdel_plansingle, null);
                viewHolder.number = (TextView) convertView.findViewById(R.id.number);
                viewHolder.line = (TextView) convertView.findViewById(R.id.line);
                viewHolder.plan = (TextView) convertView.findViewById(R.id.plan);
                viewHolder.begin = (TextView) convertView.findViewById(R.id.begin_time);
                viewHolder.end = (TextView) convertView.findViewById(R.id.end_time);
                //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                //lp.setMargins(0, 1, 0, 0);
                //convertView.setLayoutParams(lp);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.number.setText(position + 1 + "、");
            viewHolder.line.setText(xiantiList.get(position));
            viewHolder.plan.setText(planList.get(position));
            viewHolder.begin.setText(beginList.get(position));
            viewHolder.end.setText(endList.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView number;
            TextView line;
            TextView plan;
            TextView begin;
            TextView end;
        }

    }

    //除第一次创建，后面显示会执行的函数 隐藏hidden为false
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //读取从删除页面点击修改后传过来的值
        if (!hidden) {//显示后
            if (PlanTimeForStopFragementMain.delrefresh == true) {//数据改变 刷新
                refreshListView();
                PlanTimeForStopFragementMain.delrefresh = false;//重置
            }

        }
        Log.e("hidden", hidden + "");
    }

    //父fragment 反射调用，标题栏刷新
    public void queryThread() {
        refreshListView();
    }

    private void topTip(final String tip, int status) {
        handler.post(new Runnable() {
            @Override
            public void run() {

                new TipUtil().showTips(parentActivity, MainActivity.toolbar, tip, status);
            }
        });
    }
}