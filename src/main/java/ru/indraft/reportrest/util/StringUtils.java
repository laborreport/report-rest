package ru.indraft.reportrest.util;

import java.util.stream.Stream;

public class StringUtils {

    private final static String SPACE_REGEX = "\\s+";
    private final static String EMPTY_STRING = "";

    public static String getSurname(String fullName) {
        return Stream.of(fullName.split(SPACE_REGEX))
                .findFirst().orElse(EMPTY_STRING);
    }

}