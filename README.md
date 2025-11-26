# Modern Timetable

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.6-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

一个基于 JavaFX 的跨平台课程表管理系统，支持 JSON 数据导入、可视化课程展示、课程编辑管理和 ICS 日历文件导出。

##  特性

-  **JSON 数据导入** - 支持从教务系统导出的 JSON 文件导入课程信息
-  **可视化周视图** - 直观的课程表展示，渐变色卡片区分不同课程
-  **课程管理** - 添加、编辑、删除课程，支持完整的课程信息维护
-  **ICS 格式导出** - 导出标准 ICS 日历文件，可导入至各类日历应用
-  **智能周次切换** - 自动计算当前周次，支持切换查看整学期课程
-  **数据持久化** - 自动保存课程数据，程序重启后自动加载
-  **现代化 UI** - 简洁优雅的界面设计，流畅的交互体验



##  使用指南

### 1. 设置学期起始日期
- 点击顶部工具栏的 **"设置开学"** 按钮
- 输入学期第一周周一的日期（格式：yyyy-MM-dd，如：2024-09-02）
- 系统会自动计算当前周次

### 2. 导入课程数据
- 点击顶部工具栏的 **"导入"** 按钮
- 选择从教务系统导出的 JSON 文件
- 系统自动解析并显示课程表

### 3. 查看和切换周次
- 课程表默认显示当前周次的课程
- 使用底部的 **"上一周"** 和 **"下一周"** 按钮切换周次
- 当天日期列会自动高亮显示

### 4. 管理课程
- **添加课程**：点击右上角 **"+"** 按钮，填写课程信息
- **编辑课程**：点击任意课程卡片，在弹出对话框中修改信息
- **删除课程**：在课程编辑对话框中点击 **"删除"** 按钮

### 5. 导出日历文件
- 点击顶部工具栏的 **"导出"** 按钮
- 选择保存位置，系统生成 ICS 格式日历文件
- 将生成的文件导入至 Google Calendar、Apple Calendar 或 Outlook

##  项目结构

```
timetableProject/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.vagrant.timetableproject/
│   │   │       ├── App.java                    # 主程序入口
│   │   │       ├── controller/
│   │   │       │   └── MainController.java     # 主控制器
│   │   │       ├── model/                      # 数据模型
│   │   │       │   ├── SimplifiedCourse.java   # 简化课程类
│   │   │       │   └── ...                     # 其他模型类
│   │   │       └── service/                    # 服务层
│   │   │           ├── CourseParser.java       # JSON 解析
│   │   │           └── IcsExporter.java        # ICS 导出
│   │   └── resources/
│   │       └── com.vagrant.timetableproject/
│   │           ├── view.fxml                   # 界面布局
│   │           └── style.css                   # 样式表
│   └── module-info.java                        # 模块配置
├── pom.xml                                     # Maven 配置
└── README.md                                   # 项目说明
```

##  技术栈

- **编程语言**: Java 17
- **UI 框架**: JavaFX 17.0.6
- **构建工具**: Maven
- **JSON 解析**: Gson 2.10.1
- **设计模式**: MVC

##  JSON 数据格式

系统支持标准教务系统导出的 JSON 格式，主要字段包括：

```json
{
  "dateList": [
    {
      "selectCourseList": [
        {
          "courseName": "课程名称",
          "attendClassTeacher": "授课教师",
          "timeAndPlaceList": [
            {
              "classDay": 1,
              "classSessions": 1,
              "continuingSession": 2,
              "classWeek": "1111111111111111",
              "teachingBuildingName": "教学楼",
              "classroomName": "101",
              "weekDescription": "1-16周"
            }
          ]
        }
      ]
    }
  ]
}
```

##  数据存储

课程数据自动保存至本地：
- **Windows**: `C:\Users\用户名\.timetable\`
- **macOS/Linux**: `/Users/用户名/.timetable/`

存储文件：
- `courses.json` - 课程数据
- `config.txt` - 学期起始日期配置

##  界面特色

- **渐变色卡片** - 15 种精美渐变配色，自动为不同课程分配颜色
- **响应式布局** - 自适应窗口大小，最小支持 900×700 分辨率
- **流畅动画** - 卡片悬停效果，视觉反馈流畅自然
- **现代化设计** - 简洁清爽的界面风格，符合现代审美


##  已知问题

- [ ] 暂不支持课程冲突检测
- [ ] 单双周课程需手动输入周次列表
- [ ] 界面语言暂仅支持中文



