package com.vagrant.timetableproject.service;

import com.google.gson.Gson;
import com.vagrant.timetableproject.model.*;

import java.util.*;

public class CourseParser {
    //使用gson库解析json文件
    private Gson gson = new Gson();

    //教务系统中返回节次对应时间信息（JcAndTime）有误，所以手动构建节次时间表
    private static final Map<Integer, String[]> jcMap = new HashMap<>();

    static {
        jcMap.put(1, new String[]{"08:00", "08:50"});
        jcMap.put(2, new String[]{"09:00", "09:50"});
        jcMap.put(3, new String[]{"10:10", "11:00"});
        jcMap.put(4, new String[]{"11:10", "12:00"});
        jcMap.put(5, new String[]{"13:30", "14:20"});
        jcMap.put(6, new String[]{"14:30", "15:20"});
        jcMap.put(7, new String[]{"15:40", "16:30"});
        jcMap.put(8, new String[]{"16:40", "17:30"});
        jcMap.put(9, new String[]{"18:00", "18:50"});
        jcMap.put(10, new String[]{"19:00", "19:50"});
        jcMap.put(11, new String[]{"20:10", "21:00"});
        jcMap.put(12, new String[]{"21:10", "22:00"});
    }

    public List<SimplifiedCourse> parse(String json) {
        //将json文件解析为root
        Root root = gson.fromJson(json, Root.class);
        //从root中选取需要的信息构建list of SimplisfiedCourse并返回
        List<SimplifiedCourse> res = new ArrayList<>();
        parseDateList(root, res);
        return res;
    }

    private void parseDateList(Root root,
                               List<SimplifiedCourse> res) {
        //解析datelist并构建简化课程对象，添加到数组中
        if (root.dateList == null)
            return;
        for (DateListItem item : root.dateList) {
            if (item.selectCourseList == null)
                continue;
            for (SelectCourseItem scItem : item.selectCourseList) {
                if (scItem.timeAndPlaceList == null)
                    continue;
                //对每个课程对象进行周次的解析
                for (TimeAndPlace tp : scItem.timeAndPlaceList) {
                    List<Integer> weeks = parseWeekBitString(tp.classWeek);
                    SimplifiedCourse sc = toSimplified(scItem, tp, weeks);
                    res.add(sc);
                }
            }
        }
    }

    private SimplifiedCourse toSimplified(SelectCourseItem c,
                                          TimeAndPlace tp,
                                          List<Integer> weeks) {
        //简化课程对象，将必要的信息保留，删除重复和冗余信息
        SimplifiedCourse sc = new SimplifiedCourse();

        sc.courseId = c.id.coureNumber;
        sc.courseSeq = c.id.coureSequenceNumber;
        sc.courseName = c.courseName;
        sc.teacher = c.attendClassTeacher;

        sc.weekday = tp.classDay;
        sc.startJc = tp.classSessions;
        sc.duration = tp.continuingSession;
        sc.location = tp.teachingBuildingName + tp.classroomName;
        sc.weekDescription = tp.weekDescription;
        sc.weeks = weeks;
        //ICS时间处理
        int startJc = tp.classSessions;
        int endJc = startJc + tp.continuingSession - 1;

        String[] st = jcMap.get(startJc);
        String[] et = jcMap.get(endJc);

        sc.startTime = st != null ? st[0] : null;
        sc.endTime = et != null ? et[1] : null;

        return sc;
    }


    private List<Integer> parseWeekBitString(String classweek) {
        //将一个01字符串表示的周次信息转换为list
        List<Integer> list = new ArrayList<>();
        if (classweek == null)
            return list;
        for (int i = 0; i < classweek.length(); i++) {
            if (classweek.charAt(i) == '1') {
                list.add(i + 1);
            }
        }
        return list;
    }
}
