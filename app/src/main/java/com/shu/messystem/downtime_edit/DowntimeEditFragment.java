package com.shu.messystem.downtime_edit;

import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import com.shu.messystem.MainActivity;
import com.shu.messystem.ManageRetrofit;
import com.shu.messystem.R;
import com.shu.messystem.component.ClearEditText;
import com.shu.messystem.component.GetGeneralInfomation;

import com.shu.messystem.component.TopTips.util.TipUtil;
import com.shu.messystem.result_bean.GetLineBean;
import com.yyydjk.library.DropDownMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 服务器 on 2018-8-21.
 */

public class DowntimeEditFragment extends Fragment {


    private String headers[] = {"部门","智能排序", "筛选"};
    private String dept[] = {"不限", "工程", "质量", "生产", "运营", "人力","财务"};
    private String sortStrings[] = {"智能排序", "姓名", "部门", "IP", "MAC"};
    //排序字段
    private String sortColumns[] = {"id", "name", "dept", "ip", "mac"};
    private List<View> popupViews = new ArrayList<>();
    private ListDropDownAdapter deptAdapter;
    private ListDropDownAdapter sortAdapter;
    private ArrayList<String[]> data = new ArrayList<>();//保存读取的数据
    private ArrayList<String> recordID = new ArrayList<>();//保存记录的唯一值，ID
    private View rootView;
    private MainActivity mActivity;
    private Handler handler = new Handler();
    private SmartRefreshLayout mRefreshLayout;
    private int top = 1;//当前页，
    final private int num = 50;//每页多少条数据
    private boolean isInit = true;//第一次加载
    private ListView listView;
    private StopTimeAdapter listViewAdapter;
    private DropDownMenu mDropDownMenu;
    final static String argumentsStr = "stoptime_info";
    private String sortColumn = "id";//默认时间排序
    private String nameScreenStr = null;//筛选订单
    private String deptScreenStr = null;//筛选型号
    private String ipScreenStr = null;//筛选条码号
    private String macScreenStr = null;//筛选条码号
    private String screenBeginDate = GetGeneralInfomation.getCurrDateByDayBegin();//筛选开始时间,默认为当天08:00
    private String screenEndDate = null;//筛选结束时间
    private boolean screenWeishifang = false;//筛选未释放
    private String bcname = null;
    private String bzname = null;
    private String deptColum = null;//默认所有线体
    private TextView statusView;
    private boolean stop = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.stoptime_fragement_edit, container, false);
        this.rootView = rootView;
        mActivity = (MainActivity) getActivity();

        initContentView();
        initDropDownMenu();
        statusView = rootView.findViewById(R.id.loading);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();

    }

    @Override
    public void onStart() {
        super.onStart();
        //
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {//显示时
            readArguments();

        }
    }

    //释放停机时后 刷新
    private void readArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            boolean isSuccess = bundle.getBoolean("isSuccess");
            if (isSuccess) {
                mRefreshLayout.autoRefresh();
            }
        }
    }
    //监听 fragment动画是否结束
   /* @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
       Animator anim= AnimatorInflater.loadAnimator(mActivity,transit);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("fragment切换动画结束","");

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return super.onCreateAnimator(transit, enter, nextAnim);
    }*/


    private void initDropDownMenu() {
        mDropDownMenu = rootView.findViewById(R.id.dropDownMenu);
        //init popupViews
        popupViews.add(createMenuViewByxianti());
        popupViews.add(createMenuViewBysort());
       // popupViews.add(createMenuViewBybanzuci());
        popupViews.add(createMenuViewByscreen());
        //init dropdownview
        mDropDownMenu.setDropDownMenu(Arrays.asList(headers), popupViews, mRefreshLayout);
    }
    private View createMenuViewByxianti(){
        //add item click event
        //线体选择菜单
        final ListView xiantiView = new ListView(mActivity);
        deptAdapter = new ListDropDownAdapter(mActivity, Arrays.asList(dept));
        xiantiView.setDividerHeight(0);
        xiantiView.setAdapter(deptAdapter);
        xiantiView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deptAdapter.setCheckItem(position);
                mDropDownMenu.setTabText(position == 0 ? headers[0] : dept[position].substring(6));
                //刷新数据 筛选线体
                deptColum = dept[position];
                mRefreshLayout.autoRefresh();
                mDropDownMenu.closeMenu();
            }
        });
        return xiantiView;
    }
    private View createMenuViewBybanzuci(){
        //班次班组筛选菜单
        final View banzuciView = LayoutInflater.from(mActivity).inflate(R.layout.stoptime_fragement_edit_screenbancibanzu, null);
        CheckBox dabaiban = banzuciView.findViewById(R.id.dabaiban);
        CheckBox dayeban = banzuciView.findViewById(R.id.dayeban);
        CheckBox baiban = banzuciView.findViewById(R.id.baiban);
        CheckBox zhongban = banzuciView.findViewById(R.id.zhongban);
        CheckBox yeban = banzuciView.findViewById(R.id.yeban);
        CheckBox jia = banzuciView.findViewById(R.id.jia);
        CheckBox yi = banzuciView.findViewById(R.id.yi);
        CheckBox bing = banzuciView.findViewById(R.id.bing);
        TextView confirmBanzu = banzuciView.findViewById(R.id.confirm);
        confirmBanzu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bcname=null;
                bzname=null;
                if (dabaiban.isChecked()) {
                    bcname = " AND (bc_name='大白班'";
                }
                if (dayeban.isChecked()) {
                    if (bcname == null) {
                        bcname = " AND (bc_name='大夜班'";
                    } else {
                        bcname = bcname + " OR bc_name='大夜班'";
                    }
                }
                if (baiban.isChecked()) {
                    if (bcname == null) {
                        bcname = " AND (bc_name='白班'";
                    } else {
                        bcname = bcname + " OR bc_name='白班'";
                    }
                }
                if (zhongban.isChecked()) {
                    if (bcname == null) {
                        bcname = " AND (bc_name='中班'";
                    } else {

                        bcname = bcname + " OR bc_name='中班'";
                    }
                }
                if (yeban.isChecked()) {
                    if (bcname == null) {
                        bcname = " AND (bc_name='夜班'";
                    } else {

                        bcname = bcname + " OR bc_name='夜班'";
                    }
                }
                if (bcname!=null&&!TextUtils.isEmpty(bcname.trim())) {
                    bcname = bcname + ")";
                }
                if (jia.isChecked()) {
                    if (bzname == null) {
                        bzname = " AND (banzu_name='甲班'";
                    } else {

                        bzname = bzname+" OR banzu_name='甲班'";
                    }
                }
                if (yi.isChecked()) {
                    if (bzname == null) {
                        bzname = " AND (banzu_name='乙班'";
                    } else {

                        bzname = bzname+" OR banzu_name='乙班'";
                    }
                }
                if (bing.isChecked()) {
                    if (bzname == null) {
                        bzname = " AND (banzu_name='丙班'";
                    } else {

                        bzname =bzname+ " OR banzu_name='丙班'";
                    }
                }
                if (bzname!=null&&!TextUtils.isEmpty(bzname.trim())) {
                    bzname = bzname + ")";
                }
                mRefreshLayout.autoRefresh();
                mDropDownMenu.closeMenu();
            }
        });

        return banzuciView;
    }
    private View createMenuViewBysort(){
        //智能排序菜单
        final ListView sortView = new ListView(mActivity);
        sortView.setDividerHeight(0);
        sortAdapter = new ListDropDownAdapter(mActivity, Arrays.asList(sortStrings));
        sortView.setAdapter(sortAdapter);

        sortView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sortAdapter.setCheckItem(position);
                mDropDownMenu.setTabText(position == 0 ? headers[2] : sortStrings[position]);
                //刷新数据 排序
                sortColumn = sortColumns[position];
                mRefreshLayout.autoRefresh();
                mDropDownMenu.closeMenu();
            }
        });
        return sortView;
    }
    private View createMenuViewByscreen(){
        //筛选菜单
        final View screenView = LayoutInflater.from(mActivity).inflate(R.layout.stoptime_fragement_edit_screen, null);
        ClearEditText nameScreen = screenView.findViewById(R.id.name);
        ClearEditText deptScreen = screenView.findViewById(R.id.dept);
        ClearEditText ipScreen = screenView.findViewById(R.id.ip);
        ClearEditText macScreen = screenView.findViewById(R.id.mac);
        ClearEditText beginDate = screenView.findViewById(R.id.begin_date);
        beginDate.setText(GetGeneralInfomation.getCurrDateByDayBegin());
        ClearEditText endDate = screenView.findViewById(R.id.end_date);
        endDate.setText(GetGeneralInfomation.getCurrDateByDayEnd());
        TextView confirm = screenView.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenBeginDate = beginDate.getText().toString();
                screenEndDate = endDate.getText().toString();
                //判断时间格式
                if(!GetGeneralInfomation.deteDateFormat(screenBeginDate,"yyyy-MM-dd HH:mmm")
                        ||!GetGeneralInfomation.deteDateFormat(screenEndDate,"yyyy-MM-dd HH:mmm")){
                    new TipUtil().showTips(mActivity,MainActivity.toolbar,"时间格式不正确",0);
                    return;
                }

                nameScreenStr = nameScreen.getText().toString();
                deptScreenStr = deptScreen.getText().toString();
                ipScreenStr = ipScreen.getText().toString();
                macScreenStr = macScreen.getText().toString();
                mRefreshLayout.autoRefresh();
                mDropDownMenu.closeMenu();
            }
        });

        return screenView;
    }
    private void initContentView() {
        //init context view
        View content = LayoutInflater.from(mActivity).inflate(R.layout.stoptime_fragement_edit_content, null);
        mRefreshLayout = (SmartRefreshLayout) content.findViewById(R.id.refreshLayout);
        listView = content.findViewById(R.id.stoptime_list);
    }

    /*    public void onBackPressed() {
            //退出activity前关闭菜单
            if (mDropDownMenu.isShowing()) {
                mDropDownMenu.closeMenu();
            } else {
                super.onBackPressed();
            }
        }*/
    synchronized private void loadData() {
        if (stop) {
            mRefreshLayout.finishLoadmore();
            return;
        }

        Call<GetLineBean> request = ManageRetrofit.getRetroInter(mActivity).getList(getSqlStr());
        request.enqueue(new Callback<GetLineBean>() {
            @Override
            public void onResponse(Call<GetLineBean > call, Response<GetLineBean > response) {
                int i=0;
                int count=data.size();
                if (response.body()!=null){

                    String msg = response.body().getMsg();

                    if ("success".equals(msg)) {//密码正确"success".equals(msg)

                        List<GetLineBean.DataBean> getData = response.body().getData();

                        for (; i < getData.size(); i++) {
                            String[] column = new String[]{
                                    getData.get(i).getId()
                                    ,getData.get(i).getName()
                                    ,getData.get(i).getDept()
                                    ,getData.get(i).getIp()
                                    ,getData.get(i).getMac()
                                    ,getData.get(i).getComputer()
                                    ,getData.get(i).getType()};

                            data.add(count+i, column);

                        }

                        }


                    }
                if (i+count == (top * num)) {
                    top++;
                } else {//说明已经没数据，不需翻页
                    stop = true;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (data.size() == 0) {
                            statusView.setVisibility(View.VISIBLE);
                            statusView.setText("很棒!今天没有停机时");
                        } else {
                            statusView.setVisibility(View.GONE);
                        }
                    }
                });
                //刷新列表
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isInit) {//第一次加载
                            initListView();
                            initRefreshControl();
                            isInit = !isInit;
                        } else {//刷新数据
                            mRefreshLayout.finishRefresh();
                            mRefreshLayout.finishLoadmore();
                        }

                        if (listViewAdapter != null) {
                            listViewAdapter.notifyDataSetChanged();
                        }
                    }
                });



                }




            @Override
            public void onFailure(Call<GetLineBean > call, Throwable t) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setVisibility(View.VISIBLE);
                        statusView.setText("查询失败，请重试");
                    }
                });
                //刷新列表
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isInit) {//第一次加载
                            initListView();
                            initRefreshControl();
                            isInit = !isInit;
                        } else {//刷新数据
                            mRefreshLayout.finishRefresh();
                            mRefreshLayout.finishLoadmore();
                        }

                        if (listViewAdapter != null) {
                            listViewAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }


        });


    }

    private String getSqlStr() {
        String sql = "SELECT TOP " + num + " id" +//取开始的40条
                ",name,dept,ip,mac,computer,type" +
                " FROM cqgt_IpMac " +
                "WHERE id>0 " ;
        //取开始的20条  40-20 等于中间的20条 即 id 20-40
        String sql1 = "SELECT TOP " + (top - 1) * num + " ID" +
                " FROM cqgt_IpMac WHERE id>0  ";//大于5分钟 小于7H
        //未选择线体，或选择不限时，不筛选线体

        if (deptColum != null && !deptColum.equals("不限")) {
            sql1 = sql1 + " AND dept='" + deptColum + "' ";
            sql = sql + " AND dept='" + deptColum + "' ";
        }
        if (nameScreenStr != null && !TextUtils.isEmpty(nameScreenStr.trim())) {
            sql1 = sql1 + " AND name='" + nameScreenStr + "' ";
            sql = sql + " AND name='" + nameScreenStr + "' ";
        }
        if (deptScreenStr != null && !TextUtils.isEmpty(deptScreenStr.trim())) {
            sql1 = sql1 + " AND dept='" + deptScreenStr + "' ";
            sql = sql + " AND dept='" + deptScreenStr + "' ";
        }
        if (ipScreenStr != null && !TextUtils.isEmpty(ipScreenStr.trim())) {
            sql1 = sql1 + " AND ip='" + ipScreenStr + "' ";
            sql = sql + " AND ip='" + ipScreenStr + "' ";
        }
        if (macScreenStr != null && !TextUtils.isEmpty(macScreenStr.trim())) {
            sql1 = sql1 + " AND mac='" + macScreenStr + "' ";
            sql = sql + " AND mac='" + macScreenStr + "' ";
        }
 /*       if (screenBeginDate != null && !TextUtils.isEmpty(screenBeginDate.trim())) {
            sql1 = sql1 + " AND OfflineTimeInfo>='" + screenBeginDate + "' ";
            sql = sql + " AND OfflineTimeInfo>='" + screenBeginDate + "' ";
        }
        if (screenEndDate != null && !TextUtils.isEmpty(screenEndDate.trim())) {
            sql1 = sql1 + " AND OfflineTimeInfo<'" + screenEndDate + "' ";
            sql = sql + " AND OfflineTimeInfo<'" + screenEndDate + "' ";
        }*/

        sql1 = sql1 + " ORDER BY " + sortColumn + " ASC ";

        sql = sql + " AND ID NOT IN ( " + sql1 + ")";
        sql = sql + " ORDER BY " + sortColumn + " ASC ";

        return sql;
    }

    private void initListView() {
        listViewAdapter = new StopTimeAdapter(mActivity);
        listView.addHeaderView(LayoutInflater.from(mActivity).inflate(R.layout.stoptime_fragement_edit_listview_header, null));
        listView.setAdapter(listViewAdapter);
        // listView.setEmptyView(statusView);

    }

    private void initRefreshControl() {

        //设置 Header 为 Material风格
        //mRefreshLayout.setRefreshHeader(new MaterialHeader(this).setShowBezierWave(true));
        //设置 Footer 为 球脉冲
        mRefreshLayout.setRefreshFooter(new BallPulseFooter(mActivity).setSpinnerStyle(SpinnerStyle.Scale));

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh(RefreshLayout refreshlayout) {

                //refreshlayout.finishRefresh(20000);//延迟20s
                //查询计划停机数据
                // queryPlanData(rootView,isRefresh);
                top = 1;
                stop = false;
                Log.e("加载", "加载第" + top + "页");
                data.clear();
                recordID.clear();
                loadData();
            }

        });
        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                //加载第top页
                Log.e("加载", "加载第" + top + "页");
                loadData();
                refreshlayout.finishLoadmore(20000);//延迟20s
            }
        });
    }



    public void queryThread() {
        mRefreshLayout.autoRefresh();
    }

    class StopTimeAdapter extends BaseAdapter {
        private Context context;

        StopTimeAdapter(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.stoptime_fragement_edit_single, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.setValue(position);

            return convertView;
        }


        public class ViewHolder {
            public View rootView;
            TextView id;
             TextView dept;
            TextView name;
            TextView ip;
            TextView mac;
            TextView computer;
            TextView type;


             ViewHolder(View rootView) {


                this.rootView = rootView;
                this.id= (TextView) rootView.findViewById(R.id.id);
                this.dept = (TextView) rootView.findViewById(R.id.dept);
                this.name = (TextView) rootView.findViewById(R.id.name);
                this.ip = (TextView) rootView.findViewById(R.id.ip);
                this.mac = (TextView) rootView.findViewById(R.id.mac);
                 this.computer = (TextView) rootView.findViewById(R.id.computer);
                 this.type = (TextView) rootView.findViewById(R.id.type);

            }

            private void setValue(int position) {

                //未释放的点击释放
              //  if (data.get(position)[14].equals("原因未释放")) {
                    rootView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                            Fragment fragment = fragmentManager.findFragmentByTag("02_0");
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            ft.hide(DowntimeEditFragment.this);
                            ft.setCustomAnimations(R.anim.fragment_right_in_anim
                                    , R.anim.fragment_left_out_anim
                                    , R.anim.fragment_left_in_anim
                                    , R.anim.fragment_right_out_anim);
                            if (fragment == null) {
                                fragment = new DowntimeEditSubmitFragment();
                                ft.add(R.id.fragement_frame, fragment, "02_0");
                            } else {
                                ft.show(fragment);
                            }
                            Bundle bundle = new Bundle();
                            bundle.putStringArray(argumentsStr, data.get(position));
                            bundle.putString("ID", data.get(position)[0]);
                            //传递参数
                            fragment.setArguments(bundle);
                            mActivity.currentFragment=fragment;
                            ft.commit();
                        }
                    });
               // }

                String[] singleData = data.get(position);
                this.id.setText(String.format("%s、", singleData[0]));
                this.name.setText(singleData[1]);
                this.dept.setText(singleData[2]);
                this.ip.setText(singleData[3]);
                this.mac.setText(singleData[4]);
                this.computer.setText(singleData[5]);
                this.type.setText(singleData[6]);

            }
            private String dateTrans(String date){
                 if(date.equals("无")){
                     return date;
                 }
                return date.substring(0,date.lastIndexOf('.'));//去掉日期后面的毫秒

            }
        }

    }
}

