package com.vagrant.timetableproject.model;

import java.util.List;

public class Course {
    public String attendClassTeacher;
    public String courseCategoryCode;
    public String courseCategoryName;
    public String courseName;
    public String coursePropertiesCode;
    public String coursePropertiesName;
    public String dgFlag;
    public String examTypeCode;
    public String examTypeName;
    public String flag;

    public Id id;

    public String pkbz;
    public String programPlanName;
    public String programPlanNumber;
    public String qqqh;
    public String restrictedCondition;
    public String rlFlag;
    public String selectCourseStatusCode;
    public String selectCourseStatusName;
    public String sfyx;
    public String skzcs;

    public String studyModeCode;
    public String studyModeName;

    public List<TimeAndPlace> timeAndPlaceList;

    public Double unit;//Double允许 null
    public List<Object> wxq;

    public String ywdgFlag;
    public String zkxh;
}
