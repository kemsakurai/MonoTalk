/*
 * Copyright (C) 2014 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monotalk.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * <p>
 * An annotation that indicates a member should define its SQLite column using the NOT NULL constraint. A conflict
 * clause may be defined, but there is none by default. Must be used in conjunction with
 * {@link ollie.annotation.Column}.
 * </p>
 * <p>
 * <a href="http://www.sqlite.org/lang_createtable.html#notnullconst">
 * http://www.sqlite.org/lang_createtable.html#notnullconst
 * </a>
 * <a href="http://www.sqlite.org/syntaxdiagrams.html#column-constraint">
 * http://www.sqlite.org/syntaxdiagrams.html#column-constraint
 * </a>
 * </p>
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNull {
    /**
     * Returns a behaviour when the operation encounters a conflict.
     * Fieldに付与した場合、Null指定不可でカラムを作成する
     * ConflictClauseは、未指定時はConflictClause.NONE(デフォルト動作:ON CONFLICT ABORT)として動作する
     *
     * @return The conflict clause.
     */
    public ConflictClause value() default ConflictClause.NONE;
}