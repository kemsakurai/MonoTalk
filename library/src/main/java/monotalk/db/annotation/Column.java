/*******************************************************************************
 * Copyright (C) 2012-2015 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package monotalk.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Columnアノテーション
 *
 * @author Kem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * カラム名
     *
     * @return
     */
    public String name();

    /**
     * デフォルト値の定義
     * defaultValueの指定があった場合も、insert value指定のnullは有効。
     * nullable=falseの場合は、null制約となるため、null制約エラー(ON CONFLICT ABORT)となる
     *
     * @return
     */
    public String defaultValue() default "";

    /**
     * falseの場合、Null指定不可でカラムを作成する
     * ConflictClauseは、ConflictClause.NONE(デフォルト動作:ON CONFLICT ABORT)として動作する
     */
    public boolean nullable() default true;

    /**
     * trueの場合、
     * ConflictClauseは、ConflictClause.NONE(デフォルト動作:ON CONFLICT ABORT)として動作する
     */
    public boolean unique() default false;

}