## 使用方法

### 1. 引入分页插件 <a href="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper"><img src="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg"/></a>

#### 1). 使用 Maven

在 pom.xml 中添加如下依赖：

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>最新版本</version>
</dependency>
```

#### 2). 使用 Gradle

在 `build.gradle` 中添加：

```groovy
dependencies {
    compile("com.github.pagehelper:pagehelper:最新版本")
}
```

#### 3). 使用 Spring Boot 时 <a href="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper-spring-boot-starter"><img src="https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper-spring-boot-starter/badge.svg"/></a>

Maven：

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>最新版本</version>
</dependency>
```

Gradle:

```groovy
dependencies {
    compile("com.github.pagehelper:pagehelper-spring-boot-starter:最新版本")
}
```

### 2. 配置拦截器插件

#### 选择 JSqlParser 版本（可选）

PageHelper 默认使用 JSqlParser 4.7。如需使用其他版本的 JSqlParser，可以从 PageHelper 排除默认的 jsqlparser 依赖，然后引入对应版本的 sqlparser 模块。

| JSqlParser 版本 | 对应模块 | 说明 |
|----------------|---------|------|
| 4.5 | sqlparser4.5 | 支持 JSqlParser 4.5 |
| 4.7 | 默认（无需额外配置） | PageHelper 默认版本 |
| 4.9 / 5.0 | sqlparser4.9 | JSqlParser 5.0 与 4.9 API 兼容 |
| 5.1 / 5.2 / 5.3 / 5.4 | sqlparser5.1 | 支持 JSqlParser 5.1+（API 兼容） |

例如，使用 JSqlParser 4.9：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>最新版本</version>
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
    <version>最新版本</version>
</dependency>
```

如需使用 JSqlParser 5.4，可选择 `sqlparser5.1` 模块并显式指定 `jsqlparser` 版本为 `5.4`。

如需 SQL 解析超时保护（10秒超时），可额外引入：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>sqlparser-timeout</artifactId>
    <version>最新版本</version>
</dependency>
```

