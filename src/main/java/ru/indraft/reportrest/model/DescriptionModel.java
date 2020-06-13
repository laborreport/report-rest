package ru.indraft.reportrest.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DescriptionModel {
    private String surname;
    private String fullName;
    private String shortFullName;
    private LocalDate reportDate;
}
