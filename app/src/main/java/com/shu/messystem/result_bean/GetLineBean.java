package com.shu.messystem.result_bean;

import java.util.List;

/**
 * Created by Administrator on 2019-2-2.
 */

public class GetLineBean {
    /**
     * msg : 123
     * data : [{"lid":"success","Linename":"Haier458 "},{"lid":"success","Linename":"Haier458 "}]
     */

    private String msg;
    private List<DataBean> data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * lid : success
         * Linename : Haier458
         */

        private String lid;
        private String Linename;
        private String LineName;

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        private String info;
        public String getBc_Name() {
            return Bc_Name;
        }

        private String id;
        private String ip;
        private String computer;

        public String getComputer() {
            return computer;
        }

        public void setComputer(String computer) {
            this.computer = computer;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        private String type;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDept() {
            return dept;
        }

        public void setDept(String dept) {
            this.dept = dept;
        }

        public String getId() {

            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        private String mac;
        private String name;
        private String dept;


        public void setBc_Name(String bc_Name) {
            Bc_Name = bc_Name;
        }

        private String Bc_Name;
        public String getMark() {
            return Mark;
        }

        public void setMark(String mark) {
            Mark = mark;
        }

        public String getStartTime() {
            return StartTime;
        }

        public void setStartTime(String startTime) {
            StartTime = startTime;
        }

        public String getEndTime() {
            return EndTime;
        }

        public void setEndTime(String endTime) {
            EndTime = endTime;
        }

        private String Mark;

        public String getLineName() {
            return LineName;
        }

        public void setLineName(String lineName) {
            LineName = lineName;
        }

        private String StartTime;
        private String EndTime;

        public String getLid() {
            return lid;
        }

        public void setLid(String lid) {
            this.lid = lid;
        }

        public String getLinename() {
            return Linename;
        }

        public void setLinename(String Linename) {
            this.Linename = Linename;
        }
    }
}