> 更多详情请参考 [pagehelper-sqlparser](https://github.com/pagehelper/pagehelper-sqlparser) 项目。

特别注意，新版拦截器是 `com.github.pagehelper.PageInterceptor`。
`com.github.pagehelper.PageHelper` 现在是一个特殊的 `dialect` 实现类，是分页插件的默认实现类，提供了和以前相同的用法。

#### 1). 在 MyBatis 配置 xml 中配置拦截器插件

```xml
<!--
    plugins在配置文件中的位置必须符合要求，否则会报错，顺序如下:
    properties?, settings?,
    typeAliases?, typeHandlers?,
    objectFactory?,objectWrapperFactory?,
    plugins?,
    environments?, databaseIdProvider?, mappers?
-->
<plugins>
    <!-- com.github.pagehelper为PageHelper类所在包名 -->
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <!-- 使用下面的方式配置参数，后面会有所有的参数介绍 -->
        <property name="param1" value="value1"/>
    </plugin>
</plugins>
```

#### 2). 在 Spring 配置文件中配置拦截器插件

使用 spring 的 XML 配置方式，可以使用 `plugins` 属性像下面这样配置：
```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
  <!-- 注意其他配置 -->
  <property name="plugins">
    <array>
      <bean class="com.github.pagehelper.PageInterceptor">
          <property name="properties">
              <!--使用下面的方式配置参数，一行配置一个 -->
              <value>
                  params=value1
              </value>
          </property>
      </bean>
    </array>
  </property>
</bean>
```

#### 3). 在 Spring Boot 中配置

Spring Boot 引入 starter 后自动生效，对分页插件进行配置时，在 Spring Boot 对应的配置文件 `application.[properties|yaml]` 中配置：

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
  defaultCount: true # 分页插件默认参数支持 default-count 形式，自定义扩展的参数，必须大小写一致
```

> 分页插件默认参数支持 default-count 形式，自定义扩展的参数，必须大小写一致。
>
>
支持的默认参数参考: [PageHelperStandardProperties.java](https://github.com/pagehelper/pagehelper-spring-boot/blob/master/pagehelper-spring-boot-autoconfigure/src/main/java/com/github/pagehelper/autoconfigure/PageHelperStandardProperties.java)

#### 4). 分页插件横幅banner设置

为了避免多次配置分页插件导致的错误，配置分页插件后，启动时会输出 banner。

```
DEBUG [main] -

,------.                           ,--.  ,--.         ,--.
|  .--. '  ,--,--.  ,---.   ,---.  |  '--'  |  ,---.  |  |  ,---.   ,---.  ,--.--.
|  '--' | ' ,-.  | | .-. | | .-. : |  .--.  | | .-. : |  | | .-. | | .-. : |  .--'
|  | --'  \ '-'  | ' '-' ' \   --. |  |  |  | \   --. |  | | '-' ' \   --. |  |
`--'       `--`--' .`-  /   `----' `--'  `--'  `----' `--' |  |-'   `----' `--'
`---'                                   `--'                        is intercepting.
```

如果在项目启动时输出了多次 banner，就是配置了多次分页插件，根据日志输出的位置排查系统通过哪些方式配置了分页插件。

如果不想在启动时输出 banner，可以通过系统变量或环境变量关闭。

- 系统变量：`-Dpagehelper.banner=false`
- 环境变量：`PAGEHELPER_BANNER=false`

#### 5). 分页插件参数介绍

分页插件提供了多个可选参数，这些参数使用时，按照上面配置方式中的示例配置即可。

**分页插件可选参数如下：**

1. `debug`: 调试参数，默认 `false` 关闭，设置为 `true` 启用后，可以排查系统中存在的**不安全调用**，[#查看如何安全调用](#3-pagehelper-安全调用)。
   通过 `PageHelper.startPage` 等静态方法调用设置分页参数时，会记录当前执行的方法堆栈信息，当执行 MyBatis 的查询方法时，会使用设置好的分页参数，
   此时会输出设置时的方法堆栈，通过查看堆栈，如果和当前执行的方法不一致，那么堆栈中对应的调用就是**不安全调用**，需要根据 [#安全调用](#3-pagehelper-安全调用) 中的方式调整。输出的堆栈示例如下：
   ```
   00:19:08.915 [main] DEBUG c.github.pagehelper.PageInterceptor - java.lang.Exception: 设置分页参数时的堆栈信息
       at com.github.pagehelper.util.StackTraceUtil.current(StackTraceUtil.java:12)
       at com.github.pagehelper.Page.<init>(Page.java:111)
       at com.github.pagehelper.Page.<init>(Page.java:126)
       at com.github.pagehelper.page.PageMethod.startPage(PageMethod.java:139)
       at com.github.pagehelper.page.PageMethod.startPage(PageMethod.java:113)
       at com.github.pagehelper.page.PageMethod.startPage(PageMethod.java:102)
       at com.github.pagehelper.test.basic.PageHelperTest.testNamespaceWithStartPage(PageHelperTest.java:118)
       ...省略

   00:19:09.069 [main] DEBUG c.g.pagehelper.mapper.UserMapper - Cache Hit Ratio [com.github.pagehelper.mapper.UserMapper]: 0.0
   00:19:09.077 [main] DEBUG o.a.i.t.jdbc.JdbcTransaction - Opening JDBC Connection
   00:19:09.078 [main] DEBUG o.a.i.t.jdbc.JdbcTransaction - Setting autocommit to false on JDBC Connection [org.hsqldb.jdbc.JDBCConnection@6da21078]
   00:19:09.087 [main] DEBUG c.g.p.m.UserMapper.selectAll_COUNT - ==>  Preparing: SELECT count(1) FROM user
   00:19:09.121 [main] DEBUG c.g.p.m.UserMapper.selectAll_COUNT - ==> Parameters:
   00:19:09.131 [main] TRACE c.g.p.m.UserMapper.selectAll_COUNT - <==    Columns: C1
   00:19:09.131 [main] TRACE c.g.p.m.UserMapper.selectAll_COUNT - <==        Row: 183
   00:19:09.147 [main] DEBUG c.g.p.m.UserMapper.selectAll_COUNT - <==      Total: 1
   ```

2. `dialect`：默认情况下会使用 PageHelper 方式进行分页，如果想要实现自己的分页逻辑，可以实现 `Dialect`(`com.github.pagehelper.Dialect`)
   接口，然后配置该属性为实现类的全限定名称。

3. `countSuffix`：根据查询创建或者查找对应的 count 查询时，追加的 msId 后缀，默认 `_COUNT`。

4. `countMsIdGen`（5.3.2+）：count 方法的 msId 生成方式，默认是 查询的 msId +
   countSuffix，想要自己定义时，可以实现 `com.github.pagehelper.CountMsIdGen` 接口，将该参数配置为实现的全限定类名即可。
   **一个常见的用途：** 在有Example查询的情况，`selectByExample` 可以使用对应的 `selectCountByExample` 方法进行 count 查询。

5. `msCountCache`：自动创建查询的 count 查询方法时，创建的 count `MappedStatement` 会进行缓存，默认会优先查找 `com.google.common.cache.Cache`
   的实现，如果项目没有 guava 依赖就会使用 mybatis 内置的 `CacheBuilder` 创建。想要对缓存进行细粒度的配置请参考源码: `com.github.pagehelper.cache.CacheFactory`
   ，两种默认方案提供了多个属性进行配置，也可以按照这里要求自己扩展实现。

**下面几个参数都是针对默认 dialect 情况下的参数。使用自定义 dialect 实现时，下面的参数没有任何作用。**

1. `helperDialect`：分页插件会自动检测当前的数据库链接，自动选择合适的分页方式。
   你可以配置`helperDialect`属性来指定分页插件使用哪种方言。配置时，可以使用下面的缩写值：
   `oracle`,`mysql`,`mariadb`,`sqlite`,`hsqldb`,`postgresql`,`db2`,`sqlserver`,`informix`,`h2`,`sqlserver2012`,`derby`
   （完整内容看 [PageAutoDialect](src/main/java/com/github/pagehelper/page/PageAutoDialect.java)）
   <b>特别注意：</b>使用 SqlServer2012 数据库时，需要手动指定为 `sqlserver2012`，否则会使用 SqlServer2005 的方式进行分页，还可以设置 `useSqlserver2012=true`
   将2012改为sqlserver的默认方式。
   你也可以实现 `AbstractHelperDialect`，然后配置该属性为实现类的全限定名称即可使用自定义的实现方法。

2. `dialectAlias`：允许配置自定义实现的 别名，可以用于根据 JDBCURL 自动获取对应实现，允许通过此种方式覆盖已有的实现，配置示例如（多个时分号隔开）：
   ```xml
   <property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
   ```
   当你使用的 jdbcurl 不在 [PageAutoDialect](src/main/java/com/github/pagehelper/page/PageAutoDialect.java)
   默认提供范围时，可以通过改参数实现自动识别。

3. `useSqlserver2012`(sqlserver)：使用 SqlServer2012 数据库时，需要手动指定为 `sqlserver2012`，否则会使用 SqlServer2005
   的方式进行分页，还可以设置 `useSqlserver2012=true`将2012改为sqlserver的默认方式。

4. `defaultCount`：用于控制默认不带 count 查询的方法中，是否执行 count 查询，默认 `true` 会执行 count 查询，这是一个全局生效的参数，多数据源时也是统一的行为。

5. `countColumn`：用于配置自动 count 查询时的查询列，默认值`0`，也就是 `count(0)`，`Page` 对象也新增了 `countColumn` 参数，可以针对具体查询进行配置。

6. `offsetAsPageNum`：默认值为 `false`，该参数对使用 `RowBounds` 作为分页参数时有效。
   当该参数设置为 `true` 时，会将 `RowBounds` 中的 `offset` 参数当成 `pageNum` 使用，可以用页码和页面大小两个参数进行分页。

7. `rowBoundsWithCount`：默认值为`false`，该参数对使用 `RowBounds` 作为分页参数时有效。
   当该参数设置为`true`时，使用 `RowBounds` 分页会进行 count 查询。

8. `pageSizeZero`：默认值为 `false`，当该参数设置为 `true` 时，如果 `pageSize=0` 或者 `RowBounds.limit = 0`
   就会查询出全部的结果（相当于没有执行分页查询，但是返回结果仍然是 `Page` 类型）。

9. `reasonable`：分页合理化参数，默认值为`false`。当该参数设置为 `true` 时，`pageNum<=0` 时会查询第一页，
   `pageNum>pages`（超过总数时），会查询最后一页。默认`false` 时，直接根据参数进行查询。

10. `params`：为了支持`startPage(Object params)`方法，增加了该参数来配置参数映射，用于从对象中根据属性名取值，
    可以配置 `pageNum,pageSize,count,pageSizeZero,reasonable`，不配置映射的用默认值，
    默认值为`pageNum=pageNum;pageSize=pageSize;count=countSql;reasonable=reasonable;pageSizeZero=pageSizeZero`。

11. `supportMethodsArguments`：支持通过 Mapper 接口参数来传递分页参数，默认值`false`，分页插件会从查询方法的参数值中，自动根据上面 `params`
    配置的字段中取值，查找到合适的值时就会自动分页。
    使用方法可以参考测试代码中的 `com.github.pagehelper.test.basic` 包下的 `ArgumentsMapTest` 和 `ArgumentsObjTest`。

12. `autoRuntimeDialect`：默认值为 `false`。设置为 `true` 时，允许在运行时根据多数据源自动识别对应方言的分页
    （不支持自动选择`sqlserver2012`，只能使用`sqlserver`），用法和注意事项参考下面的**场景五**。

13. `closeConn`：默认值为 `true`。当使用运行时动态数据源或没有设置 `helperDialect` 属性自动获取数据库类型时，会自动获取一个数据库连接，
    通过该属性来设置是否关闭获取的这个连接，默认`true`关闭，设置为 `false` 后，不会关闭获取的连接，这个参数的设置要根据自己选择的数据源来决定。

14. `aggregateFunctions`(5.1.5+)：默认为所有常见数据库的聚合函数，允许手动添加聚合函数（影响行数），所有以聚合函数开头的函数，在进行 count 转换时，会套一层。其他函数和列会被替换为 count(0)
    ，其中count列可以自己配置。

15. `replaceSql`(sqlserver): 可选值为 `regex` 和 `simple`，默认值空时采用 `regex`
    方式，也可以自己实现 `com.github.pagehelper.dialect.ReplaceSql` 接口。

16. `sqlCacheClass`(sqlserver): 针对 sqlserver 生成的 count 和 page sql 进行缓存，缓存使用的 `com.github.pagehelper.cache.CacheFactory`
    ，可选的参数和前面的 `msCountCache` 一样。

17. `autoDialectClass`：增加 `AutoDialect` 接口用于自动获取数据库类型，可以通过 `autoDialectClass`
    配置为自己的实现类，默认使用 `DataSourceNegotiationAutoDialect`，优先根据连接池获取。
    默认实现中，增加针对 `hikari,druid,tomcat-jdbc,c3p0,dbcp` 类型数据库连接池的特殊处理，直接从配置获取jdbcUrl，当使用其他类型数据源时，仍然使用旧的方式获取连接在读取jdbcUrl。
    想要使用和旧版本完全相同方式时，可以配置 `autoDialectClass=old`。当数据库连接池类型非常明确时，建议配置为具体值，例如使用 hikari 时，配置 `autoDialectClass=hikari`
    ，使用其他连接池时，配置为自己的实现类。

18. `boundSqlInterceptors`：增加分页插件的 `BoundSqlInterceptor` 拦截器，可以在3个阶段对 SQL 进行处理或者简单读取，
    增加参数 `boundSqlInterceptors`，可以配置多个实现 `BoundSqlInterceptor` 接口的实现类名，
    使用英文逗号隔开。PageHelper调用时，也可以通过类似
    `PageHelper.startPage(x,x).boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor)`针对本次分页进行设置。

19. `keepOrderBy`：转换count查询时保留查询的 order by 排序。除全局配置外，可以针对单次操作进行设置。

20. `keepSubSelectOrderBy`：转换count查询时保留子查询的 order by 排序。可以避免给所有子查询添加 `/*keep orderby*/`，除全局配置外，可以针对单次操作进行设置。

21. `asyncCount`：默认值为 `false`。设置为 `true` 时，count 查询会异步执行，可以提高分页查询的整体效率。
    除全局配置外，也可以针对单次操作进行设置。

22. `asyncCountParallelism`：异步 count 查询的线程池大小，默认值为 `Runtime.getRuntime().availableProcessors() * 2`。
    仅在 `asyncCount=true` 时有效。

23. `countSqlParser`：配置自定义的 count SQL 解析器，需要实现 `com.github.pagehelper.parser.CountSqlParser` 接口，
    配置为实现类的全限定名称。默认使用 `DefaultCountSqlParser`。用于替代已移除的 `sqlParser` 参数中 count 相关的功能。

24. `orderBySqlParser`：配置自定义的 ORDER BY SQL 解析器，需要实现 `com.github.pagehelper.parser.OrderBySqlParser` 接口，
    配置为实现类的全限定名称。默认使用 `DefaultOrderBySqlParser`。

25. `sqlServerSqlParser`：配置自定义的 SqlServer 分页 SQL 解析器，需要实现 `com.github.pagehelper.parser.SqlServerSqlParser` 接口，
    配置为实现类的全限定名称。默认使用 `DefaultSqlServerSqlParser`。

> ~~`sqlParser`：配置 JSqlParser 解析器，注意是 `com.github.pagehelper.JSqlParser` 接口，用于支持 sqlserver
> 等需要额外配置的情况。**该参数已在 6.1.0 中移除**，相关功能已拆分为 `countSqlParser`、`orderBySqlParser` 和 `sqlServerSqlParser` 三个独立参数。~~

**重要提示：**

当 `offsetAsPageNum=false` 的时候，由于 `PageNum` 问题，`RowBounds`查询的时候 `reasonable` 会强制为 `false`。使用 `PageHelper.startPage`
方法不受影响。

#### 6. 如何选择配置这些参数

单独看每个参数的说明可能是一件让人不爽的事情，这里列举一些可能会用到某些参数的情况。

##### 场景一

如果你仍然在用类似ibatis式的命名空间调用方式，你也许会用到`rowBoundsWithCount`，
分页插件对`RowBounds`支持和 MyBatis 默认的方式是一致，默认情况下不会进行 count 查询，如果你想在分页查询时进行 count 查询，
以及使用更强大的 `PageInfo` 类，你需要设置该参数为 `true`。

**注：** `PageRowBounds` 想要查询总数也需要配置该属性为 `true`。

##### 场景二

如果你仍然在用类似ibatis式的命名空间调用方式，你觉得 `RowBounds` 中的两个参数 `offset,limit` 不如 `pageNum,pageSize` 容易理解，
你可以使用 `offsetAsPageNum` 参数，将该参数设置为 `true` 后，`offset`会当成 `pageNum` 使用，`limit` 和 `pageSize` 含义相同。

##### 场景三

如果觉得某个地方使用分页后，你仍然想通过控制参数查询全部的结果，你可以配置 `pageSizeZero` 为 `true`，
配置后，当 `pageSize=0` 或者 `RowBounds.limit = 0` 就会查询出全部的结果。

##### 场景四

如果你分页插件使用于类似分页查看列表式的数据，如新闻列表，软件列表，
你希望用户输入的页数不在合法范围（第一页到最后一页之外）时能够正确的响应到正确的结果页面，
那么你可以配置 `reasonable` 为 `true`，这时如果 `pageNum<=0` 会查询第一页，如果 `pageNum>总页数` 会查询最后一页。

##### 场景五

如果你在 Spring 中配置了动态数据源，并且连接不同类型的数据库，这时你可以配置 `autoRuntimeDialect` 为 `true`，这样在使用不同数据源时，会使用匹配的分页进行查询。
这种情况下，你还需要特别注意 `closeConn` 参数，由于获取数据源类型会获取一个数据库连接，所以需要通过这个参数来控制获取连接后，是否关闭该连接。
默认为 `true`，有些数据库连接关闭后就没法进行后续的数据库操作。而有些数据库连接不关闭就会很快由于连接数用完而导致数据库无响应。所以在使用该功能时，特别需要注意你使用的数据源是否需要关闭数据库连接。

当不使用动态数据源而只是自动获取 `helperDialect` 时，数据库连接只会获取一次，所以不需要担心占用的这一个连接是否会导致数据库出错，但是最好也根据数据源的特性选择是否关闭连接。

### 3. 如何在代码中使用

阅读前请注意看[重要提示](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)

分页插件支持以下几种调用方式：

```java
//第一种，RowBounds方式的调用
List<User> list = sqlSession.selectList("x.y.selectIf", null, new RowBounds(0, 10));

