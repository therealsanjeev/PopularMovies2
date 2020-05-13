package com.therealsanjeev.popularmoviestage2.Data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.therealsanjeev.popularmoviestage2.models.Movie;

@Database(entities = {Movie.class}, version = 2)
public abstract class AppDatabase  extends RoomDatabase {

    private static AppDatabase INSTANCE;
    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "popularmovies")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `Movie` ADD `originalTitle` TEXT;");
        }
    };

    public abstract MovieDao movieDao();
}
