package com.vagrant.timetableproject.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vagrant.timetableproject.model.SimplifiedCourse;
import com.vagrant.timetableproject.service.CourseParser;
import com.vagrant.timetableproject.service.IcsExporter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MainController {

    @FXML
    private Label labelDate;

    @FXML
    private Label labelWeekStatus;

    @FXML
    private Label labelDayOfWeek;

    @FXML
    private Label labelMonth;

    @FXML
    private GridPane gridCourse;

    //学期起始日期
    private LocalDate termStartDate = LocalDate.of(2025, 8, 25);
    //当前显示的周次
    private int currentDisplayWeek = 1;
    //日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("M月");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d");
    private static final String[] WEEKDAY_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    // 课程数据列表
    private List<SimplifiedCourse> courseList = new ArrayList<>();

    // 课程解析器
    private CourseParser courseParser = new CourseParser();

    private Map<String, String> coursecolormap = new HashMap<>();

    private static final String[] COLOR_CLASSES = {
            //20种渐变色
            "color-blue", "color-pink", "color-green", "color-purple", "color-orange",
            "color-red", "color-cyan", "color-emerald", "color-indigo", "color-magenta",
            "color-ocean", "color-yellow", "color-teal", "color-coral", "color-slate",
            "color-gold", "color-gray", "color-olive", "color-brick", "color-violet"
    };

    @FXML
    public void initialize() {
        //初始化时计算当前周次并更新标签日期显示，再读取课程数据
        calculateCurrentWeek();
        updateAllDateLabels();
        loadCourseData();
    }

    private void calculateCurrentWeek() {
        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(termStartDate, today);
        if (daysBetween < 0) {
            //还未开学显示第一周
            currentDisplayWeek = 1;
        } else {
            //计算当前周次
            currentDisplayWeek = (int) (daysBetween / 7) + 1;
        }
    }

    private LocalDate getDisplayWeekMonday() {
        //返回当前显示周的周一日期
        return termStartDate.plusWeeks(currentDisplayWeek - 1);
    }

    private LocalDate getDisplayWeekDay(int dayOfWeek) {
        //返回显示周的某一天的日期
        LocalDate monday = getDisplayWeekMonday();
        return monday.plusDays(dayOfWeek - 1);
    }


    private void updateAllDateLabels() {
        //更新顶部日期信息
        updateHeaderDateLabels();
        //更新表格表头的日期
        updateGridHeaderDates();
        //更新月份显示
        updateMonthLabel();
    }

    private void updateHeaderDateLabels() {
        LocalDate today = LocalDate.now();
        //格式化为xx月xx日
        labelDate.setText(today.format(DATE_FORMATTER));
        labelWeekStatus.setText("第" + currentDisplayWeek + "周");
        int dayOfWeek = today.getDayOfWeek().getValue();//1-7
        labelDayOfWeek.setText(WEEKDAY_NAMES[dayOfWeek - 1]);
    }

    private void updateGridHeaderDates() {
        //遍历更新表头周一到周日的日期显示
        for (int c = 1; c <= 7; c++) {
            LocalDate date = getDisplayWeekDay(c);
            //找到对应列的VBox（rowIndex=0是表头行）
            VBox headerBox = findGridHeaderBox(c);
            if (headerBox != null) {
                //VBox里有两个Label：第一个是"周X"，第二个是日期
                if (headerBox.getChildren().size() >= 2) {
                    Label dateLabel = (Label) headerBox.getChildren().get(1);
                    dateLabel.setText(date.format(SHORT_DATE_FORMATTER));
                }
                //高亮今天的日期
                highlightCurDay(headerBox, date);
            }
        }
    }

    private void updateMonthLabel() {
        LocalDate monday = getDisplayWeekMonday();
        labelMonth.setText(monday.format(MONTH_FORMATTER));
    }

    //返回表头某一列的VBox
    private VBox findGridHeaderBox(int columnIndex) {
        return (VBox) gridCourse.getChildren().stream()
                .filter(node -> {
                    Integer c = GridPane.getColumnIndex(node);
                    Integer r = GridPane.getRowIndex(node);
                    return (c != null && c == columnIndex) &&
                            (r != null && r == 0) &&
                            node instanceof VBox;
                }).findFirst().orElse(null);
    }

    private void highlightCurDay(VBox headerBox, LocalDate date) {
        LocalDate today = LocalDate.now();
        //移除所有高亮
        headerBox.getStyleClass().removeAll("current-day-highlight");
        //给今天添加高亮
        if (date.equals(today)) {
            if (!headerBox.getStyleClass().contains("current-day-highlight")) {
                headerBox.getStyleClass().add("current-day-highlight");
            }
        }
    }

    @FXML
    public void onSetStartDate() {
        TextInputDialog dialog = new TextInputDialog(termStartDate.toString());
        dialog.setTitle("设置学期起始日期");
        dialog.setHeaderText("请输入学期第一周周一的日期");
        dialog.setContentText("格式（yyyy-MM-dd）：");

        Optional<String> res = dialog.showAndWait();
        res.ifPresent(dateStr -> {
            try {
                termStartDate = LocalDate.parse(dateStr);
                //重新计算当前周次
                calculateCurrentWeek();
                //刷新所有日期显示
                updateAllDateLabels();
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("设置成功");
                success.setHeaderText(null);
                success.setContentText("学期起始日期已设置为：" + dateStr);
                success.show();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("格式错误");
                error.setHeaderText(null);
                error.setContentText("日期格式不正确，请使用 yyyy-MM-dd 格式\n例如：2025-08-25");
                error.show();
            }
        });
    }

    @FXML
    public void onPrevWeek() {
        if (currentDisplayWeek > 1) {
            currentDisplayWeek--;
            updateAllDateLabels();
            refreshCourseDisplay();
        }
    }

    @FXML
    public void onNextWeek() {
        // 假设最多30周（可根据实际调整）
        if (currentDisplayWeek < 30) {
            currentDisplayWeek++;
            updateAllDateLabels();
            refreshCourseDisplay();
        }
    }


    @FXML
    public void onImportJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择JSON文件导入");
        //筛选.json文件
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON文件", "*.json")
        );
        //设置初始目录为项目根目录
        File initialDir = new File(System.getProperty("user.dir"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        //获取当前窗口的Stage
        Stage stage = (Stage) gridCourse.getScene().getWindow();
        //显示文件选择对话框
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                //读取和解析json文件
                String json = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
                List<SimplifiedCourse> parsedCourses = courseParser.parse(json);
                //解析结果为空警告用户
                if (parsedCourses == null || parsedCourses.isEmpty()) {
                    Alert warning = new Alert(Alert.AlertType.WARNING);
                    warning.setTitle("导入失败");
                    warning.setHeaderText("未找到课程数据");
                    warning.setContentText("JSON文件中没有有效的课程信息。");
                    warning.show();
                    return;
                }
                courseList = parsedCourses;
                saveCourseData();
                refreshCourseDisplay();

                // 显示成功提示
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("导入成功");
                success.setHeaderText(null);
                success.setContentText("成功从" + selectedFile.getName() + "导入" + courseList.size() + " 门课程\n");
                success.show();
            } catch (IOException e) {
                //文件读取错误
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("文件读取失败");
                error.setHeaderText("无法读取JSON文件");
                error.setContentText("错误信息：" + e.getMessage());
                error.show();
            } catch (Exception e) {
                //JSON解析错误
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("解析失败");
                error.setHeaderText("JSON格式错误");
                error.setContentText("无法解析课程数据，请检查JSON文件格式。\n" + "错误信息：" + e.getMessage());
                error.show();
            }
        }
    }

    @FXML
    public void onExportICS() {
        if (courseList == null || courseList.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("导出ics文件失败");
            warning.setHeaderText("课程数据为空");
            warning.setContentText("请先导入或添加课程！");
            warning.show();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存ics日历文件");
        fileChooser.setInitialFileName("course.ics");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("ICS文件", "*.ics")
        );
        //设置初始目录为项目根目录
        File initialDir = new File(System.getProperty("user.dir"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        Stage stage = (Stage) gridCourse.getScene().getWindow();
        File save = fileChooser.showSaveDialog(stage);
        if (save != null) {
            try {
                String filePath = save.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".ics")) {
                    filePath += ".ics";
                }
                IcsExporter.toICS(courseList, termStartDate, filePath);
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("导出ics文件成功");
                success.setHeaderText(null);
                success.setContentText("课程表已经成功导出到" + filePath);
                success.show();
            } catch (IOException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("导出失败");
                error.setHeaderText("无法写入ics文件");
                error.setContentText("错误信息：" + e.getMessage());
                error.show();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("导出失败");
                error.setHeaderText("其他错误");
                error.setContentText("错误信息：" + e.getMessage());
                error.show();
            }
        }
    }

    @FXML
    public void onAddCourse() {
        SimplifiedCourse newCourse = new SimplifiedCourse();
        newCourse.courseName = "";
        newCourse.teacher = "";
        newCourse.location = "";
        newCourse.weekday = 1;
        newCourse.startJc = 1;
        newCourse.duration = 2;
        newCourse.startTime = "08:00";
        newCourse.endTime = "09:50";
        newCourse.weeks = new ArrayList<>();
        newCourse.weeks.add(currentDisplayWeek);
        newCourse.weekDescription = "第" + currentDisplayWeek + "周";
        showAddCourseDialog(newCourse);
    }

    private void showAddCourseDialog(SimplifiedCourse course) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("添加课程");
        dialog.setHeaderText("请填写新课程信息");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfCourseName = new TextField(course.courseName);
        TextField tfTeacher = new TextField(course.teacher);
        TextField tfLocation = new TextField(course.location);
        TextField tfWeekday = new TextField(String.valueOf(course.weekday));
        TextField tfStartJc = new TextField(String.valueOf(course.startJc));
        TextField tfDuration = new TextField(String.valueOf(course.duration));
        TextField tfStartTime = new TextField(String.valueOf(course.startTime));
        TextField tfEndTime = new TextField(String.valueOf(course.endTime));
        TextField tfWeeks = new TextField(course.weeks != null ? course.weeks.toString() : "[]");
        TextField tfWeekDesc = new TextField(course.weekDescription);

        tfCourseName.setPromptText("例如: 数据结构");
        tfTeacher.setPromptText("例如: 张三");
        tfLocation.setPromptText("例如：东配楼101");
        tfWeekday.setPromptText("1-7表示周一到周日");
        tfStartJc.setPromptText("1-12");
        tfDuration.setPromptText("一般为2");
        tfStartTime.setPromptText("HH:mm如08:00");
        tfEndTime.setPromptText("HH:mm格式如09:50");
        tfWeeks.setPromptText("[1,2,3,4] 或 [1-16]");
        tfWeekDesc.setPromptText("例如: 1-16单周");

        grid.add(new Label("*课程名称:"), 0, 0);
        grid.add(tfCourseName, 1, 0);
        grid.add(new Label("授课教师:"), 0, 1);
        grid.add(tfTeacher, 1, 1);
        grid.add(new Label("上课地点:"), 0, 2);
        grid.add(tfLocation, 1, 2);
        grid.add(new Label("*星期几 (1-7):"), 0, 3);
        grid.add(tfWeekday, 1, 3);
        grid.add(new Label("*起始节次 (1-12):"), 0, 4);
        grid.add(tfStartJc, 1, 4);
        grid.add(new Label("*持续节数:"), 0, 5);
        grid.add(tfDuration, 1, 5);
        grid.add(new Label("*开始时间:"), 0, 6);
        grid.add(tfStartTime, 1, 6);
        grid.add(new Label("*结束时间:"), 0, 7);
        grid.add(tfEndTime, 1, 7);
        grid.add(new Label("*周次列表:"), 0, 8);
        grid.add(tfWeeks, 1, 8);
        grid.add(new Label("周次描述:"), 0, 9);
        grid.add(tfWeekDesc, 1, 9);

        Label noteLabel = new Label("带*为必填项");
        noteLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        grid.add(noteLabel, 0, 10, 2, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType addButton = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, cancelButton);
        //按下添加尝试保存数据
        dialog.showAndWait().ifPresent(response -> {
            if (response == addButton) {
                try {
                    //检查课程名称
                    if (tfCourseName.getText().trim().isEmpty())
                        throw new IllegalArgumentException("课程名称不能为空");
                    course.courseName = tfCourseName.getText().trim();
                    course.teacher = tfTeacher.getText().trim();
                    course.location = tfLocation.getText().trim();
                    course.weekday = Integer.parseInt(tfWeekday.getText().trim());
                    course.startJc = Integer.parseInt(tfStartJc.getText().trim());
                    course.duration = Integer.parseInt(tfDuration.getText().trim());
                    course.startTime = tfStartTime.getText().trim();
                    course.endTime = tfEndTime.getText().trim();
                    course.weekDescription = tfWeekDesc.getText().trim();
                    //检查数据合法
                    if (course.weekday < 1 || course.weekday > 7)
                        throw new IllegalArgumentException("星期必须在0到7之间");
                    if (course.startJc < 1 || course.startJc > 12)
                        throw new IllegalArgumentException("节次必须在1-12之间");
                    if (course.duration < 1 || course.duration > 12)
                        throw new IllegalArgumentException("持续节次必须在1-12之间");
                    String weeksStr = tfWeeks.getText().trim();
                    course.weeks = parseWeeksInput(weeksStr);
                    if (course.weeks.isEmpty())
                        throw new IllegalArgumentException("周次列表不能为空");
                    //通过检查后添加新课程到列表
                    courseList.add(course);
                    saveCourseData();
                    refreshCourseDisplay();
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("添加成功");
                    success.setHeaderText(null);
                    success.setContentText(course.courseName + "已成功添加");
                    success.show();
                } catch (NumberFormatException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("格式错误");
                    error.setHeaderText("数字格式不正确");
                    error.setContentText(e.getMessage());
                    error.show();
                } catch (IllegalArgumentException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("输入错误");
                    error.setHeaderText("数据非法");
                    error.setContentText(e.getMessage());
                    error.show();
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("添加失败");
                    error.setHeaderText("发生未知错误");
                    error.setContentText(e.getMessage());
                    error.show();
                }
            }
        });
    }

    private List<Integer> parseWeeksInput(String input) throws IllegalArgumentException {
        //解析两种形式的周次范围
        List<Integer> weeks = new ArrayList<>();
        if (input == null || input.isEmpty())
            return weeks;
        input = input.replaceAll("[\\[\\]\\s]", "");
        if (input.contains("-")) {
            //1-16模式
            String[] parts = input.split("-");
            if (parts.length == 2) {
                try {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    if (start > end)
                        throw new IllegalArgumentException("起始周次不能大于结束周次");
                    if (start < 1 || end > 20)
                        throw new IllegalArgumentException("周次必须在1-20之间");
                    for (int i = start; i <= end; i++)
                        weeks.add(i);
                    return weeks;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("周次范围格式错误");
                }
            }
        }
        //1,2,3模式
        String[] parts = input.split(",");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                try {
                    int week = Integer.parseInt(part);
                    if (week < 1 || week > 20)
                        throw new IllegalArgumentException("周次必须在1-20之间");
                    weeks.add(week);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("周次范围格式错误");
                }
            }
        }
        return weeks;
    }

    private void clearCourseCards() {
        gridCourse.getChildren().removeIf(node -> {
            return node.getUserData() instanceof SimplifiedCourse;
        });
    }

    private void refreshCourseDisplay() {
        //刷新页面
        clearCourseCards();
        for (SimplifiedCourse course : courseList) {
            if (course.weeks != null && course.weeks.contains(currentDisplayWeek)) {
                VBox courseCard = createCourseCard(course);
                int c = course.weekday;
                int r = course.startJc;
                gridCourse.add(courseCard, c, r);
                GridPane.setRowSpan(courseCard, course.duration);
            }
        }
    }

    private VBox createCourseCard(SimplifiedCourse course) {
        //使用VBox作为课程卡片的容器
        VBox card = new VBox();
        card.getStyleClass().add("course-card");
        //分配颜色
        String colorClass = getCourseColor(course.courseName);
        card.getStyleClass().add(colorClass);
        card.setUserData(course);
        //设置四个标签，课程名称、地点、授课教师、课程备注
        Label nameLabel = new Label(course.courseName != null ? course.courseName : "未命名");
        nameLabel.getStyleClass().add("course-name");
        nameLabel.setWrapText(true);

        Label locationLabel = new Label(course.location != null ? course.location : "");
        locationLabel.getStyleClass().add("course-location");
        locationLabel.setWrapText(true);

        Label teacherLabel = new Label(course.teacher != null ? course.teacher : "");
        teacherLabel.getStyleClass().add("course-teacher");
        teacherLabel.setWrapText(true);

        Label weekLabel = new Label(course.weekDescription != null ? course.weekDescription : "");
        weekLabel.getStyleClass().add("course-week");
        weekLabel.setWrapText(true);

        card.getChildren().addAll(nameLabel, locationLabel, teacherLabel, weekLabel);
        card.setOnMouseClicked(event -> showCourseEditDialog(course));
        return card;
    }

    private String getCourseColor(String courseName) {
        if (courseName == null)
            courseName = "default";
        if (coursecolormap.containsKey(courseName))
            return coursecolormap.get(courseName);
        int hash = Math.abs(courseName.hashCode());
        String colorClass = COLOR_CLASSES[hash % COLOR_CLASSES.length];
        coursecolormap.put(courseName, colorClass);
        return colorClass;
    }

    private void showCourseEditDialog(SimplifiedCourse course) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("编辑课程");
        dialog.setHeaderText("课程信息");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        //所有课程信息
        TextField tfCourseName = new TextField(course.courseName);
        TextField tfTeacher = new TextField(course.teacher);
        TextField tfLocation = new TextField(course.location);
        TextField tfWeekday = new TextField(String.valueOf(course.weekday));
        TextField tfStartJc = new TextField(String.valueOf(course.startJc));
        TextField tfDuration = new TextField(String.valueOf(course.duration));
        TextField tfStartTime = new TextField(String.valueOf(course.startTime));
        TextField tfEndTime = new TextField(String.valueOf(course.endTime));
        TextField tfWeeks = new TextField(course.weeks != null ? course.weeks.toString() : "[]");
        TextField tfWeekDesc = new TextField(course.weekDescription);

        //设置网格
        grid.add(new Label("课程名称:"), 0, 0);
        grid.add(tfCourseName, 1, 0);
        grid.add(new Label("授课教师:"), 0, 1);
        grid.add(tfTeacher, 1, 1);
        grid.add(new Label("上课地点:"), 0, 2);
        grid.add(tfLocation, 1, 2);
        grid.add(new Label("星期几 (1-7):"), 0, 3);
        grid.add(tfWeekday, 1, 3);
        grid.add(new Label("起始节次 (1-12):"), 0, 4);
        grid.add(tfStartJc, 1, 4);
        grid.add(new Label("持续节数:"), 0, 5);
        grid.add(tfDuration, 1, 5);
        grid.add(new Label("开始时间:"), 0, 6);
        grid.add(tfStartTime, 1, 6);
        grid.add(new Label("结束时间:"), 0, 7);
        grid.add(tfEndTime, 1, 7);
        grid.add(new Label("周次列表:"), 0, 8);
        grid.add(tfWeeks, 1, 8);
        grid.add(new Label("周次描述:"), 0, 9);
        grid.add(tfWeekDesc, 1, 9);

        dialog.getDialogPane().setContent(grid);
        //设置按钮
        ButtonType saveButton = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButton = new ButtonType("删除", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, deleteButton,cancelButton);
        //按下保存按钮时尝试更新信息
        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                try {
                    course.courseName = tfCourseName.getText();
                    course.teacher = tfTeacher.getText();
                    course.location = tfLocation.getText();
                    course.weekday = Integer.parseInt(tfWeekday.getText());
                    course.startJc = Integer.parseInt(tfStartJc.getText());
                    course.duration = Integer.parseInt(tfDuration.getText());
                    course.startTime = tfStartTime.getText();
                    course.endTime = tfEndTime.getText();
                    course.weekDescription = tfWeekDesc.getText();
                    //解析输入的字符串，删除[]和空格内容
                    String weeksStr = tfWeeks.getText().replaceAll("[\\[\\]\\s]", "");
                    if (!weeksStr.isEmpty()) {
                        course.weeks = new ArrayList<>();
                        for (String week : weeksStr.split(",")) {
                            course.weeks.add(Integer.parseInt(week.trim()));
                        }
                    }
                    //保存刷新
                    saveCourseData();
                    refreshCourseDisplay();
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("修改失败");
                    error.setHeaderText("输入格式错误");
                    error.setContentText(e.getMessage());
                    error.show();
                }
            } else if (response == deleteButton) {
                // 删除课程 - 显示确认对话框
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("确认删除");
                confirmDialog.setHeaderText("删除课程");
                confirmDialog.setContentText("确定要删除课程" + course.courseName + "吗？\n");

                Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    // 从列表中删除课程
                    courseList.remove(course);
                    //保存
                    saveCourseData();
                    // 刷新显示
                    refreshCourseDisplay();
                    // 显示成功提示
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("删除成功");
                    success.setHeaderText(null);
                    success.setContentText("课程 \"" + course.courseName + "\" 已删除");
                    success.show();
                }
            }
        });
    }
    private void saveCourseData() {
        try {
            //保存到用户目录下的.timetable下
            String userHome = System.getProperty("user.home");
            File saveDir = new File(userHome, ".timetable");
            if (!saveDir.exists())
                saveDir.mkdirs();
            File saveFile = new File(saveDir, "courses.json");
            //使用Gson序列化
            Gson gson = new Gson();
            String json = gson.toJson(courseList);
            Files.writeString(saveFile.toPath(), json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("保存课程数据失败: " + e.getMessage());
        }
    }
    private void loadCourseData() {
        try {
            String userHome = System.getProperty("user.home");
            File saveFile = new File(userHome, ".timetable/courses.json");
            if (saveFile.exists()) {
                String json = Files.readString(saveFile.toPath(), StandardCharsets.UTF_8);
                Gson gson = new Gson();
                //使用 TypeToken反序列化courseList
                Type listType = new TypeToken<ArrayList<SimplifiedCourse>>(){}.getType();
                List<SimplifiedCourse> loadedCourses = gson.fromJson(json, listType);
                if (loadedCourses != null && !loadedCourses.isEmpty()) {
                    courseList = loadedCourses;
                    refreshCourseDisplay();
                    System.out.println("成功加载 " + courseList.size() + " 门课程");
                }
            }
        } catch (Exception e) {
            System.err.println("加载课程数据失败: " + e.getMessage());
        }
    }
}