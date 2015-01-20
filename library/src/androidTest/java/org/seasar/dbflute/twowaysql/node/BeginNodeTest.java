/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.twowaysql.node;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.seasar.dbflute.twowaysql.SqlAnalyzer;
import org.seasar.dbflute.twowaysql.context.CommandContext;
import org.seasar.dbflute.twowaysql.context.CommandContextCreator;
import org.seasar.dbflute.twowaysql.exception.IfCommentNotFoundPropertyException;

import monotalk.db.rules.LogRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author jflute
 * @since 0.9.7.0 (2010/05/29 Saturday)
 */
@RunWith(RobolectricTestRunner.class)
public class BeginNodeTest {

    @Rule
    public LogRule log = new LogRule(getClass());

    // ===================================================================================
    // Basic
    // =====
    @Test
    public void test_parse_BEGIN_for_where_all_true() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where member.MEMBER_ID = 3 and member.MEMBER_NAME = 'TEST'";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_where_all_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_where_either_true() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("pmb");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where  member.MEMBER_NAME = 'TEST'";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_where_either_true_log() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where" + log.ln();
        sql = sql + " /*IF pmb.memberId != null*/" + log.ln();
        sql = sql + " member.MEMBER_ID = 3" + log.ln();
        sql = sql + " /*END*/" + log.ln();
        sql = sql + " /*IF pmb.memberName != null*/" + log.ln();
        sql = sql + " and member.MEMBER_NAME = 'TEST'" + log.ln();
        sql = sql + " /*END*/" + log.ln();
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("pmb");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where" + log.ln() + " " + log.ln() + " member.MEMBER_NAME = 'TEST'" + log.ln() + " "
                + log.ln();
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_where_either_true_or() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/or member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("pmb");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where  member.MEMBER_NAME = 'TEST'";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_select_all_true() {
        // ## Arrange ##
        String sql = "newSelect /*BEGIN*/";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/, member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "newSelect member.MEMBER_ID as c1, member.MEMBER_NAME as c2";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_select_all_false() {
        // ## Arrange ##
        String sql = "newSelect /*BEGIN*/";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/, member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "newSelect ";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_for_select_either_true() {
        // ## Arrange ##
        String sql = "newSelect /*BEGIN*/";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/, member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "newSelect member.MEMBER_NAME as c2";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_not_adjustConnector_direct() {
        // ## Arrange ##
        String sql = "newSelect /*BEGIN*/, ";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/, member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "newSelect , member.MEMBER_NAME as c2";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_not_adjustConnector_space() {
        // ## Arrange ##
        String sql = "newSelect /*BEGIN*/foo ";
        sql = sql + "/*IF pmb.memberId != null*/member.MEMBER_ID as c1/*END*/";
        sql = sql + "/*IF pmb.memberName != null*/" + log.ln() + " , member.MEMBER_NAME as c2/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "newSelect foo member.MEMBER_NAME as c2";
        assertEquals(expected, ctx.getSql());
    }

    // ===================================================================================
    // Nested
    // ======
    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_true() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "FIXED2 /*IF true*/and BBB/*END*/ /*IF true*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        // Basically Unsupported!
        assertEquals("where FIXED FIXED2 BBB and CCC", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "FIXED2 /*IF false*/and BBB/*END*/ /*IF false*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        // Basically Unsupported!
        assertEquals("where FIXED ", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_allnest_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "FIXED2 /*IF false*/and BBB/*END*/ /*IF false*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        // Basically Unsupported!
        assertEquals("", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_toponly_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "FIXED2 /*IF true*/and BBB/*END*/ /*IF true*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        assertEquals("where  FIXED2 BBB and CCC", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_toponly_false_either_true() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "FIXED2 /*IF false*/and BBB/*END*/ /*IF true*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        assertEquals("where  FIXED2  CCC", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_nest_and_adjustment() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "FIXED2 /*IF false*/and BBB/*END*/ /*IF true*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        // Basically Unsupported!
        assertEquals("where FIXED FIXED2  CCC", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_BEGIN_self_and_adjustment() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/";
        sql = sql + "and FIXED2 /*IF false*/and BBB/*END*/ /*IF true*/and CCC/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);

        // Basically Unsupported!
        assertEquals("where  FIXED2  CCC", ctx.getSql());
    }

    // ===================================================================================
    // IF Nested
    // =========
    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_root_has_and() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where  AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_root_has_no_and() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_root_has_both() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where  AAA and BBB and CCC and DDD where  AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_fixed_condition_() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " 1 = 1";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "and FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "and AAA /*IF true*/and BBB /*IF true*/and CCC/*END*//*END*/ /*IF true*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where 1 = 1 AAA and BBB and CCC and DDD";
        assertEquals(expected, ctx.getSql()); // BEGIN comment does not need in
        // this pattern
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_all_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "AAA /*IF false*/and BBB /*IF false*/and CCC/*END*//*END*/ /*IF false*/and DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        assertEquals("", ctx.getSql());
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_nonsense_all_true() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*//*IF true*/and AAA/*END*//*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/and BBB/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        assertEquals("where AAA and BBB", ctx.getSql()); // basically nonsense
    }

    @Test
    public void test_parse_BEGIN_that_has_nested_IFIF_nonsense_nested_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*//*IF false*/and AAA/*END*//*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/and BBB/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberId(3);
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        assertEquals("where  and BBB", ctx.getSql()); // basically nonsense
    }

    // ===================================================================================
    // UpperCase
    // =========
    @Test
    public void test_parse_BEGIN_where_upperCase_that_has_nested_IFIF_root_has_and() {
        // ## Arrange ##
        String sql = "/*BEGIN*/WHERE";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "AND AAA /*IF true*/AND BBB /*IF true*/AND CCC/*END*//*END*/ /*IF true*/AND DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "WHERE  AAA AND BBB AND CCC AND DDD";
        assertEquals(expected, ctx.getSql());
    }

    // ===================================================================================
    // OR
    // ==
    @Test
    public void test_parse_BEGIN_where_upperCase_that_has_nested_IFIF_root_has_or() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberId != null*/";
        sql = sql + "FIXED";
        sql = sql + "/*END*/";
        sql = sql + " ";
        sql = sql + "/*IF pmb.memberName != null*/";
        sql = sql + "or AAA /*IF true*/and BBB /*IF true*/OR CCC/*END*//*END*/ /*IF true*/or DDD/*END*/";
        sql = sql + "/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        Node rootNode = analyzer.analyze();

        // ## Assert ##
        MockMemberPmb pmb = new MockMemberPmb();
        pmb.setMemberName("foo");
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);
        log.i("ctx:" + ctx);
        String expected = "where  AAA and BBB OR CCC or DDD";
        assertEquals(expected, ctx.getSql());
    }

