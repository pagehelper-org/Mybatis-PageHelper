## 更新日志


### 6.1.1 - 2025-06-20

- 新增对 SunDB 数据库的分页功能支持 **by wangsl**
- 新增对 Xugu（虚谷数据库）的适配支持 **by 吴启洋**
- 修复在复制 `countBoundSql` 时未正确复制 `additionalParameter` 的问题 **by yefeng**
- 修复 `offsetPage` 示例代码错误 **by S00ahKim**
- 修复 Jakarta/Javax ServletRequest 兼容性问题，支持 Spring Boot 3.x **by PING**
- 新增 `PageInfo.of()` 重载方法，支持手动指定查询记录总数返回分页信息 **by yesAnd**
- 升级 MyBatis 版本至 3.5.19（从 3.5.10）
- 升级 Guava 版本至 33.4.8-jre（从 32.0.0-jre）
- 升级 Logback Classic 版本至 1.2.13（从 1.2.11）
- 优化 Xugu JDBC 依赖配置，添加 `<scope>test</scope>`
- 修复英文文档中的拼写错误和链接引用 **by Coco Liliace**
- 补充相关注释信息，提高代码可读性 **by yesAnd**
- 解决依赖安全漏洞，更新相关组件版本
- Maven 发布从旧 OSSRH (oss.sonatype.org) 迁移到新的 Maven Central Portal (central.sonatype.com)
- pagehelper-sqlparser 同步升级至 6.1.1，新增 sqlparser4.9 和 sqlparser5.1 模块，支持 JSqlParser 4.9/5.0/5.1/5.2/5.3/5.4

#### 兼容性说明
- 本版本与之前版本保持向后兼容
- 新增数据库支持：SunDB、Xugu（虚谷数据库）
- 修复了 Spring Boot 3.x 兼容性问题，建议使用 Jakarta EE 的用户升级

#### 升级建议
- 所有用户建议升级，特别是使用 Spring Boot 3.x 或需要新数据库支持的用户
- 使用 SunDB 或虚谷数据库的用户可以直接使用分页功能
- 升级前请确保相关依赖版本兼容性

### 6.1.0 - 2023-12-16

- 发布6.1.0，PageHelper 提供 jsqlparser直接依赖都是中间接口，可以通过SPI替换默认实现
- 升级jsqlparser版本4.7，重新实现order by，分页，count查询
- 简化pom.xml配置，去掉shade内嵌jsqlparser方式，改为通过外部依赖选择不同的jsqlparser版本，允许自己SPI扩展
- jsqlparser解析不使用线程池，支持SPI扩展覆盖SqlParser实现
- SqlServer分页改为SqlServerSqlParser接口，添加参数 sqlServerSqlParser 覆盖默认值
- OrderByParser提取OrderBySqlParser接口，增加 orderBySqlParser 参数，可以覆盖默认实现
- OrderByParser静态方法改为普通方法，为后续改接口做准备
- jdk8+后不再需要JSqlParser接口，移除该接口，文档标记该参数（_该参数早期用于支持sqlserver特殊配置_）
  兼容jsqlparser4.7版本
- maven-compiler-plugin固定版本以去除警告，并增加构建稳定性 qxo
- gitignore .vscode for vscode ide qxo
- 修改bug https://github.com/pagehelper/Mybatis-PageHelper/issues/779 chenyuehui

为了兼容 jsqlparser 4.5 和 4.7，以及后续可能存在的其他版本，新建了一个 pagehelper-sqlparser 项目，目前提供了 4.5 和 4.7
两个实现，
使用时从 pagehelper 排除 jsqlparser，然后选择一个 jsqlparser 实现即可，当前版本默认使用的 4.7 版本的代码，
因此如果想换 4.5 的实现，可以按照下面方式进行配置：

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>6.1.1</version>
    <exclusions>
        <exclusion>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>sqlparser4.5</artifactId>
    <version>6.1.1</version>
</dependency>
```

SPI 替换默认值的优先级低于 `sqlServerSqlParser`,`orderBySqlParser`,`countSqlParser` 参数指定的实现，不指定时如果存在SPI实现，即可生效，
SPI 可以参考 pagehelper-sqlsource 模块代码。

JSqlParser 默认解析 SQL 会使用临时创建的 `Executors.newSingleThreadExecutor()`，这里通过 API 跳过了线程池：

```java
CCJSqlParser parser = CCJSqlParserUtil.newParser(statementReader);
parser.withSquareBracketQuotation(true);
return parser.Statement();
```

JSqlParser 使用线程池的目的是为了防止解析超时，因此如果你遇到过超时的情况，可以引入下面的依赖（通过SPI覆盖了默认实现，超时时间10秒）：

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>sqlparser-timeout</artifactId>
    <version>6.1.1</version>
</dependency>
```

### 6.0.0 - 2023-11-05

- 基于jdk8适配，6.0开始不支持jdk6和7，如果有需要可以使用5.x版本
- 增加异步count支持，全局配置`asyncCount`，默认`false`，单次设置：`PageHelper.startPage(1, 10).enableAsyncCount()`;
  异步使用独立连接（事务）查询，有增删改操作影响查询时不适合开启异步查询。closed #334
- JSqlParser默认开启 `parser.withSquareBracketQuotation(true)`，支持 SqlServer `[]`
- feat: 在`PageInfo`类中新增了用以进行数据对象转换的方法 ` <E> PageInfo<E> convert(Page.Function<T, E> function)` **by
  codeke**
- `CountSqlParser`改为接口，允许通过`countSqlParser`参数替换为自己的实现，支持 #772
- `dialectAlias`支持简化配置，例如`dm=oracle;oracle=oracle9i`，直接引用现在的缩写，不用写类全名
- `countColumn`添加注入检测，fixed #686
- 增加`PageParam`类，不内嵌对象（会影响使用），如果想用可以继承该对象，closed #562
- 所有异常信息改为英文提示
- 放开 `setLocalPage`，支持 #771
- 解决`sqlserver`带union sql解析时处理order by错误的问题，fixed #768
- 优化total逻辑，解决指定不分页查询，同时指定order by时无效的问题，fixed #641
- 修改 dialect 实例化逻辑，保证类完成配置后使用，fixed #742
- `dialectAliasMap`改为`LinkedHashMap`，可以按配置顺序进行匹配，fixed #758
- 行云数据库分页BUG修复 **by maimaitiyaer_bonc**

### 5.3.3 - 2023-06-03

- Ignoring unnecessarily generated surefire-report **by java-codehunger**
- 支持从URL中解析openGauss 数据库 对应的方言 **by saxisuer**
- 修复sql注入检验不正确问题 #716 **by uyong**
- 支持从url中解析人大金仓kingbase8对应的方言 **by univ**
- 添加支持cirrodata的分页 #705 **by sxh0570**

### 5.3.2 - 2022-09-18

- 使用文档更新，所有参数都包含在内，首页默认文档改为中文。
- Add support for kingbase. by **HanHuimin001**
- 增加 `debug` 参数，默认 `false`，为`true`时开启`debug`模式，开始 `debug` 模式后将记录调用堆栈 by **huyingqian**
- Add 支持count的sql支持hint语法 by **zhanliquan**
- 增加 `PageProperties` 接口，框架内部实例化的扩展类如果实现了这个接口，可以通过这个接口的方法获取分页插件配置。
- 增加 `CountMsIdGen` 接口，可以通过 `countMsIdGen`
  配置自定义实现类，该类用于生成查询对应COUNT查询的msId。默认实现还是使用`countSuffix`
  ，通过扩展可以实现如 `selectByExample` 映射到对应的 `selectCountByExample` 方法。
