package com.vagrant.timetableproject;

import com.vagrant.timetableproject.model.SimplifiedCourse;
import com.vagrant.timetableproject.service.CourseParser;
import com.vagrant.timetableproject.service.IcsExporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class ICSTest {
    public static void main(String[] args) {
        try {
            // 1) 读取课程 JSON（请将路径替换为你的实际路径）
            String jsonPath = "E:\\CodeFile_01\\CognizeExperiment\\timetableProject\\timetableProject\\course.json";
            String json = new String(Files.readAllBytes(Paths.get(jsonPath)));

            // 2) 解析 JSON → SimplifiedCourse 列表
            CourseParser parser = new CourseParser();
            List<SimplifiedCourse> courses = parser.parse(json);

            System.out.println("解析完成，课程数 = " + courses.size());

            // 3) 设置开学日期（按实际填写）
            LocalDate termStart = LocalDate.of(2025, 8, 25);

            // 4) 导出 ICS
            String output = "E:/CodeFile_01/CognizeExperiment/timetableProject/timetableProject/test.ics";
            IcsExporter.toICS(courses, termStart, output);

            System.out.println("ICS 文件已生成： " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
