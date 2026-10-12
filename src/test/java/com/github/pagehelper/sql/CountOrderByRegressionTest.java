package com.github.pagehelper.sql;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.parser.defaults.DefaultCountSqlParser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class CountOrderByRegressionTest {
    private Connection connection;
    private UnpooledDataSource dataSource;
    private final DefaultCountSqlParser parser = new DefaultCountSqlParser();

    @Before
    public void createDatabase() throws Exception {
        dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver",
                "jdbc:hsqldb:mem:count_order_" + System.nanoTime(), "SA", "");
        connection = dataSource.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t(id INTEGER, hidden INTEGER)");
            statement.execute("INSERT INTO t VALUES (1,10),(2,20),(3,30),(4,40)");
        }
    }

    @After
    public void closeDatabase() throws Exception {
        PageHelper.clearPage();
        if (connection != null) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SHUTDOWN");
            }
            connection.close();
        }
    }

    @Test
    public void qualifiedOrderingKeepsItsTableScope() throws Exception {
        assertCount(4, "SELECT a.id FROM t a ORDER BY CASE WHEN a.id = ? THEN 0 ELSE 1 END", 2);
    }

    @Test
    public void unprojectedOrderingKeepsItsColumnScope() throws Exception {
        assertCount(4, "SELECT id FROM t ORDER BY CASE WHEN hidden = ? THEN 0 ELSE 1 END", 20);
    }

    @Test
    public void aggregateOrderingIsNotWrappedInAnotherAggregate() throws Exception {
        assertCount(1, "SELECT COUNT(*) AS total FROM t ORDER BY COUNT(*) + ?", 1);
    }

    @Test
    public void offsetFetchBindingsKeepTheirOriginalPositions() throws Exception {
        assertCount(2, "SELECT id FROM t ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY", 2, 1, 2);
    }

    @Test
    public void cteAndOrderingBindingsKeepTheirScope() throws Exception {
        assertCount(4, "WITH cte AS (SELECT id, hidden FROM t WHERE id > ?) SELECT id FROM cte "
                + "ORDER BY CASE WHEN hidden = ? THEN 0 ELSE 1 END", 0, 20);
    }

    @Test
    public void emptyAndNullInputsKeepCorrectCounts() throws Exception {
        assertCount(0, "SELECT id FROM t WHERE id = ? ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END", 99, 2);
        assertCount(4, "SELECT * FROM t ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END", (Object) null);
    }

    @Test
    public void pageInterceptorBindsParametersAndClearsState() {
        Configuration configuration = new Configuration(new Environment("review",
                new JdbcTransactionFactory(), dataSource));
        configuration.setLogImpl(NoLoggingImpl.class);
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "hsqldb");
        interceptor.setProperties(properties);
        configuration.addInterceptor(interceptor);
        configuration.addMapper(Mapper.class);
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(configuration);
        try (SqlSession session = factory.openSession()) {
            Mapper mapper = session.getMapper(Mapper.class);
            PageHelper.startPage(1, 10);
            List<Integer> rows = mapper.ordered(20);
            assertEquals(4, ((Page<?>) rows).getTotal());
            assertEquals(Arrays.asList(2, 1, 3, 4), rows);
            assertNull(PageHelper.getLocalPage());
            assertEquals(Arrays.asList(1, 2, 3, 4), mapper.all());
        }
    }

    private void assertCount(int expected, String sql, Object... parameters) throws Exception {
        String countSql = parser.getSmartCountSql(sql);
        try (PreparedStatement statement = connection.prepareStatement(countSql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(countSql, result.next());
                assertEquals(countSql, expected, result.getInt(1));
                assertFalse(countSql, result.next());
            }
        }
    }

    public interface Mapper {
        @Select("SELECT id FROM t ORDER BY CASE WHEN hidden = #{hidden} THEN 0 ELSE 1 END, id")
        List<Integer> ordered(@Param("hidden") int hidden);

        @Select("SELECT id FROM t ORDER BY id")
        List<Integer> all();
    }
}