- 增加 `keepOrderBy` 和 `keepSubSelectOrderBy` 配置。
- 增加 `sqlParser` 配置，增加 `JSqlParser` 接口，解决 jsqlparser 和 jdk 兼容性导致无法额外配置的问题(**6.1.0 移除该参数
  **)。
- 测试使用 logback 日志框架，去掉log4j。
- 解决 `dialectKey` 为空导致NPE，fixed #656

关于以上参数的详细介绍，请查看 [如何使用分页插件](HowToUse.md)。

### 5.3.1 - 2022-06-14

- 处理 CVE-2022-28111 漏洞，限制 order by 参数，避免 SQL 注入
- Add support for as400. **by bluezealot**
- 优化分页结果包装类的泛型参数 **by 章福来**
- 规范PostgreSQL分页参数的顺序 **by outian**

### 5.3.0 - 2021-10-07

- 增加 `AutoDialect` 接口用于自动获取数据库类型，可以通过 `autoDialectClass` 配置为自己的实现类，默认使用 `DataSourceNegotiationAutoDialect`，优先根据连接池获取。
  默认实现中，增加针对 `hikari,druid,tomcat-jdbc,c3p0,dbcp` 类型数据库连接池的特殊处理，直接从配置获取jdbcUrl，当使用其他类型数据源时，仍然使用旧的方式获取连接在读取jdbcUrl。
  想要使用和旧版本完全相同方式时，可以配置 `autoDialectClass=old`。当数据库连接池类型非常明确时，建议配置为具体值，例如使用 hikari 时，配置 `autoDialectClass=hikari`
  ，使用其他连接池时，配置为自己的实现类。
- 支持运行时动态指定使用的 dialect 实现，例如 `PageHelper.startPage(1, 10).using("oracle");`
  或者 `PageHelper.startPage(2, 10).using("org.exmaple.CustomDialect");`
- `PageInfo` 增加空实例常量属性 `PageInfo.EMPTY` 以及内容判断 `boolean hasContent()`。
- 启动中增加 banner, 需要日志级别 debug，可以通过 `-Dpagehelper.banner=false` 或者环境变量 `PAGEHELPER_BANNER=false` 关闭
  ```
   DEBUG [main] -

   ,------.                           ,--.  ,--.         ,--.
   |  .--. '  ,--,--.  ,---.   ,---.  |  '--'  |  ,---.  |  |  ,---.   ,---.  ,--.--.
   |  '--' | ' ,-.  | | .-. | | .-. : |  .--.  | | .-. : |  | | .-. | | .-. : |  .--'
   |  | --'  \ '-'  | ' '-' ' \   --. |  |  |  | \   --. |  | | '-' ' \   --. |  |
   `--'       `--`--' .`-  /   `----' `--'  `--'  `----' `--' |  |-'   `----' `--'
   `---'                                   `--'                        is intercepting.
   ```
  增加 banner 的目的在于，如果你配置了多次分页插件，你会看到 banner 输出多次，你可以在 `PageInterceptor` 构造方法断点看看那些地方进行了实例化。
- 完善 Count 查询，当存在 having 时，不在优化查询列。查询列存在有别名的函数或者运算时也不优化查询列，避免 order by 或 having 中使用的别名不存在。
- 增加判断处理某些数据（如 TDEngine）查询 count 无结果时返回 null
- 添加 Firebird 数据库支持和 SqlServer2012 分页语法相同。
- 添加 impala 数据库自动识别。
- JSqlParser 升级为 4.2 版本。

> 距离上次更新3个月左右，这次更新直接让假期少了3天 :running: ，关了 GitHub 和 Gitee 上的 200 多个issue，不一定所有问题都得到了处理，如果你还有疑问，可以继续提 issue，下个大版本会考虑直接 6.0，计划全部升级到 java 8，功能保持不变。

### 5.2.1 - 2021-06-20

- 升级依赖 jsqlparser 4.0, mybatis 3.5.7
- 自动识别以下数据库：
    - 虚谷数据库 xugu #599
    - 神通数据库 oscar by **ranqing**
    - 瀚高数据库 highgo by **ashaiqing**
- BoundSqlInterceptorChain拦截器index参数bug, fixed #587
- fixed #558
- 添加 PostgreSQL 方言 by **liym@home**
- fixed #604, 解决total丢失的问题
- 规范注释, fixed #547

### 5.2.0 - 2020-07-26

- jsqlparser升级到3.2版本，sql解析更好，对sqlserver支持更好。
- 修改 sqlserver 方式中的替换正则，现在允许 with( nolock ) 括号中存在空格。
- 解决 reasonable 和 pageSizeZero,以及 offset 用法中的bug，现在的含义和结果更一致。
- 分页 SQL 拼接过程中增加换行符，避免原始 SQL 中存在注释导致分页部分无效。
- Oracle 和 Db2 中的行号 ROW_ID 别名改为 PAGEHELPER_ROW_ID，避免和常用名称冲突。
- 解决单个参数ProviderSql使用其他拦截器时的特殊问题（支持 mybatis 3.4.0+）[by 罗震宇](https://github.com/luozhenyu)
- 支持自动识别 clickhouse，使用 MySQL 方式进行分页。
- 将 startRow, endRow 类型从 int 改为 long。
- Page 增加 `public <T> PageInfo<T> toPageInfo(Function<E, T> function)` 方法，用于转换查询结果中的数据。
- 参考 pr#476 提供 `·`Oracle9iDialect`，这也是曾经用过的一种分页方式，可以自己测试选择合适的分页方式。

   目前提供的两种 Oracle 分页如下：
   ```sql
   -- OracleDialect 外层控制范围
   WHERE ROW_ID <= ? AND ROW_ID > ?
   -- Oracle9iDialect 内外分别控制范围
   TMP_PAGE WHERE ROWNUM <= ? ) WHERE ROW_ID > ?
   ```
- 增加分页插件的 `BoundSqlInterceptor` 拦截器，可以在3个阶段对 SQL 进行处理或者简单读取，
   增加参数 `boundSqlInterceptors`，可以配置多个实现 `BoundSqlInterceptor` 接口的实现类名，
   使用英文逗号隔开。PageHelper调用时，也可以通过类似
   `PageHelper.startPage(x,x).boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor)`针对本次分页进行设置。


本次更新最大的变化是增加了 `BoundSqlInterceptor`，通过该接口可以在运行时拦截分页处理的 SQL（BoundSQL对象）：
```java
/**
 * BoundSql 处理器
 */
public interface BoundSqlInterceptor {
    /**
     * boundsql 处理
     *
     * @param type     类型
     * @param boundSql 当前类型的 boundSql
     * @param cacheKey 缓存 key
     * @param chain    处理器链，通过 chain.doBoundSql 方法继续执行后续方法，也可以直接返回 boundSql 终止后续方法的执行
     * @return 允许修改 boundSql 并返回修改后的
     */
    BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain);

    enum Type {
        /**
         * 原始SQL，分页插件执行前，先执行这个类型
         */
        ORIGINAL,
        /**
         * count SQL，第二个执行这里
         */
        COUNT_SQL,
        /**
         * 分页 SQL，最后执行这里
         */
        PAGE_SQL
    }

    /**
     * 处理器链，可以控制是否继续执行
     */
    interface Chain {
        Chain DO_NOTHING = new Chain() {
            @Override
            public BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey) {
                return boundSql;
            }
        };

        BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey);
    }
}
```

