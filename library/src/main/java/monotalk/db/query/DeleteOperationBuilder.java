package monotalk.db.query;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.net.Uri;

import monotalk.db.Entity;
import monotalk.db.MonoTalk;
import monotalk.db.UriUtils;

import static monotalk.db.utility.AssertUtils.assertNotNull;

/**
 * Created by Kem on 2015/01/14.
 */
public class DeleteOperationBuilder<T extends Entity> extends AbstractSelection {
    protected ContentProviderOperation.Builder builder;
    protected Selection selection;

    public DeleteOperationBuilder(Uri authorityUri, Class<T> clazz) {
        Uri uri = getUri(authorityUri, clazz);
        builder = newBuilder(uri);
    }

    private Uri getUri(Uri authorityUri, Class<T> clazz) {
        assertNotNull(authorityUri, "authorityUri is null");
        assertNotNull(clazz, "clazz is null");
        return UriUtils.buildEntityUri(authorityUri, MonoTalk.getTableName(clazz));
    }

    protected ContentProviderOperation.Builder newBuilder(Uri uri) {
        return ContentProviderOperation.newDelete(uri);
    }

    public DeleteOperationBuilder(Uri authorityUri, Class<T> clazz, long id) {
        Uri uri = getUri(authorityUri, clazz);
        uri = ContentUris.withAppendedId(uri, id);
        builder = newBuilder(uri);
    }

    public DeleteOperationBuilder yieldAllowed(boolean yieldAllowed) {
        builder.withYieldAllowed(yieldAllowed);
        return this;
    }

    public Expressions<DeleteOperationBuilder> and(String clause) {
        return where(clause);
    }

    @SuppressWarnings("unchecked")
    public Expressions<DeleteOperationBuilder> where(String clause) {
        selection.where(clause);
        return new Expressions<DeleteOperationBuilder>((DeleteOperationBuilder) this);
    }

    public DeleteOperationBuilder and(String clause, Object... args) {
        return where(clause, args);
    }

    @SuppressWarnings("unchecked")
    public DeleteOperationBuilder where(String clause, Object... args) {
        selection.where(clause, args);
        return (DeleteOperationBuilder) this;
    }

    @SuppressWarnings("unchecked")
    public Expressions<DeleteOperationBuilder> or(String clause) {
        selection.or(clause);
        return new Expressions<DeleteOperationBuilder>((DeleteOperationBuilder) this);
    }

    @SuppressWarnings("unchecked")
    public DeleteOperationBuilder or(String clause, Object... args) {
        selection.or(clause, args);
        return (DeleteOperationBuilder) this;
    }

    @SuppressWarnings("unchecked")
    public DeleteOperationBuilder where(Selection selection) {
        this.selection = selection;
        return (DeleteOperationBuilder) this;
    }

    public ContentProviderOperation build() {
        if (this.selection != null) {
            builder.withSelection(selection.getSelection(), selection.getSelectionStringArgs());
        }
        return builder.build();
    }

    public DeleteOperationBuilder selectionBackReference(int selectionArgIndex, int previousResult) {
        builder.withSelectionBackReference(selectionArgIndex, previousResult);
        return this;
    }

    @Override
    protected void addSelection(String string) {
        selection.addSelection(string);
    }

    @Override
    protected void addArguments(Object... args) {
        selection.addArguments(args);
    }
}
