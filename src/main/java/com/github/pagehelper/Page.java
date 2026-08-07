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

package com.github.pagehelper;

import com.github.pagehelper.util.SqlSafeUtil;
import com.github.pagehelper.util.StackTraceUtil;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis - 分页对象
 *
 * @author liuzh/abel533/isea533
 * @version 3.6.0
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
public class Page<E> extends ArrayList<E> implements Closeable {
    private static final long serialVersionUID = 1L;

    private static final Log                       log        = LogFactory.getLog(Page.class);
    /**
     * 记录当前堆栈,可查找到page在何处创建
     * 需开启pagehelper.debug
     */
    private final        String                    stackTrace = PageInterceptor.isDebug() ? StackTraceUtil.current() : null;
    /**
     * 页码，从1开始
     */
    private              int                       pageNum;
    /**
     * 页面大小
     */
    private              int                       pageSize;
    /**
     * 起始行
     */
    private              long                      startRow;
    /**
     * 末行
     */
    private              long                      endRow;
    /**
     * 总数
     */
    private              long                      total;
    /**
     * 总页数
     */
    private              int                       pages;
    /**
     * 包含count查询
     */
    private              boolean                   count      = true;
    /**
     * 分页合理化
     */
    private              Boolean                   reasonable;
    /**
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
     */
    private              Boolean                   pageSizeZero;
    /**
     * 进行count查询的列名
     */
    private              String                    countColumn;
    /**
     * 排序
     */
    private              String                    orderBy;
    /**
     * 只增加排序
     */
    private              boolean                   orderByOnly;
    /**
     * sql拦截处理
     */
    private              BoundSqlInterceptor       boundSqlInterceptor;
    private transient    BoundSqlInterceptor.Chain chain;
    /**
     * 分页实现类，可以使用 {@link com.github.pagehelper.page.PageAutoDialect} 类中注册的别名，例如 "mysql", "oracle"
     */
    private              String                    dialectClass;
    /**
     * 转换count查询时保留查询的 order by 排序
     */
    private              Boolean                   keepOrderBy;
    /**
     * 转换count查询时保留子查询的 order by 排序
     */
    private              Boolean                   keepSubSelectOrderBy;
    /**
     * 异步count查询
     */
    private              Boolean                   asyncCount;

    public Page() {
        super();
    }

    public Page(int pageNum, int pageSize) {
        this(pageNum, pageSize, true, null);
    }

    public Page(int pageNum, int pageSize, boolean count) {
        this(pageNum, pageSize, count, null);
    }

    private Page(int pageNum, int pageSize, boolean count, Boolean reasonable) {
        super(0);
        if (pageNum == 1 && pageSize == Integer.MAX_VALUE) {
            pageSizeZero = true;
            pageSize = 0;
        }
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;
        calculateStartAndEndRow();
        setReasonable(reasonable);
    }

    /**
     * int[] rowBounds
     * 0 : offset
     * 1 : limit
     */
    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
            this.pageNum = 1;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNum = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public List<E> getResult() {
        return this;
    }

    public int getPages() {
        return pages;
    }

    public Page<E> setPages(int pages) {
        this.pages = pages;
        return this;
    }

    public long getEndRow() {
        return endRow;
    }

    public Page<E> setEndRow(long endRow) {
        this.endRow = endRow;
        return this;
    }

    public int getPageNum() {
        return pageNum;
    }

    public Page<E> setPageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Page<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public long getStartRow() {
        return startRow;
    }

    public Page<E> setStartRow(long startRow) {
        this.startRow = startRow;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (total == -1) {
            pages = 1;
            return;
        }
        if (pageSize > 0) {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        } else {
            pages = 0;
        }
        //分页合理化，针对不合理的页码自动处理
        if ((reasonable != null && reasonable) && pageNum > pages) {
            if (pages != 0) {
                pageNum = pages;
            }
            calculateStartAndEndRow();
        }
    }

    public Boolean getReasonable() {
        return reasonable;
    }

