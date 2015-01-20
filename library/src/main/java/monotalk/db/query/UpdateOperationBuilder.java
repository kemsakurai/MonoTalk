package monotalk.db.query;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import monotalk.db.Entity;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

/**
 * Created by Kem on 2015/01/14.
 */
public class UpdateOperationBuilder<T extends Entity> extends DeleteOperationBuilder {

    public UpdateOperationBuilder(Uri authorityUri, Class<T> clazz) {
        super(authorityUri, clazz);
    }

    public UpdateOperationBuilder(Uri authorityUri, Class<T> clazz, long id) {
        super(authorityUri, clazz, id);
    }
    
    protected ContentProviderOperation.Builder newBuilder(Uri uri) {
        return ContentProviderOperation.newUpdate(uri);
    }

    public UpdateOperationBuilder withValueBackReference(String key, int previousResult) {
        builder.withValueBackReference(key, previousResult);
        return this;
    }

    public UpdateOperationBuilder withValues(T entity) {
        builder.withValues(QueryBuilder.toValues(entity));
        return this;
    }

    public UpdateOperationBuilder withValues(T entity, String... includesColumns) {
        builder.withValues(QueryBuilder.toValues(entity, includesColumns));
        return this;
    }

    public UpdateOperationBuilder withValuesExcludesNull(T entity) {
        builder.withValues(QueryBuilder.toValues(entity));
        return this;
    }

    public UpdateOperationBuilder withValues(ContentValues values) {
        builder.withValues(values);
        return this;
    }

    public UpdateOperationBuilder withValueBackReferences(ContentValues backReferences) {
        builder.withValueBackReferences(backReferences);
        return this;
    }

    public UpdateOperationBuilder withValue(String key, Object value) {
        ContentValues values = new ContentValues();
        TypeConverter typeConverter = TypeConverterCache.getTypeConverter(value.getClass());
        if (typeConverter != null) {
            typeConverter.pack(value, values, key);
        }
        builder.withValues(values);
        return this;
    }
}
