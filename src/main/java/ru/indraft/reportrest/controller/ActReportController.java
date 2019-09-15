package ru.indraft.reportrest.controller;

import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.indraft.reportrest.model.DescriptionModel;
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.model.UserModel;
import ru.indraft.reportrest.service.TaskService;
import ru.indraft.reportrest.service.act.ActReportService;
import ru.indraft.reportrest.service.act.ExportReportType;
import ru.indraft.reportrest.util.FileUtils;
import ru.indraft.reportrest.util.HttpUtils;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/act-report")
public class ActReportController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ActReportService reportService;

    private ResponseEntity createActReport(
            MultipartFile file,
            UserModel user,
            String actNumber,
            ExportReportType exportReportType
    ) throws IOException, JRException {
        List<TaskModel> taskModels = taskService.getTaskModels(file);
        DescriptionModel description = taskService.getDescription(file);
        var report = reportService.generateReport(taskModels, user, description, actNumber, exportReportType);
        String filename = FileUtils.getActReportFileName(description, exportReportType);
        HttpHeaders httpHeaders = HttpUtils.generateHttpHeaders(filename, exportReportType);
        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(
                        new InputStreamResource(report)
                );
    }

    @PostMapping("/pdf")
    public ResponseEntity createPdfActReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user") UserModel user,
            @RequestParam("act_number") String actNumber
    ) throws IOException, JRException {
        return createActReport(file, user, actNumber, ExportReportType.PDF);
    }

    @PostMapping("/docx")
    public ResponseEntity createDocxActReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user") UserModel user,
            @RequestParam("act_number") String actNumber
    ) throws IOException, JRException {
        return createActReport(file, user, actNumber, ExportReportType.DOCX);
    }

    @PostMapping("/odt")
    public ResponseEntity createOdtActReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user") UserModel user,
            @RequestParam("act_number") String actNumber
    ) throws IOException, JRException {
        return createActReport(file, user, actNumber, ExportReportType.ODT);
    }

}