    public Page<E> setReasonable(Boolean reasonable) {
        if (reasonable == null) {
            return this;
        }
        this.reasonable = reasonable;
        //分页合理化，针对不合理的页码自动处理
        if (this.reasonable && this.pageNum <= 0) {
            this.pageNum = 1;
            calculateStartAndEndRow();
        }
        return this;
    }

    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    public Page<E> setPageSizeZero(Boolean pageSizeZero) {
        if (this.pageSizeZero == null && pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 设置排序字段，增加 SQL 注入校验，如果需要在 order by 使用函数，可以使用 {@link #setUnsafeOrderBy(String)} 方法
     *
     * @param orderBy 排序字段
     */
    public <E> Page<E> setOrderBy(String orderBy) {
        if (SqlSafeUtil.check(orderBy)) {
            throw new PageException("order by [" + orderBy + "] has a risk of SQL injection, " +
                    "if you want to avoid SQL injection verification, you can call Page.setUnsafeOrderBy");
        }
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    /**
     * 不安全的设置排序方法，如果从前端接收参数，请自行做好注入校验。
     * <p>
     * 请不要故意使用该方法注入然后提交漏洞!!!
     *
     * @param orderBy 排序字段
     */
    public <E> Page<E> setUnsafeOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    public boolean isOrderByOnly() {
        return orderByOnly;
    }

    public void setOrderByOnly(boolean orderByOnly) {
        this.orderByOnly = orderByOnly;
    }

    public String getDialectClass() {
        return dialectClass;
    }

    public void setDialectClass(String dialectClass) {
        this.dialectClass = dialectClass;
    }

    public Boolean getKeepOrderBy() {
        return keepOrderBy;
    }

    public Page<E> setKeepOrderBy(Boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    public Boolean getKeepSubSelectOrderBy() {
        return keepSubSelectOrderBy;
    }

    public void setKeepSubSelectOrderBy(Boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
    }

    public Boolean getAsyncCount() {
        return asyncCount;
    }

    public void setAsyncCount(Boolean asyncCount) {
        this.asyncCount = asyncCount;
    }

    /**
     * 指定使用的分页实现，如果自己使用的很频繁，建议自己增加一层封装再使用
     *
     * @param dialect 分页实现类，可以使用 {@link com.github.pagehelper.page.PageAutoDialect} 类中注册的别名，例如 "mysql", "oracle"
     * @return
     */
    public Page<E> using(String dialect) {
        this.dialectClass = dialect;
        return this;
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
    }

    public boolean isCount() {
        return this.count;
    }

    public Page<E> setCount(boolean count) {
        this.count = count;
        return this;
    }

    /**
     * 设置页码
     *
     * @param pageNum
     * @return
     */
    public Page<E> pageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
        return this;
    }

    /**
     * 设置页面大小
     *
     * @param pageSize
     * @return
     */
    public Page<E> pageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
        return this;
    }

    /**
     * 是否执行count查询
     *
     * @param count
     * @return
     */
    public Page<E> count(Boolean count) {
        this.count = count;
        return this;
    }

    /**
     * 设置合理化
     *
     * @param reasonable
     * @return
     */
    public Page<E> reasonable(Boolean reasonable) {
        setReasonable(reasonable);
        return this;
    }

    /**
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
     *
     * @param pageSizeZero
     * @return
     */
    public Page<E> pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    /**
     * 设置 BoundSql 拦截器
     *
     * @param boundSqlInterceptor
     * @return
     */
    public Page<E> boundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor) {
        setBoundSqlInterceptor(boundSqlInterceptor);
        return this;
    }

    /**
     * 指定 count 查询列
     *
     * @param columnName
     * @return
     */
    public Page<E> countColumn(String columnName) {
        setCountColumn(columnName);
        return this;
    }

    /**
     * 转换count查询时保留查询的 order by 排序
     *
     * @param keepOrderBy
     * @return
     */
    public Page<E> keepOrderBy(boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    public boolean keepOrderBy() {
        return this.keepOrderBy != null && this.keepOrderBy;
    }

    /**
     * 转换count查询时保留子查询的 order by 排序
     *
     * @param keepSubSelectOrderBy
     * @return
     */
    public Page<E> keepSubSelectOrderBy(boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
        return this;
    }

    public boolean keepSubSelectOrderBy() {
        return this.keepSubSelectOrderBy != null && this.keepSubSelectOrderBy;
    }

    /**
     * 异步count查询
     *
     * @param asyncCount
     * @return
     */
    public Page<E> asyncCount(boolean asyncCount) {
        this.asyncCount = asyncCount;
        return this;
    }

    /**
     * 使用异步count查询
     *
     * @return
     */
    public Page<E> enableAsyncCount() {
        return asyncCount(true);
    }

    /**
     * 不使用异步count查询
     *
     * @return
     */
    public Page<E> disableAsyncCount() {
        return asyncCount(false);
    }


    public boolean asyncCount() {
        return this.asyncCount != null && this.asyncCount;
    }

    public PageInfo<E> toPageInfo() {
        return new PageInfo<E>(this);
    }

    /**
     * 数据对象转换
     *
     * @param function
     * @param <T>
     * @return
     */
    public <T> PageInfo<T> toPageInfo(Function<E, T> function) {
        List<T> list = new ArrayList<T>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        PageInfo<T> pageInfo = new PageInfo<T>(list);
        pageInfo.setTotal(this.getTotal());
        pageInfo.setPageNum(this.getPageNum());
        pageInfo.setPageSize(this.getPageSize());
        pageInfo.setPages(this.getPages());
        pageInfo.setStartRow(this.getStartRow());
        pageInfo.setEndRow(this.getEndRow());
        pageInfo.calcByNavigatePages(PageInfo.DEFAULT_NAVIGATE_PAGES);
        return pageInfo;
    }

    public PageSerializable<E> toPageSerializable() {
        return new PageSerializable<E>(this);
    }

    /**
     * 数据对象转换
     *
     * @param function
     * @param <T>
     * @return
     */
    public <T> PageSerializable<T> toPageSerializable(Function<E, T> function) {
        List<T> list = new ArrayList<T>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        PageSerializable<T> pageSerializable = new PageSerializable<T>(list);
        pageSerializable.setTotal(this.getTotal());
        return pageSerializable;
    }

    public <E> Page<E> doSelectPage(ISelect select) {
        select.doSelect();
        return (Page<E>) this;
    }

    public <E> PageInfo<E> doSelectPageInfo(ISelect select) {
        select.doSelect();
        return (PageInfo<E>) this.toPageInfo();
    }

    public <E> PageSerializable<E> doSelectPageSerializable(ISelect select) {
        select.doSelect();
        return (PageSerializable<E>) this.toPageSerializable();
    }

    public long doCount(ISelect select) {
        this.pageSizeZero = true;
        this.pageSize = 0;
        select.doSelect();
        return this.total;
    }

    public String getCountColumn() {
        return countColumn;
    }

    public void setCountColumn(String countColumn) {
        if (!"0".equals(countColumn) && !"*".equals(countColumn) && SqlSafeUtil.check(countColumn)) {
            throw new PageException("count(" + countColumn + ") has a risk of SQL injection");
        }
        this.countColumn = countColumn;
    }

    public BoundSqlInterceptor getBoundSqlInterceptor() {
        return boundSqlInterceptor;
    }

    public void setBoundSqlInterceptor(BoundSqlInterceptor boundSqlInterceptor) {
        this.boundSqlInterceptor = boundSqlInterceptor;
    }

    BoundSqlInterceptor.Chain getChain() {
        return chain;
    }

    void setChain(BoundSqlInterceptor.Chain chain) {
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "Page{" +
                "count=" + count +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", startRow=" + startRow +
                ", endRow=" + endRow +
                ", total=" + total +
                ", pages=" + pages +
                ", reasonable=" + reasonable +
                ", pageSizeZero=" + pageSizeZero +
                '}' + super.toString();
    }

    @Override
    public void close() {
        PageHelper.clearPage();
    }

    /**
     * 兼容低版本 Java 7-
     */
    public interface Function<E, T> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         */
        T apply(E t);

    }
}
