package ru.indraft.reportrest.controller;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.service.TaskService;
import ru.indraft.reportrest.service.labor.LaborReportService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    public void createReport(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        if (file == null) {
            return;
        }
        InputStream inputStream = file.getInputStream();
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        List<TaskModel> taskModels = taskService.getTaskModels(workbook);
        XSSFWorkbook resultWorkBook = laborReportService.generate(taskModels);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=Трудозатраты.xlsx");
        resultWorkBook.write(response.getOutputStream());
    }

}
