package ru.indraft.reportrest.util;

import ru.indraft.reportrest.model.TaskModel;

import static ru.indraft.reportrest.util.DateUtils.formatDate;

public class TaskUtils {

    public static String getTerm(TaskModel taskModel) {
        StringBuilder taskTermStrBuilder = new StringBuilder();
        taskTermStrBuilder.append(formatDate(taskModel.getStartDate()));
        if (!taskModel.getStartDate().isEqual(taskModel.getEndDate())) {
            taskTermStrBuilder.append(" - ");
            taskTermStrBuilder.append(formatDate(taskModel.getEndDate()));
        }
        return taskTermStrBuilder.toString();
    }

}
