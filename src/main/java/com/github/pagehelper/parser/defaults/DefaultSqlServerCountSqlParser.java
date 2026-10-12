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

import com.github.pagehelper.parser.SqlParserUtil;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collections;
import java.util.List;

/**
 * SQL Server 保留 ORDER BY 时使用 TOP 100 PERCENT，保持表达式作用域和参数顺序。
 */
public class DefaultSqlServerCountSqlParser extends DefaultCountSqlParser {
    @Override
    public Select sqlToCount(Select select, String name) {
        return super.sqlToCount(allowOrderBy(select), name);
    }

    @Override
    public String getSimpleCountSql(String sql, String name) {
        try {
            Statement statement = SqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = allowOrderBy((Select) statement);
                PlainSelect count = new PlainSelect();
                ParenthesedSelect from = new ParenthesedSelect();
                from.setSelect(select);
                from.setAlias(TABLE_ALIAS);
                count.setFromItem(from);
                count.setSelectItems(Collections.singletonList(new SelectItem<>(new Column("count(" + name + ")"))));
                count.setWithItemsList(select.getWithItemsList());
                select.setWithItemsList(null);
                return count.toString();
            }
        } catch (Throwable ignored) {
            // 无法解析时保持原有回退行为
        }
        return super.getSimpleCountSql(sql, name);
    }

    private Select allowOrderBy(Select select) {
        if (select == null) {
            return null;
        }
        if (select.getWithItemsList() != null) {
            for (WithItem item : select.getWithItemsList()) {
                item.setSelect(allowOrderBy(item.getSelect()));
            }
        }
        if (select instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesed = (ParenthesedSelect) select;
            parenthesed.setSelect(allowOrderBy(parenthesed.getSelect()));
        } else if (select instanceof PlainSelect) {
            PlainSelect plain = (PlainSelect) select;
            plain.setFromItem(allowOrderByInFrom(plain.getFromItem()));
            allowOrderByInJoins(plain.getJoins());
            if (hasOrderBy(plain) && plain.getTop() == null && plain.getOffset() == null
                    && plain.getFetch() == null && plain.getForXmlPath() == null) {
                plain.setTop(allRows());
            }
            return plain;
        } else if (select instanceof SetOperationList) {
            List<Select> branches = ((SetOperationList) select).getSelects();
            if (branches != null) {
                for (int i = 0; i < branches.size(); i++) {
                    branches.set(i, allowOrderBy(branches.get(i)));
                }
            }
        }
        if (hasOrderBy(select) && select.getOffset() == null && select.getFetch() == null) {
            PlainSelect ordered = new PlainSelect();
            ordered.setTop(allRows());
            ordered.setSelectItems(Collections.singletonList(new SelectItem<>(new AllColumns())));
            ParenthesedSelect from = new ParenthesedSelect();
            from.setSelect(select);
            from.setAlias(new Alias("pagehelper_order"));
            ordered.setFromItem(from);
            ordered.setOrderByElements(select.getOrderByElements());
            select.setOrderByElements(null);
            ordered.setWithItemsList(select.getWithItemsList());
            select.setWithItemsList(null);
            return ordered;
        }
        return select;
    }

    private FromItem allowOrderByInFrom(FromItem from) {
        if (from instanceof ParenthesedSelect) {
            ParenthesedSelect subquery = (ParenthesedSelect) from;
            subquery.setSelect(allowOrderBy(subquery.getSelect()));
        } else if (from instanceof ParenthesedFromItem) {
            ParenthesedFromItem parenthesed = (ParenthesedFromItem) from;
            parenthesed.setFromItem(allowOrderByInFrom(parenthesed.getFromItem()));
            allowOrderByInJoins(parenthesed.getJoins());
        }
        return from;
    }

    private void allowOrderByInJoins(List<Join> joins) {
        if (joins != null) {
            for (Join join : joins) {
                join.setRightItem(allowOrderByInFrom(join.getRightItem()));
            }
        }
    }

    private boolean hasOrderBy(Select select) {
        return select.getOrderByElements() != null && !select.getOrderByElements().isEmpty();
    }

    private Top allRows() {
        Top top = new Top();
        top.setExpression(new LongValue(100));
        top.setPercentage(true);
        return top;
    }
}
