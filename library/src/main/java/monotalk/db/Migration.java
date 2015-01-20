package monotalk.db;

import android.database.sqlite.SQLiteDatabase;

public interface Migration {
    public abstract void upgradeMigrate(SQLiteDatabase db);
    public abstract void downgradeMigrate(SQLiteDatabase db);
}
