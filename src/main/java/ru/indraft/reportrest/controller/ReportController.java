package ru.indraft.reportrest.controller;

import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.indraft.reportrest.model.DescriptionModel;
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.model.UserModel;
import ru.indraft.reportrest.service.LocaleService;
import ru.indraft.reportrest.service.TaskService;
import ru.indraft.reportrest.service.act.ActReportService;
import ru.indraft.reportrest.service.labor.LaborReportService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class ReportController {

    @Autowired
    private LaborReportService laborReportService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ActReportService actReportService;

    private LocaleService lres = LocaleService.getInstance();

    @RequestMapping("/")
    public String index() {
        return "Server is work!";
    }

    @PostMapping("/user")
    public String testUser(@RequestParam("user") UserModel userModel) {
        return userModel.toString();
    }

    private static final String FILE_NAME_DELIMITER = "_";
    private static final String LABOR_FILENAME_START = "labor.report.filename.start";
    private static final String ACT_FILENAME_START = "act.report.filename.start";

    // TODO: Вынести отсюда эти два метода и убрать дублирование кода
    private String getActFileName(MultipartFile file) throws IOException {
        DescriptionModel description = taskService.getDescription(file);
        String result = lres.get(ACT_FILENAME_START);
        result += FILE_NAME_DELIMITER;
        result += description.getSurname();
        result += FILE_NAME_DELIMITER;
        LocalDate reportDate = description.getReportDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_yyyy");
        result += reportDate.format(formatter);
        result += ".pdf";
        return result;
    }

    private String getLaborFileName(MultipartFile file) throws IOException {
        DescriptionModel description = taskService.getDescription(file);
        String result = lres.get(LABOR_FILENAME_START);
        result += FILE_NAME_DELIMITER;
        result += description.getSurname();
        result += FILE_NAME_DELIMITER;
        LocalDate reportDate = description.getReportDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM_yyyy");
        result += reportDate.format(formatter);
        result += ".xlsx";
        return result;
    }

    @PostMapping("/labor-report")
    public ResponseEntity createLaborReport(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null) {
            return ResponseEntity.badRequest().body("No file loaded");
        }
        List<TaskModel> taskModels = taskService.getTaskModels(file);
        var report = laborReportService.generateReport(taskModels);
        HttpHeaders httpHeaders = new HttpHeaders();
        String filename = getLaborFileName(file);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())).build();
        httpHeaders.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        httpHeaders.setContentDisposition(contentDisposition);
        return ResponseEntity.ok().headers(httpHeaders).body(new InputStreamResource(report));
    }

    // TODO: убрать дублирование создания Headers

    @PostMapping("/act-report/pdf")
    public ResponseEntity createActReport(@RequestParam("file") MultipartFile file, @RequestParam("user") UserModel user, @RequestParam("act_number") String actNumber) throws IOException, JRException {
        if (file == null) {
            return ResponseEntity.badRequest().body("No file loaded");
        }
        List<TaskModel> taskModels = taskService.getTaskModels(file);
        DescriptionModel description = taskService.getDescription(file);

        var report = actReportService.generateReport(taskModels, user, description, actNumber);
        HttpHeaders httpHeaders = new HttpHeaders();
        String filename = getActFileName(file);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())).build();
        httpHeaders.setContentType(
                MediaType.APPLICATION_PDF);
        httpHeaders.setContentDisposition(contentDisposition);
        return ResponseEntity.ok().headers(httpHeaders).body(new InputStreamResource(report));
    }

}
