package com.Ishwarjit.Wolf_OVRN_backend.util;

import java.util.Locale;

public class SlugUtils {

    public static String generate(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", "-");        // Replace spaces with hyphens
    }
}
