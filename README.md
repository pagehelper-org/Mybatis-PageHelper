![MyBatis Pagination - PageHelper](logo.png)
# MyBatis 分页插件 - PageHelper

[![Build Status](https://travis-ci.org/pagehelper/Mybatis-PageHelper.svg?branch=master)](https://travis-ci.org/pagehelper/Mybatis-PageHelper)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper)

[English](README_en.md)

如果你也在用 MyBatis，建议尝试该分页插件，这一定是<b>最方便</b>使用的分页插件。

分页插件支持任何复杂的单表、多表分页，部分特殊情况请看[重要提示](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)。

想要使用分页插件？请看[如何使用分页插件](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)。

## 《MyBatis 从入门到精通》

![MyBatis 从入门到精通](https://github.com/mybatis-book/book/raw/master/book.png)

[京东](https://item.jd.com/12103309.html) ，[当当](http://product.dangdang.com/25098208.html)
，[亚马逊](https://www.amazon.cn/MyBatis从入门到精通-刘增辉/dp/B072RC11DM/ref=sr_1_18?ie=UTF8&qid=1498007125&sr=8-18&keywords=mybatis)

CSDN博客：http://blog.csdn.net/isea533/article/details/73555400

GitHub项目：https://github.com/mybatis-book/book

## 支持 [MyBatis 3.1.0+](https://github.com/mybatis/mybatis-3)

## PageHelper 6 支持 jdk8+

## PageHelper 5 支持 jdk6+

## 物理分页

该插件目前支持以下数据库的<b>物理分页</b> [PageAutoDialect](src/main/java/com/github/pagehelper/page/PageAutoDialect.java):

```java
static {
    //注册别名
    registerDialectAlias("hsqldb",HsqldbDialect.class);
    registerDialectAlias("h2",HsqldbDialect.class);
    registerDialectAlias("phoenix",HsqldbDialect.class);

    registerDialectAlias("postgresql",PostgreSqlDialect.class);

    registerDialectAlias("mysql",MySqlDialect.class);
    registerDialectAlias("mariadb",MySqlDialect.class);
    registerDialectAlias("sqlite",MySqlDialect.class);

    registerDialectAlias("herddb",HerdDBDialect.class);

    registerDialectAlias("oracle",OracleDialect.class);
    registerDialectAlias("oracle9i",Oracle9iDialect.class);
    registerDialectAlias("db2",Db2Dialect.class);
    registerDialectAlias("as400",AS400Dialect.class);
    registerDialectAlias("informix",InformixDialect.class);
    //解决 informix-sqli #129，仍然保留上面的
    registerDialectAlias("informix-sqli",InformixDialect.class);

    registerDialectAlias("sqlserver",SqlServerDialect.class);
    registerDialectAlias("sqlserver2012",SqlServer2012Dialect.class);

    registerDialectAlias("derby",SqlServer2012Dialect.class);
    //达梦数据库,https://github.com/mybatis-book/book/issues/43
    registerDialectAlias("dm",OracleDialect.class);
    //阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281
    registerDialectAlias("edb",OracleDialect.class);
    //PolarDB Oracle兼容模式
    registerDialectAlias("polardb",OracleDialect.class);
    registerDialectAlias("polardb2",OracleDialect.class);
    //神通数据库
    registerDialectAlias("oscar",OscarDialect.class);
    registerDialectAlias("clickhouse",MySqlDialect.class);
    //瀚高数据库
    registerDialectAlias("highgo",HsqldbDialect.class);
    //虚谷数据库
    registerDialectAlias("xugu",HsqldbDialect.class);
    registerDialectAlias("impala",HsqldbDialect.class);
    registerDialectAlias("firebirdsql",FirebirdDialect.class);
    //人大金仓数据库
    registerDialectAlias("kingbase",PostgreSqlDialect.class);
    // 人大金仓新版本kingbase8
    registerDialectAlias("kingbase8",PostgreSqlDialect.class);
    //行云数据库
    registerDialectAlias("xcloud",CirroDataDialect.class);

    //openGauss数据库
    registerDialectAlias("opengauss",PostgreSqlDialect.class);

    //注册 AutoDialect
    //想要实现和以前版本相同的效果时，可以配置 autoDialectClass=old
    registerAutoDialectAlias("old",DefaultAutoDialect.class);
    registerAutoDialectAlias("hikari",HikariAutoDialect.class);
    registerAutoDialectAlias("druid",DruidAutoDialect.class);
    registerAutoDialectAlias("tomcat-jdbc",TomcatAutoDialect.class);
    registerAutoDialectAlias("dbcp",DbcpAutoDialect.class);
    registerAutoDialectAlias("c3p0",C3P0AutoDialect.class);
    //不配置时，默认使用 DataSourceNegotiationAutoDialect
    registerAutoDialectAlias("default",DataSourceNegotiationAutoDialect.class);
}
```

> 如果你使用的数据库不在这个列表时，你可以配置 `dialectAlias` 参数。
>
>这个参数允许配置自定义实现的别名，可以用于根据 JDBCURL 自动获取对应实现，允许通过此种方式覆盖已有的实现，配置示例如（多个配置时使用分号隔开）：
>
>```xml
><property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
><!-- 6.0支持下面的引用方式，引用 Oracle9iDialect.class 的实现 -->
><property name="dialectAlias" value="oracle=oracle9i"/>
><!-- 6.0支持下面的引用方式，达梦使用oracle语法分页，简化类全名写法 -->
><property name="dialectAlias" value="dm=oracle"/>
>```

## 使用 [QueryInterceptor 规范](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java)

[Executor 拦截器高级教程 - QueryInterceptor 规范](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Interceptor.md)

## 集成

使用 PageHelper 你只需要在 classpath
中包含 [pagehelper-x.y.z.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/)
和 [jsqlparser-x.y.z.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/)。

> pagehelper 和 jsqlparser 对应关系参考 pom.xml 中的依赖版本。

如果你使用 Maven，你只需要在 pom.xml 中添加下面的依赖：

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>最新版本</version>
</dependency>
```

如果你使用 Spring Boot 可以参考： [pagehelper-spring-boot-starter](https://github.com/pagehelper/pagehelper-spring-boot)

[继续查看配置和用法](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)

## 文档：

- [如何使用分页插件](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)
- [更新日志](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Changelog.md)
- [重要提示](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/Important.md)

## Spring 集成示例

- [集成 Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [集成 Spring 4.x](https://github.com/abel533/Mybatis-Spring)
- [集成 Spring Boot](https://github.com/abel533/MyBatis-Spring-Boot)

## 提交 BUG

https://github.com/pagehelper/Mybatis-PageHelper/issues/new

## 微信公众号

<img src="wx_mybatis.jpg" height="300"/>

## 项目的发展离不开你的支持

### 请作者喝杯咖啡吧！

<img src="ali_pay.png" height="300"/>

<img src="wx_pay.png" height="300"/>

## 作者信息

网站：https://mybatis.io

作者博客：http://blog.csdn.net/isea533

作者邮箱： abel533@gmail.com

如需加群，请通过 http://mybatis.io 首页按钮加群。

本项目在 github 的项目地址：https://github.com/pagehelper/Mybatis-PageHelper

本项目在 gitosc 的项目地址：http://git.oschina.net/free/Mybatis_PageHelper

## MyBatis-3

- 项目：https://github.com/mybatis/mybatis-3
- 文档：http://mybatis.github.io/mybatis-3/zh/index.html

MyBatis 专栏：

- [MyBatis示例](http://blog.csdn.net/column/details/mybatis-sample.html)
- [MyBatis问题集](http://blog.csdn.net/column/details/mybatisqa.html)

## 感谢所有项目贡献者！

<a href="https://github.com/pagehelper/Mybatis-PageHelper/graphs/contributors">
  <img src="https://contributors-img.web.app/image?repo=pagehelper/Mybatis-PageHelper" />
</a>