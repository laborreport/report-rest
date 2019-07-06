package ru.indraft.reportrest.service.labor;

import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FontService {

    private static final class Font {
        private static final String NAME = "Arial";
        private static final short TITLE_HEIGHT = 10;
        private static final short DEFAULT_HEIGHT = 9;
        private static final short FOOTER_HEIGHT = 10;
    }

    private XSSFWorkbook workbook;

    public FontService(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    private XSSFFont titleFont;
    private XSSFFont defaultFont;
    private XSSFFont footerFont;

    private XSSFFont createFont(short height, boolean bold) {
        var font = workbook.createFont();
        font.setFontName(Font.NAME);
        font.setFontHeightInPoints(height);
        font.setBold(bold);
        return font;
    }

    public XSSFFont getTitleFont() {
        if (titleFont == null) {
            titleFont = createFont(Font.TITLE_HEIGHT, true);
        }
        return titleFont;
    }

    public XSSFFont getDefaultFont() {
        if (defaultFont == null) {
            defaultFont = createFont(Font.DEFAULT_HEIGHT, false);
        }
        return defaultFont;
    }

    public XSSFFont getFooterFont() {
        if (footerFont != null) {
            footerFont = createFont(Font.FOOTER_HEIGHT, true);
        }
        return footerFont;
    }

}
