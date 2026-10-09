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

import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import com.github.pagehelper.page.PageBoundSqlInterceptors;
import com.github.pagehelper.page.PageMethod;
import com.github.pagehelper.page.PageParams;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;

/**
 * Mybatis - 通用分页拦截器<br/>
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 *
 * @author liuzh/abel533/isea533
 * @version 5.0.0
 */
public class PageHelper extends PageMethod implements Dialect, BoundSqlInterceptor.Chain {
    private PageParams               pageParams;
    private PageAutoDialect          autoDialect;
    private PageBoundSqlInterceptors pageBoundSqlInterceptors;
    private ForkJoinPool             asyncCountService;

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = pageParams.getPage(parameterObject, rowBounds);
        if (page == null) {
            return true;
        } else {
            //设置默认的 count 列
            if (StringUtil.isEmpty(page.getCountColumn())) {
                page.setCountColumn(pageParams.getCountColumn());
            }
            //设置默认的异步 count 设置
            if (page.getAsyncCount() == null) {
                page.setAsyncCount(pageParams.isAsyncCount());
            }
            autoDialect.initDelegateDialect(ms, page.getDialectClass());
            return false;
        }
    }

    @Override
    public boolean isAsyncCount() {
        Page<?> page = getLocalPage();
        // 异步 count 在 afterCount 之前执行，不能靠总数跳过数据查询。
        // 只有还会执行数据查询时才保留请求里的异步设置：pageSize > 0，
        // 或 pageSize = 0 且 pageSizeZero = true（返回全部结果）。
        // pageSize < 0，以及 pageSize = 0 且 pageSizeZero 不为 true 时只查总数，
        // 改走同步 count，让 afterCount 提前返回；不修改 Page 上保存的异步设置。
        int pageSize = page.getPageSize();
        boolean pageSizeZero = page.getPageSizeZero() != null && page.getPageSizeZero();
        if (pageSize > 0 || (pageSize == 0 && pageSizeZero)) {
            return page.asyncCount();
        }
        return false;
    }

    @Override
    public <T> Future<T> asyncCountTask(Callable<T> task) {
        //异步执行时需要将ThreadLocal值传递，否则会找不到
        AbstractHelperDialect dialectThreadLocal = autoDialect.getDialectThreadLocal();
        Page<Object> localPage = getLocalPage();
        String countId = UUID.randomUUID().toString();
        return asyncCountService.submit(() -> {
            try {
                //设置 ThreadLocal
                autoDialect.setDialectThreadLocal(dialectThreadLocal);
                setLocalPage(localPage);
                return task.call();
            } finally {
                autoDialect.clearDelegate();
                clearPage();
            }
        });
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforeCount(ms, parameterObject, rowBounds);
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return autoDialect.getDelegate().getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().afterCount(count, parameterObject, rowBounds);
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return autoDialect.getDelegate().processParameterObject(ms, parameterObject, boundSql, pageKey);
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforePage(ms, parameterObject, rowBounds);
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
    }

    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(sql, page, pageKey);
    }

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        //这个方法即使不分页也会被执行，所以要判断 null
        AbstractHelperDialect delegate = autoDialect.getDelegate();
        if (delegate != null) {
            return delegate.afterPage(pageList, parameterObject, rowBounds);
        }
        return pageList;
    }

    @Override
    public void afterAll() {
        //这个方法即使不分页也会被执行，所以要判断 null
        AbstractHelperDialect delegate = autoDialect.getDelegate();
        if (delegate != null) {
            delegate.afterAll();
            autoDialect.clearDelegate();
        }
        clearPage();
    }

    @Override
    public BoundSql doBoundSql(BoundSqlInterceptor.Type type, BoundSql boundSql, CacheKey cacheKey) {
        Page<Object> localPage = getLocalPage();
        BoundSqlInterceptor.Chain chain = localPage != null ? localPage.getChain() : null;
        if (chain == null) {
            BoundSqlInterceptor boundSqlInterceptor = localPage != null ? localPage.getBoundSqlInterceptor() : null;
            BoundSqlInterceptor.Chain defaultChain = pageBoundSqlInterceptors != null ? pageBoundSqlInterceptors.getChain() : null;
            if (boundSqlInterceptor != null) {
                chain = new BoundSqlInterceptorChain(defaultChain, Arrays.asList(boundSqlInterceptor));
            } else if (defaultChain != null) {
                chain = defaultChain;
            }
            if (chain == null) {
                chain = DO_NOTHING;
            }
            if (localPage != null) {
                localPage.setChain(chain);
            }
        }
        return chain.doBoundSql(type, boundSql, cacheKey);
    }

    @Override
    public void setProperties(Properties properties) {
        setStaticProperties(properties);
        pageParams = new PageParams();
        autoDialect = new PageAutoDialect();
        pageBoundSqlInterceptors = new PageBoundSqlInterceptors();
        pageParams.setProperties(properties);
        autoDialect.setProperties(properties);
        pageBoundSqlInterceptors.setProperties(properties);
        //20180902新增 aggregateFunctions, 允许手动添加聚合函数（影响行数）
        CountSqlParser.addAggregateFunctions(properties.getProperty("aggregateFunctions"));
        // 异步 asyncCountService 并发度设置，这里默认为应用可用的处理器核心数 * 2，更合理的值应该综合考虑数据库服务器的处理能力
        int asyncCountParallelism = Integer.parseInt(properties.getProperty("asyncCountParallelism",
                "" + (Runtime.getRuntime().availableProcessors() * 2)));
        asyncCountService = new ForkJoinPool(asyncCountParallelism,
                pool -> {
                    final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    worker.setName("pagehelper-async-count-" + worker.getPoolIndex());
                    return worker;
                }, null, true);
    }
}
