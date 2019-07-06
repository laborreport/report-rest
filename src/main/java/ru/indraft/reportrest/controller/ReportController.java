package ru.indraft.reportrest.controller;

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
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.service.TaskService;
import ru.indraft.reportrest.service.labor.LaborReportService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class ReportController {

    @Autowired
    private LaborReportService laborReportService;

    @Autowired
    private TaskService taskService;

    @RequestMapping("/")
    public String index() {
        return "Server is work!";
    }

    @PostMapping("/labor-report")
    public ResponseEntity createReport(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null) {
            return ResponseEntity.badRequest().body("No file loaded");
        }
        List<TaskModel> taskModels = taskService.getTaskModels(file);
        var report = laborReportService.generateReport(taskModels);
        HttpHeaders httpHeaders = new HttpHeaders();
        String filename = "Трудозатраты.xls";
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        httpHeaders.setContentDisposition(contentDisposition);
        return ResponseEntity.ok().headers(httpHeaders).body(new InputStreamResource(report));
    }

}
