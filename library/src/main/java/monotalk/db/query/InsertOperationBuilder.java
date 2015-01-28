package monotalk.db.query;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import monotalk.db.Entity;
import monotalk.db.UriUtils;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

import static monotalk.db.query.QueryUtils.from;
import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * Created by Kem on 2015/01/14.
 */
public class InsertOperationBuilder<T extends Entity> {
    private ContentProviderOperation.Builder builder;

    public InsertOperationBuilder(Uri authorityUri, Class<T> clazz) {
        assertNotNull(authorityUri, "authorityUri is null");
        assertNotNull(clazz, "clazz is null");
        Uri uri = UriUtils.buildEntityUri(authorityUri, clazz);
        builder = ContentProviderOperation.newInsert(uri);
    }

    public InsertOperationBuilder valueBackReference(String key, int previousResult) {
        builder.withValueBackReference(key, previousResult);
        return this;
    }

    public InsertOperationBuilder values(T entity) {
        builder.withValues(from(entity));
        return this;
    }

    public InsertOperationBuilder values(T entity, String... includesColumns) {
        builder.withValues(from(entity, includesColumns));
        return this;
    }

    public InsertOperationBuilder valuesExcludesNull(T entity) {
        builder.withValues(from(entity));
        return this;
    }

    public InsertOperationBuilder values(ContentValues values) {
        builder.withValues(values);
        return this;
    }

    public InsertOperationBuilder valueBackReferences(ContentValues backReferences) {
        builder.withValueBackReferences(backReferences);
        return this;
    }

    public InsertOperationBuilder value(String key, Object value) {
        ContentValues values = new ContentValues();
        TypeConverter typeConverter = TypeConverterCache.getTypeConverter(value.getClass());
        if (typeConverter != null) {
            typeConverter.pack(value, values, key);
        }
        builder.withValues(values);
        return this;
    }

    public InsertOperationBuilder yieldAllowed(boolean yieldAllowed) {
        builder.withYieldAllowed(yieldAllowed);
        return this;
    }

    public ContentProviderOperation build() {
        return builder.build();
    }
}
