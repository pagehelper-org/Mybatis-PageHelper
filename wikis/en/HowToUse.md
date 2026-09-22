## HOW TO USE

### 1. Installation <a href="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper"><img src="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg"/></a>

#### 1). Using Maven

Add the following dependencies to the pom.xml:

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>latestVersion</version>
</dependency>
```

#### 2). Using Gradle

To 'build.gradle' add:

```groovy
dependencies {
    compile("com.github.pagehelper:pagehelper:latestVersion")
}
```

#### 3). When using Spring Boot <a href="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper-spring-boot-starter"><img src="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper-spring-boot-starter/badge.svg"/></a>

Maven：

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>latestVersion</version>
</dependency>
```

Gradle:

```groovy
dependencies {
    compile("com.github.pagehelper:pagehelper-spring-boot-starter:latestVersion")
}
```

### 2. Config PageHelper

#### Choosing JSqlParser Version (Optional)

PageHelper uses JSqlParser 4.7 by default. To use a different version of JSqlParser, exclude the default jsqlparser dependency from PageHelper and include the corresponding sqlparser module.

| JSqlParser Version | Module | Description |
|-------------------|--------|-------------|
| 4.5 | sqlparser4.5 | Supports JSqlParser 4.5 |
| 4.7 | Default (no extra config) | PageHelper default version |
| 4.9 / 5.0 | sqlparser4.9 | JSqlParser 5.0 is API-compatible with 4.9 |
| 5.1 / 5.2 / 5.3 / 5.4 | sqlparser5.1 | Supports JSqlParser 5.1+ (API-compatible) |

For example, to use JSqlParser 4.9:

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>latestVersion</version>
    <exclusions>
        <exclusion>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>sqlparser4.9</artifactId>
    <version>latestVersion</version>
</dependency>
```

To use JSqlParser 5.4, select the `sqlparser5.1` module and explicitly set the `jsqlparser` dependency version to `5.4`.

For SQL parsing timeout protection (10-second timeout), add the following dependency:

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>sqlparser-timeout</artifactId>
    <version>latestVersion</version>
</dependency>
```

