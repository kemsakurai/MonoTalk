package monotalk.db.query;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import monotalk.db.Entity;
import monotalk.db.typeconverter.TypeConverter;
import monotalk.db.typeconverter.TypeConverterCache;

import static monotalk.db.query.QueryUtils.from;

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

    public UpdateOperationBuilder valueBackReference(String key, int previousResult) {
        builder.withValueBackReference(key, previousResult);
        return this;
    }

    public UpdateOperationBuilder values(T entity) {
        builder.withValues(from(entity));
        return this;
    }

    public UpdateOperationBuilder values(T entity, String... includesColumns) {
        builder.withValues(QueryUtils.from(entity, includesColumns));
        return this;
    }

    public UpdateOperationBuilder valuesExcludesNull(T entity) {
        builder.withValues(from(entity));
        return this;
    }

    public UpdateOperationBuilder values(ContentValues values) {
        builder.withValues(values);
        return this;
    }

    public UpdateOperationBuilder valueBackReferences(ContentValues backReferences) {
        builder.withValueBackReferences(backReferences);
        return this;
    }

    public UpdateOperationBuilder value(String key, Object value) {
        ContentValues values = new ContentValues();
        TypeConverter typeConverter = TypeConverterCache.getTypeConverter(value.getClass());
        if (typeConverter != null) {
            typeConverter.pack(value, values, key);
        }
        builder.withValues(values);
        return this;
    }
}
