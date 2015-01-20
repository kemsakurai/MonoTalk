package monotalk.db.query;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.UriUtils;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * Created by Kem on 2015/01/14.
 */
public class InsertOperationBuilder<T extends Entity> {
    private ContentProviderOperation.Builder builder;

    public InsertOperationBuilder(Uri authorityUri, Class<T> clazz) {
        assertNotNull(authorityUri, "authorityUri is null");
        assertNotNull(clazz, "clazz is null");
        Uri uri = UriUtils.buildEntityUri(authorityUri, MonoTalk.getTableName(clazz));
        builder = ContentProviderOperation.newInsert(uri);
    }

    public InsertOperationBuilder withValueBackReference(String key, int previousResult) {
        builder.withValueBackReference(key, previousResult);
        return this;
    }

    public InsertOperationBuilder withValues(T entity) {
        builder.withValues(QueryBuilder.toValues(entity));
        return this;
    }

    public InsertOperationBuilder withValues(T entity, String... includesColumns) {
        builder.withValues(QueryBuilder.toValues(entity, includesColumns));
        return this;
    }

    public InsertOperationBuilder withValuesExcludesNull(T entity) {
        builder.withValues(QueryBuilder.toValues(entity));
        return this;
    }

    public InsertOperationBuilder withValues(ContentValues values) {
        builder.withValues(values);
        return this;
    }

    public InsertOperationBuilder withValueBackReferences(ContentValues backReferences) {
        builder.withValueBackReferences(backReferences);
        return this;
    }

    public InsertOperationBuilder withValue(String key, Object value) {
        ContentValues values = new ContentValues();
        TypeConverter typeConverter = TypeConverterCache.getTypeConverter(value.getClass());
        if (typeConverter != null) {
            typeConverter.pack(value, values, key);
        }
        builder.withValues(values);
        return this;
    }

    public InsertOperationBuilder withYieldAllowed(boolean yieldAllowed) {
        builder.withYieldAllowed(yieldAllowed);
        return this;
    }

    public ContentProviderOperation build() {
        return builder.build();
    }
}
