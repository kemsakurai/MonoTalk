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

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.seasar.dbflute.cbean.SimpleMapPmb;
import org.seasar.dbflute.cbean.coption.LikeSearchOption;
import org.seasar.dbflute.twowaysql.exception.BindVariableCommentListIndexNotNumberException;
import org.seasar.dbflute.twowaysql.exception.BindVariableCommentListIndexOutOfBoundsException;
import org.seasar.dbflute.twowaysql.exception.BindVariableCommentNotFoundPropertyException;
import org.seasar.dbflute.twowaysql.exception.ForCommentNotFoundPropertyException;
import org.seasar.dbflute.twowaysql.exception.ForCommentPropertyReadFailureException;
import org.seasar.dbflute.twowaysql.node.ValueAndTypeSetupper.CommentType;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

import java.util.List;
import java.util.Map;

import monotalk.db.rules.LogRule;

/**
 * @author jflute
 */
@RunWith(RobolectricTestRunner.class)
public class ValueAndTypeSetupperTest extends TestCase {
    @Rule
    public LogRule log = new LogRule(getClass());

    public ValueAndTypeSetupperTest() {

    }

    // ===================================================================================
    //                                                                                Bean
    //                                                                                ====
    @Test
    public void test_setupValueAndType_bean_basic() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberId");
        MockPmb pmb = new MockPmb();
        pmb.setMemberId(3);
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getLikeSearchOption());
    }

    @Test
    public void test_setupValueAndType_bean_likeSearch() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberName");
        MockPmb pmb = new MockPmb();
        pmb.setMemberName("f|o%o");
        pmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getLikeSearchOption().getRearOption());
    }

    @Test
    public void test_setupValueAndType_bean_likeSearch_notFound() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberName");
        MockPmb pmb = new MockPmb();
        pmb.setMemberName("f|o%o");
        //pmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);

        // ## Assert ##
        assertEquals("f|o%o", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
    }

    @Test
    public void test_setupValueAndType_bean_likeSearch_split() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberName");
        MockPmb pmb = new MockPmb();
        pmb.setMemberName("f|o%o");
        pmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likePrefix().splitByPipeLine());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType); // no check here
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
    }

    @Test
    public void test_setupValueAndType_bean_nest() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.nestPmb.memberId");
        MockPmb nestPmb = new MockPmb();
        nestPmb.setMemberId(3);
        MockPmb pmb = new MockPmb();
        pmb.setNestPmb(nestPmb);
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getLikeSearchOption());
    }

    @Test
    public void test_setupValueAndType_bean_nest_likeSearch_basic() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.nestLikePmb.memberName");
        MockPmb nestLikePmb = new MockPmb();
        nestLikePmb.setMemberName("f|o%o");
        MockPmb pmb = new MockPmb();
        pmb.setNestLikePmb(nestLikePmb);
        pmb.setNestLikePmbInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getLikeSearchOption().getRearOption());
    }

    @Test
    public void test_setupValueAndType_bean_nest_likeSearch_override() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.nestLikePmb.memberName");
        MockPmb nestLikePmb = new MockPmb();
        nestLikePmb.setMemberName("f|o%o");
        nestLikePmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likeContain());
        MockPmb pmb = new MockPmb();
        pmb.setNestLikePmb(nestLikePmb);
        pmb.setNestLikePmbInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("%f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getLikeSearchOption().getRearOption());
    }

    @Test
    public void test_setupValueAndType_bean_propertyReadFailure() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsForComment("pmb.memberId");
        MockPmb pmb = new MockPmb() {
            @Override
            public Integer getMemberId() { // not accessible
                return super.getMemberId();
            }
        };
        pmb.setMemberId(3);
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.setupValueAndType(valueAndType);

            // ## Assert ##
            fail();
        } catch (ForCommentPropertyReadFailureException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    @Test
    public void test_setupValueAndType_bean_notFoundProperty() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsForComment("pmb.memberIo");
        MockPmb pmb = new MockPmb();
        pmb.setMemberId(3);
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.setupValueAndType(valueAndType);

            // ## Assert ##
            fail();
        } catch (ForCommentNotFoundPropertyException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    @Test
    public void test_setupValueAndType_list_likeSearch() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberNameList.get(1)");
        MockPmb pmb = new MockPmb();
        pmb.setMemberNameList(DfCollectionUtil.newArrayList("f|oo", "ba%r", "b|a%z"));
        pmb.setMemberNameListInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("ba|%r%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getLikeSearchOption().getRearOption());
    }

    @Test
    public void test_setupValueAndType_list_notNumber() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberNameList.get(index)");
        MockPmb pmb = new MockPmb();
        pmb.setMemberNameList(DfCollectionUtil.newArrayList("f|oo", "ba%r", "b|a%z"));
        pmb.setMemberNameListInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.setupValueAndType(valueAndType);

            // ## Assert ##
            fail();
        } catch (BindVariableCommentListIndexNotNumberException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    @Test
    public void test_setupValueAndType_list_outOfBounds() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberNameList.get(4)");
        MockPmb pmb = new MockPmb();
        pmb.setMemberNameList(DfCollectionUtil.newArrayList("f|oo", "ba%r", "b|a%z"));
        pmb.setMemberNameListInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.setupValueAndType(valueAndType);

            // ## Assert ##
            fail();
        } catch (BindVariableCommentListIndexOutOfBoundsException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                              MapPmb
    //                                                                              ======
    @Test
    public void test_setupValueAndType_mappmb_basic() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberId");
        SimpleMapPmb<Integer> pmb = new SimpleMapPmb<Integer>();
        pmb.addParameter("memberId", 3);
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getLikeSearchOption());
    }

    @Test
    public void test_setupValueAndType_mappmb_likeSearch() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberName");
        SimpleMapPmb<Object> pmb = new SimpleMapPmb<Object>();
        pmb.addParameter("memberId", 3);
        pmb.addParameter("memberName", "f|o%o");
        pmb.addParameter("memberNameInternalLikeSearchOption", new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getLikeSearchOption().getRearOption());
    }

    @Test
    public void test_setupValueAndType_mappmb_notKey() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberId");
        SimpleMapPmb<Integer> pmb = new SimpleMapPmb<Integer>();
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.setupValueAndType(valueAndType);

            // ## Assert ##
            fail();
        } catch (BindVariableCommentNotFoundPropertyException e) {
            // OK
            log.i(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                                 Map
    //                                                                                 ===
    @Test
    public void test_setupValueAndType_map_basic() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberId");
        Map<String, Object> pmb = DfCollectionUtil.newHashMap();
        pmb.put("memberId", 3);
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getLikeSearchOption());
    }

    @Test
    public void test_setupValueAndType_map_likeSearch() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberName");
        Map<String, Object> pmb = DfCollectionUtil.newHashMap();
        pmb.put("memberId", 3);
        pmb.put("memberName", "f|o%o");
        pmb.put("memberNameInternalLikeSearchOption", new LikeSearchOption().likePrefix());
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getLikeSearchOption().getRearOption());
    }

    @Test
    public void test_setupValueAndType_map_notKey() {
        // ## Arrange ##
        ValueAndTypeSetupper setupper = createTargetAsBind("pmb.memberId");
        Map<String, Object> pmb = DfCollectionUtil.newHashMap();
        ValueAndType valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.setupValueAndType(valueAndType);

        // ## Assert ##
        assertEquals(null, valueAndType.getTargetValue());
        assertEquals(null, valueAndType.getTargetType());
        assertNull(valueAndType.getLikeSearchOption());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected ValueAndTypeSetupper createTargetAsBind(String expression) {
        CommentType type = CommentType.BIND;
        return new ValueAndTypeSetupper(Srl.splitList(expression, "."), expression, "newSelect * from ...", type);
    }

    protected ValueAndTypeSetupper createTargetAsForComment(String expression) {
        CommentType type = CommentType.FORCOMMENT;
        return new ValueAndTypeSetupper(Srl.splitList(expression, "."), expression, "newSelect * from ...", type);
    }

    protected ValueAndType createTargetAndType(Object value) {
        ValueAndType valueAndType = new ValueAndType();
        valueAndType.setFirstValue(value);
        valueAndType.setFirstType(value.getClass());
        return valueAndType;
    }

    protected static class MockPmb {
        protected Integer _memberId;
        protected String _memberName;
        protected LikeSearchOption _memberNameInternalLikeSearchOption;
        protected List<String> _memberNameList;
        protected LikeSearchOption _memberNameListInternalLikeSearchOption;
        protected MockPmb _nestPmb;
        protected MockPmb _nestLikePmb;
        protected LikeSearchOption _nestLikePmbInternalLikeSearchOption;

        public Integer getMemberId() {
            return _memberId;
        }

        @Test
        public void setMemberId(Integer memberId) {
            this._memberId = memberId;
        }

        public String getMemberName() {
            return _memberName;
        }

        @Test
        public void setMemberName(String memberName) {
            this._memberName = memberName;
        }

        public LikeSearchOption getMemberNameInternalLikeSearchOption() {
            return _memberNameInternalLikeSearchOption;
        }

        @Test
        public void setMemberNameInternalLikeSearchOption(LikeSearchOption memberNameInternalLikeSearchOption) {
            this._memberNameInternalLikeSearchOption = memberNameInternalLikeSearchOption;
        }

        public List<String> getMemberNameList() {
            return _memberNameList;
        }

        @Test
        public void setMemberNameList(List<String> memberNameList) {
            this._memberNameList = memberNameList;
        }

        public LikeSearchOption getMemberNameListInternalLikeSearchOption() {
            return _memberNameListInternalLikeSearchOption;
        }

        @Test
        public void setMemberNameListInternalLikeSearchOption(LikeSearchOption memberNameListInternalLikeSearchOption) {
            this._memberNameListInternalLikeSearchOption = memberNameListInternalLikeSearchOption;
        }

        public MockPmb getNestPmb() {
            return _nestPmb;
        }

        @Test
        public void setNestPmb(MockPmb nestPmb) {
            this._nestPmb = nestPmb;
        }

        public MockPmb getNestLikePmb() {
            return _nestLikePmb;
        }

        @Test
        public void setNestLikePmb(MockPmb nestLikePmb) {
            this._nestLikePmb = nestLikePmb;
        }

        public LikeSearchOption getNestLikePmbInternalLikeSearchOption() {
            return _nestLikePmbInternalLikeSearchOption;
        }

        @Test
        public void setNestLikePmbInternalLikeSearchOption(LikeSearchOption nestLikePmbInternalLikeSearchOption) {
            this._nestLikePmbInternalLikeSearchOption = nestLikePmbInternalLikeSearchOption;
        }
    }
}
