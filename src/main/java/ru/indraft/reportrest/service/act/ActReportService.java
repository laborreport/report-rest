package ru.indraft.reportrest.service.act;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.indraft.reportrest.manager.ExtJasperExportManager;
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
import java.util.regex.Pattern;

@Service
public class ActReportService {

    private static final String JASPER_TEMPLATE_PATH = "/template/akt.jrxml";
    private static final double URGENCY_RATIO = 1.5;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ExtJasperExportManager extJasperExportManager;

    private JasperReport getJasperReport() throws IOException, JRException {
        Resource resource = new ClassPathResource(JASPER_TEMPLATE_PATH);
        InputStream jasperStream = resource.getInputStream();
        JasperDesign jasperDesign = JRXmlLoader.load(jasperStream);
        return JasperCompileManager.compileReport(jasperDesign);
    }

    private boolean isNewPeVersion(String peNumber) {
        String newVersionPeNumberPattern = "\\d{15}";
        return Pattern.matches(newVersionPeNumberPattern, peNumber);
    }

    private ImmutablePair<String, String> getPeSeriesAndNumber(String peNumber) {
        String[] arr = peNumber.split(StringUtils.SPACE_REGEX);
        return new ImmutablePair<>(arr[0], arr[1]);
    }

    private void setPeParams(Map<String, Object> parameters, UserModel userModel) {
        var isNewPeVersion = isNewPeVersion(userModel.getPeNumber());
        parameters.put(ReportParams.IS_NEW_PE_VERSION, isNewPeVersion);
        parameters.put(ReportParams.PE_DATE, DateUtils.getContractDateStr(userModel.getPeDate()));
        if (isNewPeVersion) {
            parameters.put(ReportParams.PE_SERIES, null);
            parameters.put(ReportParams.PE_NUMBER, userModel.getPeNumber());
        } else {
            ImmutablePair<String, String> pair = getPeSeriesAndNumber(userModel.getPeNumber());
            parameters.put(ReportParams.PE_SERIES, pair.getLeft());
            parameters.put(ReportParams.PE_NUMBER, pair.getRight());
        }
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

        setPeParams(parameters, userModel);

        return parameters;
    }

    private Double getReportTotalCost(List<TaskModel> taskModels, Double rate) {
        double totalWorkTime = 0;
        double totalOverTime = 0;
        for (TaskModel taskModel : taskModels) {
            if (taskModel.getWorkTime() != null) {
                totalWorkTime += taskModel.getWorkTime();
            }
            if (taskModel.getOverTime() != null) {
                totalOverTime += taskModel.getOverTime();
            }
        }
        return (Math.round(totalWorkTime) * rate) + (Math.round(totalOverTime) * URGENCY_RATIO * rate);
    }

    public ByteArrayInputStream generateReport(
            List<TaskModel> taskModels,
            UserModel userModel,
            DescriptionModel descriptionModel,
            String actNumber,
            ExportReportType exportReportType
    ) throws IOException, JRException {
        JasperReport jasperReport = getJasperReport();

        double totalCost = getReportTotalCost(taskModels, userModel.getRate());
        Map<String, Object> parameters = generateParams(userModel, descriptionModel, actNumber, totalCost);

        List<ReportTaskModel> reportTaskModels = taskService.convert(taskModels, userModel.getRate());

        JRBeanCollectionDataSource itemsJRBean = new JRBeanCollectionDataSource(reportTaskModels, true);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, itemsJRBean);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        switch (exportReportType) {
            case PDF:
                JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
                break;
            case DOCX:
                extJasperExportManager.exportToDocxStream(jasperPrint, outputStream);
                break;
            case ODT:
                extJasperExportManager.exportToOdtStream(jasperPrint, outputStream);
                break;
        }

//      line bellow is only for demo purpose.
//      JasperExportManager.exportReportToPdfStream(jasperPrint,
//           new FileOutputStream("src/main/resources/report.pdf")
//      );
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}
