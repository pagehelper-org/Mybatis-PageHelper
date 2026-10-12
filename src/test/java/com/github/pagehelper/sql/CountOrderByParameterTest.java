package com.github.pagehelper.sql;

import com.github.pagehelper.parser.defaults.DefaultSqlServerCountSqlParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CountOrderByParameterTest {

    private final DefaultSqlServerCountSqlParser countSqlParser = new DefaultSqlServerCountSqlParser();

    @Test
    public void unionOrderByParametersKeepTheirOutputScope() {
        String countSql = countSqlParser
                .getSmartCountSql("SELECT a FROM t UNION ALL SELECT a FROM t2 ORDER BY a, ?", "0");
        assertEquals("SELECT count(0) FROM (SELECT TOP 100 PERCENT * FROM (SELECT a FROM t UNION ALL "
                + "SELECT a FROM t2) AS pagehelper_order ORDER BY a, ?) table_count", countSql);
        assertFalse(countSql, countSql.contains("MIN("));
    }

    @Test
    public void plainOrderByParameterKeepsItsOriginalScope() {
        String countSql = countSqlParser.getSmartCountSql("SELECT a FROM t ORDER BY ?", "0");
        assertEquals("SELECT count(0) FROM (SELECT TOP 100 PERCENT a FROM t ORDER BY ?) table_count", countSql);
        assertFalse(countSql, countSql.contains("MIN("));
    }

    @Test
    public void orderByWithoutParameterKeepsSimpleCount() {
        assertEquals("SELECT count(0) FROM t",
                countSqlParser.getSmartCountSql("SELECT a FROM t ORDER BY a", "0"));
    }
}