> For more details, refer to the [pagehelper-sqlparser](https://github.com/pagehelper/pagehelper-sqlparser) project.

#### 1). Using in mybatis-config.xml

```xml
<!--
    In the configuration file,
    plugins location must meet the requirements as the following order:
    properties?, settings?,
    typeAliases?, typeHandlers?,
    objectFactory?,objectWrapperFactory?,
    plugins?,
    environments?, databaseIdProvider?, mappers?
-->
<plugins>
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <!-- config params as the following -->
        <property name="param1" value="value1"/>
	</plugin>
</plugins>
```

#### 2). Using in Spring application.xml
config `org.mybatis.spring.SqlSessionFactoryBean` as following:
```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
  <!-- other configuration -->
  <property name="plugins">
    <array>
      <bean class="com.github.pagehelper.PageInterceptor">
        <property name="properties">
          <!-- config params as the following -->
          <value>
            param1=value1
          </value>
        </property>
      </bean>
    </array>
  </property>
</bean>
```

#### 3). Configured in Spring Boot

Spring Boot automatically take effect after the introduction of the starter, the paging plug-in configuration, in the
Spring the Boot the corresponding configuration file ` application. [properties | yaml] ` configuration:

properties:

```properties
pagehelper.propertyName=propertyValue
pagehelper.reasonable=false
pagehelper.defaultCount=true
```

yaml:

```yaml
pagehelper:
  propertyName: propertyValue
  reasonable: false
  defaultCount: true # The default parameter of the paging plug-in is in the form of default-count. The parameter of the customized extension must be case - consistent
```

> The default parameter of the paging plug-in is in the form of default-count. The parameter of the customized extension
> must be case - consistent.
>
>
Supported default parameters for
reference:[PageHelperStandardProperties.java](https://github.com/pagehelper/pagehelper-spring-boot/blob/master/pagehelper-spring-boot-autoconfigure/src/main/java/com/github/pagehelper/autoconfigure/PageHelperStandardProperties.java)

#### 4). Banner Setting

To avoid errors caused by multiple configurations of the paging plug-in, banner is displayed when the paging plug-in is
configured.

```
DEBUG [main] -

,------.                           ,--.  ,--.         ,--.
|  .--. '  ,--,--.  ,---.   ,---.  |  '--'  |  ,---.  |  |  ,---.   ,---.  ,--.--.
|  '--' | ' ,-.  | | .-. | | .-. : |  .--.  | | .-. : |  | | .-. | | .-. : |  .--'
|  | --'  \ '-'  | ' '-' ' \   --. |  |  |  | \   --. |  | | '-' ' \   --. |  |
`--'       `--`--' .`-  /   `----' `--'  `--'  `----' `--' |  |-'   `----' `--'
`---'                                   `--'                        is intercepting.
```

If the banner is output for many times during the project startup, the paging plug-in has been configured for many
times. Check whether the system has configured the paging plug-in based on the log output location.

If you don't want to output the banner at startup, you can turn it off via system variables or environment variables.

- system variables: `-Dpagehelper.banner=false`
- envionment variables: `PAGEHELPER_BANNER=false`

#### 5). PageHelper Parameters

PageHelper provides several optional parameters,
these parameters when used in accordance with the above examples to configuration.

**Optional parameters as follows:**

1. `debug` : Debug parameter, default 'false' off, set to 'true' enabled, can check the existence of unsafe calls in the
   system, [see how to call safely](3-Pagehelper - secure calls). When static methods such as' pageHelper. startPage '
   are called to set paging parameters, the current executed method stack information will be recorded. When the query
   method of MyBatis is executed, the set paging parameters will be used, and the set method stack will be output. If it
   is not the same as the currently executing method, then the corresponding call in the stack is an unsafe call and
   needs to be adjusted according to the way in Safe Call (3-PageHelper-Safe Call). An example of the output stack is as
   follows:
   ```
   00:19:08.915 [main] DEBUG c.github.pagehelper.PageInterceptor - java.lang.Exception: 设置分页参数时的堆栈信息
       at com.github.pagehelper.util.StackTraceUtil.current(StackTraceUtil.java:12)
       at com.github.pagehelper.Page.<init>(Page.java:111)
       at com.github.pagehelper.Page.<init>(Page.java:126)
       at com.github.pagehelper.page.PageMethod.startPage(PageMethod.java:139)
       at com.github.pagehelper.page.PageMethod.startPage(PageMethod.java:113)
       at com.github.pagehelper.page.PageMethod.startPage(PageMethod.java:102)
       at com.github.pagehelper.test.basic.PageHelperTest.testNamespaceWithStartPage(PageHelperTest.java:118)
       ...omit

   00:19:09.069 [main] DEBUG c.g.pagehelper.mapper.UserMapper - Cache Hit Ratio [com.github.pagehelper.mapper.UserMapper]: 0.0
   00:19:09.077 [main] DEBUG o.a.i.t.jdbc.JdbcTransaction - Opening JDBC Connection
   00:19:09.078 [main] DEBUG o.a.i.t.jdbc.JdbcTransaction - Setting autocommit to false on JDBC Connection [org.hsqldb.jdbc.JDBCConnection@6da21078]
   00:19:09.087 [main] DEBUG c.g.p.m.UserMapper.selectAll_COUNT - ==>  Preparing: SELECT count(1) FROM user
   00:19:09.121 [main] DEBUG c.g.p.m.UserMapper.selectAll_COUNT - ==> Parameters:
   00:19:09.131 [main] TRACE c.g.p.m.UserMapper.selectAll_COUNT - <==    Columns: C1
   00:19:09.131 [main] TRACE c.g.p.m.UserMapper.selectAll_COUNT - <==        Row: 183
   00:19:09.147 [main] DEBUG c.g.p.m.UserMapper.selectAll_COUNT - <==      Total: 1
   ```

2. `dialect` : by default paging will use PageHelper way, if you want to achieve their own paging logic, can
   realize `Dialect` (`com.github.pagehelper.Dialect`) interface, and then configure the properties to achieve the fully
   qualified name of the class.

3. `countSuffix` : msId suffix appended when creating or looking for a corresponding count query based on a query,
   default '_COUNT'.

4. `countMsIdGen`(5.3.2+) : The count method of the msId generation, the default is to query the `msId + countSuffix`,
   want my own definition, can realize `com.github.pagehelper.CountMsIdGen` interface, the parameter configuration in
   order to realize the fully qualified class name. A common use: In the case of an Example query, 'selectByExample' can
   be queried using the corresponding 'selectCountByExample' method.

5. `msCountCache`: Automatically create a query count query method, created the count `MappedStatement` caching, the
   default will be preferred to find `com.google.common.cache.Cache`. The cache implementation, Projects without Guava
   dependencies are created using MyBatis' built-in CacheBuilder. Want to fine-grained cache configuration: please refer
   to the source code. `com.github.pagehelper.cache.CacheFactory`, two configurations of default provides multiple
   attributes, can also according to the requirements to build themselves here.

**The following parameters are the parameters for the default dialect case.
When implemented using a custom dialect, the following parameter has no effect.**

1. `helperDialect`: PageHelper will detect the current database url by default, automatically select the corresponding
   database dialect.
   You can configure `helperDialect` Property to specify the dialect. You can use the following abbreviations :
   `oracle`, `mysql`, `mariadb`, `sqlite`, `hsqldb`, `postgresql`,
   `db2`, `sqlserver`, `informix`, `h2`, `sqlserver2012`, `derby`.
   You can also implement `AbstractHelperDialect`,
   and then configure the attribute to achieve the fully qualified class name.
   **Special note :** When using the SqlServer2012 database,
   you need to manually specify for `sqlserver2012`, otherwise it will use the SqlServer2005 for paging.

2. `dialectAlias`：Allows you to configure an alias for a custom implementation. It can be used to automatically obtain
   the corresponding implementation according to JDBCURL. It allows you to override existing implementations in this
   way.
   ```xml
   <property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
   ```
   When you use jdbcurl is not [PageAutoDialect](src/main/java/com/github/pagehelper/page/PageAutoDialect.java) default
   provide range, can be realized through the change of parameters automatic identification.

3. `useSqlserver2012`(sqlserver)：To use the SqlServer2012 database, manually specify SqlServer2012. Otherwise,
   SqlServer2005 will be used for paging. You can also set `useSqlserver2012=true` to change 2012 to the default mode of
   SQLServer.

4. `defaultCount`：Use to control whether a count query is executed in a method that does not default to count queries.
   By default, true executes a count query. This is a globally valid parameter and a uniform behavior across multiple
   data sources.

5. `countColumn`：Used to configure the query column for automatic count queries. The default value is `0`, which
   is `count(0)`. The Page object also has a new 'countColumn' parameter, which can be configured for specific queries.

6`offsetAsPageNum`: Default value is `false`, This parameter is valid for `RowBounds` as a pagination parameter.
When this parameter is set to `true`, the` offset` parameter in `RowBounds` is used as` pageNum`.

7. `rowBoundsWithCount`: Default value is `false`, When this parameter is set to `true`,
   PageHelper will execute count query.

8. `pageSizeZero`: Default value is `false`, When this parameter is set to `true`,
   if `pageSize=0` or `RowBounds.Limit = 0` will query all the results (the equivalent of a Paged query did not execute,
   but the return type of the result is still `Page`).


9. `reasonable`: Rationalization of paging parameters, Default value is `false`。
   When this parameter is set to `true`,` pageNum <= 0` will query the first page,
   `PageNum> pages` (over the total number), will query the last page. Default `false`, the query directly based on
   parameters.

10. `params`: In support of `startPage(Object params)` method,
    The parameter is added to configure the parameter mapping for the value from the object based on the attribute name,
    you can configure `pageNum,pageSize,count,pageSizeZero,reasonable`,
    Default value is `pageNum=pageNum;pageSize=pageSize;count=countSql;reasonable=reasonable;pageSizeZero=pageSizeZero`。

11. `supportMethodsArguments`: Support via the Mapper interface parameters to pass the page parameter, the default value
    is 'false'.
    The use of methods can refer to the test code in the `com.github.pagehelper.test.basic` package under
    the` ArgumentsMapTest` and `ArgumentsObjTest`.

12. `autoRuntimeDialect`: Default value is `false`。When set to `true`,
    it is possible to automatically recognize pagination of the corresponding dialect at run time from multiple data
    sources
    (Does not support automatic selection of `sqlserver2012`, can only use` sqlserver`), usage and precautions refer to
    the following **Scene 5**.


13. `closeConn`: Default value is **`true`**。
    When you use a runtime dynamic data source or do not set the `helperDialect` property, PageHelper will automatically
    get the database type, then a database connection is automatically obtained,
    This property is used to set whether to close the connection, the default `true` close. When 'false' is set, It will
    not close the connection.

14. `aggregateFunctions`(5.1.5+): The default is the aggregate function of all common databases,
    allowing you to manually add aggregate functions ( affecting the number of rows ).
    All functions that start with aggregate functions will be wrap as subquery.
    Other functions and columns will be replaced with count(0).

15. `replaceSql`(sqlserver): Optional value of `regex` and `simple`, default value is used when empty `regex` way, also
    can realize `com.github.pagehelper.dialect.ReplaceSql` interface.

16. `sqlCacheClass`(sqlserver): Used to generate the count and page caching, SQL cache
    using `com.github.pagehelper.cache.CacheFactory`, optional parameters and the front `msCountCache`.

17. `autoDialectClass`: Add `AutoDialect` interface for automatic access to the database type, can be achieved
    by `autoDialectClass` configuration for their implementation class, default `DataSourceNegotiationAutoDialect`,
    priority according to the connection pool. Default implementation, added special handling for 'hikari, Druid,
    tomcat-JDBC, C3P0, DBCP' type database connection pool, directly from the configuration to get jdbcUrl, when using
    other types of data sources, still use the old way to get the connection in read jdbcUrl. To use the same method as
    the old version, you can configure 'autoDialectClass=old'. If the database connection pool type is very clear, you
    are advised to set it to a specific value. For example, if hikari is used, 'autoDialectClass=hikari' is set. If
    other connection pools are used, set it to its own implementation class.

18. `boundSqlInterceptors`: Add the `BoundSqlInterceptor` interceptor of the paging plug-in, which can process or simply
    read SQL in three stages, add the parameter `boundSqlInterceptors`, You can configure multiple implementation class
    names that implement the BoundSqlInterceptor interface, separated by commas. When PageHelper is called, You can also
    set this page by using something
    like `PageHelper.startPage(x,x).boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor)`.

19. `keepOrderBy`: Preserves the Order by sort of the query when converting count queries. In addition to global
    configuration, you can set the parameters for a single operation.

20. `keepSubSelectOrderBy`: Preserves the Order by sort of subqueries when converting count queries. You can avoid
    adding `/*keep orderby*/` to all subqueries and can set it for a single operation in addition to the global
    configuration.

21. `asyncCount`: Default value is `false`. When set to `true`, count queries will be executed asynchronously,
    which can improve overall pagination query efficiency. Can also be set for individual operations in addition to the global configuration.

22. `asyncCountParallelism`: Thread pool size for async count queries, default value is `Runtime.getRuntime().availableProcessors() * 2`.
    Only effective when `asyncCount=true`.

23. `countSqlParser`: Configure a custom count SQL parser. Must implement the `com.github.pagehelper.parser.CountSqlParser` interface.
    Set to the fully qualified class name of the implementation. Default is `DefaultCountSqlParser`. Replaces the count-related functionality
    from the removed `sqlParser` parameter.

24. `orderBySqlParser`: Configure a custom ORDER BY SQL parser. Must implement the `com.github.pagehelper.parser.OrderBySqlParser` interface.
    Set to the fully qualified class name of the implementation. Default is `DefaultOrderBySqlParser`.

25. `sqlServerSqlParser`: Configure a custom SqlServer pagination SQL parser. Must implement the `com.github.pagehelper.parser.SqlServerSqlParser` interface.
    Set to the fully qualified class name of the implementation. Default is `DefaultSqlServerSqlParser`.

> ~~`sqlParser`: Configure JSqlParser parser, which is the `com.github.pagehelper.JSqlParser` interface, used to support
> sqlserver and other scenarios that need additional configuration. **This parameter was removed in 6.1.0**, and its functionality
> has been split into three separate parameters: `countSqlParser`, `orderBySqlParser`, and `sqlServerSqlParser`.~~

#### 6. How to choose Configure these parameters

Here are a few examples for some of the parameters may be used.

##### Scene 1

If you are still in with a way to call a namespace like iBATIS, you might use `rowBoundsWithCount`.
If you want to count when the paging query query, you need to set this parameter to `true`.

**Note:** `PageRowBounds` also need `true`.

##### Scene 2

If you are still in with a way to call a namespace like iBATIS, If  you think `RowBounds` in the two parameters` offset, limit` not as good as `pageNum, pageSize` easy to understand.
You can use the `offsetAsPageNum` parameter, when the parameter is set to `true`, `offset` as `pageNum`, `limit` and `pageSize` mean the same thing.

##### Scene 3

If you feel you have to paginate a page somewhere and you still want to query all the results with control parameters.
You can configure `pageSizeZero` to` true`,
After configuration, when `pageSize = 0` or `RowBounds.limit = 0` will query all the results.

##### Scene 4

If you want the user to enter the page number is not in the legal scope (the first page to the last page) to correctly respond to the correct results page,
Then you can configure `reasonable` to` true`, and if `pageNum <= 0` will query the first page, the `pageNum> pages(total pages)` will query the last page.

##### Scene 5

If you configure dynamic data sources in Spring and connect different types of databases,
you can configure `autoRuntimeDialect` to` true`,
which will use matching pagination queries when using different data sources.
In this case, you also need to pay attention to the `closeConn` parameter,
because the type of access to the data source will get a database connection,
so the need to control this parameter to obtain a connection, whether to close the connection.

Default is `true`, and some database connections can not be closed after the follow-up database operations.
And some database connections will not be closed soon because the number of connections out of the database caused no
response.
Therefore, when using this feature, in particular, you need to pay attention to whether the use of the data source needs
to close the database connection.

When you do not use dynamic data sources but only automatically get `helperDialect`,
the database connection will only get once,
so there is no need to worry about whether this connection will lead to a database error,
but also according to the characteristics of the data source to choose whether to close the connection.

### 3. How to use in your code

Please note before
reading [Important Notice](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md)

PageHelper supports the following usage:

```java
//1. use by RowBounds
List<User> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(0, 10));
//or interface
List<User> list = userMapper.selectIf(1, new RowBounds(0, 10));
//or PageRowBounds
PageRowBounds rowBounds = new PageRowBounds(0, 10);
List<User> list = userMapper.selectIf(1, rowBounds);
long total = rowBounds.getTotal();

