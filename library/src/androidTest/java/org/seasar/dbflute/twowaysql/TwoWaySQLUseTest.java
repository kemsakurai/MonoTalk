/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.seasar.dbflute.twowaysql;

import org.junit.Rule;
import org.junit.Test;
import org.seasar.dbflute.cbean.SimpleMapPmb;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.context.CommandContextCreator;
import org.seasar.dbflute.twowaysql.factory.DefaultSqlAnalyzerFactory;
import org.seasar.dbflute.twowaysql.factory.SqlAnalyzerFactory;
import org.seasar.dbflute.twowaysql.node.Node;

import monotalk.db.rules.LogRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 2WAYSQLの使用方法を確認するためのテストケース
 */
public class TwoWaySQLUseTest {
    @Rule
    public LogRule log = new LogRule(TwoWaySQLUseTest.class);

    // # pmb付きでないコメントパラメータはサポートしない
    @Test
    public void test_SqlParserImpl_ReplaceLiteral_NoPrefix() {
        StringBuffer varname1 = new StringBuffer();
        varname1.append("SELECT * FROM BOOK ");
        varname1.append("WHERE AUTHOR = /*author*/'Naoki Takezoe' ");
        varname1.append("ORDER BY BOOK_ID ASC");

        // ## Node Create ##
        SqlAnalyzerFactory factory = new DefaultSqlAnalyzerFactory();
        SqlAnalyzer sqlAnalyzer = factory.create(varname1.toString(), false);
        Node node = sqlAnalyzer.analyze();

        // ## Arrange ##
        SimpleMapPmb<String> pmb = new SimpleMapPmb<String>();
        pmb.addParameter("author", "Kem");
        CommandContext createCtx = createCtx(pmb);
        try {
            node.accept(createCtx);
            fail();
        } catch (RuntimeException e) {
            log.i("Raise Expected RuntimeException" + e.toString());
        }
        log.i("ctx#getArg().length>>>" + createCtx.getBindVariables().length);
        log.i("ctx#getSql>>>" + createCtx.getSql());
    }

    // # pmb付きのコメントパラメータはサポートする
    @Test
    public void test_SqlParserImpl_ReplaceLiteral_Prefix() {
        StringBuffer varname1 = new StringBuffer();
        varname1.append("SELECT * FROM BOOK ");
        varname1.append("WHERE AUTHOR = /*pmb.author*/'Naoki Takezoe' ");
        varname1.append("ORDER BY BOOK_ID ASC");

        // ## Node Create ##
        SqlAnalyzerFactory factory = new DefaultSqlAnalyzerFactory();
        SqlAnalyzer sqlAnalyzer = factory.create(varname1.toString(), false);
        Node node = sqlAnalyzer.analyze();

        // ## Arrange ##
        SimpleMapPmb<String> pmb = new SimpleMapPmb<String>();
        pmb.addParameter("author", "Kem");
        CommandContext createCtx = createCtx(pmb);
        node.accept(createCtx);

        log.i("ctx#getArg().length>>>" + createCtx.getBindVariables()[0]);
        log.i("ctx#getSql>>>" + createCtx.getSql());

        StringBuffer expected = new StringBuffer();
        expected.append("SELECT * FROM BOOK ");
        expected.append("WHERE AUTHOR = ? ");
        expected.append("ORDER BY BOOK_ID ASC");

        // ## Verify ##
        assertEquals("Kem", createCtx.getBindVariables()[0]);
        assertEquals(expected.toString(), createCtx.getSql());
    }

    @Test
    public void test_SqlParserImpl_ReplaceString() {
        StringBuffer varname1 = new StringBuffer();
        varname1.append("SELECT * FROM BOOK ");
        varname1.append("ORDER BY /*$pmb.orderByColumn*/BOOK_ID ASC");

        // ## Node Create ##
        SqlAnalyzerFactory factory = new DefaultSqlAnalyzerFactory();
        SqlAnalyzer sqlAnalyzer = factory.create(varname1.toString(), false);
        Node node = sqlAnalyzer.analyze();

        // ## Arrange ##
        SimpleMapPmb<String> pmb = new SimpleMapPmb<String>();
        pmb.addParameter("orderByColumn", "BOOK_NAME DESC");
        CommandContext createCtx = createCtx(pmb);
        node.accept(createCtx);

        log.i("ctx#getArg().length>>>" + createCtx.getBindVariables().length);
        log.i("ctx#getSql>>>" + createCtx.getSql());

        // ## Wrong SQL
        StringBuffer expected = new StringBuffer();
        expected.append("SELECT * FROM BOOK ");
        expected.append("ORDER BY BOOK_NAME DESC ASC");

        // ## Verify
        assertEquals(expected.toString(), createCtx.getSql());
    }

    // # IFコメントの置換を確認
    @Test
    public void test_SqlParserImpl_ReplaceIfEndNotNull() {
        StringBuffer varname1 = new StringBuffer();
        varname1.append("SELECT * FROM BOOK ");
        varname1.append("/*IF pmb.author != null*/");
        varname1.append("  WHERE AUTHOR = /*pmb.author*/'Naoki Takezoe' ");
        varname1.append("/*END*/");
        varname1.append("ORDER BY BOOK_ID ASC");

        // ## Node Create ##
        SqlAnalyzerFactory factory = new DefaultSqlAnalyzerFactory();
        SqlAnalyzer sqlAnalyzer = factory.create(varname1.toString(), false);
        Node node = sqlAnalyzer.analyze();

        // ## Arrange ##
        SimpleMapPmb<String> pmb = new SimpleMapPmb<String>();
        pmb.addParameter("author", "Kem");
        CommandContext createCtx = createCtx(pmb);
        node.accept(createCtx);

        log.i("ctx#getArg>>>" + createCtx.getBindVariables()[0]);
        log.i("ctx#getSql>>>" + createCtx.getSql());

        StringBuffer expected = new StringBuffer();
        expected.append("SELECT * FROM BOOK ");
        expected.append("  WHERE AUTHOR = ? ");
        expected.append("ORDER BY BOOK_ID ASC");

        // ## Verify ##
        assertEquals("Kem", createCtx.getBindVariables()[0]);
        assertEquals(expected.toString(), createCtx.getSql());
    }

    // ===================================================================================
    // Test Helper
    // ===========
    private CommandContext createCtx(Object pmb) {
        return xcreateCommandContext(new Object[]{pmb}, new String[]{"pmb"}, new Class<?>[]{pmb.getClass()});
    }

    private CommandContext xcreateCommandContext(Object[] args, String[] argNames, Class<?>[] argTypes) {
        return xcreateCommandContextCreator(argNames, argTypes).createCommandContext(args);
    }

    private CommandContextCreator xcreateCommandContextCreator(String[] argNames, Class<?>[] argTypes) {
        return new CommandContextCreator(argNames, argTypes);
    }

}