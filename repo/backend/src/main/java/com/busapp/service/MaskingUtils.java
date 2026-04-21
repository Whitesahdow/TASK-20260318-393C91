package com.busapp.service;

import com.busapp.model.SensitivityLevel;

public final class MaskingUtils {
    private MaskingUtils() {}

    public static String mask(String rawContent, SensitivityLevel level) {
        if (rawContent == null) {
            return "";
        }
        if (level == null || level == SensitivityLevel.LEVEL1) {
            return rawContent;
        }
        if (level == SensitivityLevel.LEVEL2) {
            return rawContent.replaceAll("([A-Za-z])[A-Za-z]+", "$1**");
        }
        return rawContent
                .replaceAll("\\d{2,}", "****")
                .replaceAll("([A-Za-z])[A-Za-z]+", "$1**");
    }
}