//第二种，Mapper接口方式的调用，推荐这种使用方式。
PageHelper.startPage(1, 10);
List<User> list = userMapper.selectIf(1);

//第三种，Mapper接口方式的调用，推荐这种使用方式。
PageHelper.offsetPage(0, 10);
List<User> list = userMapper.selectIf(1);

//第四种，参数方法调用
//存在以下 Mapper 接口方法，你不需要在 xml 处理后两个参数
public interface CountryMapper {
    List<User> selectByPageNumSize(
            @Param("user") User user,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize);
}
//配置supportMethodsArguments=true
//在代码中直接调用：
List<User> list = userMapper.selectByPageNumSize(user, 1, 10);

//第五种，参数对象
//如果 pageNum 和 pageSize 存在于 User 对象中，只要参数有值，也会被分页
//有如下 User 对象
public class User {
    //其他fields
    //下面两个参数名和 params 配置的名字一致
    private Integer pageNum;
    private Integer pageSize;
}
//存在以下 Mapper 接口方法，你不需要在 xml 处理后两个参数
public interface CountryMapper {
    List<User> selectByPageNumSize(User user);
}
//当 user 中的 pageNum!= null && pageSize!= null 时，会自动分页
List<User> list = userMapper.selectByPageNumSize(user);

//第六种，ISelect 接口方式
//jdk6,7用法，创建接口
Page<User> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectGroupBy();
    }
});
//jdk8 lambda用法
Page<User> page = PageHelper.startPage(1, 10).doSelectPage(()-> userMapper.selectGroupBy());