//2. use static method startPage
PageHelper.startPage(1, 10);
List<User> list = userMapper.selectIf(1);

//3. use static method offsetPage
PageHelper.offsetPage(0, 10);
List<User> list = userMapper.selectIf(1);

//4. method parameters
public interface CountryMapper {
    List<User> selectByPageNumSize(
            @Param("user") User user,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize);
}
//config supportMethodsArguments=true
List<User> list = userMapper.selectByPageNumSize(user, 1, 10);

//5. POJO parameters
public class User {
    //other fields
    //The following two parameters must be the same name as the params parameter
    private Integer pageNum;
    private Integer pageSize;
}
public interface CountryMapper {
    List<User> selectByPageNumSize(User user);
}
//When the pageNum! = null && pageSize! = null in the user instance, this method will be automatically pagination
List<User> list = userMapper.selectByPageNumSize(user);

//6. ISelect interface
//jdk6,7 anonymous class, return Page
Page<User> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectGroupBy();
    }
});
//jdk8 lambda
Page<User> page = PageHelper.startPage(1, 10).doSelectPage(()-> userMapper.selectGroupBy());

//return PageInfo
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectGroupBy();
    }
});
//in lambda
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(() -> userMapper.selectGroupBy());

//do count only
long total = PageHelper.count(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectLike(user);
    }
});
//lambda
total = PageHelper.count(()->userMapper.selectLike(user));
```

Introduced The most common ways.

#### 1). RowBounds and PageRowBounds

```java
List<User> list=sqlSession.selectList("x.y.selectIf",null,new RowBounds(1,10));
```

Using this method, you can use the Row Bounds parameter for paging, which is the least invasive method. As we can see,
the call using the Row Bounds parameter doesn't add anything else.

When the paging plug-in detects that the Row Bounds parameter is used, the query is <b> physically paginated</b>.

There are two special arguments to RowBounds for this method of calling. You can see Scene 1 and 2 above

**Note:**  You can use Row Bounds not only in namespace mode, but also when using an interface. For example:

```java
//A physical paging query is also performed in this case
List<User> selectAll(RowBounds rowBounds);
```

**Note:** Since RowBounds by default does not get the total number of queries, the PageRowBounds plug-in provides a '
PageRowBounds' object that inherits from RowBounds. The' Total 'property is added to this object to get the total number
of queries after performing a pagination query.

#### 2). `PageHelper.startPage` Static method calls

In addition to the `PageHelper.startPage` method, the `PageHelper.offsetPage` method is also provided.

Call the `PageHelper.startPage` static method before the MyBatis query method that you want to page. The first MyBatis
query method immediately following this method will be paginated.

##### Example 1：

```java
//Get page 1, 10 items, default query total count
PageHelper.startPage(1,10);
//The first SELECT method immediately following is paginated
        List<User> list=userMapper.selectIf(1);
