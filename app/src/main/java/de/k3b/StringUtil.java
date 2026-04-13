package de.k3b;

import androidx.annotation.Nullable;

public class StringUtil {
    @Nullable
    public static String getLast(@Nullable String str, int minLen, int maxLen) {
        String result = str;
        if (str != null) {
            int lastDot = str.lastIndexOf(".");
            if (lastDot >= 0) {
                result = str.substring(lastDot+1);
            }
        }
        return trim(result, minLen, maxLen);
    }

    /**
     * @return str with len <= maxLen or null if str is shorter than minLen;
     */
    public static String trim(String str, int minLen, int maxLen) {
        String result = str;
        if (str != null) {
            if (result.length() <= minLen) {
                result = null;
            } else if (result.length() > maxLen) {
                result = result.substring(0,maxLen-4)+"...";
            }
        }
        return result;
    }

    public static String append(String delimiter, String... params) {
        StringBuilder result = new StringBuilder();
        for(String param : params) {
            if (param != null && !param.isEmpty() ) {
                if (result.length() > 0) result.append(delimiter);
                result.append(param);
            }
        }
        return result.toString();
    }

}
