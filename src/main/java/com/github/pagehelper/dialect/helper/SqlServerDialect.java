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

package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.dialect.ReplaceSql;
import com.github.pagehelper.dialect.replace.RegexWithNolockReplaceSql;
import com.github.pagehelper.dialect.replace.SimpleWithNolockReplaceSql;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.parser.SqlServerSqlParser;
import com.github.pagehelper.parser.defaults.DefaultSqlServerCountSqlParser;
import com.github.pagehelper.parser.defaults.DefaultSqlServerSqlParser;
import com.github.pagehelper.util.ClassUtil;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;
import java.util.Properties;

/**
 * @author liuzh
 */
public class SqlServerDialect extends AbstractHelperDialect {
    protected SqlServerSqlParser sqlServerSqlParser;
    protected Cache<String, String> CACHE_COUNTSQL;
    protected Cache<String, String> CACHE_PAGESQL;
    protected ReplaceSql            replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        String cacheSql = CACHE_COUNTSQL.get(sql);
        if (cacheSql != null) {
            return cacheSql;
        } else {
            cacheSql = sql;
        }
        cacheSql = replaceSql.replace(cacheSql);
        cacheSql = countSqlParser.getSmartCountSql(cacheSql);
        cacheSql = replaceSql.restore(cacheSql);
        CACHE_COUNTSQL.put(sql, cacheSql);
        return cacheSql;
    }

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        //处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        String cacheSql = CACHE_PAGESQL.get(sql);
        if (cacheSql == null) {
            cacheSql = sql;
            cacheSql = replaceSql.replace(cacheSql);
            cacheSql = sqlServerSqlParser.convertToPageSql(cacheSql, null, null);
            cacheSql = replaceSql.restore(cacheSql);
            CACHE_PAGESQL.put(sql, cacheSql);
        }
        cacheSql = cacheSql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(page.getStartRow()));
        cacheSql = cacheSql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(page.getPageSize()));
        return cacheSql;
    }

    /**
     * 分页查询，pageHelper转换SQL时报错with(nolock)不识别的问题，
     * 重写父类AbstractHelperDialect.getPageSql转换出错的方法。
     * 1. this.replaceSql.replace(sql);先转换成假的表名
     * 2. 然后进行SQL转换
     * 3. this.replaceSql.restore(sql);最后再恢复成真的with(nolock)
     */
    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = this.getLocalPage();
        String orderBy = page.getOrderBy();
        if (StringUtil.isNotEmpty(orderBy)) {
            pageKey.update(orderBy);
            sql = this.replaceSql.replace(sql);
            sql = orderBySqlParser.converToOrderBySql(sql, orderBy);
            sql = this.replaceSql.restore(sql);
        }

        return page.isOrderByOnly() ? sql : this.getPageSql(sql, page, pageKey);
    }

    @Override
    protected CountSqlParser createDefaultCountSqlParser() {
        return new DefaultSqlServerCountSqlParser();
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        this.sqlServerSqlParser = ClassUtil.newInstance(properties.getProperty("sqlServerSqlParser"), SqlServerSqlParser.class, properties, DefaultSqlServerSqlParser::new);
        String replaceSql = properties.getProperty("replaceSql");
        if (StringUtil.isEmpty(replaceSql) || "regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolockReplaceSql();
        } else if ("simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolockReplaceSql();
        } else {
            this.replaceSql = ClassUtil.newInstance(replaceSql, properties);
        }
        String sqlCacheClass = properties.getProperty("sqlCacheClass");
        if (StringUtil.isNotEmpty(sqlCacheClass) && !sqlCacheClass.equalsIgnoreCase("false")) {
            CACHE_COUNTSQL = CacheFactory.createCache(sqlCacheClass, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(sqlCacheClass, "page", properties);
        } else {
            CACHE_COUNTSQL = CacheFactory.createCache(null, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(null, "page", properties);
        }
    }
}
