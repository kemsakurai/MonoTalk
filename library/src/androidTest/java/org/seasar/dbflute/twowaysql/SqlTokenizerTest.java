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
package org.seasar.dbflute.twowaysql;

import org.junit.Rule;
import org.junit.Test;
import org.seasar.dbflute.exception.CommentTerminatorNotFoundException;

import monotalk.db.rules.LogRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/08 Wednesday)
 */
public class SqlTokenizerTest {

    @Rule
    public LogRule log = new LogRule(SqlTokenizerTest.class);

    // ===================================================================================
    // Skip
    // ====
    @Test
    public void test_skipToken() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        tokenizer.skipWhitespace();
        String skippedToken = tokenizer.skipToken();
        tokenizer.skipWhitespace();

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        log.i("before       : " + tokenizer.getBefore());
        log.i("after        : " + tokenizer.getAfter());
        assertEquals("and", skippedToken);
    }

    @Test
    public void test_skipToken_integerTestValue() {
        // ## Arrange ##
        String sql = "/*foo*/123/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken();

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        assertEquals("123", skippedToken);
    }

    @Test
    public void test_skipToken_stringTestValue() {
        // ## Arrange ##
        String sql = "/*foo*/'2001-12-15'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken();

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        assertEquals("'2001-12-15'", skippedToken);
    }

    @Test
    public void test_skipToken_nonTestValue_dateLiteralPrefix() {
        // ## Arrange ##
        String sql = "/*foo*/date '2001-12-15'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken();

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        assertEquals("date", skippedToken);
    }

    @Test
    public void test_skipToken_testValue_dateLiteralPrefix() {
        // ## Arrange ##
        String sql = "/*foo*/date '2001-12-15'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken(true);

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        assertEquals("date '2001-12-15'", skippedToken);
    }

    @Test
    public void test_skipToken_testValue_timestampLiteralPrefix() {
        // ## Arrange ##
        String sql = "/*foo*/timestamp '2001-12-15 12:34:56.123'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        String skippedToken = tokenizer.skipToken(true);

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        assertEquals("timestamp '2001-12-15 12:34:56.123'", skippedToken);
    }

    @Test
    public void test_skipWhitespace() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/ and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        tokenizer.next();
        tokenizer.skipWhitespace();
        String skippedToken = tokenizer.skipToken();
        tokenizer.skipWhitespace();

        // ## Assert ##
        log.i("skippedToken : " + skippedToken);
        log.i("before       : " + tokenizer.getBefore());
        log.i("after        : " + tokenizer.getAfter());
        assertEquals("and", skippedToken);
    }

    @Test
    public void test_extractDateLiteralPrefix_date() {
        // ## Arrange ##
        String sql = "foo/*bar*/date '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("date ", prefix);
    }

    @Test
    public void test_extractDateLiteralPrefix_dateNonSpace() {
        // ## Arrange ##
        String sql = "foo/*bar*/date'2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("date", prefix);
    }

    @Test
    public void test_extractDateLiteralPrefix_timestamp() {
        // ## Arrange ##
        String sql = "foo/*bar*/timestamp '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("timestamp ", prefix);
    }

    @Test
    public void test_extractDateLiteralPrefix_timestampNonSpace() {
        // ## Arrange ##
        String sql = "foo/*bar*/timestamp'2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("timestamp", prefix);
    }

    @Test
    public void test_extractDateLiteralPrefix_dateUpperCase() {
        // ## Arrange ##
        String sql = "foo/*bar*/DATE '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("DATE ", prefix);
    }

    @Test
    public void test_extractDateLiteralPrefix_timestampUpperCase() {
        // ## Arrange ##
        String sql = "foo/*bar*/TIMESTAMP '2009-10-29";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        String prefix = tokenizer.extractDateLiteralPrefix(true, sql, "foo/*bar*/".length());

        // ## Assert ##
        assertEquals("TIMESTAMP ", prefix);
    }

    @Test
    public void test_extractDateLiteralPrefix_nonTarget() {
        // ## Arrange ##
        SqlTokenizer tokenizer = new SqlTokenizer(null);

        // ## Act & Assert ##
        int l = "foo/*bar*/".length();
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/'2009-10-29", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/23", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/abc", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/date foo", l));
        assertEquals(null, tokenizer.extractDateLiteralPrefix(true, "foo/*bar*/timestamp bar", l));
    }

    // ===================================================================================
    // Show Token
    // ==========
    @Test
    public void test_show_next_with_BEGIN_comment() {
        String sql = "newSelect * from MEMBER";
        sql = sql + " /*BEGIN*/";
        sql = sql + " where";
        sql = sql + "   /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + "   /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        sql = sql + " /*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        log.i("01: " + tokenizer._token);
        tokenizer.next();
        log.i("02: " + tokenizer._token);
        tokenizer.next();
        log.i("03: " + tokenizer._token);
        tokenizer.next();
        log.i("04: " + tokenizer._token);
        tokenizer.next();
        log.i("05: " + tokenizer._token);
        tokenizer.next();
        log.i("06: " + tokenizer._token);
        tokenizer.next();
        log.i("07: " + tokenizer._token);
        tokenizer.next();
        log.i("08: " + tokenizer._token);
        tokenizer.next();
        log.i("09: " + tokenizer._token);
        tokenizer.next();
        log.i("10: " + tokenizer._token);
        tokenizer.next();
        log.i("11: " + tokenizer._token);
        tokenizer.next();
        log.i("12: " + tokenizer._token);
        tokenizer.next();
        log.i("13: " + tokenizer._token);
        tokenizer.next();
        log.i("14: " + tokenizer._token);
    }

    @Test
    public void test_show_next_without_BEGIN_comment() {
        String sql = "newSelect * from MEMBER";
        sql = sql + " where";
        sql = sql + "   /*IF pmb.memberId != null*/member.MEMBER_ID = 3/*END*/";
        sql = sql + "   /*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END*/";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        log.i("01: " + tokenizer._token);
        tokenizer.next();
        log.i("02: " + tokenizer._token);
        tokenizer.next();
        log.i("03: " + tokenizer._token);
        tokenizer.next();
        log.i("04: " + tokenizer._token);
        tokenizer.next();
        log.i("05: " + tokenizer._token);
        tokenizer.next();
        log.i("06: " + tokenizer._token);
        tokenizer.next();
        log.i("07: " + tokenizer._token);
        tokenizer.next();
        log.i("08: " + tokenizer._token);
        tokenizer.next();
        log.i("09: " + tokenizer._token);
        tokenizer.next();
        log.i("10: " + tokenizer._token);
        tokenizer.next();
        log.i("11: " + tokenizer._token);
        tokenizer.next();
        log.i("12: " + tokenizer._token);
        tokenizer.next();
        log.i("13: " + tokenizer._token);
        tokenizer.next();
        log.i("14: " + tokenizer._token);
    }

    // ===================================================================================
    // Exception
    // =========
    @Test
    public void test_commentEndNotFound() {
        // ## Arrange ##
        String sql = "/*IF pmb.memberName != null*/and member.MEMBER_NAME = 'TEST'/*END";
        SqlTokenizer tokenizer = new SqlTokenizer(sql);

        // ## Act ##
        try {
            while (SqlTokenizer.EOF != tokenizer.next()) {
            }

            // ## Assert ##
            fail();
        } catch (CommentTerminatorNotFoundException e) {
            // OK
            log.i(e.getMessage());
        }
    }
}
