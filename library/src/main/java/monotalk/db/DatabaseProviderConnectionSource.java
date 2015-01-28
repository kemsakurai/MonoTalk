package monotalk.db;

import android.net.Uri;

import java.util.List;

/**
 * Created by Kem on 2015/01/15.
 */
public class DatabaseProviderConnectionSource {
    private List<Class<? extends Entity>> entityClasses;
    private String authority;
    private boolean isDefaultAuthority = true;

    private String databaseName;

    private DatabaseProviderConnectionSource(Builder builder) {
        entityClasses = builder.entityClasses;
        authority = builder.authority;
        isDefaultAuthority = builder.isDefaultAuthority;
        databaseName = builder.databaseName;
    }

    @Override
    public String toString() {
        return "DatabaseProviderConnectionSource{" +
                "entityClasses=" + entityClasses +
                ", authority='" + authority + '\'' +
                ", isDefaultAuthority=" + isDefaultAuthority +
                '}';
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isDefaultAuthority() {
        return isDefaultAuthority;
    }

    public Uri getAuthorityUri() {
        return UriUtils.buildAuthorityUri(authority);
    }

    public String getAuthority() {
        return authority;
    }

    public List<Class<? extends Entity>> getEntityClasses() {
        return entityClasses;
    }

    public static final class Builder {
        private List<Class<? extends Entity>> entityClasses;
        private String authority;
        private boolean isDefaultAuthority;
        private String databaseName;

        public Builder() {
        }

        public Builder entityClasses(List<Class<? extends Entity>> entityClasses) {
            this.entityClasses = entityClasses;
            return this;
        }

        public Builder databaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        public Builder authority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder isDefaultAuthority(boolean isDefaultAuthority) {
            this.isDefaultAuthority = isDefaultAuthority;
            return this;
        }

        public DatabaseProviderConnectionSource build() {
            return new DatabaseProviderConnectionSource(this);
        }
    }
}
