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
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.parser.SqlParserUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Map;

/**
 * @author liuzh
 */
public class Db2Dialect extends AbstractHelperDialect {

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow() + 1);
        paramMap.put(PAGEPARAMETER_SECOND, page.getEndRow());
        //处理pageKey
        pageKey.update(page.getStartRow() + 1);
        pageKey.update(page.getEndRow());
        //处理参数配置
        handleParameter(boundSql, ms, long.class, long.class);
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        String[] withPrefix = SqlParserUtil.splitWithPrefix(sql);
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 140);
        if (withPrefix[0] != null) {
            sqlBuilder.append(withPrefix[0]).append(' ');
        }
        sqlBuilder.append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS PAGEHELPER_ROW_ID FROM ( \n");
        sqlBuilder.append(withPrefix[1]);
        sqlBuilder.append("\n ) AS TMP_PAGE) TMP_PAGE WHERE PAGEHELPER_ROW_ID BETWEEN ? AND ?");
        return sqlBuilder.toString();
    }

}