接口中包含了 boundSql 接口方法，还有 Type 枚举，和 Chain 接口的定义，自己实现的时候不需要考虑 Chain。

通过 `boundSqlInterceptors` 参数配置拦截器，执行时存在下面三种情况：

1. 不管当前执行的 SQL 是否会分页，都会执行 `Type.ORIGINAL` 类型的拦截器方法，配置后一定会执行。

2. 调用分页方法时，拦截器会继续执行 `Type.COUNT_SQL` 类型的拦截器方法，这个方法只有执行分页并且指定要进行 count 查询时才会执行。

3. 调用分页方法时，如果 count > 0，就会执行 `Type.PAGE_SQL` 类型的拦截器方法，这个方法只有执行分页时才会执行。

> 通过 `PageHelper.startPage(1, Integer.MAX_VALUE, false).boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor)`
> 这种指定的参数时，也能起到不进行分页和count查询，但是可以执行 `Type.ORIGINAL` 类型的拦截器方法。

当前拦截器在整个分页执行过程中，会执行3次，对应 Type 枚举的 3 个类型，执行顺序也一致。

如果想获取分页 SQL 执行前的，只需要关注 Type.ORIGINAL，另外两种就是 count 执行前和分页执行前（count=0时分页方法不执行，这里也不会执行）。

以测试代码为例：

```java
public class TestBoundSqlInterceptor implements BoundSqlInterceptor {
    public static final String COMMENT = "\n /* TestBoundSqlInterceptor */\n";

    @Override
    public BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain) {
        if (type == Type.ORIGINAL) {
            String sql = boundSql.getSql();
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("sql", sql + COMMENT);
        }
        return chain.doBoundSql(type, boundSql, cacheKey);
    }

}
```
上面这段代码在 sql 执行前先修改原始 SQL，只是在最后增加了一段注释，不影响 SQL 执行，通过下面的方式配置：

```xml
<plugin interceptor="com.github.pagehelper.PageInterceptor">
    <!-- 支持通过Mapper接口参数来传递分页参数 -->
    <property name="helperDialect" value="mysql"/>
    <property name="boundSqlInterceptors"
              value="com.github.pagehelper.test.basic.provider.TestBoundSqlInterceptor,com.github.pagehelper.test.basic.provider.TestBoundSqlInterceptor"/>
</plugin>
```
这里为了说明该参数值可以是多个，因此重复配置了一次，也就是上面的拦截器会执行两次。

这样配置后，上面的 SQL 在分页执行的时候就会修改 SQL。

除了这种配置方式外，还支持 PageHelper.startPage 时临时指定，这种方式会把拦截器放到链头先执行，因此可以控制后续的是否执行，也可以在后续所有执行外，做最后处理再返回。

示例：

```java
PageHelper.startPage(1, 10).boundSqlInterceptor(new BoundSqlInterceptor() {
    @Override
    public BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain) {
        System.out.println("before: " + boundSql.getSql());
        BoundSql doBoundSql = chain.doBoundSql(type, boundSql, cacheKey);
        System.out.println("after: " + doBoundSql.getSql());
        if (type == Type.ORIGINAL) {
            Assert.assertTrue(doBoundSql.getSql().contains(TestBoundSqlInterceptor.COMMENT));
        }
        return doBoundSql;
    }
});
```




### 5.1.11 - 2019-11-26

- 增加神通数据库的支持 **wangss**
- Add support for HerdDB - support HerdDB, mostly like MySQL - auto detect HerdDB **Enrico Olivelli**
- fix some typos and grammar issues **LHearen**

### 5.1.10 - 2019-06-05

在 *5.1.0 - 2017-08-28* 版本中，增加 `ReplaceSql` 接口用于处理 sqlServer 的 `with(nolock)` 问题，增加了针对性的 `replaceSql` 参数，
可选值为 `simple` 和 `regex`，或者是实现了ReplaceSql接口的全限定类名。默认值为 `simple`，仍然使用原来的方式处理，
新的 `regex` 会将如 `table with(nolock)` 处理为 `table_PAGEWITHNOLOCK`。

本次更新仅仅是把默认值从 `simple` 改为了 `regex`，使用 `regex` 方式几乎能 100% 解决 sqlServer 的分页问题。

下面是两个 issue 中的示例。

#### 示例 SQL [#76](https://github.com/pagehelper/pagehelper-spring-boot/issues/76)

原始 SQL：
```sql
SELECT *
FROM
forum_post_info a with(nolock)
LEFT JOIN forum_carcase_tags as b with(nolock) on a.id = b.carcase_id where b.tag_id = 127
```
转换的 Count SQL：
```sql
SELECT COUNT(0)
FROM forum_post_info a WITH (NOLOCK)
	LEFT JOIN forum_carcase_tags b WITH (NOLOCK) ON a.id = b.carcase_id
WHERE b.tag_id = 127
```
转换的分页 SQL：
```sql
SELECT TOP 10 *
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY RAND()) AS PAGE_ROW_NUMBER, *
	FROM (
		SELECT *
		FROM forum_post_info a WITH (NOLOCK)
			LEFT JOIN forum_carcase_tags b WITH (NOLOCK) ON a.id = b.carcase_id
		WHERE b.tag_id = 127
	) PAGE_TABLE_ALIAS
) PAGE_TABLE_ALIAS
WHERE PAGE_ROW_NUMBER > 1
ORDER BY PAGE_ROW_NUMBER
```

#### 示例 SQL [#398](https://github.com/pagehelper/Mybatis-PageHelper/issues/398)

