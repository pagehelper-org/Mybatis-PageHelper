package com.github.pagehelper.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ServiceLoader;

public class SqlParserUtil {

    private static final SqlParser SQL_PARSER;

    static {
        SqlParser temp = null;
        ServiceLoader<SqlParser> loader = ServiceLoader.load(SqlParser.class);
        for (SqlParser sqlParser : loader) {
            temp = sqlParser;
            break;
        }
        if (temp == null) {
            temp = SqlParser.DEFAULT;
        }
        SQL_PARSER = temp;
    }

    public static Statement parse(String statementReader) {
        try {
            return SQL_PARSER.parse(statementReader);
        } catch (JSQLParserException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拆出 WITH 前缀，同时保留原 SQL 文本。未包含 WITH 或无法解析的语句原样返回。
     */
    public static String[] splitWithPrefix(String sql) {
        int start = skipTrivia(sql, 0);
        if (wordEnd(sql, start) - start == 4 && sql.regionMatches(true, start, "WITH", 0, 4)) {
            try {
                Statement statement = parse(sql);
                if (statement instanceof Select) {
                    Select select = (Select) statement;
                    if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
                        int end = findWithEnd(sql, start + 4, select.getWithItemsList().size());
                        if (end >= 0) {
                            return new String[] {sql.substring(0, end), sql.substring(end)};
                        }
                    }
                }
            } catch (RuntimeException e) {
                // 保留无法解析的原语句
            }
        }
        return new String[] {null, sql};
    }

    private static int findWithEnd(String sql, int offset, int itemCount) {
        int depth = 0;
        boolean afterAs = false;
        while (offset < sql.length()) {
            int next = skipTrivia(sql, offset);
            if (next != offset) {
                offset = next;
                continue;
            }
            char ch = sql.charAt(offset);
            if (ch == '\'' || ch == '"' || ch == '`' || ch == '[') {
                offset = skipQuoted(sql, offset, ch == '[' ? ']' : ch);
            } else if ((ch == 'q' || ch == 'Q') && offset + 2 < sql.length()
                    && sql.charAt(offset + 1) == '\'') {
                char open = sql.charAt(offset + 2);
                char close = open == '[' ? ']' : open == '(' ? ')' : open == '{' ? '}' : open == '<' ? '>' : open;
                int end = sql.indexOf("" + close + '\'', offset + 3);
                offset = end < 0 ? sql.length() : end + 2;
            } else if (isWord(ch)) {
                int end = wordEnd(sql, offset);
                if (depth == 0 && end - offset == 2 && sql.regionMatches(true, offset, "AS", 0, 2)) {
                    afterAs = true;
                }
                offset = end;
            } else {
                if (ch == '(') {
                    depth++;
                } else if (ch == ')') {
                    if (--depth < 0) {
                        return -1;
                    }
                    if (depth == 0 && afterAs) {
                        if (--itemCount == 0) {
                            return offset + 1;
                        }
                        afterAs = false;
                    }
                }
                offset++;
            }
        }
        return -1;
    }

    private static int skipTrivia(String sql, int offset) {
        while (offset < sql.length()) {
            if (Character.isWhitespace(sql.charAt(offset))) {
                offset++;
            } else if (sql.startsWith("/*", offset)) {
                int end = sql.indexOf("*/", offset + 2);
                offset = end < 0 ? sql.length() : end + 2;
            } else if (sql.startsWith("--", offset)) {
                offset += 2;
                while (offset < sql.length() && sql.charAt(offset) != '\r' && sql.charAt(offset) != '\n') {
                    offset++;
                }
            } else {
                break;
            }
        }
        return offset;
    }

    private static int skipQuoted(String sql, int offset, char quote) {
        offset++;
        while (offset < sql.length()) {
            if (sql.charAt(offset++) == quote) {
                if (offset < sql.length() && sql.charAt(offset) == quote) {
                    offset++;
                } else {
                    return offset;
                }
            }
        }
        return offset;
    }

    private static int wordEnd(String sql, int offset) {
        while (offset < sql.length() && isWord(sql.charAt(offset))) {
            offset++;
        }
        return offset;
    }

    private static boolean isWord(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$' || ch == '#';
    }
}
