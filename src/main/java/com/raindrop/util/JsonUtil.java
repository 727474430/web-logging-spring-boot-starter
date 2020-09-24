package com.raindrop.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @name: com.raindrop.util.JsonUtil.java
 * @description: JSON工具类
 * @author: Wang Liang
 * @create Time: 2020/9/24 14:18
 */
public class JsonUtil {

    public static String jsonFormat(String text) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        int tabNum = 0;
        for (int i = 0; i < text.length(); i++) {
            char charValue = text.charAt(i);
            if ('{' == charValue || '[' == charValue) {
                if (i - 1 > 0 && text.charAt(i - 1) == ':') {
                    sb.append("\n");
                    sb.append(geneSpace(tabNum));
                }
                tabNum++;
                sb.append(charValue);
                sb.append("\n");
                sb.append(geneSpace(tabNum));
            } else if ('}' == charValue || ']' == charValue) {
                tabNum--;
                sb.append("\n");
                sb.append(geneSpace(tabNum));
                sb.append(charValue);
            } else if (',' == charValue) {
                sb.append(charValue);
                sb.append("\n");
                sb.append(geneSpace(tabNum));
            } else if ('\\' == charValue) {
            } else {
                sb.append(charValue);
            }
        }
        return sb.toString();
    }

    public static boolean isValidObject(String text) {
        Object object = JSON.parse(text);
        if (!(object instanceof JSONObject) && !(object instanceof JSONArray)) {
            return false;
        }
        return true;
    }

    private static String geneSpace(int tabNum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabNum; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }

}
