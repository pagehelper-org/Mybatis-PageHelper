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

package com.github.pagehelper.dialect.rowbounds;

import com.github.pagehelper.dialect.AbstractRowBoundsDialect;
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

import java.util.Properties;

/**
 * sqlserver 基于 RowBounds 的分页
 *
 * @author liuzh
 */
public class SqlServerRowBoundsDialect extends AbstractRowBoundsDialect {
    protected SqlServerSqlParser sqlServerSqlParser;
    protected ReplaceSql         replaceSql;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        sql = replaceSql.replace(sql);
        sql = countSqlParser.getSmartCountSql(sql);
        sql = replaceSql.restore(sql);
        return sql;
    }

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        //处理pageKey
        pageKey.update(rowBounds.getOffset());
        pageKey.update(rowBounds.getLimit());
        sql = replaceSql.replace(sql);
        sql = sqlServerSqlParser.convertToPageSql(sql, null, null);
        sql = replaceSql.restore(sql);
        sql = sql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(rowBounds.getOffset()));
        sql = sql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(rowBounds.getLimit()));
        return sql;
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
        if (StringUtil.isEmpty(replaceSql) || "simple".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new SimpleWithNolockReplaceSql();
        } else if ("regex".equalsIgnoreCase(replaceSql)) {
            this.replaceSql = new RegexWithNolockReplaceSql();
        } else {
            this.replaceSql = ClassUtil.newInstance(replaceSql, properties);
        }
    }
}
