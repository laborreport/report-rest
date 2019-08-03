package ru.indraft.reportrest.controller;

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
import ru.indraft.reportrest.service.LocaleService;
import ru.indraft.reportrest.service.TaskService;
import ru.indraft.reportrest.service.act.ActReportService;
import ru.indraft.reportrest.service.act.ExportReportType;
import ru.indraft.reportrest.service.labor.LaborReportService;
import ru.indraft.reportrest.util.FileUtils;
import ru.indraft.reportrest.util.HttpUtils;

import java.io.IOException;
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

    @PostMapping("/labor-report")
    public ResponseEntity createLaborReport(@RequestParam("file") MultipartFile file) throws IOException {
        List<TaskModel> taskModels = taskService.getTaskModels(file);
        DescriptionModel description = taskService.getDescription(file);
        var report = laborReportService.generateReport(taskModels);
        String filename = FileUtils.getLaborReportFileName(description);
        HttpHeaders httpHeaders = HttpUtils.generateHttpHeaders(filename, ExportReportType.XLSX);
        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(
                        new InputStreamResource(report)
                );
    }

}
