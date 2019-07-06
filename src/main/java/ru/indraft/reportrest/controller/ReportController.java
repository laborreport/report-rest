package ru.indraft.reportrest.controller;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class ReportController {

    @Autowired
    LaborReportService laborReportService;

    @Autowired
    TaskService taskService;

    @RequestMapping("/")
    public String index() {
        return "Server is work!";
    }

    @PostMapping("/labor-report")
    public ResponseEntity createReport(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null) {
            return ResponseEntity.badRequest().body("No file loaded");
        }
        InputStream inputStream = file.getInputStream();
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        List<TaskModel> taskModels = taskService.getTaskModels(workbook);
        XSSFWorkbook resultWorkBook = laborReportService.generate(taskModels);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resultWorkBook.write(out);
        HttpHeaders httpHeaders = new HttpHeaders();
        String filename = "Трудозатраты.xls";
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        httpHeaders.setContentDisposition(contentDisposition);
        var bin = new ByteArrayInputStream(out.toByteArray());
        return ResponseEntity.ok().headers(httpHeaders).body(new InputStreamResource(bin));
    }

}
