package ru.indraft.reportrest.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskModel {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double workTime;
    private Double overTime;
    private String projectName;
}
