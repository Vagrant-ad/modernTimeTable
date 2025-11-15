package com.vagrant.timetableproject.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vagrant.timetableproject.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CourseParser {
    private Gson gson = new Gson();

    public List<SimplifiedCourse> parse(String json){
        //将json文件解析为root再简化删除不需要的信息
        Root root = gson.fromJson(json,Root.class);
        List<SimplifiedCourse> res = new ArrayList<>();
        Map<Integer,JcAndTime> jcMap = buildJcMap(root.jcsjbs);
        parseXkxx(root,jcMap,res);
        parseDataList(root,jcMap,res);
        return res;
    }
    private Map<Integer,JcAndTime> buildJcMap(List<JcAndTime> jcsjbs){
        //构建节次映射
        Map<Integer,JcAndTime> map = new HashMap<>();
        for(JcAndTime item : jcsjbs){
            map.put(Integer.parseInt(item.jc),item);
        }
        return map;
    }
    private void parseXkxx(Root root,Map<Integer,JcAndTime> jcMap,List<SimplifiedCourse> res){
        if(root.xkxx == null)
            return;
        for(XkxxItem item : root.xkxx){
            for(Course course : item.values()){
                if(course.timeAndPlaceList == null)
                    continue;
                for(TimeAndPlace tp :course.timeAndPlaceList){
                    List<Integer> weeks = parseWeekBitString(tp.classWeek);
                    SimplifiedCourse sc = toSimplified(course,tp,weeks);
                    res.add(sc);
                }
            }
        }
    }
    private void parseDataList(Root root,Map<Integer,JcAndTime> jcMap,List<SimplifiedCourse> res){

    }
    private SimplifiedCourse toSimplified(Course course,TimeAndPlace tp,List<Integer> weeks){
        SimplifiedCourse sc = new SimplifiedCourse();
        sc.courseId = course.id.coureNumber;
        sc.courseSeq = course.id.coureSequenceNumber;
        sc.courseName = course.courseName;
        sc.teacher = course.attendClassTeacher;

        sc.weekday = tp.classDay;
        sc.startJc = tp.classSessions;
        sc.duration = tp.continuingSession;
        sc.location = tp.teachingBuildingName + tp.classroomName;
        sc.weekDescription = tp.weekDescription;

        sc.weeks = weeks;
        return sc;
    }
    private List<Integer> parseWeekBitString(String classweek){
        List<Integer> list = new ArrayList<>();
        if(classweek == null)
            return list;
        for(int i = 0;i<classweek.length();i++){
            if(classweek.charAt(i)=='1'){
                list.add(i);
            }
        }
        return list;
    }
}
