package ru.indraft.reportrest.service.labor;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.service.LocaleService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LaborReportService {

    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy";
    private static final String FORMULA_PATTERN = "SUM({0}:{1})";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private static final int TITLE_ROW_INDEX = 0;
    private static final int START_ROW_INDEX = 1;

    private static final class Height {
        private static final short TITLE_ROW = 700;
        private static final short TASK_ROW = 700;
        private static final short FOOTER_ROW = 600;
    }

    private static final class TitleNameKey {
        private static final String TASK_NAME = "labor.report.title.taskName";
        private static final String TASK_TERM = "labor.report.title.taskTerm";
        private static final String PROJECT_NAME = "labor.report.title.projectName";
        private static final String WORK_TIME = "labor.report.title.workTime";
        private static final String OVER_TIME = "labor.report.title.overTime";

        private static final String TOTAL = "labor.report.footer.total";
    }

    private static final class ColumnNum {
        private static final int TASK_NAME = 0;
        private static final int TASK_TERM = 1;
        private static final int PROJECT_NAME = 2;
        private static final int WORK_TIME = 3;
        private static final int OVER_TIME = 4;
        private static final int TOTAL = 2;
    }

    private static final int COLUMN_WIDTH_FACTOR = 256;

    private static final class ColumnWidth {
        private static final int TASK_NAME = 45;
        private static final int TASK_TERM = 20;
        private static final int PROJECT_NAME = 20;
        private static final int WORK_TIME = 20;
        private static final int OVER_TIME = 20;
    }

    private LocaleService lres = LocaleService.getInstance();
    private CellStyleService cellStyleService;
    private XSSFWorkbook workbook;

    public ByteArrayInputStream generateReport(List<TaskModel> taskModels) throws IOException {
        XSSFWorkbook resultWorkbook = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultWorkbook  = generate(taskModels);
            resultWorkbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } finally {
            if (resultWorkbook != null) {
                resultWorkbook.close();
            }
        }
    }

    private XSSFWorkbook generate(List<TaskModel> taskModels) {
        workbook = new XSSFWorkbook();
        cellStyleService = new CellStyleService(workbook);

        XSSFSheet sheet = workbook.createSheet();
        sheet.createFreezePane(0, 1);
        setColumnWidths(sheet);
        writeTitleRow(sheet);
        writeTaskRows(sheet, taskModels);
        writeFooterRow(sheet, taskModels.size() + START_ROW_INDEX);

        return workbook;
    }

    private String createFormula(XSSFSheet sheet, int lastRowIndex, int column) {
        var messageFormat = new MessageFormat(FORMULA_PATTERN);
        var firstRowAddress = sheet
                .getRow(START_ROW_INDEX)
                .getCell(column)
                .getAddress()
                .formatAsString();
        var lastRowAddress = sheet
                .getRow(lastRowIndex - START_ROW_INDEX)
                .getCell(column)
                .getAddress()
                .formatAsString();
        return messageFormat.format(new String[]{firstRowAddress, lastRowAddress});
    }

    private void writeFooterRow(XSSFSheet sheet, int lastRowIndex) {
        XSSFRow row = sheet.createRow(lastRowIndex);
        row.setHeight(Height.FOOTER_ROW);
        XSSFCell totalCell = row.createCell(ColumnNum.TOTAL, CellType.STRING);
        totalCell.setCellStyle(cellStyleService.getTitleCellStyle());
        totalCell.setCellValue(lres.get(TitleNameKey.TOTAL));

        XSSFCell totalWorkTimeCell = row.createCell(ColumnNum.WORK_TIME, CellType.FORMULA);
        totalWorkTimeCell.setCellStyle(cellStyleService.getTitleCellStyle());
        totalWorkTimeCell.setCellFormula(createFormula(sheet, lastRowIndex, ColumnNum.WORK_TIME));

        XSSFCell totalOverTimeCell = row.createCell(ColumnNum.OVER_TIME, CellType.FORMULA);
        totalOverTimeCell.setCellStyle(cellStyleService.getTitleCellStyle());
        totalOverTimeCell.setCellFormula(createFormula(sheet, lastRowIndex, ColumnNum.OVER_TIME));

        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        formulaEvaluator.evaluateAll();
    }

    private void writeTaskRows(XSSFSheet sheet, List<TaskModel> taskModels) {
        int rowNum = START_ROW_INDEX;
        for (TaskModel taskModel : taskModels) {
            writeTaskRow(sheet, rowNum, taskModel);
            rowNum++;
        }
    }

    private void writeTaskRow(XSSFSheet sheet, int rowNum, TaskModel taskModel) {
        XSSFRow row = sheet.createRow(rowNum);
        row.setHeight(Height.TASK_ROW);

        XSSFCell taskNameCell = row.createCell(ColumnNum.TASK_NAME, CellType.STRING);
        taskNameCell.setCellStyle(cellStyleService.getTaskNameCellStyle());
        taskNameCell.setCellValue(taskModel.getId() + ' ' + taskModel.getName());

        XSSFCell taskTermCell = row.createCell(ColumnNum.TASK_TERM, CellType.STRING);
        taskTermCell.setCellStyle(cellStyleService.getTaskTermCellStyle());
        StringBuilder taskTermStrBuilder = new StringBuilder();
        taskTermStrBuilder.append(formatDate(taskModel.getStartDate()));
        if (!taskModel.getStartDate().isEqual(taskModel.getEndDate())) {
            taskTermStrBuilder.append(" - ");
            taskTermStrBuilder.append(formatDate(taskModel.getEndDate()));
        }
        taskTermCell.setCellValue(taskTermStrBuilder.toString());

        XSSFCell projectNameCell = row.createCell(ColumnNum.PROJECT_NAME, CellType.STRING);
        projectNameCell.setCellStyle(cellStyleService.getProjectNameCellStyle());
        projectNameCell.setCellValue(taskModel.getProjectName());

        XSSFCell workTimeCell = row.createCell(ColumnNum.WORK_TIME, CellType.NUMERIC);
        workTimeCell.setCellStyle(cellStyleService.getWorkTimeCellStyle());
        workTimeCell.setCellValue(taskModel.getWorkTime() != null ? taskModel.getWorkTime() : 0);

        XSSFCell overTimeCell = row.createCell(ColumnNum.OVER_TIME, CellType.NUMERIC);
        overTimeCell.setCellStyle(cellStyleService.getOverTimeCellStyle());
        overTimeCell.setCellValue(taskModel.getOverTime() != null ? taskModel.getOverTime() : 0);
    }

    private void setColumnWidths(XSSFSheet sheet) {
        sheet.setColumnWidth(ColumnNum.TASK_NAME, ColumnWidth.TASK_NAME * COLUMN_WIDTH_FACTOR);
        sheet.setColumnWidth(ColumnNum.TASK_TERM, ColumnWidth.TASK_TERM * COLUMN_WIDTH_FACTOR);
        sheet.setColumnWidth(ColumnNum.PROJECT_NAME, ColumnWidth.PROJECT_NAME * COLUMN_WIDTH_FACTOR);
        sheet.setColumnWidth(ColumnNum.WORK_TIME, ColumnWidth.WORK_TIME * COLUMN_WIDTH_FACTOR);
        sheet.setColumnWidth(ColumnNum.OVER_TIME, ColumnWidth.OVER_TIME * COLUMN_WIDTH_FACTOR);
    }

    private void writeTitleRow(XSSFSheet sheet) {
        XSSFRow row = sheet.createRow(TITLE_ROW_INDEX);
        row.setHeight(Height.TITLE_ROW);
        writeTitleCell(row, ColumnNum.TASK_NAME, TitleNameKey.TASK_NAME);
        writeTitleCell(row, ColumnNum.TASK_TERM, TitleNameKey.TASK_TERM);
        writeTitleCell(row, ColumnNum.PROJECT_NAME, TitleNameKey.PROJECT_NAME);
        writeTitleCell(row, ColumnNum.WORK_TIME, TitleNameKey.WORK_TIME);
        writeTitleCell(row, ColumnNum.OVER_TIME, TitleNameKey.OVER_TIME);
    }

    private void writeTitleCell(XSSFRow row, int columnIndex, String nameKey) {
        XSSFCell cell = row.createCell(columnIndex, CellType.STRING);
        cell.setCellStyle(cellStyleService.getTitleCellStyle());
        cell.setCellValue(lres.get(nameKey));
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_TIME_FORMATTER);
    }

}
