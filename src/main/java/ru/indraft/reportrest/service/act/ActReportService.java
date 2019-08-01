package ru.indraft.reportrest.service.act;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.indraft.reportrest.model.DescriptionModel;
import ru.indraft.reportrest.model.ReportTaskModel;
import ru.indraft.reportrest.model.TaskModel;
import ru.indraft.reportrest.model.UserModel;
import ru.indraft.reportrest.service.TaskService;
import ru.indraft.reportrest.util.DateUtils;
import ru.indraft.reportrest.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ActReportService {

    private static final String JASPER_TEMPLATE_PATH = "/template/akt.jrxml";

    @Autowired
    private TaskService taskService;

    private JasperReport getJasperReport() throws IOException, JRException {
        Resource resource = new ClassPathResource(JASPER_TEMPLATE_PATH);
        InputStream jasperStream = resource.getInputStream();
        JasperDesign jasperDesign = JRXmlLoader.load(jasperStream);
        return JasperCompileManager.compileReport(jasperDesign);
    }

    private Map<String, Object> generateParams(UserModel userModel, DescriptionModel descriptionModel, String actNumber, Double totalCost) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(JRParameter.REPORT_LOCALE, new Locale("ru", "RU"));

        parameters.put(ReportParams.ACT_NUMBER, actNumber);
        parameters.put(ReportParams.TOTAL_COST, StringUtils.format(totalCost));
        parameters.put(ReportParams.TOTAL_COST_IN_WORDS, StringUtils.getAmountInWords(totalCost));

        parameters.put(ReportParams.ACCOUNT_PERIOD, DateUtils.getAccountPeriodStr(descriptionModel.getReportDate()));
        parameters.put(ReportParams.GENETIVE_FULL_NAME, descriptionModel.getGenetiveFullName());
        parameters.put(ReportParams.SHORT_FULL_NAME, descriptionModel.getShortFullName());

        parameters.put(ReportParams.CONTRACT_NUMBER, userModel.getContractNumber());
        parameters.put(ReportParams.CONTRACT_DATE, DateUtils.getContractDateStr(userModel.getContractDate()));
        parameters.put(ReportParams.CONTRACT_YEAR, DateUtils.getContractYearStr(userModel.getContractDate()));
        parameters.put(ReportParams.PE_SERIES, userModel.getPeSeries());
        parameters.put(ReportParams.PE_NUMBER, userModel.getPeNumber());

        return parameters;
    }

    private static final double URGENCY_RATIO = 1.5;

    private Double getReportTotalCost(List<TaskModel> taskModels, Double rate) {
        double sum = 0;
        for(TaskModel taskModel : taskModels) {
            if (taskModel.getWorkTime() != null) {
                sum += (taskModel.getWorkTime() * rate);
            }
            if (taskModel.getOverTime() != null) {
                sum += (taskModel.getOverTime() * URGENCY_RATIO * rate);
            }
        }
        return sum;
    }

    public ByteArrayInputStream generateReport(List<TaskModel> taskModels, UserModel userModel, DescriptionModel descriptionModel, String actNumber) throws IOException, JRException {
        JasperReport jasperReport = getJasperReport();

        double totalCost = getReportTotalCost(taskModels, userModel.getRate());
        Map<String, Object> parameters = generateParams(userModel, descriptionModel, actNumber, totalCost);

        List<ReportTaskModel> reportTaskModels = taskService.convert(taskModels, userModel.getRate());

        JRBeanCollectionDataSource itemsJRBean = new JRBeanCollectionDataSource(reportTaskModels, true);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, itemsJRBean);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        // line bellow is only for demo purpose.
//        JasperExportManager.exportReportToPdfStream(jasperPrint,
//                new FileOutputStream("src/main/resources/report.pdf")
//        );
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
