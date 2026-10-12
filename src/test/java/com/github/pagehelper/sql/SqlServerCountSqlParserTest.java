package com.github.pagehelper.sql;

import com.github.pagehelper.*;
import com.github.pagehelper.dialect.helper.*;
import com.github.pagehelper.dialect.rowbounds.*;
import com.github.pagehelper.page.PageAutoDialect;
import com.github.pagehelper.parser.defaults.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SqlServerCountSqlParserTest {
    private Connection connection;
    private UnpooledDataSource dataSource;
    private final DefaultSqlServerCountSqlParser parser = new DefaultSqlServerCountSqlParser();

    @Before
    public void createDatabase() throws Exception {
        dataSource = new UnpooledDataSource("org.h2.Driver", "jdbc:h2:mem:count_mssql_"
                + System.nanoTime() + ";MODE=MSSQLServer;DB_CLOSE_DELAY=-1", "sa", "");
        connection = dataSource.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t(id INTEGER, hidden INTEGER)");
            statement.execute("INSERT INTO t VALUES (1,10),(2,20),(3,30),(4,40)");
        }
    }

    @After
    public void closeDatabase() throws Exception {
        PageHelper.clearPage();
        try (Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
        connection.close();
    }

    @Test
    public void orderingExpressionsKeepScopeAndAreNotAggregated() throws Exception {
        assertCount(4, "SELECT a.id FROM t a ORDER BY CASE WHEN a.id = ? THEN 0 ELSE 1 END", 2);
        assertCount(4, "SELECT id FROM t ORDER BY CASE WHEN hidden = ? THEN 0 ELSE 1 END", 20);
        assertCount(1, "SELECT COUNT(*) AS total FROM t ORDER BY COUNT(*) + ?", 1);
        assertCount(4, "SELECT a.id FROM t a ORDER BY CASE WHEN EXISTS "
                + "(SELECT 1 FROM t b WHERE b.id = a.id AND b.hidden = ?) THEN 0 ELSE 1 END", 20);
    }

    @Test
    public void existingTopAndOffsetPreserveBindingOrder() throws Exception {
        String top = "SELECT TOP (?) id FROM t WHERE hidden > ? ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END";
        String count = assertCount(2, top, 2, 0, 2);
        assertTrue(count, count.contains("TOP (?)"));
        assertFalse(count, count.contains("100 PERCENT"));
        String offset = "SELECT id FROM t ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        count = assertCount(2, offset, 2, 1, 2);
        assertFalse(count, count.contains("TOP"));
    }

    @Test
    public void keepOrderByAndUnionUseFirstBranchNames() throws Exception {
        String union = "SELECT id AS first_name FROM t UNION ALL SELECT id AS other_name FROM t ORDER BY first_name";
        String count = assertCount(8, "/*keep orderby*/ " + union);
        assertTrue(count, count.contains("SELECT TOP 100 PERCENT *"));
        assertTrue(count, count.contains("ORDER BY first_name"));
        PageHelper.startPage(1, 10).keepOrderBy(true);
        assertCount(8, union);
    }

    @Test
    public void nestedUnionAndCteKeepOrderingLegal() throws Exception {
        PageHelper.startPage(1, 10).keepSubSelectOrderBy(true);
        String count = assertCount(8, "WITH cte AS (SELECT id AS first_name FROM t UNION ALL "
                + "SELECT id AS other_name FROM t ORDER BY first_name) SELECT u.first_name FROM cte u "
                + "ORDER BY CASE WHEN u.first_name = ? THEN 0 ELSE 1 END", 2);
        assertTrue(count, count.startsWith("WITH cte AS"));
        assertTrue(count, count.contains("SELECT TOP 100 PERCENT *"));
        assertCount(8, "SELECT u.first_name FROM (SELECT id AS first_name FROM t UNION ALL "
                + "SELECT id AS other_name FROM t ORDER BY first_name) u "
                + "ORDER BY CASE WHEN u.first_name = ? THEN 0 ELSE 1 END", 2);
    }

    @Test
    public void emptyNullAndUnparseableSqlKeepTheirContracts() throws Exception {
        assertCount(0, "SELECT id FROM t WHERE id = ? ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END", 99, 2);
        assertCount(4, "SELECT * FROM t ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END", (Object) null);
        String sql = "SELECT FROM ???";
        assertEquals(new DefaultCountSqlParser().getSimpleCountSql(sql), parser.getSmartCountSql(sql));
    }

    @Test
    public void helpersAndRowBoundsChooseSqlServerParser() {
        String sql = "SELECT a.id FROM t a ORDER BY CASE WHEN a.id = ? THEN 0 ELSE 1 END";
        for (Dialect dialect : new Dialect[] {new SqlServerDialect(), new SqlServer2012Dialect(),
                new SqlServerRowBoundsDialect(), new SqlServer2012RowBoundsDialect()}) {
            dialect.setProperties(new Properties());
            String count = dialect.getCountSql(null, boundSql(sql), null, RowBounds.DEFAULT, new CacheKey());
            assertTrue(count, count.contains("TOP 100 PERCENT"));
            assertFalse(count, count.contains("MIN("));
        }
    }

    @Test
    public void derbyAndCustomCountParserArePreserved() {
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "derby");
        PageAutoDialect auto = new PageAutoDialect();
        auto.setProperties(properties);
        assertTrue(auto.getDelegate() instanceof DerbyDialect);
        String sql = "SELECT id FROM t ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END";
        String count = auto.getDelegate().getCountSql(null, boundSql(sql), null, RowBounds.DEFAULT, new CacheKey());
        assertEquals(new DefaultCountSqlParser().getSmartCountSql(sql), count);
        properties.setProperty("countSqlParser", CustomParser.class.getName());
        for (Dialect dialect : new Dialect[] {new SqlServerDialect(), new SqlServerRowBoundsDialect()}) {
            dialect.setProperties(properties);
            assertEquals("custom-count", dialect.getCountSql(null, boundSql(sql), null, RowBounds.DEFAULT, new CacheKey()));
        }
    }

    @Test
    public void pageInterceptorBindsCountAndPageParameters() {
        Configuration configuration = new Configuration(new Environment("review",
                new JdbcTransactionFactory(), dataSource));
        configuration.setLogImpl(NoLoggingImpl.class);
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "sqlserver2012");
        interceptor.setProperties(properties);
        configuration.addInterceptor(interceptor);
        configuration.addMapper(Mapper.class);
        try (SqlSession session = new SqlSessionFactoryBuilder().build(configuration).openSession()) {
            PageHelper.startPage(1, 2);
            List<Integer> rows = session.getMapper(Mapper.class).ordered(2);
            assertEquals(4, ((Page<?>) rows).getTotal());
            assertEquals(Arrays.asList(2, 1), rows);
            assertNull(PageHelper.getLocalPage());
        }
    }


    @Test
    public void rowBoundsBindsCountAndPageParameters() {
        Properties properties = new Properties();
        properties.setProperty("dialect", SqlServer2012RowBoundsDialect.class.getName());
        Configuration configuration = configuration(properties);
        try (SqlSession session = new SqlSessionFactoryBuilder().build(configuration).openSession()) {
            PageRowBounds bounds = new PageRowBounds(0, 2);
            List<Integer> rows = session.getMapper(Mapper.class).orderedWithBounds(2, bounds);
            assertEquals(4, bounds.getTotal().longValue());
            assertEquals(Arrays.asList(2, 1), rows);
        }
    }

    @Test
    public void countFailureClearsPageState() {
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "sqlserver2012");
        try (SqlSession session = new SqlSessionFactoryBuilder().build(configuration(properties)).openSession()) {
            Mapper mapper = session.getMapper(Mapper.class);
            PageHelper.startPage(1, 2);
            try {
                mapper.invalid(2);
                fail("invalid query should fail during count");
            } catch (org.apache.ibatis.exceptions.PersistenceException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("ABSENT_COLUMN"));
            }
            assertNull(PageHelper.getLocalPage());
            assertEquals(Arrays.asList(1, 2, 3, 4), mapper.all());
        }
    }

    @Test
    public void countParserSpiAndExplicitPropertyKeepTheirPriority() throws Exception {
        Path directory = Files.createTempDirectory(Paths.get("target"), "count-spi-");
        Path services = directory.resolve("META-INF/services");
        Files.createDirectories(services);
        Files.write(services.resolve("com.github.pagehelper.parser.CountSqlParser"),
                CustomParser.class.getName().getBytes(StandardCharsets.UTF_8));
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {directory.toUri().toURL()}, previous)) {
            Thread.currentThread().setContextClassLoader(loader);
            Properties properties = new Properties();
            SqlServerDialect dialect = new SqlServerDialect();
            dialect.setProperties(properties);
            assertEquals("custom-count", dialect.getCountSql(null, boundSql("SELECT id FROM t"),
                    null, RowBounds.DEFAULT, new CacheKey()));
            properties.setProperty("countSqlParser", ExplicitParser.class.getName());
            dialect = new SqlServerDialect();
            dialect.setProperties(properties);
            assertEquals("explicit-count", dialect.getCountSql(null, boundSql("SELECT id FROM t"),
                    null, RowBounds.DEFAULT, new CacheKey()));
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                    Files.delete(path);
                }
            }
        }
    }

    private Configuration configuration(Properties properties) {
        Configuration configuration = new Configuration(new Environment("review",
                new JdbcTransactionFactory(), dataSource));
        configuration.setLogImpl(NoLoggingImpl.class);
        PageInterceptor interceptor = new PageInterceptor();
        interceptor.setProperties(properties);
        configuration.addInterceptor(interceptor);
        configuration.addMapper(Mapper.class);
        return configuration;
    }

    public static class ExplicitParser extends CustomParser {
        @Override
        public String getSmartCountSql(String sql, String name) {
            return "explicit-count";
        }
    }

    private BoundSql boundSql(String sql) {
        return new BoundSql(new Configuration(), sql, Collections.emptyList(), null);
    }

    private String assertCount(int expected, String sql, Object... parameters) throws Exception {
        String count = parser.getSmartCountSql(sql);
        assertFalse(count, count.contains("MIN("));
        try (PreparedStatement statement = connection.prepareStatement(count)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(count, result.next());
                assertEquals(count, expected, result.getInt(1));
                assertFalse(count, result.next());
            }
        }
        return count;
    }

    public static class CustomParser extends DefaultCountSqlParser {
        @Override
        public String getSmartCountSql(String sql, String name) {
            return "custom-count";
        }
    }

    public interface Mapper {
        @Select("SELECT a.id FROM t a ORDER BY CASE WHEN a.id = #{id} THEN 0 ELSE 1 END, a.id")
        List<Integer> ordered(@Param("id") int id);

        @Select("SELECT a.id FROM t a ORDER BY CASE WHEN a.id = #{id} THEN 0 ELSE 1 END, a.id")
        List<Integer> orderedWithBounds(@Param("id") int id, RowBounds bounds);

        @Select("SELECT absent_column FROM t ORDER BY CASE WHEN hidden = #{id} THEN 0 ELSE 1 END")
        List<Integer> invalid(@Param("id") int id);

        @Select("SELECT id FROM t ORDER BY id")
        List<Integer> all();
    }
}
