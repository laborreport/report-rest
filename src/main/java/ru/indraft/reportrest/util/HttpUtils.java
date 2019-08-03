package ru.indraft.reportrest.util;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import ru.indraft.reportrest.service.act.ExportReportType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class HttpUtils {

    public static HttpHeaders generateHttpHeaders(String filename, ExportReportType exportReportType) throws UnsupportedEncodingException {
        HttpHeaders httpHeaders = new HttpHeaders();
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(
                        URLEncoder.encode(
                                filename,
                                StandardCharsets.UTF_8.toString()
                        )
                )
                .build();
        httpHeaders.setContentType(FileUtils.getMediaType(exportReportType));
        httpHeaders.setContentDisposition(contentDisposition);
        return httpHeaders;
    }

}