assertEquals(2, list.get(0).getId());
        assertEquals(10,list.size());
//When paging, the actual returned result list type is Page<E>. If you want to take out paging information, you need to force the conversion to Page<E>.
        assertEquals(182,((Page)list).getTotal());
```

##### Example 2：
```java
//request: url?pageNum=1&pageSize=10
//Supports ServletRequest,Map,POJO objects, and PARams parameters
PageHelper.startPage(request);
//The first SELECT method immediately following is paginated
List<User> list = userMapper.selectIf(1);

//Subsequent pages will not be paginated unless Page helper.start Page is called again
List<User> list2 = userMapper.selectIf(null);
//list1
assertEquals(2, list.get(0).getId());
assertEquals(10, list.size());
//During paging, the actual returned result list type is Page<E>. If you want to take out paging information, you need to force conversion to Page<E>.
//Or use the Page Info class (described in the example below)
assertEquals(182, ((Page) list).getTotal());
//list2
assertEquals(1, list2.get(0).getId());
assertEquals(182, list2.size());
```

##### Example 3，Use `PageInfo`:

```java
//Get page 1, 10 items, default query total count
PageHelper.startPage(1,10);
        List<User> list=userMapper.selectAll();
//Wrap the results with Page Info
        PageInfo page=new PageInfo(list);
