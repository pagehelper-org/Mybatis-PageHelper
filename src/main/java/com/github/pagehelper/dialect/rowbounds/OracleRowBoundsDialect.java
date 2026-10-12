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
import com.github.pagehelper.parser.SqlParserUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * oracle 基于 RowBounds 的分页
 *
 * @author liuzh
 */
public class OracleRowBoundsDialect extends AbstractRowBoundsDialect {

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        int startRow = rowBounds.getOffset();
        int endRow = rowBounds.getOffset() + rowBounds.getLimit();
        String[] withPrefix = SqlParserUtil.splitWithPrefix(sql);
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        if (withPrefix[0] != null) {
            sqlBuilder.append(withPrefix[0]).append(' ');
        }
        if (startRow > 0) {
            sqlBuilder.append("SELECT * FROM ( ");
        }
        if (endRow > 0) {
            sqlBuilder.append(" SELECT TMP_PAGE.*, ROWNUM PAGEHELPER_ROW_ID FROM ( ");
        }
        sqlBuilder.append("\n");
        sqlBuilder.append(withPrefix[1]);
        sqlBuilder.append("\n");
        if (endRow > 0) {
            sqlBuilder.append(" ) TMP_PAGE WHERE ROWNUM <= ");
            sqlBuilder.append(endRow);
            pageKey.update(endRow);
        }
        if (startRow > 0) {
            sqlBuilder.append(" ) WHERE PAGEHELPER_ROW_ID > ");
            sqlBuilder.append(startRow);
            pageKey.update(startRow);
        }
        return sqlBuilder.toString();
    }

}
