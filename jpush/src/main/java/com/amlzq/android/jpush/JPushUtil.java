package com.amlzq.android.jpush;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 极光推送工具
 * from com.example.jpushdemo.ExampleUtil.java
 */
public class JPushUtil {

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String original) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_!@#$&*+=.|]+$");
        Matcher m = p.matcher(original);
        return m.matches();
    }

}
