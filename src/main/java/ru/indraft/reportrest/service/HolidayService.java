package ru.indraft.reportrest.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
class HolidayService {

    private static final String CSV_PATH = "classpath:calendar/proizv.csv";

    private static class Column {
        private final static int YEAR = 0;
    }

    @Autowired
    ResourceLoader resourceLoader;

    private List<CSVRecord> rows;

    @PostConstruct
    public void init() throws IOException {
        var resource = resourceLoader.getResource(CSV_PATH);
        var in = resource.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        var parser = CSVFormat.EXCEL.withSkipHeaderRecord().parse(input);
        rows = parser.getRecords();
    }

    boolean checkDateIsHoliday(LocalDate date) {
        List<Integer> holidays = getHolidays(date);
        var day = date.getDayOfMonth();
        return holidays.contains(day);
    }

    private List<Integer> getHolidays(LocalDate date) {
        String holidaysStr = getHolidaysOfMonthAndYear(date);
        String[] holidaysArr = holidaysStr.split(",");
        return Arrays.stream(holidaysArr).filter(item -> !item.endsWith("*")).map(Integer::valueOf).collect(Collectors.toList());
    }

    private String getHolidaysOfMonthAndYear(LocalDate date) {
        var year = date.getYear();
        var month = date.getMonth().getValue();
        for (CSVRecord record : rows) {
            if (String.valueOf(year).equals(record.get(Column.YEAR))) {
                return record.get(month);
            }
        }
        return "0";
    }

}
