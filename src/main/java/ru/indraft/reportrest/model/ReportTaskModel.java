package ru.indraft.reportrest.model;

import lombok.Data;

@Data
public class ReportTaskModel {

    /**
     * Порядковый номер
     */
    private Integer taskNumber;

    /**
     * Наименование услуги
     */
    private String taskName;

    /**
     * Срок оказания услуги
     */
    private String termTask;

    /**
     * Длительность работ (часов)
     */
    private String taskWorkTime;

    /**
     * Длительность работ (часов) с коэффициентом срочности
     */
    private String taskOverTime;

    /**
     * Ставка (руб.)
     */
    private String taskRate;

    /**
     * Стоимость (руб.)
     */
    private String taskCost;

}
