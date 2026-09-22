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

package com.github.pagehelper.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlSafeUtilTest {

	@Test
	public void check() {
		assertSql(true, "insert into xx");
		// 无空格
		assertSql(false, "insertxxinto xx");
		assertSql(false, "insert_into");
		assertSql(true, "SELECT aa FROM user");
		// 无空格
		assertSql(true, "SELECT*FROM user");
		// 左空格
		assertSql(true, "SELECT *FROM user");
		// 右空格
		assertSql(true, "SELECT* FROM user");
		// 左tab
		assertSql(true, "SELECT                 *FROM user");
		// 右tab
		assertSql(true, "SELECT*        FROM user");
		assertSql(false, "SELECT*FROMuser");

		// 验证 issue #707 问题
		assertSql(false, "databaseType desc,orderNum desc");
		// 验证 issue #854 问题
		assertSql(true, "case when current_user regexp 0x726f6f74 then uuid else id end ASC");
	}

	private void assertSql(boolean injection, String sql) {
		assertEquals(injection, SqlSafeUtil.check(sql));
	}
}
