package com.vagrant.timetableproject;

import com.vagrant.timetableproject.model.SimplifiedCourse;
import com.vagrant.timetableproject.service.CourseParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            String json = Files.readString(Paths.get("course.json"));

            CourseParser parser = new CourseParser();
            List<SimplifiedCourse> list = parser.parse(json);
            System.out.println("===Parsed Courses===");
            for(SimplifiedCourse sc : list) {
                printCourse(sc);
            }
            System.out.println("===Total:" +list.size() + "===");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void printCourse(SimplifiedCourse sc) {
        System.out.println("课程名称: " + sc.courseName);
        System.out.println("教师: " + sc.teacher);
        System.out.println("课程号: " + sc.courseId + "-" + sc.courseSeq);
        System.out.println("地点: " + sc.location);

        System.out.println("星期: " + sc.weekday);
        System.out.println("第 " + sc.startJc + " 节开始，持续 " + sc.duration + " 节");

        System.out.println("时间: " + sc.startTime + " - " + sc.endTime);

        System.out.println("周次: " + sc.weeks);
        System.out.println("周描述: " + sc.weekDescription);

        System.out.println("-------------------------");
    }
}
