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

package com.github.pagehelper.sql;

import com.github.pagehelper.parser.OrderBySqlParser;
import com.github.pagehelper.parser.defaults.DefaultOrderBySqlParser;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DefaultOrderBySqlParserTest {
    private OrderBySqlParser orderBySqlParser = new DefaultOrderBySqlParser();

    @Test
    public void testOrderBy() {
        String sql = orderBySqlParser.converToOrderBySql("select * from user where length(name) > 0 order by id desc", "name desc");
        Assert.assertEquals("SELECT * FROM user WHERE length(name) > 0 ORDER BY name DESC", sql);
    }

    @Test
    public void testOrderByBeforeLimit() {
        String sql = orderBySqlParser.converToOrderBySql(
                "select * from user order by id desc limit 10 offset 5", "name desc");
        Assert.assertEquals("SELECT * FROM user ORDER BY name DESC LIMIT 10 OFFSET 5", sql);
    }

    @Test
    public void testOrderByBeforeFetch() {
        String sql = orderBySqlParser.converToOrderBySql(
                "select * from user order by id offset 5 rows fetch next 10 rows only", "name desc");
        Assert.assertEquals("SELECT * FROM user ORDER BY name DESC OFFSET 5 ROWS FETCH NEXT 10 ROWS ONLY", sql);
    }

    @Test
    public void testUnionOrderByBeforeLimit() {
        String sql = orderBySqlParser.converToOrderBySql(
                "select id from user union all select id from admin order by id desc limit 10", "id asc");
        Assert.assertEquals("SELECT id FROM user UNION ALL SELECT id FROM admin ORDER BY id ASC LIMIT 10", sql);
    }

    @Test
    public void testOrderByBeforeLimitWithoutOriginalOrderBy() {
        String sql = orderBySqlParser.converToOrderBySql("select * from user limit 10", "id desc, name asc");
        Assert.assertEquals("SELECT * FROM user ORDER BY id DESC, name ASC LIMIT 10", sql);
    }

    @Test
    public void testExecuteOrderByWithLimit() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:order_by_limit", "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("create table order_by_limit_test (id integer)");
            try {
                statement.execute("insert into order_by_limit_test values (3), (1), (2)");
                String sql = orderBySqlParser.converToOrderBySql(
                        "select id from order_by_limit_test order by id desc limit 2", "id asc");
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    Assert.assertTrue(resultSet.next());
                    Assert.assertEquals(1, resultSet.getInt(1));
                    Assert.assertTrue(resultSet.next());
                    Assert.assertEquals(2, resultSet.getInt(1));
                    Assert.assertFalse(resultSet.next());
                }
            } finally {
                statement.execute("drop table order_by_limit_test");
            }
        }
    }
}
