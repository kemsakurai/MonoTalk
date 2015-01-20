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
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.seasar.dbflute.twowaysql.DisplaySqlBuilder.DateFormatResource;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

import java.sql.Timestamp;
import java.util.Date;

import monotalk.db.rules.LogRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author jflute
 * @since 0.9.6 (2009/10/27 Tuesday)
 */
@RunWith(RobolectricTestRunner.class)
public class DisplaySqlBuilderTest {

    @Rule
    public LogRule log = new LogRule(getClass());

    // ===================================================================================
    // Display SQL
    // ===========
    @Test
    public void test_buildDisplaySql_basic() {
        // ## Arrange ##
        String sql = "newSelect * from where FOO_CODE = ?";
        String fooCode = "qux";

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[]{fooCode});

        // ## Assert ##
        log.i(actual);
        assertTrue(actual.contains("FOO_CODE = 'qux'"));
    }

    @Test
    public void test_buildDisplaySql_date_default() {
        // ## Arrange ##
        String sql = "newSelect * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("2010/12/12 12:34:56");

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[]{fooDate});

        // ## Assert ##
        log.i(actual);
        assertTrue(actual.contains("FOO_DATE = '2010-12-12'"));
    }

    @Test
    public void test_buildDisplaySql_date_default_bc_basic() {
        // ## Arrange ##
        String sql = "newSelect * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("BC1010/12/12 12:34:56");

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[]{fooDate});

        // ## Assert ##
        log.i(actual);
        assertTrue(actual.contains("FOO_DATE = 'BC1010-12-12'"));
    }

    @Test
    public void test_buildDisplaySql_date_default_bc_limit() {
        // ## Arrange ##
        String sql = "newSelect * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("BC0001/12/12 12:34:56");
        log.i(DfTypeUtil.toString(fooDate, "Gyyyy/MM/dd"));

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[]{fooDate});

        // ## Assert ##
        log.i(actual);
        assertTrue(actual.contains("FOO_DATE = 'BC0001-12-12'"));
    }

    @Test
    public void test_buildDisplaySql_date_format() {
        // ## Arrange ##
        String sql = "newSelect * from where FOO_DATE = ?";
        Date fooDate = DfTypeUtil.toDate("2010/12/12 12:34:56");

        // ## Act ##
        String actual = createTargetWithDateFormat("yyyy@MM@dd").buildDisplaySql(sql, new Object[]{fooDate});

        // ## Assert ##
        log.i(actual);
        assertTrue(actual.contains("FOO_DATE = '2010@12@12'"));
    }

    @Test
    public void test_buildDisplaySql_questionInComment_quotationInComment() {
        // ## Arrange ##
        String sql = "/*foo's bar*/newSelect * from where FOO_NAME/*qux?*/ like ? escape '|' and ? /*quux'?*/and ?";
        String fooName = "fooName%";
        String barCode = "barCode";
        Integer bazId = 3;

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[]{fooName, barCode, bazId});

        // ## Assert ##
        log.i(actual);
        assertTrue(Srl.containsAll(
                actual,
                fooName,
                barCode,
                String.valueOf(bazId),
                "/*foo's bar*/",
                "escape '|'",
                "/*qux?*/",
                "/*quux'?*/and"));
    }

    @Test
    public void test_buildDisplaySql_beforeComment_quotationOverComment() {
        // ## Arrange ##
        String sql = "' ?/*foo's bar*/newSelect * from where FOO_NAME like ? escape '|'";
        String fooName = "fooName%";
        String barCode = "barCode";

        // ## Act ##
        String actual = createTarget().buildDisplaySql(sql, new Object[]{fooName, barCode});

        // ## Assert ##
        log.i(actual);
        assertTrue(Srl.containsAll(actual, fooName, barCode, "/*foo's bar*/", "escape '|'"));
    }

    // ===================================================================================
    // BindVariable
    // ============
    @Test
    public void test_getBindVariableText_dateFormat_basic() {
        // ## Arrange ##
        Date date = DfTypeUtil.toDate("2009-10-27");

        // ## Act ##
        String actual = createTarget().getBindVariableText(date);

        // ## Assert ##
        assertEquals("'2009-10-27'", actual);
    }

    @Test
    public void test_getBindVariableText_dateFormat_custom() {
        // ## Arrange ##
        String format = "date $df:{yyyy-MM-dd}";
        Date date = DfTypeUtil.toDate("2009-10-27");

        // ## Act ##
        String actual = createTargetWithDateFormat(format).getBindVariableText(date);

        // ## Assert ##
        assertEquals("date '2009-10-27'", actual);
    }

    @Test
    public void test_getBindVariableText_timestampFormat_basic() {
        // ## Arrange ##
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        Timestamp timestamp = DfTypeUtil.toTimestamp("2009-10-27 16:22:23.123");

        // ## Act ##
        String actual = createTargetWithTimestampFormat(format).getBindVariableText(timestamp);

        // ## Assert ##
        assertEquals("'2009-10-27 16:22:23.123'", actual);
    }

    @Test
    public void test_getBindVariableText_timestampFormat_custom() {
        // ## Arrange ##
        String format = "timestamp $df:{yyyy-MM-dd HH:mm:ss.SSS}";
        Timestamp timestamp = DfTypeUtil.toTimestamp("2009-10-27 16:22:23.123");

        // ## Act ##
        String actual = createTargetWithTimestampFormat(format).getBindVariableText(timestamp);

        // ## Assert ##
        assertEquals("timestamp '2009-10-27 16:22:23.123'", actual);
    }

    // ===================================================================================
    // DateFormat
    // ==========
    @Test
    public void test_analyzeDateFormat_basic() {
        // ## Arrange ##
        String format = "yyyy-MM-dd";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals(format, resource.getFormat());
        assertNull(resource.getPrefix());
        assertNull(resource.getSuffix());
    }

    @Test
    public void test_analyzeDateFormat_markOnly() {
        // ## Arrange ##
        String format = "$df:{yyyy-MM-dd}";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("", resource.getPrefix());
        assertEquals("", resource.getSuffix());
    }

    @Test
    public void test_analyzeDateFormat_prefixOnly() {
        // ## Arrange ##
        String format = "date $df:{yyyy-MM-dd}";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("date ", resource.getPrefix());
        assertEquals("", resource.getSuffix());
    }

    @Test
    public void test_analyzeDateFormat_suffixOnly() {
        // ## Arrange ##
        String format = "$df:{yyyy-MM-dd}sufsuf";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("", resource.getPrefix());
        assertEquals("sufsuf", resource.getSuffix());
    }

    @Test
    public void test_analyzeDateFormat_prefixSuffix() {
        // ## Arrange ##
        String format = "FOO($df:{yyyy-MM-dd}, 'BAR')";

        // ## Act ##
        DateFormatResource resource = createTarget().analyzeDateFormat(format);

        // ## Assert ##
        assertEquals("yyyy-MM-dd", resource.getFormat());
        assertEquals("FOO(", resource.getPrefix());
        assertEquals(", 'BAR')", resource.getSuffix());
    }

    // ===================================================================================
    // Quote Helper
    // ============
    @Test
    public void test_quote_basic() {
        assertEquals("'foo'", createTarget().quote("foo"));
    }

    @Test
    public void test_quote_with_DateFormatResource() {
        // ## Arrange ##
        DateFormatResource resource = new DateFormatResource();
        resource.setPrefix("prepre");
        resource.setSuffix("sufsuf");

        // ## Act & Assert ##
        assertEquals("prepre'foo'sufsuf", createTarget().quote("foo", resource));
    }

    // ===================================================================================
    // Test Helper
    // ===========
    protected DisplaySqlBuilder createTarget() {
        return doCreateTarget(null, null);
    }

    protected DisplaySqlBuilder createTargetWithDateFormat(String logDateFormat) {
        return doCreateTarget(logDateFormat, null);
    }

    protected DisplaySqlBuilder createTargetWithTimestampFormat(String logTimestampFormat) {
        return doCreateTarget(null, logTimestampFormat);
    }

    protected DisplaySqlBuilder doCreateTarget(String logDateFormat, String logTimestampFormat) {
        return new DisplaySqlBuilder(logDateFormat, logTimestampFormat);
    }
}