原始 SQL：
```sql
Select AUS.ScheduleID, AUS.SystemID, AUS.ClinicID, AUS.DoctorID, AUS.ScheduleDate,
	AUS.StartTime, AUS.EndTime, AUS.Status, AUS.BookBy, AUS.Note, AUS.Remark, AUS.SourceType, CM.CompanyName,
	AU.UserName As DoctorName, AU.UserNumber As DoctorNumber, CC.CodeDesc As ClinicName, CD.Lat, CD.Lng,
	CD.ContactTel, CD.Address, CR.ConsultationStatusID, CR.RegisterStatus,A1.CodeDesc as AreaLevel1, A2.CodeDesc as AreaLevel2
	From ACM_User_Schedule AUS with(nolock)
	Left Join Client_Register CR with(nolock) On AUS.BookBy=CR.ClientID And CR.SourceType='F' And AUS.ClientRegisterNum=CR.ClientRegisterNum
	Inner Join ACM_User AU with(nolock) On AU.UserID = AUS.DoctorID
	Inner Join Code_Clinic CC with(nolock) On AUS.ClinicID=CC.CodeID
	Inner Join Clinic_Detail CD with(nolock) On CC.CodeID = CD.ClinicID
	Inner Join Code_Area A1 with(nolock) On CD.AreaLevel1ID=A1.CodeID
	Inner Join Code_Area A2 with(nolock) On CD.AreaLevel2ID=A2.CodeID
	Inner Join Company_Master CM with(nolock) On CC.SystemID = CM.SystemID
	Where BookBy=1
```
转换的 Count SQL：
```sql
SELECT COUNT(0)
FROM ACM_User_Schedule AUS WITH (NOLOCK)
	LEFT JOIN Client_Register CR WITH (NOLOCK)
	ON AUS.BookBy = CR.ClientID
		AND CR.SourceType = 'F'
		AND AUS.ClientRegisterNum = CR.ClientRegisterNum
	INNER JOIN ACM_User AU WITH (NOLOCK) ON AU.UserID = AUS.DoctorID
	INNER JOIN Code_Clinic CC WITH (NOLOCK) ON AUS.ClinicID = CC.CodeID
	INNER JOIN Clinic_Detail CD WITH (NOLOCK) ON CC.CodeID = CD.ClinicID
	INNER JOIN Code_Area A1 WITH (NOLOCK) ON CD.AreaLevel1ID = A1.CodeID
	INNER JOIN Code_Area A2 WITH (NOLOCK) ON CD.AreaLevel2ID = A2.CodeID
	INNER JOIN Company_Master CM WITH (NOLOCK) ON CC.SystemID = CM.SystemID
WHERE BookBy = 1
```
转换的分页 SQL：
```sql
SELECT TOP 10 ScheduleID, SystemID, ClinicID, DoctorID, ScheduleDate
	, StartTime, EndTime, Status, BookBy, Note
	, Remark, SourceType, CompanyName, DoctorName, DoctorNumber
	, ClinicName, Lat, Lng, ContactTel, Address
	, ConsultationStatusID, RegisterStatus, AreaLevel1, AreaLevel2
FROM (
	SELECT ROW_NUMBER() OVER (ORDER BY RAND()) AS PAGE_ROW_NUMBER, ScheduleID, SystemID, ClinicID, DoctorID
		, ScheduleDate, StartTime, EndTime, Status, BookBy
		, Note, Remark, SourceType, CompanyName, DoctorName
		, DoctorNumber, ClinicName, Lat, Lng, ContactTel
		, Address, ConsultationStatusID, RegisterStatus, AreaLevel1, AreaLevel2
	FROM (
		SELECT AUS.ScheduleID, AUS.SystemID, AUS.ClinicID, AUS.DoctorID, AUS.ScheduleDate
			, AUS.StartTime, AUS.EndTime, AUS.Status, AUS.BookBy, AUS.Note
			, AUS.Remark, AUS.SourceType, CM.CompanyName, AU.UserName AS DoctorName, AU.UserNumber AS DoctorNumber
			, CC.CodeDesc AS ClinicName, CD.Lat, CD.Lng, CD.ContactTel, CD.Address
			, CR.ConsultationStatusID, CR.RegisterStatus, A1.CodeDesc AS AreaLevel1, A2.CodeDesc AS AreaLevel2
		FROM ACM_User_Schedule AUS WITH (NOLOCK)
			LEFT JOIN Client_Register CR WITH (NOLOCK)
			ON AUS.BookBy = CR.ClientID
				AND CR.SourceType = 'F'
				AND AUS.ClientRegisterNum = CR.ClientRegisterNum
			INNER JOIN ACM_User AU WITH (NOLOCK) ON AU.UserID = AUS.DoctorID
			INNER JOIN Code_Clinic CC WITH (NOLOCK) ON AUS.ClinicID = CC.CodeID
			INNER JOIN Clinic_Detail CD WITH (NOLOCK) ON CC.CodeID = CD.ClinicID
			INNER JOIN Code_Area A1 WITH (NOLOCK) ON CD.AreaLevel1ID = A1.CodeID
			INNER JOIN Code_Area A2 WITH (NOLOCK) ON CD.AreaLevel2ID = A2.CodeID
			INNER JOIN Company_Master CM WITH (NOLOCK) ON CC.SystemID = CM.SystemID
		WHERE BookBy = 1
	) PAGE_TABLE_ALIAS
) PAGE_TABLE_ALIAS
WHERE PAGE_ROW_NUMBER > 1
ORDER BY PAGE_ROW_NUMBER
```

SQL 经过 https://tool.oschina.net/codeformat/sql 格式化。


### 5.1.9 - 2019-05-29

- 升级 jsqlparser 为 2.0，升级 mybatis 为 3.5.1，解决兼容性问题。
- 完善分页逻辑判断，fixed #389
- 解决 MetaObject 版本兼容性问题 fixed #349
- 处理 order by 解析失败时输出警告日志，不在抛出异常
- 解决三处可能会导致countColumn失效的问题 fixed #325
- 解决 BIT_ 少的逗号 fixed #341
- 处理文档中的失效链接 isea533
- 文档示例错误，fixed #366
- fixed #373 NPE 问题

### 5.1.8 - 2018-11-11

