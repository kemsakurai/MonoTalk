/*******************************************************************************
 * Copyright (C) 2013-2015 Kem
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

import monotalk.db.rowmapper.EntityRowMapper;

/**
 * @param <T>
 * @author Kem
 */
public class LazyList<T extends Entity> implements List<T>, Closeable {
    private final Cursor mCursor;
    private final EntityRowMapper rowMapper;

    /**
     * コンストラクター
     *
     * @param cursor
     */
    public LazyList(Cursor cursor, Class<T> clazz) {
        mCursor = cursor;
        rowMapper = new EntityRowMapper<T>(clazz);
    }

    @Override
    public void close() throws IOException {
        mCursor.close();
    }

    @Override
    public int size() {
        return mCursor.isClosed() ? 0 : mCursor.getCount();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public T get(int location) {
        if (mCursor.moveToPosition(location)) {
            T model = (T) rowMapper.mapRow(mCursor);
            return model;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int location) {
        if (isEmpty()) {
            return new EmptyListIterator<T>();
        }
        return new LazyListIterator(location);
    }

    // Unsupported methods

    @Override
    public void add(int location, T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int location, T object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<T> subList(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        throw new UnsupportedOperationException();
    }

    private static class EmptyListIterator<T> implements ListIterator<T> {
        @Override
        public void add(T object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public T next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T previous() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T object) {
            throw new UnsupportedOperationException();
        }
    }

    private class LazyListIterator implements ListIterator<T> {
        private int index;

        public LazyListIterator(int location) {
            this.index = location;
        }

        @Override
        public void add(T object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return index < mCursor.getCount();
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public T next() {
            return get(index++);
        }

        @Override
        public int nextIndex() {
            return index + 1;
        }

        @Override
        public T previous() {
            return get(--index);
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T object) {
            throw new UnsupportedOperationException();
        }
    }
}