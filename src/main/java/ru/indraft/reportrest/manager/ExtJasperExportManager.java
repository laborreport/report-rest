package ru.indraft.reportrest.manager;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOdtReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
public class ExtJasperExportManager {

    public void exportToDocxStream(JasperPrint jasperPrint, OutputStream outputStream) throws JRException {
        JRDocxExporter exporter = new JRDocxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        SimpleDocxReportConfiguration config = new SimpleDocxReportConfiguration();
        config.setFlexibleRowHeight(true);
        exporter.setConfiguration(config);
        exporter.exportReport();
    }

    public void exportToOdtStream(JasperPrint jasperPrint, OutputStream outputStream) throws JRException {
        JROdtExporter exporter = new JROdtExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        SimpleOdtReportConfiguration config = new SimpleOdtReportConfiguration();
        config.setFlexibleRowHeight(true);
        exporter.setConfiguration(config);
        exporter.exportReport();
    }

}