//Test all Page Info properties
//Page Info contains a very comprehensive set of paging properties
        assertEquals(1,page.getPageNum());
        assertEquals(10,page.getPageSize());
        assertEquals(1,page.getStartRow());
        assertEquals(10,page.getEndRow());
        assertEquals(183,page.getTotal());
        assertEquals(19,page.getPages());
        assertEquals(1,page.getFirstPage());
        assertEquals(8,page.getLastPage());
        assertEquals(true,page.isFirstPage());
        assertEquals(false,page.isLastPage());
        assertEquals(false,page.isHasPreviousPage());
        assertEquals(true,page.isHasNextPage());
```

#### 3). Using parameters

To use the parametric approach, you need to set the `supportMethodsArguments` parameter to `true`, as well as
the `params` parameter.
For example, the following configuration:

```xml
<plugins>
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <!-- Use the following method to configure the parameters, all of which are described later -->
        <property name="supportMethodsArguments" value="true"/>
        <property name="params" value="pageNum=pageNumKey;pageSize=pageSizeKey;"/>
	</plugin>
</plugins>
```

In the MyBatis method:
```java
List<User> selectByPageNumSize(
@Param("user") User user,
@Param("pageNumKey") int pageNum,
@Param("pageSizeKey") int pageSize);
```

When this method is called, it is paged because both `pageNumKey` and `pageSizeKey` arguments are found. Several
parameters provided by PARams can be used in this way.

In addition to the above method, if the User object contains these two parameter values, you can also have the following
method:

```java
List<User> selectByPageNumSize(User user);
```

When both `pageNumKey` and `pageSizeKey` arguments are found from User, the method is paged.

Note: The presence of both `pageNum` and `pageSize` will trigger the paging operation. In this case, the other paging
parameters will take effect.

#### 3). `PageHelper` Safety call

##### 1. Using the RowBounds and PageRowBounds arguments is extremely safe

##### 2. The parametric approach is extremely safe

##### 3. The call using the `ISelect` interface is extremely secure

In addition to being secure, the ISelect interface specifically converts the query to a simple count query, which
converts any query method into a `select count(*)` query method.

##### 4. When does this lead to unsafe paging?

The `PageHelper` method uses a static `ThreadLocal` argument, and the page argument is bound to the thread.

This is safe as long as you can ensure that the MyBatis query method is followed by the `PageHelper` method call.
Because PageHelper automatically clears objects stored in ThreadLocal in the Finally section.

If an exception occurs before the code enters the Executor, the thread is not available. This is an artificial Bug (for
example, when an interface method does not match an XML method and a 'MappedStatement' is not found). It does not cause
the ThreadLocal argument to be used incorrectly.

But if you write code like this, it is not safe to use it:
```java
PageHelper.startPage(1, 10);
List<User> list;
if(param1 != null){
    list = userMapper.selectIf(param1);
} else {
    list = new ArrayList<User>();
}
```

In this case, because param1 is null, it causes PageHelper to produce a page parameter, but it is not consumed, so it
remains on the thread. When this thread is used again, it may cause the paging parameter to be consumed by a method that
should not be paged, resulting in unexplained paging.

The above code should look like this:
```java
List<User> list;
if(param1 != null){
    PageHelper.startPage(1, 10);
    list = userMapper.selectIf(param1);
} else {
    list = new ArrayList<User>();
}
```

It's safe to write it this way.

If you're worried about this, you can manually clean up the paging parameters stored in ThreadLocal by using the
following:
```java
List<User> list;
if(param1 != null){
    PageHelper.startPage(1, 10);
    try{
        list = userMapper.selectAll();
    } finally {
        PageHelper.clearPage();
    }
} else {
    list = new ArrayList<User>();
}
```

It's not pretty, and it's unnecessary.

### 4. MyBatis and Spring integration example

If you are not familiar with Spring integration, you can refer to the following two

There is only basic configuration information, without any off-the-shelf functionality, as a starting point for building
the framework

- [integration Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [integration Spring 4.x](https://github.com/abel533/Mybatis-Spring)

### 5. Spring Boot integration example

- [pagehelper-spring-boot-samples](https://github.com/pagehelper/pagehelper-spring-boot/tree/master/pagehelper-spring-boot-samples)
- https://github.com/abel533/MyBatis-Spring-Boot