//也可以直接返回PageInfo，注意doSelectPageInfo方法和doSelectPage
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectGroupBy();
    }
});
//对应的lambda用法
pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(() -> userMapper.selectGroupBy());

//count查询，返回一个查询语句的count数
long total = PageHelper.count(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectLike(user);
    }
});
//lambda
        total=PageHelper.count(()->userMapper.selectLike(user));
```

下面对最常用的方式进行详细介绍

#### 1). RowBounds方式的调用

```java
List<User> list=sqlSession.selectList("x.y.selectIf",null,new RowBounds(1,10));
```

使用这种调用方式时，你可以使用RowBounds参数进行分页，这种方式侵入性最小，我们可以看到，通过RowBounds方式调用只是使用了这个参数，并没有增加其他任何内容。

分页插件检测到使用了RowBounds参数时，就会对该查询进行<b>物理分页</b>。

关于这种方式的调用，有两个特殊的参数是针对 `RowBounds` 的，你可以参看上面的 **场景一** 和 **场景二**

<b>注：</b>不只有命名空间方式可以用RowBounds，使用接口的时候也可以增加RowBounds参数，例如：

```java
//这种情况下也会进行物理分页查询
List<User> selectAll(RowBounds rowBounds);
```

**注意：** 由于默认情况下的 `RowBounds` 无法获取查询总数，分页插件提供了一个继承自 `RowBounds` 的 `PageRowBounds`，这个对象中增加了 `total` 属性，执行分页查询后，可以从该属性得到查询总数。


#### 2). `PageHelper.startPage` 静态方法调用

除了 `PageHelper.startPage` 方法外，还提供了类似用法的 `PageHelper.offsetPage` 方法。

在你需要进行分页的 MyBatis 查询方法前调用 `PageHelper.startPage` 静态方法即可，紧跟在这个方法后的第一个**MyBatis 查询方法**会被进行分页。

##### 例一：

```java
//获取第1页，10条内容，默认查询总数count
PageHelper.startPage(1, 10);
//紧跟着的第一个select方法会被分页
List<User> list = userMapper.selectIf(1);
assertEquals(2, list.get(0).getId());
assertEquals(10, list.size());
//分页时，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>
assertEquals(182, ((Page) list).getTotal());
```

##### 例二：
```java
//request: url?pageNum=1&pageSize=10
//支持 ServletRequest,Map,POJO 对象，需要配合 params 参数
PageHelper.startPage(request);
//紧跟着的第一个select方法会被分页
List<User> list = userMapper.selectIf(1);

