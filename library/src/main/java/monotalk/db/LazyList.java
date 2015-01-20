/*******************************************************************************
 * Copyright (C) 2012-2013 Kem
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
 ******************************************************************************/
package monotalk.db;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import monotalk.db.utility.ConvertUtils;

/**
 * @param <T>
 * @author Kem
 */
public class LazyList<T extends Entity> implements List<T>, Closeable {
    private final Cursor mCursor;
    /**
     * Tableクラス
     */
    private final Class<T> element;

    /**
     * コンストラクター
     *
     * @param cursor
     */
    public LazyList(Cursor cursor, Class<T> clazz) {
        mCursor = cursor;
        element = clazz;
    }

    @Override
    public void close() throws IOException {
        mCursor.close();
    }

    @Override
    public void add(int location, T object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean add(T object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean contains(Object object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public T get(int location) {
        if (!mCursor.moveToPosition(location)) {
            throw new IndexOutOfBoundsException("List index out of bounds");
        }
        return getItem();
    }

    @Override
    public int indexOf(Object object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean isEmpty() {
        return !mCursor.moveToFirst();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public int lastIndexOf(Object object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public ListIterator<T> listIterator(int location) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public T remove(int location) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public T set(int location, T object) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public int size() {
        return mCursor.getCount();
    }

    @Override
    public List<T> subList(int start, int end) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    @Override
    public <E> E[] toArray(E[] array) {
        throw new UnsupportedOperationException("This Method is Unsupported!!!");
    }

    private T getItem() {
        T model = ConvertUtils.toEntity(element, mCursor, null);
        return model;
    }
}