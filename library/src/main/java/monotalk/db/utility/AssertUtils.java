/*
 * Copyright (C) 2007 The Guava Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package monotalk.db.utility;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.List;

import monotalk.db.DatabaseConfigration;
import monotalk.db.Entity;
import monotalk.db.annotation.Id;
import monotalk.db.exception.IllegalAnnotationStateException;

public class AssertUtils {

    private static boolean assertEntity = false;

    public static void setAssertEntity(boolean value) {
        assertEntity = value;
    }

    public static boolean canAssertEntity() {
        return assertEntity;
    }

    public static void assertAnnotation(DatabaseConfigration config) {
        List<Class<? extends Entity>> classes = null;
        if (config.getEntityList() != null && !config.getEntityList().isEmpty()) {
            classes = config.getEntityList();
        } else {
            throw new IllegalStateException("packageName not Supported!!!");
        }
        for (Class<? extends Entity> entity : classes) {
            for (Field field : entity.getDeclaredFields()) {
                int count = 0;
                if (field.isAnnotationPresent(Id.class)) {
                    count++;
                }
                if (count > 0) {
                    throw new IllegalAnnotationStateException("Id annotation can have only one definition...");
                }
            }
        }
    }

    public static <T> List<T> assertNotNullOrNotEmpty(List<T> reference, @Nullable Object errorMessage) {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static <T> T assertNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static void assertArgument(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static String assertNotEmpty(String reference, @Nullable Object errorMessage) {
        if (TextUtils.isEmpty(reference)) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
        return reference;
    }
}
