package com.vagrant.timetableproject.model;

import java.util.List;

public class SimplifiedCourse {
    //解析json后构建的简化类，存储必要的课程信息
    public String courseId;
    public String courseSeq;
    public String courseName;
    public String teacher;
    public int weekday;
    public int startJc;
    public int duration;
    public List<Integer> weeks;
    public String location;
    public String weekDescription;
    //解析后生成课程的开始和结束时间
    public String startTime;
    public String endTime;
}
