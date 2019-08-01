package ru.indraft.reportrest.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final String DEFAULT_DATE_TIME_PATTERN = "dd.MM.yyyy";
    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);

    public static String getAccountPeriodStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL yyyy");
        return formatter.format(date);
    }

    public static String getContractDateStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return formatter.format(date);
    }

    public static String getContractYearStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return formatter.format(date);
    }

    public static String formatDate(LocalDate date) {
        return date.format(DEFAULT_DATE_TIME_FORMATTER);
    }

}
