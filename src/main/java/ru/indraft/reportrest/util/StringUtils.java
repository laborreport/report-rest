package ru.indraft.reportrest.util;

import com.ibm.icu.text.RuleBasedNumberFormat;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.stream.Stream;

public class StringUtils {

    public final static String SPACE_REGEX = "\\s+";
    private final static String EMPTY_STRING = "";
    private final static DecimalFormat doubleFormat = new DecimalFormat("0.##");

    public static String getSurname(String jiraName) {
        return Stream.of(jiraName.split(SPACE_REGEX))
                .findFirst().orElse(EMPTY_STRING);
    }

    public static String getFullName(String jiraName) {
        String[] jiraNameArr = jiraName.split(SPACE_REGEX);
        return jiraNameArr[0] + " " + jiraNameArr[1] + " " + jiraNameArr[2];
    }

    public static String getShortFullName(String jiraName) {
        String[] jiraNameArr = jiraName.split(SPACE_REGEX);
        return jiraNameArr[0] + " " + jiraNameArr[1].substring(0, 1) + ". " + jiraNameArr[2].substring(0, 1) + ".";
    }

    public static String format(Double value) {
        return doubleFormat.format(value);
    }

    public static String getAmountInWords(Double value) {
        RuleBasedNumberFormat nf = new RuleBasedNumberFormat(Locale.forLanguageTag("ru"),
                RuleBasedNumberFormat.SPELLOUT);
        return nf.format(value);
    }

}