- 解决 sqlserver 中 with(nolock) 的问题([#pr10](https://gitee.com/free/Mybatis_PageHelper/pulls/10)) by [lvshuyan](https://gitee.com/lvshuyan)

### 5.1.7 - 2018-10-11

- 增加对阿里云PPAS数据库的支持，自动识别edb，fixed #281

### 5.1.6 - 2018-09-05

- 增加参数 useSqlserver2012，设置为 true 后，使用 sqlserver2012(Dialect) 作为 SqlServer 数据库的默认分页方式，这种情况在动态数据源时方便使用。默认使用的低版本(05,08)分页方式。
- 增加 IPage 接口，目前支持 mybatis 查询方法只有一个参数，并且参数实现 IPage 接口时，如果存在分页参数，就会自动进行分页查询。感谢 [moonfruit](https://github.com/moonfruit) 两年前的 issue。
- 解决 HashSet 并发问题 fixed #276
- 优化代码结构，精简拦截器代码

### 5.1.5 - 2018-09-02

- 优化代码，去掉没必要的校验(**by lenosp**)
- 解决 pageKey 多处理一次的小问题 #268
- 新增 gitee 提供的 javadoc 文档(https://apidoc.gitee.com/free/Mybatis_PageHelper)
- 解决默认反射不带缓存的问题 fixed #275
- 优化mysql ifnull函数导致分页性能问题 (**by miaogr**)（这个修改最终改成了下面的 `aggregateFunctions`）
- jsqlparser 升级为 1.2 版本，和 1.0 有不兼容的情况，已经解决。 fixed 273
- 去掉 PageInfo 中存在歧义的 g(s)etFirstPage 和 g(s)etLastPage 两个方法
- 抛出排序时解析失败的异常 fixed #257
- 解决 Spring `<bean>` 方式配置时，没有 `properties` 属性时的初始化问题 fixed #26
- 修复Oracle分页会漏查数据的问题 (**by muyun12**)
- 新增 `aggregateFunctions` 参数(`CountSqlParser`), 允许手动添加聚合函数（影响行数），所以以聚合函数开头的函数，在进行 count 转换时，会套一层。其他函数和列会被替换为 count(0),其中count列可以自己配置。

增加 `aggregateFunctions` 参数后，和原先最大的区别是，如果存在 `select ifnull(xxx, yy) from table ...`，原先的 count 查询是
`select count(0) from (select ifnull(xxx, yy) from table ...) temp_count`，现在会区别聚合函数，如果不是聚合函数，就会变成
`select count(0) from table ...`。

默认包含的聚合函数前缀如下：

```java
/**
 * 聚合函数，以下列函数开头的都认为是聚合函数
 */
private static final Set<String> AGGREGATE_FUNCTIONS = new HashSet<String>(Arrays.asList(
        ("APPROX_COUNT_DISTINCT," +
        "ARRAY_AGG," +
        "AVG," +
        "BIT_" +
        //"BIT_AND," +
        //"BIT_OR," +
        //"BIT_XOR," +
        "BOOL_," +
        //"BOOL_AND," +
        //"BOOL_OR," +
        "CHECKSUM_AGG," +
        "COLLECT," +
        "CORR," +
        //"CORR_," +
        //"CORRELATION," +
        "COUNT," +
        //"COUNT_BIG," +
        "COVAR," +
        //"COVAR_POP," +
        //"COVAR_SAMP," +
        //"COVARIANCE," +
        //"COVARIANCE_SAMP," +
        "CUME_DIST," +
        "DENSE_RANK," +
        "EVERY," +
        "FIRST," +
        "GROUP," +
        //"GROUP_CONCAT," +
        //"GROUP_ID," +
        //"GROUPING," +
        //"GROUPING," +
        //"GROUPING_ID," +
        "JSON_," +
        //"JSON_AGG," +
        //"JSON_ARRAYAGG," +
        //"JSON_OBJECT_AGG," +
        //"JSON_OBJECTAGG," +
        //"JSONB_AGG," +
        //"JSONB_OBJECT_AGG," +
        "LAST," +
        "LISTAGG," +
        "MAX," +
        "MEDIAN," +
        "MIN," +
        "PERCENT_," +
        //"PERCENT_RANK," +
        //"PERCENTILE_CONT," +
        //"PERCENTILE_DISC," +
        "RANK," +
        "REGR_," +
        "SELECTIVITY," +
        "STATS_," +
        //"STATS_BINOMIAL_TEST," +
        //"STATS_CROSSTAB," +
        //"STATS_F_TEST," +
        //"STATS_KS_TEST," +
        //"STATS_MODE," +
        //"STATS_MW_TEST," +
        //"STATS_ONE_WAY_ANOVA," +
        //"STATS_T_TEST_*," +
        //"STATS_WSR_TEST," +
        "STD," +
        //"STDDEV," +
        //"STDDEV_POP," +
        //"STDDEV_SAMP," +
        //"STDDEV_SAMP," +
        //"STDEV," +
        //"STDEVP," +
        "STRING_AGG," +
        "SUM," +
        "SYS_OP_ZONE_ID," +
        "SYS_XMLAGG," +
        "VAR," +
        //"VAR_POP," +
        //"VAR_SAMP," +
        //"VARIANCE," +
        //"VARIANCE_SAMP," +
        //"VARP," +
        "XMLAGG").split(",")));
```

### 5.1.4 - 2018-04-22

- 默认增加达梦数据库(dm)，可以自动根据 jdbcurl 使用Oracle方式进行分页。如果想换 SqlServer 可以参考 5.1.3 更新日志中的 `dialectAlias` 参数。

### 5.1.3 - 2018-04-07

- `Page` 的 `toString` 方法增加 `super.toString()`。最终输出形式如 `Page{属性}[集合]`。
- 增加 `defaultCount` 参数，用于控制默认不带 count 查询的方法中，是否执行 count 查询，默认 true 会执行 count 查询，这是一个全局生效的参数，多数据源时也是统一的行为。
- 增加 `dialectAlias` 参数，允许配置自定义实现的 别名，可以用于根据 JDBCURL 自动获取对应实现，允许通过此种方式覆盖已有的实现，配置示例如（多个时分号隔开）：
  ```xml
  <property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
  ```
- 增加 `PageSerializable`，简化版的 `PageInfo` 类，不需要那么多信息时，推荐使用或者参考这个类实现。

### 5.1.2 - 2017-09-18

- 解决单独使用 `PageHelper.orderBy` 方法时的问题 #110;

### 5.1.1 - 2017-08-30

- 此次更新解决的问题只和 SqlServer 2005,2008 有关
- 解决 `RegexWithNolockReplaceSql` 中正则 `w?` 错误的问题，应该是 `w+`。
- 解决 `SqlServerDialect` 中没有初始化默认 `SimpleWithNolockReplaceSql` 的错误。
- `SqlServerRowBoundsDialect` 增加对 `replaceSql` 参数的支持。

### 5.1.0 - 2017-08-28

- 增加 4.x 以前版本包含的排序功能，用法一致（PageHelper增加了几个排序相关的方法）。
- 分页 SQL 转换为预编译 SQL。
- 增加 `ReplaceSql` 接口用于处理 sqlServer 的 `with(nolock)` 问题，增加了针对性的 `replaceSql` 参数，可选值为 `simple` 和 `regex`，或者是实现了ReplaceSql接口的全限定类名。默认值为
`simple`，仍然使用原来的方式处理，新的 `regex` 会将如 `table with(nolock)` 处理为 `table_PAGEWITHNOLOCK`。
- `PageRowBounds` 增加 `count` 属性，可以控制是否进行 `count` 查询。

### 5.1.0-beta2 - 2017-08-23

- 增加 `ReplaceSql` 接口用于处理 sqlServer 的 `with(nolock)` 问题，增加了针对性的 `replaceSql` 参数，可选值为 `simple` 和 `regex`，或者是实现了ReplaceSql接口的全限定类名。默认值为
`simple`，仍然使用原来的方式处理，新的 `regex` 会将如 `table with(nolock)` 处理为 `table_PAGEWITHNOLOCK`。

### 5.1.0-beta - 2017-08-22

- 增加 4.x 以前版本包含的排序功能，用法一致（PageHelper增加了几个排序相关的方法）。
- 分页 SQL 转换为预编译 SQL。

### 5.0.4 - 2017-08-01

- 增加对 `Phoenix` 数据库的简单配置支持，配置 `helperDialect=phoenix` 即可，也可以自动识别 `Phoenix` 数据库的 jdbc url。
- count 查询的缓存 `msCountMap` key 改为 `String` 类型，key 为 count 查询的 `MappedStatement` 的 id。
- 增加 `countSuffix` count 查询后缀配置参数，该参数是针对 `PageInterceptor` 配置的，默认值为 `_COUNT`。
- 增加手写 count 查询支持，详情看下面介绍。

#### 增加手写 count 查询支持

增加 `countSuffix` count 查询后缀配置参数，该参数是针对 `PageInterceptor` 配置的，默认值为 `_COUNT`。

分页插件会优先通过当前查询的 msId + `countSuffix` 查找手写的分页查询。

如果存在就使用手写的 count 查询，如果不存在，仍然使用之前的方式自动创建 count 查询。

例如，如果存在下面两个查询：
```xml
<select id="selectLeftjoin" resultType="com.github.pagehelper.model.User">
    select a.id,b.name,a.py from user a
    left join user b on a.id = b.id
    order by a.id
</select>
<select id="selectLeftjoin_COUNT" resultType="Long">
    select count(distinct a.id) from user a
    left join user b on a.id = b.id
</select>
```
上面的 `countSuffix` 使用的默认值 `_COUNT`，分页插件会自动获取到 `selectLeftjoin_COUNT` 查询，这个查询需要自己保证结果数正确。

返回值的类型必须是`resultType="Long"`，入参使用的和 `selectLeftjoin` 查询相同的参数，所以在 SQL 中要按照 `selectLeftjoin` 的入参来使用。

因为 `selectLeftjoin_COUNT` 方法是自动调用的，所以不需要在接口提供相应的方法，如果需要单独调用，也可以提供。

上面方法执行输出的部分日志如下：
```
DEBUG [main] - ==>  Preparing: select count(distinct a.id) from user a left join user b on a.id = b.id
DEBUG [main] - ==> Parameters:
TRACE [main] - <==    Columns: C1
TRACE [main] - <==        Row: 183
DEBUG [main] - <==      Total: 1
DEBUG [main] - Cache Hit Ratio [com.github.pagehelper.mapper.CountryMapper]: 0.0
DEBUG [main] - ==>  Preparing: select a.id,b.name,a.py from user a left join user b on a.id = b.id order by a.id LIMIT 10
DEBUG [main] - ==> Parameters:
TRACE [main] - <==    Columns: ID, COUNTRYNAME, COUNTRYCODE
TRACE [main] - <==        Row: 1, Angola, AO
TRACE [main] - <==        Row: 2, Afghanistan, AF
TRACE [main] - <==        Row: 3, Albania, AL
```

### 5.0.3 -2017-06-20

- 解决`supportMethodsArguments`参数不起作用的问题，由于之前默认为`false`，不起作用后效果为`true`，建议升级到最新版本。

### 5.0.2 - 2017-05-30

- `Page<E>` 继承 `Closeable` 接口，在 JDK7+中可以使用 `try()`方式调用，自动调用`PageHelper.clearPage();`[#58](https://github.com/pagehelper/Mybatis-PageHelper/issues/58)。
- 解决：DB2分页时必须要指定子查询的别名,不然会发生异常 [#52](https://github.com/pagehelper/Mybatis-PageHelper/issues/52)
- 解决：分页取数据时，如果数据一条都没有返回, pageInfo.isIsLastPage(); 返回false [#50](https://github.com/pagehelper/Mybatis-PageHelper/issues/50)

### 5.0.1 - 2017-04-23

- 增加新的参数 `countColumn` 用于配置自动 count 查询时的查询列，默认值`0`，也就是 `count(0)`
- `Page` 对象也新增了 `countColumn` 参数，可以针对具体查询进行配置
- 针对文档显示问题进行修改，by liumian* [PR #30](https://github.com/pagehelper/Mybatis-PageHelper/pull/30)
- 解决 sqlserver2012 分页错误的问题 [42](https://github.com/pagehelper/Mybatis-PageHelper/issues/42)

### 5.0.0 - 2017-01-02

- 使用 [QueryInterceptor 规范](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java) 处理分页逻辑
- 新的分页插件拦截器为 `com.github.pagehelper.PageInterceptor`
- 新的 `PageHelper` 是一个特殊的 `Dialect` 实现类，以更友好的方式实现了以前的功能
- 新的分页插件仅有 `dialect` 一个参数，默认的 `dialect` 实现类为 `PageHelper`
- `PageHelper` 仍然支持以前提供的参数，在最新的使用文档中已经全部更新
- `PageHelper` 的 `helperDialect` 参数和以前的 `dialect` 功能一样，具体可以看文档的参数说明
- 增加了基于纯 `RowBounds` 和 `PageRowBounds` 的分页实现，在 `com.github.pagehelper.dialect.rowbounds` 包中，这是用于作为 `dialect` 参数示例的实现，后面会补充更详细的文档
- 去掉了不适合出现在分页插件中的 orderby 功能，以后会提供单独的排序插件
- 去掉了 `PageHelper` 中不常用的方法
- 新的文档，更新历来更新日志中提到的重要内容，提供英文版本文档
- 解决 bug [#149](http://git.oschina.net/free/Mybatis_PageHelper/issues/149)
- 将 Db2RowDialect 改为 Db2RowBoundsDialect
- 所有分页插件抛出的异常改为 PageException

### 4.2.1 - 2016-12-11

- 解决`SimpleCache`类遗留问题导致的错误 [#143](http://git.oschina.net/free/Mybatis_PageHelper/issues/143) fix by [dhhua](https://github.com/dhhua)

### 4.2.0 - 2016-12-09

- 使用新的方式进行分页，4.2版本是从5.0版本分离出来的一个特殊版本，这个版本兼容4.x的所有功能，5.0版本时为了简化分页逻辑，会去掉部分功能，所以4.2是4.x的最后一个版本。
- 支持 MyBatis 3.1.0+ 版本
- 增加对 Derby 数据库的支持
- 对除 informix 外的全部数据库进行测试，全部通过
- PageHelper增加手动清除方法`clearPage()`
- 解决 SqlServer 多个`with(nolock)`时出错的问题
- 对CountMappedStatement 进行缓存，配置方式见BaseSqlUtil 319行
- 由于SqlServer的sql处理特殊，因此增加了两个SQL缓存，具体配置参考SqlServerDialect类
- 添加 sqlserver 别名进行排序功能，在解析sql时，会自动将使用的别名转换成列名 by panmingzhi
- 新增`sqlCacheClass`参数，该参数可选，可以设置sql缓存实现类，默认为`SimpleCache`，当项目包含guava时，使用`GuavaCache`，也可以通过参数`sqlCacheClass`指定自己的实现类，有关详情看`com.github.pagehelper.cache`包。
- 解决#135，增加/*keep orderby*/注解，SQL中包含该注释时，count查询时不会移出order by
- sqlserver没有orderby时，使用`order by rand()` #82 #118

### 4.1.6 - 2016-06-05

- 通过间接处理字符串解决SqlServer中不支持`with(nolock)`的问题#86，详情可以看`SqlServerParser`和`SqlServer2012Dialect`

### 4.1.5 - 2016-05-29

- 更新`PageProviderSqlSource`，支持3.4.0版本的`Provider`注解方式的分页#102
- 解决`SqlUtil`未初始化`PARAMS`属性导致的错误

### 4.1.4 - 2016-05-12

- 解决`closeConn`未设置时，默认值被覆盖变成`false`的问题#97
- `closeConn`不只对动态数据源有效，当没有设置`dialect`属性自动获取数据库类型的时候同样有效
- 解决关闭tomcat的时候提示线程安全问题#98，这个问题不会导致内存溢出，已经增加处理

### 4.1.3 - 2016-03-31

- 解决反射类没有完全捕获异常的问题#94
- 把SqlServer类所有private都改成了protected，方便继承修改#93

### 4.1.2 - 2016-03-06

- 增加可配参数`closeConn`，当使用动态数据源时，分页插件获取jdbcUrl后，控制是否关闭当前连接，默认`true`关闭
- count查询改为`count(0)`，分库分表情况下的效率可能更高

### 4.1.1 - 2016-01-05：

- 解决动态数据源时获取连接后未关闭的严重bug#80
- 解决动态数据源时SqlSource和parser绑定导致不能切换方言的问题

### 4.1.0 - 2015-12-30：

- 增加`autoRuntimeDialect`参数，允许在运行时根据多数据源自动识别对应方言的分页（暂时不支持自动选择`sqlserver2012`，只能使用`sqlserver`）。
- 去掉了4.0.3版本增加的`returnPageInfo`参数，接口返回值不支持`PageInfo`类型，可以使用下面`ISelect`中演示的方法获取
- 增加对`SqlServer2012`的支持，需要手动指定`dialect=sqlserver2012`，否则会使用2005的方式进行分页
- jsqlparser升级到0.9.4版本，使用jar包时必须用最新的0.9.4版本，使用Maven会自动依赖0.9.4
- 增加`ISelect`接口，方便调用，使用方法可以参考`src/test/java/com.github.pagehelper.test.basic.TestISelect`测试。

### 使用该接口可以参考如下用法(返回值为`Page`或`PageInfo`)：

```java
//jdk6,7用法，创建接口
Page<User> page = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPage(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectGroupBy();
    }
});
//jdk8 lambda用法
Page<User> page = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPage(()-> userMapper.selectGroupBy());
//为了说明可以链式使用，上面是单独setOrderBy("id desc")，也可以直接如下
Page<User> page = PageHelper.startPage(1, 10, "id desc").doSelectPage(()-> userMapper.selectGroupBy());

//也可以直接返回PageInfo，注意doSelectPageInfo方法和doSelectPage
pageInfo = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPageInfo(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectGroupBy();
    }
});
//对应的lambda用法
pageInfo = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPageInfo(() -> userMapper.selectGroupBy());

//count查询，返回一个查询语句的count数
long total = PageHelper.count(new ISelect() {
    @Override
    public void doSelect() {
        userMapper.selectLike(user);
    }
});
//lambda
total = PageHelper.count(()->userMapper.selectLike(user));
```

### 4.0.3 - 2015-11-09：

 - `PageHelper`新增3个`offsetPage`方法，参数主要是`offset`和`limit`，允许不规则分页

 - 新增两个可配参数`supportMethodsArguments`和`returnPageInfo`（该参数在4.1.0版本去掉），具体含义和用法请看[如何使用分页插件](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)中的参数介绍

### 4.0.2 - 2015-11-02

 - 简化`Page<E>`类，包含排序条件`orderBy`

 - `dialect`参数是数据库名称时不区分大小写

 - `dialect`参数可以设置为实现`com.github.pagehelper.parser.Parser`接口的实现类全限定名称

 - 增加对`h2`数据库的支持

 - 将`OrderByHelper`(排序插件)融合到`PageHelper`中，移除`OrderByHelper`

 - 该版本调整比较大，但对开发人员影响较小，为以后扩展和完善提供方便

### 4.0.1 -2015-09-10

 - 解决[#60 -使用RPC时，因Page类引用了RowBounds，导致反序列化失败](http://git.oschina.net/free/Mybatis_PageHelper/issues/60) by [马金凯](http://git.oschina.net/mxb)

 - 这个改动主要是去掉了`Page<E>`构造方法中的`RowBounds`，用`int[]`数组替换了`RowBounds`

### 4.0.0 - 2015-07-13

 - 配置属性`dialect`不在强制要求，可以不写，分页插件会自动判断

 - 解决从request中获取分页参数时的错误,感谢<b>探路者☆</b>

 - `PageInfo`增加空构造方法，所有属性增加`setter`方法

 - 增加对排序的支持

 - 可以单独使用`PageHelper.orderBy(String orderBy)`对查询语句增加排序，也可以配合`startPage`的其他方法使用

 - 可以使用`PageHelper.startPage(int start,int size,String orderBy)`对分页查询进行排序

 - 修改分页查询的处理逻辑，主要是将原`sqlSource`包装成可以分页和排序的`sqlSource`

### 3.7.5 - 2015-06-12

 - 增加对MyBatis3.2.0以上版本的校验，如果是不是3.2.0以上版本，会抛出异常提示

 - 解决3.7.1更新中实际没有解决的入参为不可变`Map`类型时的错误

### 3.7.4 - 2015-05-26

 - 为了支持`3.3.0`去掉了分页插件自带的`SytemObjectMetaObject`类(该类在早期版本为了支持3.2.0以前的MyBatis)

 - 最新支持MyBatis - 3.2.0到最新3.3.0版本

### 3.7.3 - 2015-05-22

 - `Page`继承的`ArrayList`，会根据`pageSize`初始化大小，这就导致当`pageSize`过大（如`Integer.MAX_VALUE`），实际数据量很小时的内存溢出，此处改为初始化大小为0的`List`。

 - 当想查询某页后面的全部数据时，可以使用`PageHelper.startPage(pageNum, Integer.MAX_VALUE)`进行分页，`RowBounds(offset, Integer.MAX_VALUE)`一样。

 - 针对`PageHelper.startPage(1, Integer.MAX_VALUE)`优化，会取消分页，直接查询全部数据（能起到`pageSizeZero`参数所起的作用）。

 - 针对`RowBounds(0, Integer.MAX_VALUE)`优化，会取消分页，直接查询全部数据（能起到`pageSizeZero`参数所起的作用）。

### 3.7.2 - 2015-05-13

 - jsqlparser解析sql会抛出Error异常，由于只捕获Exception，所以导致部分解析失败的sql无法使用嵌套方式处理，所以修改为捕获`Throwable`。

### 3.7.1 - 2015-05-05

 - 增加`Infomix`数据库支持，设置`dialect`值为`infomix`即可
 - 解决入参为不可变`Map`类型时的错误

### 3.7.0 - 2015-04-21

 - 由于`orderby`参数经常被错误认为的使用，因此该版本全面移除了`orderby`
 - `Page<E>`移除`orderby`属性
 - `PageHelper`的`startPage`方法中，移除包含`orderby`参数的方法，sqlserver相关包含该参数的全部移除
 - 对SqlServer进行分页查询时，请在sql中包含order by语句，否则会抛出异常
 - 当`offsetAsPageNum=false`的时候，由于PageNum问题，`RowBounds`查询的时候`reasonable`会强制为false，已解决
 - 少数情况下的select中包含单个函数查询时，会使用嵌套的count查询

### 3.6.4 - 2015-04-05

 - 重构，将原来的内部类全部独立出来，尤其是`Parser`接口以及全部实现。
   现在可以直接使用`Parser`，使用方法如下：

   ```java
   String originalSql = "Select * from user o where id > 10 order by id desc ";

   Parser parser = AbstractParser.newParser("mysql");
   //获取count查询sql
   String countSql = parser.getCountSql(originalSql);
   //获取分页sql，这种方式不适合sqlserver数据库
   String pageSql = parser.getPageSql(originalSql);

   //sqlserver用下面的方法
   SqlServer sqlServer = new SqlServer();
   pageSql = sqlServer.convertToPageSql(originalSql, 1, 10);
   ```

### 3.6.3 - 2015-03-10

 - 解决了一个潜在的bug，对[通用Mapper](http://git.oschina.net/free/Mapper)中的`SqlMapper`进行分页时，需要使用这个版本

### 3.6.2 - 2015-03-09

 - 本次更新只是增加了一个异常提示，当错误的配置了多个分页插件时，会有更友好的错误提示：

   >分页插件配置错误:请不要在系统中配置多个分页插件(使用Spring时,mybatis-config.xml和Spring<bean>配置方式，请选择其中一种，不要同时配置多个分页插件)！

### 3.6.1 - 2015-02-28

 - 解决select distinct导致count查询结果不正确的bug#35

 - 完善测试

### 3.6.0 - 2015-02-03

- 支持db2数据库

- 支持sqlserver(2005+)数据库

- sqlserver注意事项：
    - 请先保证你的SQL可以执行
    - sql中最好直接包含order by，可以自动从sql提取
    - 如果没有order by，可以通过入参提供，但是需要自己保证正确(<b>3.7.0版本以后，移除了该参数，请在sql中包含order by</b>)
    - 如果sql有order by，可以通过orderby参数覆盖sql中的order by
    - order by的列名不能使用别名(`UNION,INTERSECT,MINUS,EXCEPT`等复杂sql不受限制，具体可以自己尝试)
    - 表和列使用别名的时候不要使用单引号(')

- 简单修改结构

- `startPage`方法返回值从`void`改为`Page`，获取`Page`后可以修改参数值

 - `Page`增加一个针对sqlserver的属性`orderBy`(<b>3.7.0版本以后，移除了该属性</b>)，用法看上面的<b>注意事项</b>

 - `Page`增加了一个链式赋值的方法，可以像下面这样使用：
   `PageHelper.startPage(1,10).count(false).reasonable(true).pageSizeZero(false)`

 - `PageHelper`增加了`startPage(int pageNum, int pageSize,String orderBy)`方法(<b>3.7.0版本以后，移除了该方法</b>)，针对sqlserver

### 3.5.1 - 2015-01-20

 - 解决[bug#25](http://git.oschina.net/free/Mybatis_PageHelper/issues/25)，当参数是null并且是动态查询时，由于加入分页参数，导致参数不在是null，因而会导致部分判断出错，导致异常。

 - 上面这个bug会影响使用了动态标签并且允许入参为null的所有查询，虽然并不常见，但是建议各位使用最新版本

### 3.5.0 - 2015-01-11

 - 增加更丰富的调用方法[#23](http://git.oschina.net/free/Mybatis_PageHelper/issues/23)
   - `startPage(int pageNum, int pageSize)`
   - `startPage(int pageNum, int pageSize, boolean count)`
   - +`startPage(int pageNum, int pageSize, boolean count, Boolean reasonable)`
   - +`startPage(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero)`
   - +`startPage(Object params)`<b>注：只能是`Map`或`ServletRequest`类型</b>

   参数中的`reasonable`、`pageSizeZero`都可以覆盖默认配置，如果传`null`会用默认配置。

 - 为了支持`startPage(Object params)`方法，增加了一个`params`参数来配置参数映射，用于从`Map`或`ServletRequest`中取值，详细内容看文档下面的具体介绍。

 - 解决一个`<foreach>`标签使用对象内部属性循环时的bug[#24](http://git.oschina.net/free/Mybatis_PageHelper/issues/24)

### 3.4.2 - 2014-12-27

- `PageInfo`中的`judgePageBoudary`方法修改：
   ```java
    isLastPage = pageNum == pages && pageNum != 1;
    //改为
    isLastPage = pageNum == pages;
   ```

### 3.4.1 - 2014-12-24

 - 重大bug修复，`SqlParser`解析sql失败的时候返回了不带`count(*)`的sql，导致查询失败。

 - 产生原因，由于`SqlParser`在系统中出现的位置修改后，导致这里出现错误。

 - 强烈推荐各位更新到最新版本。

### v3.4.0 - 2014-12-18

 - 增加了对`@SelectProvider`注解方法的支持，不使用这种方式的不影响

 - 对基本逻辑进行修改，减少反射调用和获取`BoundSql`次数

### v3.3.2 - 2014-12-10

 - `PageInfo` 增加序列化。

### v3.3.1bug修复 - 2014-12-07

- 动态sql时，判断条件不会出现在ParameterMappings中，会导致获取不到属性。通常是因为判断条件中的属性没有出现在`#{}`中。

### v3.3.0

1. 对`MappedStatement`对象进行缓存，包括count查询的`MappedStatement`以及分页查询的`MappedStatement`，分页查询改为预编译查询。

2. 独立的`SqlUtil`类，由于原来的`PageHelper`太复杂，因此将拦截器外的其他代码独立到`SqlUtil`中，方便查看代码和维护。`SqlUtil`中增加`Parser`接口，提供一个抽象的`SimpleParser`实现，不同数据库的分页代码通过继承`SimpleParser`实现。

3. 特殊的`Parser`实现类`SqlParser`类，这是一个独立的java类，主要提供了更高性能的count查询sql，可以根据sql自动改为`count(*)`查询，自动去除不需要的`order by`
   语句，如果需要使用该类，只要把该类放到`SqlUtil`类相同的包下即可，同时需要引入Jar包`jsqlparser-0.9.1.jar`。

4. 增强的`PageInfo`类，`PageInfo`类包含了分页几乎所有需要用到的属性值，减少了对分页逻辑的过多投入。

4. 分页合理化，自动处理pageNum的异常情况。例如当pageNum<=0时，会设置pageNum=1，然后查询第一页。当pageNum>pages(总页数)时，自动将pageNum=pages，查询最后一页。

5. 增加对`PostgreSQL`,`MariaDB`,`SQLite`支持。其中`MariaDB`,`SQLite`和`Mysql`分页一样。

### v3.2.3

1. 解决`mysql`带有`for update`时分页错误的问题。

2. 当`pageSize`（或`RowBounds`的`limit`）`<=0` 时不再进行分页查询，只会进行count查询（RowBounds需要配置进行count查询），相当于用分页查询来做count查询了。

3. 增加了`pageSizeZero`参数，当`pageSizeZero=true`时，如果`pageSize=0`（或`RowBounds.limit`
   =0），就会查询全部的结果。这个参数对于那些在特殊情况下要查询全部结果的人有用。配置该参数后会与上面第二条冲突，解决方法就是如果只想查询count，就设置`pageSize<0`（如 `-1`
   ），只要不等于0（或者不配置pageSizeZero）就不会出现全部查询的情况。

4. 这个版本没有包含count查询时自动去除`order by`的功能，这个功能将会添加到3.3.0版本中。

5. 为了便于本项目的统一管理和发布，本项目会和github上面同步，项目会改为Maven管理的结构。


### v3.2.2

1. 简单重构优化代码。

2. 新增`PageInfo`包装类，对分页结果Page<E>进行封装，方便EL使用。

3. 将`SystemMetaObject`类的`fromObject`方法内置到分页插件中，方便低版本的Mybatis使用该插件。

### v3.2.1

1. 新增`offsetAsPageNum`参数，用来控制`RowBounds`中的`offset`是否作为`pageNum`使用，`pageNum`和`startPage`中的含义相同，`pageNum`是页码。该参数默认为`false`，使用默认值时，不需要配置该参数。

2. 新增`rowBoundsWithCount`参数，用来控制使用`RowBounds`时是否执行`count`查询。该参数默认为`false`，使用默认值时，不需要配置该参数。

### v3.2.0

1. 增加了对`Hsqldb`的支持，主要目的是为了方便测试使用`Hsqldb`

2. 增加了该项目的一个测试项目[Mybatis-Sample](http://git.oschina.net/free/Mybatis-Sample)，测试项目数据库使用`Hsqldb`

3. 增加MIT协议

### v3.1.2

1. 解决count sql在`oracle`中的错误

### v3.1.1

1. 统一返回值为`Page<E>`（可以直接按`List`使用）,方便在页面使用EL表达式，如`${page.pageNum}`,`${page.total}`

### v3.1.0

1. 解决了`RowBounds`分页的严重BUG，原先会在物理分页基础上进行内存分页导致严重错误，已修复

2. 增加对MySql的支持，该支持由[鲁家宁](http://my.oschina.net/lujianing)增加。

### v3.0

1. 现在支持两种形式的分页，使用`PageHelper.startPage`方法或者使用`RowBounds`参数

2. `PageHelper.startPage`方法修改，原先的`startPage(int pageNum, int pageSize)`
   默认求count，新增的`startPage(int pageNum, int pageSize, boolean count)`设置`count=false`可以不执行count查询

3. 移除`endPage`方法，现在本地变量`localPage`改为取出后清空本地变量。

4. 修改`Page<E>`类，继承`ArrayList<E>`

5. 关于两种形式的调用，请看示例代码

### v2.1

1. 解决并发异常

2. 分页sql改为直接拼sql

### v2.0

1. 支持Mybatis缓存，count和分页同时支持（二者同步）

2. 修改拦截器签名，拦截`Executor`

3. 将`Page<E>`类移到外面，方便调用

### v1.0

1. 支持`<foreach>`等标签的分页查询

2. 提供便捷的使用方式

