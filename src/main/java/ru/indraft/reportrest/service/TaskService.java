package ru.indraft.reportrest.service;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.indraft.reportrest.model.DescriptionModel;
import ru.indraft.reportrest.model.ReportTaskModel;
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.util.StringUtils;
import ru.indraft.reportrest.util.TaskUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class TaskService {

    private static final int SHEET_INDEX = 0;
    private static final int START_ROW_INDEX = 1;

    private static final class CellNum {
        private static final int TASK_ID = 0;
        private static final int TASK_NAME = 1;
        private static final int TASK_WORK_TIME = 2;
        private static final int TASK_DATE = 3;
        private static final int SURNAME = 5;
        private static final int TASK_PROJECT = 12;
    }

    @Autowired
    private HolidayService holidayService;


    public ArrayList<TaskModel> getTaskModels(MultipartFile file) throws IOException {
        var inputStream = file.getInputStream();
        var workbook = new HSSFWorkbook(inputStream);
        return getTaskModels(workbook);
    }

    public DescriptionModel getDescription(MultipartFile file) throws IOException {
        var inputStream = file.getInputStream();
        var workbook = new HSSFWorkbook(inputStream);
        return getDescription(workbook);
    }

    private ArrayList<TaskModel> getTaskModels(HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.getSheetAt(SHEET_INDEX);
        return getTaskModels(sheet);
    }

    private DescriptionModel getDescription(HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.getSheetAt(SHEET_INDEX);
        var descriptionModel = new DescriptionModel();
        var firstRow = sheet.getRow(START_ROW_INDEX);
        var jiraUserName = firstRow.getCell(CellNum.SURNAME).getStringCellValue();
        descriptionModel.setSurname(StringUtils.getSurname(jiraUserName));
        descriptionModel.setFullName(StringUtils.getFullName(jiraUserName));
        descriptionModel.setShortFullName(StringUtils.getShortFullName(jiraUserName));
        descriptionModel.setGenetiveFullName(StringUtils.getGenetiveFullName(jiraUserName));
        descriptionModel.setReportDate(getLocalDate(firstRow.getCell(CellNum.TASK_DATE).getDateCellValue()));
        return descriptionModel;
    }

    private ArrayList<TaskModel> getTaskModels(HSSFSheet sheet) {
        var endRowIndex = sheet.getLastRowNum();
        HashMap<String, TaskModel> taskModels = new HashMap<>();
        for (var index = START_ROW_INDEX; index <= endRowIndex; index++) {
            HSSFRow row = sheet.getRow(index);
            addTaskModelToMap(row, taskModels);
        }
        return new ArrayList<>(taskModels.values());
    }

    private void addTaskModelToMap(HSSFRow row, HashMap<String, TaskModel> taskModels) {
        String taskId = row.getCell(CellNum.TASK_ID).getStringCellValue();
        LocalDate taskDate = getLocalDate(row.getCell(CellNum.TASK_DATE).getDateCellValue());
        Double taskWorkTime = row.getCell(CellNum.TASK_WORK_TIME).getNumericCellValue();

        TaskModel taskModel = taskModels.get(taskId);
        if (taskModel == null) {
            taskModel = new TaskModel();
            taskModel.setId(taskId);
            taskModel.setName(row.getCell(CellNum.TASK_NAME).getStringCellValue());
            taskModel.setProjectName(row.getCell(CellNum.TASK_PROJECT).getStringCellValue());
            taskModel.setStartDate(taskDate);
            taskModel.setEndDate(taskDate);
        } else {
            if (taskModel.getStartDate().isAfter(taskDate)) {
                taskModel.setStartDate(taskDate);
            }
            if (taskModel.getEndDate().isBefore(taskDate)) {
                taskModel.setEndDate(taskDate);
            }
        }
        setWorkTime(taskDate, taskWorkTime, taskModel);

        taskModels.put(taskId, taskModel);
    }

    public static double roundDouble(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    private void setWorkTime(LocalDate taskDate, Double taskWorkTime, TaskModel taskModel) {
        if (!checkDayIsHoliday(taskDate)) {
            var currentWorkTime = taskModel.getWorkTime() == null ? 0 : taskModel.getWorkTime();
            taskModel.setWorkTime(currentWorkTime + taskWorkTime);
        } else {
            var currentWorkTime = taskModel.getOverTime() == null ? 0 : taskModel.getOverTime();
            taskModel.setOverTime(currentWorkTime + taskWorkTime);
        }
    }

    private boolean checkDayIsHoliday(LocalDate taskDate) {
        return holidayService.checkDateIsHoliday(taskDate);
    }

    private LocalDate getLocalDate(Date dateCellValue) {
        return LocalDate.ofInstant(dateCellValue.toInstant(), ZoneId.systemDefault());
    }

    public List<ReportTaskModel> convert(List<TaskModel> taskModels, Double rate) {
        List<ReportTaskModel> reportTasks = new ArrayList<>();
        for (int i = 0; i < taskModels.size(); i ++) {
            reportTasks.add(convert(taskModels.get(i), rate, i));
        }
        return reportTasks;
    }

    private static final double URGENCY_RATIO = 1.5;

    private Double getTaskCost(TaskModel taskModel, Double rate) {
        double sum = 0;
        if (taskModel.getWorkTime() != null) {
            sum += (taskModel.getWorkTime() * rate);
        }
        if (taskModel.getOverTime() != null) {
            sum += (taskModel.getOverTime() * URGENCY_RATIO * rate);
        }
        return sum;
    }

    private ReportTaskModel convert(TaskModel taskModel, Double rate, int i) {
        ReportTaskModel reportTask = new ReportTaskModel();
        reportTask.setTaskNumber(i);
        reportTask.setTaskName(taskModel.getId() + " " + taskModel.getName());
        reportTask.setTermTask(TaskUtils.getTerm(taskModel));
        reportTask.setTaskWorkTime(StringUtils.format(taskModel.getWorkTime() != null ? taskModel.getWorkTime() : 0));
        reportTask.setTaskOverTime(StringUtils.format(taskModel.getOverTime() != null ? taskModel.getOverTime() : 0));
        reportTask.setTaskRate(StringUtils.format(rate));
        reportTask.setTaskCost(StringUtils.format(getTaskCost(taskModel, rate)));
        return reportTask;
    }

}
