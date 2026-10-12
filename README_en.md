![MyBatis Pagination - PageHelper](logo.png)

# MyBatis Pagination - PageHelper

[![Build Status](https://travis-ci.org/pagehelper/Mybatis-PageHelper.svg?branch=master)](https://travis-ci.org/pagehelper/Mybatis-PageHelper)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pagehelper/pagehelper)

[中文版文档](README.md)

If you are using MyBatis, it is recommended to try this pagination plugin. This must be the **MOST CONVENIENT**
pagination plugin.

PageHelper supports any complex single-table, multi-table queries.
As to some special cases, please refer to the [**Important
note**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md).

Want to use PageHelper?
Please check out [**How to use
PageHelper**](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md).

## New JavaDoc

https://apidoc.gitee.com/free/Mybatis_PageHelper

APIs: https://apidoc.gitee.com/free/Mybatis_PageHelper/com/github/pagehelper/page/PageMethod.html

## 《MyBatis 从入门到精通》

![MyBatis 从入门到精通](https://github.com/mybatis-book/book/raw/master/book.png)

[京东](https://item.jd.com/12103309.html) ，[当当](http://product.dangdang.com/25098208.html)
，[Amazon](https://www.amazon.cn/MyBatis从入门到精通-刘增辉/dp/B072RC11DM/ref=sr_1_18?ie=UTF8&qid=1498007125&sr=8-18&keywords=mybatis)

CSDN Blog：http://blog.csdn.net/isea533/article/details/73555400

GitHub：https://github.com/mybatis-book/book

## Support [MyBatis 3.1.0+](https://github.com/mybatis/mybatis-3)

## PageHelper 6 Support jdk8+

## PageHelper 5 Support jdk6+

## Physical Paging

PageHelper supports the following
databases [PageAutoDialect](src/main/java/com/github/pagehelper/page/PageAutoDialect.java):

```java
static {
    //register alias
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
    //Solve informix-sqli #129, still keep the above
    registerDialectAlias("informix-sqli",InformixDialect.class);

    registerDialectAlias("sqlserver",SqlServerDialect.class);
    registerDialectAlias("sqlserver2012",SqlServer2012Dialect.class);

    registerDialectAlias("derby",SqlServer2012Dialect.class);
    //达梦数据库,https://github.com/mybatis-book/book/issues/43
    registerDialectAlias("dm",OracleDialect.class);
    //阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281
    registerDialectAlias("edb",OracleDialect.class);
    //PolarDB Oracle compatible mode
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
    //If you want to achieve the same effect as the previous version, you can configure it autoDialectClass=old
    registerAutoDialectAlias("old",DefaultAutoDialect.class);
    registerAutoDialectAlias("hikari",HikariAutoDialect.class);
    registerAutoDialectAlias("druid",DruidAutoDialect.class);
    registerAutoDialectAlias("tomcat-jdbc",TomcatAutoDialect.class);
    registerAutoDialectAlias("dbcp",DbcpAutoDialect.class);
    registerAutoDialectAlias("c3p0",C3P0AutoDialect.class);
    //If not configured, it is used by default DataSourceNegotiationAutoDialect
    registerAutoDialectAlias("default",DataSourceNegotiationAutoDialect.class);
}
```

> If the database you are using is not in this list, you can configure the `dialectAlias` parameter.
>
> This parameter allows to configure the alias of a custom implementation,
> which can be used to automatically obtain the corresponding implementation according to the JDBCURL,
> and allows to overwrite the existing implementation in this way.
> The configuration example is as follows (use semicolons to separate multiple alias):
>
>```xml
><property name="dialectAlias" value="oracle=com.github.pagehelper.dialect.helper.OracleDialect"/>
><!-- 6.0 The following reference is supported, referencing the implementation of Oracle9iDialect.class -->
><property name="dialectAlias" value="oracle=oracle9i"/>
><!-- 6.0 To support the following citation methods, DM uses oracle syntax for pagination to simplify the writing of the full name of the class -->
><property name="dialectAlias" value="dm=oracle"/>
>```

## Use [QueryInterceptor spec](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/src/main/java/com/github/pagehelper/QueryInterceptor.java)

## Installation

To use PageHelper, you just need to include the
[pagehelper-x.y.z.jar](http://repo1.maven.org/maven2/com/github/pagehelper/pagehelper/)
and [jsqlparser-x.y.z.jar](http://repo1.maven.org/maven2/com/github/jsqlparser/jsqlparser/) file in the classpath.

> For version matching relation, please refer to the dependent version in pom.

If you are using Maven, you could just add the following dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>latest version</version>
</dependency>
```

If you are using Spring Boot, You can refer to
the [pagehelper-spring-boot-starter](https://github.com/pagehelper/pagehelper-spring-boot)

[More...](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md)

## Documentation

- [How to use the PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/HowToUse.md)
- [Changelog](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Changelog.md)
- [Important note](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/en/Important.md)

## Spring integration sample

- [Integration Spring 3.x](https://github.com/abel533/Mybatis-Spring/tree/spring3.x)
- [Integration Spring 4.x](https://github.com/abel533/Mybatis-Spring)
- [Integration Spring Boot](https://github.com/abel533/MyBatis-Spring-Boot)

## Submit BUG

https://github.com/pagehelper/Mybatis-PageHelper/issues/new

## 微信公众号

<img src="wx_mybatis.jpg" height="300"/>

## Thank you for your support


### Buy the author a cup of coffee!


<img src="ali_pay.png" height="300"/>

<img src="wx_pay.png" height="300"/>

## Author Info

Web: https://mybatis.io

Blog: http://blog.csdn.net/isea533

Email: abel533@gmail.com

PageHelper on github:https://github.com/pagehelper/Mybatis-PageHelper

PageHelper on gitosc:http://git.oschina.net/free/Mybatis_PageHelper

## MyBatis-3

- Project：https://github.com/mybatis/mybatis-3
- Document：https://mybatis.org/mybatis-3/index.html

MyBatis 专栏：

- [MyBatis Sample](http://blog.csdn.net/column/details/mybatis-sample.html)
- [MyBatis QA](http://blog.csdn.net/column/details/mybatisqa.html)

## Thanks to all the people who already contributed!

<a href="https://github.com/pagehelper/Mybatis-PageHelper/graphs/contributors">
  <img src="https://contributors-img.web.app/image?repo=pagehelper/Mybatis-PageHelper" />
</a>