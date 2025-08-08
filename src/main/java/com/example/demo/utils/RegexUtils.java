package com.example.demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.demo.utils.RegexPatterns.EMAIL_REGEX;
import static com.example.demo.utils.RegexPatterns.PHONE_REGEX;

public class RegexUtils {

    /**
     * 验证手机号码是否合法
     */
    public static boolean isPhoneLegal(String phone) {
        return mismatch(phone, PHONE_REGEX);
    }

    /**
     * 验证邮箱是否合法
     */
    public static boolean isEmailLegal(String email) {
        return mismatch(email, EMAIL_REGEX);
    }

    /**
     * 正则校验方法
     * @param inputString
     * @param regex
     * @return
     */
    private static boolean mismatch(String inputString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        return matcher.matches();
    }
}
