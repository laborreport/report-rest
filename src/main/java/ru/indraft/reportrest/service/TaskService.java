package ru.indraft.reportrest.service;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.indraft.reportrest.model.TaskModel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class TaskService {

    private static final int SHEET_INDEX = 0;
    private static final int START_ROW_INDEX = 1;

    private static final class CellNum {
        private static final int TASK_ID = 0;
        private static final int TASK_NAME = 1;
        private static final int TASK_WORK_TIME = 2;
        private static final int TASK_DATE = 3;
        private static final int TASK_PROJECT = 12;
    }

    @Autowired
    private HolidayService holidayService;


    public ArrayList<TaskModel> getTaskModels(MultipartFile file) throws IOException {
        var inputStream = file.getInputStream();
        var workbook = new HSSFWorkbook(inputStream);
        return getTaskModels(workbook);
    }

    private ArrayList<TaskModel> getTaskModels(HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.getSheetAt(SHEET_INDEX);
        return getTaskModels(sheet);
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

}
