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

package com.github.pagehelper.test.basic.parameter;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestParameterTypeHandler {
    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void setUp() throws SQLException {
        UnpooledDataSource dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:mem:parameter_type_handler", "sa", "");
        Configuration configuration = new Configuration(new Environment("test",
                new JdbcTransactionFactory(), dataSource));
        configuration.getTypeHandlerRegistry().register(HashMap.class, HashMapTypeHandler.class);
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "hsqldb");
        interceptor.setProperties(properties);
        configuration.addInterceptor(interceptor);
        configuration.addMapper(ParameterMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        try (SqlSession session = sqlSessionFactory.openSession(true);
             Statement statement = session.getConnection().createStatement()) {
            statement.execute("DROP TABLE IF EXISTS parameter_type_handler");
            statement.execute("CREATE TABLE parameter_type_handler (id INTEGER)");
            statement.execute("INSERT INTO parameter_type_handler VALUES (1), (2), (3)");
        }
    }

    @Test
    public void testNamedParameter() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageHelper.startPage(2, 1, false);
            assertEquals(Collections.singletonList(2), session.getMapper(ParameterMapper.class).selectNamed(1));
        }
    }

    @Test
    public void testScalarParameter() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageHelper.startPage(2, 1, false);
            assertEquals(Collections.singletonList(2), session.getMapper(ParameterMapper.class).selectScalar(1));
        }
    }

    @Test
    public void testNoParameter() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageHelper.startPage(2, 1, false);
            assertEquals(Collections.singletonList(2), session.getMapper(ParameterMapper.class).selectAll());
        }
    }

    @Test
    public void testImmutableMapParameter() {
        Map<String, Object> parameters = Collections.<String, Object>singletonMap("id", 1);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageHelper.startPage(2, 1, false);
            assertEquals(Collections.singletonList(2), session.getMapper(ParameterMapper.class).selectMap(parameters));
            assertEquals(Collections.<String, Object>singletonMap("id", 1), parameters);
        }
    }

    @Test
    public void testExplicitHashMapTypeHandler() {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("id", 1);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageHelper.startPage(2, 1, false);
            assertEquals(Collections.singletonList(2), session.getMapper(ParameterMapper.class).selectHashMap(filter));
            assertEquals(Collections.<String, Object>singletonMap("id", 1), filter);
        }
    }

    public interface ParameterMapper {
        @Select("SELECT id FROM parameter_type_handler WHERE id >= #{id} ORDER BY id")
        List<Integer> selectNamed(@Param("id") int id);

        @Select("SELECT id FROM parameter_type_handler WHERE id >= #{id} ORDER BY id")
        List<Integer> selectScalar(int id);

        @Select("SELECT id FROM parameter_type_handler ORDER BY id")
        List<Integer> selectAll();

        @Select("SELECT id FROM parameter_type_handler WHERE id >= #{id} ORDER BY id")
        List<Integer> selectMap(Map<String, Object> parameters);

        @Select("SELECT id FROM parameter_type_handler WHERE id >= #{filter,javaType=java.util.HashMap} ORDER BY id")
        List<Integer> selectHashMap(@Param("filter") HashMap<String, Object> filter);
    }

    public static class HashMapTypeHandler extends ObjectTypeHandler {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
                throws SQLException {
            ps.setInt(i, (Integer) ((HashMap<?, ?>) parameter).get("id"));
        }
    }
}
