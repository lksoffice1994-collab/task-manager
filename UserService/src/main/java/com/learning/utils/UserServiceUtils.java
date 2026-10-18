package com.learning.utils;

import java.util.regex.Pattern;

public class UserServiceUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isEmail(String input) {
        if (input == null) return false;
        return EMAIL_PATTERN.matcher(input).matches();
    }
}
