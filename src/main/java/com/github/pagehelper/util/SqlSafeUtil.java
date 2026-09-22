/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper.util;

import java.util.regex.Pattern;

/**
 * 更严格的SQL注入检测
 */
public class SqlSafeUtil {
    /**
     * SQL语法检查正则：符合两个关键字（有先后顺序）才算匹配
     * <p>
     * 参考: mybatis-plus-core/src/main/java/com/baomidou/mybatisplus/core/toolkit/sql/SqlInjectionUtils.java
     */
    private static final Pattern SQL_SYNTAX_PATTERN  = Pattern.compile("(insert|delete|update|select|create|drop|truncate|grant|alter|deny|revoke|call|execute|exec|declare|show|rename|set)" +
            "\\s+.*(into|from|set|where|table|database|view|index|on|cursor|procedure|trigger|for|password|union|and|or)|(select\\s*\\*\\s*from\\s+)", Pattern.CASE_INSENSITIVE);
    /**
     * 使用'、;或注释截断SQL检查正则
     * <p>
     * 参考: mybatis-plus-core/src/main/java/com/baomidou/mybatisplus/core/toolkit/sql/SqlInjectionUtils.java
     */
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("'.*(or|union|--|#|/*|;)", Pattern.CASE_INSENSITIVE);
    /**
     * 检查参数是否存在 SQL 注入
     *
     * @param value 检查参数
     * @return true 非法 false 合法
     */
    public static boolean check(String value) {
        if (value == null) {
            return false;
        }
        // 不允许使用任何函数（不能出现括号），否则无法检测后面这个注入 order by id,if(1=2,1,(sleep(100)));
        return value.contains("(") || SQL_COMMENT_PATTERN.matcher(value).find() || SQL_SYNTAX_PATTERN.matcher(value).find() || containsCaseWhen(value);
    }

    private static boolean containsCaseWhen(String value) {
        int caseIndex = indexOfWord(value, "case", 0);
        if (caseIndex < 0) {
            return false;
        }
        int whenIndex = indexOfWord(value, "when", caseIndex + 4);
        if (whenIndex < 0) {
            return false;
        }
        int thenIndex = indexOfWord(value, "then", whenIndex + 4);
        if (thenIndex < 0) {
            return false;
        }
        return indexOfWord(value, "end", thenIndex + 4) >= 0;
    }

    private static int indexOfWord(String value, String word, int fromIndex) {
        int maxIndex = value.length() - word.length();
        for (int i = fromIndex; i <= maxIndex; i++) {
            if (value.regionMatches(true, i, word, 0, word.length())
                    && (i == 0 || !isWordChar(value.charAt(i - 1)))
                    && (i + word.length() == value.length() || !isWordChar(value.charAt(i + word.length())))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
