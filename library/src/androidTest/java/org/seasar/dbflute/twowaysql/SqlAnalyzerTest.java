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

import org.junit.Test;
import org.seasar.dbflute.twowaysql.node.SqlPartsNode;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/08 Wednesday)
 */
public class SqlAnalyzerTest {

    @Test
    public void test_createSqlNode() {
        // ## Arrange ##
        SqlAnalyzer analyzer = new SqlAnalyzer("foobar", false);

        // ## Act ##
        SqlPartsNode node = analyzer.createSqlPartsNode("foo");

        // ## Assert ##
        org.junit.Assert.assertEquals("foo", node.getSqlParts());
    }

    @Test
    public void testSqlAnalyzer() throws Exception {
        // ## Arrange ##
        String sql = "foobar";
        boolean blockNullParameter = true;
        SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(sql, blockNullParameter);

        // ## Arrange ##
        SqlPartsNode node = sqlAnalyzer.createSqlPartsNode("foo");

        // ## Assert ##
        org.junit.Assert.assertEquals("foo", node.getSqlParts());
    }
    // *detail tests for analyze() are moved to node tests
}
