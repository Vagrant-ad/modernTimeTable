package com.vagrant.timetableproject.service;

import com.vagrant.timetableproject.model.SimplifiedCourse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class IcsExporter {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter DTSTAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    static class RRuleInfo {
        //使用RRule规则合并连续周数或单双周的课程
        int interval;
        int count;
    }

    private static RRuleInfo analyzeWeeks(List<Integer> weeks) {
        //判断是否为连续或者单双周形式,若是则生成RRule
        if (weeks == null || weeks.size() == 1)
            return null;
        int interval = weeks.get(1) - weeks.get(0);
        if (interval != 1 && interval != 2)
            return null;
        for (int i = 2; i < weeks.size(); i++) {
            if (weeks.get(i) - weeks.get(i - 1) != interval)
                return null;
        }
        RRuleInfo ruleInfo = new RRuleInfo();
        ruleInfo.interval = interval;
        ruleInfo.count = weeks.size();
        return ruleInfo;
    }

    public static void toICS(List<SimplifiedCourse> courses,
                             LocalDate termStart,
                             String filePath) throws IOException {
        //主要函数，根据课程的list生成ics格式文件
        BufferedWriter fw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)
        );

        fw.write("BEGIN:VCALENDAR\r\n");
        fw.write("VERSION:2.0\r\n");
        fw.write("PRODID:-//Timetable//CN\r\n");
        fw.write("CALSCALE:GREGORIAN\r\n"); //公历规则
        fw.write("METHOD:PUBLISH\r\n");

        //生成统一的 DTSTAMP
        String dtStamp = DTSTAMP_FMT.format(LocalDateTime.now());

        for (SimplifiedCourse c : courses) {

            RRuleInfo rule = analyzeWeeks(c.weeks);

            LocalDate firstDate = termStart.plusWeeks(
                    c.weeks.get(0) - 1).plusDays(c.weekday - 1);

            String dateStr = DATE_FMT.format(firstDate);
            String dtStart = dateStr + "T" + c.startTime.replace(":", "") + "00";
            String dtEnd   = dateStr + "T" + c.endTime.replace(":", "") + "00";

            String uid = UUID.randomUUID().toString() + "@timetable";

            if (rule != null) {
                //进行RRule合并
                fw.write("BEGIN:VEVENT\r\n");
                fw.write("UID:" + uid + "\r\n");
                fw.write("DTSTAMP:" + dtStamp + "\r\n");

                fw.write("SUMMARY:" + c.courseName + "\r\n");
                fw.write("LOCATION:" + c.location + "\r\n");
                fw.write("DESCRIPTION:教师:" + c.teacher + "\r\n");
                fw.write("DTSTART;TZID=Asia/Shanghai:" + dtStart + "\r\n");
                fw.write("DTEND;TZID=Asia/Shanghai:" + dtEnd + "\r\n");

                if (rule.interval == 1)
                    fw.write("RRULE:FREQ=WEEKLY;COUNT=" + rule.count + "\r\n");
                else
                    fw.write("RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=" + rule.count + "\r\n");

                fw.write("END:VEVENT\r\n");
                continue;//跳到下一节课
            }

            //无法合并则展开为多个事件
            for (Integer w : c.weeks) {
                LocalDate date = termStart.plusWeeks(w - 1).plusDays(c.weekday - 1);

                String d = DATE_FMT.format(date);
                String s = d + "T" + c.startTime.replace(":", "") + "00";
                String e = d + "T" + c.endTime.replace(":", "") + "00";

                String uidSingle = UUID.randomUUID().toString() + "@timetable";

                fw.write("BEGIN:VEVENT\r\n");
                fw.write("UID:" + uidSingle + "\r\n");
                fw.write("DTSTAMP:" + dtStamp + "\r\n");
                fw.write("SUMMARY:" + c.courseName + "\r\n");
                fw.write("LOCATION:" + c.location + "\r\n");
                fw.write("DESCRIPTION:教师:" + c.teacher + " 周次:" + w + "\r\n");
                fw.write("DTSTART;TZID=Asia/Shanghai:" + s + "\r\n");
                fw.write("DTEND;TZID=Asia/Shanghai:" + e + "\r\n");
                fw.write("END:VEVENT\r\n");
            }
        }
        fw.write("END:VCALENDAR\r\n");
        fw.close();
    }
}