//后面的不会被分页，除非再次调用PageHelper.startPage
List<User> list2 = userMapper.selectIf(null);
//list1
assertEquals(2, list.get(0).getId());
assertEquals(10, list.size());
//分页时，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>，
//或者使用PageInfo类（下面的例子有介绍）
assertEquals(182, ((Page) list).getTotal());
//list2
assertEquals(1, list2.get(0).getId());
assertEquals(182, list2.size());
```

##### 例三，使用`PageInfo`的用法：

```java
//获取第1页，10条内容，默认查询总数count
PageHelper.startPage(1, 10);
List<User> list = userMapper.selectAll();
//用PageInfo对结果进行包装
PageInfo page = new PageInfo(list);
//测试PageInfo全部属性
//PageInfo包含了非常全面的分页属性
assertEquals(1, page.getPageNum());
assertEquals(10, page.getPageSize());
assertEquals(1, page.getStartRow());
assertEquals(10, page.getEndRow());
assertEquals(183, page.getTotal());
assertEquals(19, page.getPages());
assertEquals(1, page.getFirstPage());
assertEquals(8, page.getLastPage());
assertEquals(true, page.isFirstPage());
assertEquals(false, page.isLastPage());
assertEquals(false, page.isHasPreviousPage());
assertEquals(true, page.isHasNextPage());
```
#### 3). 使用参数方式
想要使用参数方式，需要配置 `supportMethodsArguments` 参数为 `true`，同时要配置 `params` 参数。
例如下面的配置：
```xml
<plugins>
    <!-- com.github.pagehelper为PageHelper类所在包名 -->
    <plugin interceptor="com.github.pagehelper.PageInterceptor">
        <!-- 使用下面的方式配置参数，后面会有所有的参数介绍 -->
        <property name="supportMethodsArguments" value="true"/>
        <property name="params" value="pageNum=pageNumKey;pageSize=pageSizeKey;"/>
	</plugin>