    // ===================================================================================
    // NotFound Property
    // =================
    @Test
    public void test_parse_BEGIN_IF_notFoundProperty_basic() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.wrongMemberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'foo'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        try {
            MockMemberPmb pmb = new MockMemberPmb();
            pmb.setMemberName("foo");
            Node rootNode = analyzer.analyze();
            CommandContext ctx = createCtx(pmb);
            rootNode.accept(ctx);

            // ## Assert ##
            fail();
        } catch (IfCommentNotFoundPropertyException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    @Test
    public void test_parse_BEGIN_IF_notFoundProperty_with_likeSearch() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.wrongMemberId != null*/member.MEMBER_ID = /*pmb.memberId*/3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME like /*pmb.memberName*/'foo%'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        try {
            MockLikeSearchMemberPmb pmb = new MockLikeSearchMemberPmb();
            Node rootNode = analyzer.analyze();
            CommandContext ctx = createCtx(pmb);
            rootNode.accept(ctx);

            // ## Assert ##
            fail();
        } catch (IfCommentNotFoundPropertyException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    // @Test public void
    // test_parse_BEGIN_IF_notFoundProperty_with_parameterMap() {
    // // ## Arrange ##
    // String sql = "/*BEGIN*/where";
    // sql = sql +
    // " /*IF pmb.wrongMemberId != null*/member.MEMBER_ID = /*pmb.memberId*/3/*END*/";
    // sql = sql +
    // " /*IF pmb.memberName != null*/and member.MEMBER_NAME like /*pmb.memberName*/'foo%'/*END*/";
    // sql = sql + "/*END*/";
    // SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);
    //
    // // ## Act ##
    // try {
    // MockPagingMemberPmb pmb = new MockPagingMemberPmb();
    // Node rootNode = analyzer.analyze();
    // CommandContext ctx = createCtx(pmb);
    // rootNode.accept(ctx);
    //
    // // ## Assert ##
    // fail();
    // } catch (IfCommentNotFoundPropertyException e) {
    // // OK
    // log(e.getMessage());
    // }
    // }

    @Test
    public void test_parse_BEGIN_BIND_notFoundProperty_IF_false() {
        // ## Arrange ##
        String sql = "/*BEGIN*/where";
        sql = sql + " /*IF pmb.memberId != null*/member.MEMBER_ID = /*pmb.wrongMemberId*/3/*END*/";
        sql = sql + " /*IF pmb.memberName != null*/and member.MEMBER_NAME like /*pmb.memberName*/'foo%'/*END*/";
        sql = sql + "/*END*/";
        SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);

        // ## Act ##
        MockMemberPmb pmb = new MockMemberPmb();
        Node rootNode = analyzer.analyze();
        CommandContext ctx = createCtx(pmb);
        rootNode.accept(ctx);

        // ## Assert ##
        assertEquals("", ctx.getSql());
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
