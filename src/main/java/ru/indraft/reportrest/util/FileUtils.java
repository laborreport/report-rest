package ru.indraft.reportrest.util;

import org.springframework.http.MediaType;
import ru.indraft.reportrest.model.DescriptionModel;
import ru.indraft.reportrest.service.LocaleService;
import ru.indraft.reportrest.service.act.ExportReportType;

public final class FileUtils {

    private static final String FILE_NAME_DELIMITER = "_";
    private static final String LABOR_FILENAME_START = "labor.report.filename.start";
    private static final String ACT_FILENAME_START = "act.report.filename.start";

    private static LocaleService lres = LocaleService.getInstance();

    private static String getReportFileName(
            DescriptionModel description,
            String startWordFileName,
            ExportReportType exportReportType
    ) {
        String result = startWordFileName;
        result += FILE_NAME_DELIMITER;
        result += description.getSurname();
        result += FILE_NAME_DELIMITER;
        result += DateUtils.getDateStrForFileName(description.getReportDate());
        switch (exportReportType) {
            case PDF:
                result += Extension.PDF;
                break;
            case ODT:
                result += Extension.ODT;
                break;
            case DOCX:
                result += Extension.DOCX;
                break;
            case XLSX:
                result += Extension.XLSX;
        }
        return result;
    }

    public static String getActReportFileName(DescriptionModel description, ExportReportType exportReportType) {
        return getReportFileName(
                description,
                lres.get(ACT_FILENAME_START),
                exportReportType
        );
    }

    public static String getLaborReportFileName(DescriptionModel description) {
        return getReportFileName(
                description,
                lres.get(LABOR_FILENAME_START),
                ExportReportType.XLSX
        );
    }

    public static MediaType getMediaType(ExportReportType exportReportType) {
        switch (exportReportType) {
            case XLSX:
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case PDF:
                return MediaType.APPLICATION_PDF;
            case ODT:
                return MediaType.parseMediaType("application/vnd.oasis.opendocument.text");
            case DOCX:
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return null;
    }

    private static final class Extension {
        private static final String XLSX = ".xlsx";
        private static final String DOCX = ".docx";
        private static final String PDF = ".pdf";
        private static final String ODT = ".odt";
    }

}
