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

package com.github.pagehelper.parser.defaults;

import com.github.pagehelper.PageException;
import com.github.pagehelper.parser.OrderBySqlParser;
import com.github.pagehelper.parser.SqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.List;

/**
 * 处理 Order by
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class DefaultOrderBySqlParser implements OrderBySqlParser {
    private static final Log log = LogFactory.getLog(DefaultOrderBySqlParser.class);

    /**
     * convert to order by sql
     *
     * @param sql
     * @param orderBy
     * @return
     */
    @Override
    public String converToOrderBySql(String sql, String orderBy) {
        //解析SQL
        Statement stmt = null;
        try {
            stmt = SqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            //处理body-去最外层order by
            List<OrderByElement> orderByElements = extraOrderBy(select);
            String defaultOrderBy = PlainSelect.orderByToString(orderByElements);
            if (defaultOrderBy.indexOf('?') != -1) {
                throw new PageException("The order by in the original SQL[" + sql + "] contains parameters, so it cannot be modified using the OrderBy plugin!");
            }
            //新的sql
            sql = select.toString();
            //通过语法树设置排序，保留 LIMIT、OFFSET、FETCH 等子句的位置
            Select orderBySelect = (Select) SqlParserUtil.parse("SELECT 1 ORDER BY " + orderBy);
            select.setOrderByElements(orderBySelect.getOrderByElements());
            return select.toString();
        } catch (Throwable e) {
            log.warn("Failed to handle sorting: " + e + ", downgraded to a direct splice of the order by parameter");
        }
        return sql + " order by " + orderBy;
    }

    /**
     * extra order by and set default orderby to null
     *
     * @param select
     */
    public static List<OrderByElement> extraOrderBy(Select select) {
        if (select != null) {
            if (select instanceof PlainSelect || select instanceof SetOperationList) {
                List<OrderByElement> orderByElements = select.getOrderByElements();
                select.setOrderByElements(null);
                return orderByElements;
            } else if (select instanceof ParenthesedSelect) {
                extraOrderBy(((ParenthesedSelect) select).getSelect());
            }
        }
        return null;
    }
}
