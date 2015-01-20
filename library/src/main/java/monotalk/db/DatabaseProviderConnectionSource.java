package monotalk.db;

import android.net.Uri;

import java.util.List;

/**
 * Created by Kem on 2015/01/15.
 */
public class DatabaseProviderConnectionSource {
    private List<Class<? extends Entity>> entityClasses;
    private DatabaseConnectionSource connectionSource;
    private String authority;
    private boolean isDefaultAuthority = true;

    private DatabaseProviderConnectionSource(Builder builder) {
        entityClasses = builder.entityClasses;
        connectionSource = builder.connectionSource;
        authority = builder.authority;
        isDefaultAuthority = builder.isDefaultAuthority;
    }

    @Override
    public String toString() {
        return "DatabaseProviderConnectionSource{" +
                "entityClasses=" + entityClasses +
                ", connectionSource=" + connectionSource +
                ", authority='" + authority + '\'' +
                ", isDefaultAuthority=" + isDefaultAuthority +
                '}';
    }

    public boolean isDefaultAuthority() {
        return isDefaultAuthority;
    }

    public DatabaseConnectionSource getConnectionSource() {
        return connectionSource;
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
        private DatabaseConnectionSource connectionSource;
        private String authority;
        private boolean isDefaultAuthority;

        public Builder() {
        }

        public Builder entityClasses(List<Class<? extends Entity>> entityClasses) {
            this.entityClasses = entityClasses;
            return this;
        }

        public Builder connectionSource(DatabaseConnectionSource connectionSource) {
            this.connectionSource = connectionSource;
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