</plugins>
```
在 MyBatis 方法中：
```java
List<User> selectByPageNumSize(
@Param("user") User user,
@Param("pageNumKey") int pageNum,
@Param("pageSizeKey") int pageSize);
```
当调用这个方法时，由于同时发现了 `pageNumKey` 和 `pageSizeKey` 参数，这个方法就会被分页。params 提供的几个参数都可以这样使用。

除了上面这种方式外，如果 User 对象中包含这两个参数值，也可以有下面的方法：
```java
List<User> selectByPageNumSize(User user);
```
当从 User 中同时发现了 `pageNumKey` 和 `pageSizeKey` 参数，这个方法就会被分页。

注意：`pageNum` 和 `pageSize` 两个属性同时存在才会触发分页操作，在这个前提下，其他的分页参数才会生效。


#### 3). `PageHelper` 安全调用

##### 1. 使用 `RowBounds` 和 `PageRowBounds` 参数方式是极其安全的

##### 2. 使用参数方式是极其安全的

##### 3. 使用 ISelect 接口调用是极其安全的

ISelect 接口方式除了可以保证安全外，还特别实现了将查询转换为单纯的 count 查询方式，这个方法可以将任意的查询方法，变成一个 `select count(*)` 的查询方法。

##### 4. 什么时候会导致不安全的分页？

`PageHelper` 方法使用了静态的 `ThreadLocal` 参数，分页参数和线程是绑定的。

只要你可以保证在 `PageHelper` 方法调用后紧跟 MyBatis 查询方法，这就是安全的。因为 `PageHelper` 在 `finally` 代码段中自动清除了 `ThreadLocal` 存储的对象。

如果代码在进入 `Executor` 前发生异常，就会导致线程不可用，这属于人为的 Bug（例如接口方法和 XML 中的不匹配，导致找不到 `MappedStatement` 时），
这种情况由于线程不可用，也不会导致 `ThreadLocal` 参数被错误的使用。

但是如果你写出下面这样的代码，就是不安全的用法：
```java
PageHelper.startPage(1, 10);
List<User> list;
if(param1 != null){
    list = userMapper.selectIf(param1);
} else {
    list = new ArrayList<User>();
}
```
这种情况下由于 param1 存在 null 的情况，就会导致 PageHelper 生产了一个分页参数，但是没有被消费，这个参数就会一直保留在这个线程上。当这个线程再次被使用时，就可能导致不该分页的方法去消费这个分页参数，这就产生了莫名其妙的分页。

上面这个代码，应该写成下面这个样子：
```java
List<User> list;
if(param1 != null){
    PageHelper.startPage(1, 10);
    list = userMapper.selectIf(param1);
} else {
    list = new ArrayList<User>();
}
```
这种写法就能保证安全。

如果你对此不放心，你可以手动清理 `ThreadLocal` 存储的分页参数，可以像下面这样使用：
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
这么写很不好看，而且没有必要。

### 4. MyBatis 和 Spring 集成示例

如果和Spring集成不熟悉，可以参考下面两个

<b>只有基础的配置信息，没有任何现成的功能，作为新手入门搭建框架的基础</b>

- [集成 Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [集成 Spring 4.x](https://github.com/abel533/Mybatis-Spring)

这两个集成框架集成了 PageHelper 和 [通用 Mapper](https://github.com/abel533/Mapper)。

### 5. Spring Boot 集成示例

- [pagehelper-spring-boot-samples](https://github.com/pagehelper/pagehelper-spring-boot/tree/master/pagehelper-spring-boot-samples)
- https://github.com/abel533/MyBatis-Spring-Boot
