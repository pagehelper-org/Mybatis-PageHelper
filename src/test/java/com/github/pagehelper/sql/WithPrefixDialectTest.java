package com.github.pagehelper.sql;

import com.github.pagehelper.Page;
import com.github.pagehelper.parser.SqlParserUtil;
import com.github.pagehelper.dialect.helper.Db2Dialect;
import com.github.pagehelper.dialect.helper.OracleDialect;
import com.github.pagehelper.dialect.rowbounds.Db2RowBoundsDialect;
import com.github.pagehelper.dialect.rowbounds.OracleRowBoundsDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WithPrefixDialectTest {

    private static final String WITH_SQL = "WITH cte AS (SELECT id FROM users) SELECT id FROM cte";

    @Test
    public void db2PageSqlHoistsWithPrefix() {
        String pageSql = new Db2Dialect().getPageSql(WITH_SQL, new Page(1, 10), new CacheKey());
        assertTrue(pageSql, pageSql.startsWith("WITH cte AS"));
        assertTrue(pageSql, pageSql.contains("ROWNUMBER()"));
        assertFalse(pageSql, pageSql.contains("FROM ( \nWITH"));
    }

    @Test
    public void db2RowBoundsPageSqlHoistsWithPrefix() {
        String pageSql = new Db2RowBoundsDialect().getPageSql(WITH_SQL, new RowBounds(0, 10), new CacheKey());
        assertTrue(pageSql, pageSql.startsWith("WITH cte AS"));
        assertFalse(pageSql, pageSql.contains("FROM ( \nWITH"));
    }

    @Test
    public void oraclePageSqlHoistsWithPrefix() {
        String pageSql = new OracleDialect().getPageSql(WITH_SQL, new Page(1, 10), new CacheKey());
        assertTrue(pageSql, pageSql.startsWith("WITH cte AS"));
        assertFalse(pageSql, pageSql.contains("FROM ( \nWITH"));
    }

    @Test
    public void oracleRowBoundsPageSqlHoistsWithPrefix() {
        String pageSql = new OracleRowBoundsDialect().getPageSql(WITH_SQL, new RowBounds(10, 10), new CacheKey());
        assertTrue(pageSql, pageSql.startsWith("WITH cte AS"));
        assertFalse(pageSql, pageSql.contains("FROM ( \nWITH"));
    }

    @Test
    public void pageSqlWithoutWithPrefixUnchanged() {
        assertEquals(
                "SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS PAGEHELPER_ROW_ID FROM ( \n"
                        + "SELECT id FROM users\n ) AS TMP_PAGE) TMP_PAGE WHERE PAGEHELPER_ROW_ID BETWEEN ? AND ?",
                new Db2Dialect().getPageSql("SELECT id FROM users", new Page(1, 10), new CacheKey()));
    }

    @Test
    public void leadingCommentsAndWhitespaceArePreserved() {
        String[] comments = {"/* trace */ ", "/* trace */\n", "/* one */ /* two */\n",
                "-- trace\n", "-- trace\r\n", "-- trace\r", "/* one */ -- two\n  "};
        for (String comment : comments) {
            assertAllDialects(comment + "WITH cte AS (SELECT id FROM users)", " SELECT id FROM cte");
        }
    }

    @Test(timeout = 1000)
    public void manyLeadingCommentsWithoutWithDoNotBlock() {
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            sql.append("/* trace */");
        }
        sql.append("SELECT id FROM users");
        assertEquals("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS PAGEHELPER_ROW_ID FROM ( \n"
                        + sql + "\n ) AS TMP_PAGE) TMP_PAGE WHERE PAGEHELPER_ROW_ID BETWEEN ? AND ?",
                new Db2Dialect().getPageSql(sql.toString(), new Page(1, 10), new CacheKey()));
    }

    @Test
    public void originalExpressionsHintsAndParametersArePreserved() {
        assertAllDialects("WITH cte AS (SELECT id FROM users WHERE id = ?)",
                "\nSELECT /*+ FULL(cte) */ COUNT(UNIQUE id) OVER () FROM cte WHERE id = ?");
    }

    @Test
    public void multipleCtesQuotesAndCommentsDoNotChangeTheBoundary() {
        assertAllDialects("WITH \"cte) AS\" (id) AS (SELECT ')' FROM users /* ) AS */), "
                        + "b AS (SELECT id FROM \"cte) AS\" WHERE id = ?)",
                "\nSELECT id FROM b UNION ALL SELECT id FROM b");
    }

    @Test
    public void reportedRecursiveCteIsHoisted() {
        assertAllDialects("WITH cte (id, parent_id, level) AS (SELECT id, parent_id, 0 FROM users "
                        + "WHERE parent_id IS NULL UNION ALL SELECT t.id, t.parent_id, cte.level + 1 "
                        + "FROM users t, cte WHERE cte.id = t.parent_id AND cte.LEVEL<10)",
                "\nSELECT id, parent_id, level FROM cte");
    }

    @Test
    public void invalidAndNonSelectStatementsAreUnchanged() {
        for (String sql : new String[] {"WITH broken AS (", "WITH cte AS (SELECT id FROM users) SELECT FROM ???",
                "WITH cte AS (SELECT id FROM users) INSERT INTO other_users SELECT id FROM cte"}) {
            String[] split = SqlParserUtil.splitWithPrefix(sql);
            assertEquals(null, split[0]);
            assertEquals(sql, split[1]);
        }
    }

    private void assertAllDialects(String prefix, String body) {
        String sql = prefix + body;
        String[] split = SqlParserUtil.splitWithPrefix(sql);
        assertEquals(prefix, split[0]);
        assertEquals(body, split[1]);
        assertEquals(prefix + " SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS PAGEHELPER_ROW_ID FROM ( \n"
                        + body + "\n ) AS TMP_PAGE) TMP_PAGE WHERE PAGEHELPER_ROW_ID BETWEEN ? AND ?",
                new Db2Dialect().getPageSql(sql, new Page(2, 10), new CacheKey()));
        assertEquals(prefix + " SELECT * FROM (  SELECT TMP_PAGE.*, ROWNUM PAGEHELPER_ROW_ID FROM ( \n"
                        + body + "\n ) TMP_PAGE) WHERE PAGEHELPER_ROW_ID <= ? AND PAGEHELPER_ROW_ID > ?",
                new OracleDialect().getPageSql(sql, new Page(2, 10), new CacheKey()));
        assertEquals(prefix + " SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS PAGEHELPER_ROW_ID FROM ( \n"
                        + body + "\n ) AS TMP_PAGE) TMP_PAGE WHERE PAGEHELPER_ROW_ID BETWEEN 11 AND 20",
                new Db2RowBoundsDialect().getPageSql(sql, new RowBounds(10, 10), new CacheKey()));
        assertEquals(prefix + " SELECT * FROM (  SELECT TMP_PAGE.*, ROWNUM PAGEHELPER_ROW_ID FROM ( \n"
                        + body + "\n ) TMP_PAGE WHERE ROWNUM <= 20 ) WHERE PAGEHELPER_ROW_ID > 10",
                new OracleRowBoundsDialect().getPageSql(sql, new RowBounds(10, 10), new CacheKey()));
        assertEquals(prefix + "  SELECT TMP_PAGE.*, ROWNUM PAGEHELPER_ROW_ID FROM ( \n"
                        + body + "\n ) TMP_PAGE WHERE ROWNUM <= 10",
                new OracleRowBoundsDialect().getPageSql(sql, new RowBounds(0, 10), new CacheKey()));
        assertEquals(prefix + " \n" + body + "\n",
                new OracleRowBoundsDialect().getPageSql(sql, new RowBounds(0, 0), new CacheKey()));
    }
}
