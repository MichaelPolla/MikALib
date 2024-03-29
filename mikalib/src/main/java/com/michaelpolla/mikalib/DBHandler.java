package com.michaelpolla.mikalib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Local database handler.
 * - CRUD operations.
 * - Import and Export methods.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class DBHandler extends SQLiteOpenHelper {

    // To update the database, for example after changing the database schema,
    // the database version must be incremented.
    public static final String DATABASE_NAME = "stufftracker.db";
    public static final int DATABASE_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String DELIMITER = ",";

    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_PICTURE = "picture";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBHandler.TABLE_ITEMS + " (" +
                    DBHandler.COLUMN_NAME + TEXT_TYPE + DELIMITER +
                    DBHandler.COLUMN_BRAND + TEXT_TYPE + DELIMITER +
                    DBHandler.COLUMN_MODEL + TEXT_TYPE + DELIMITER +
                    DBHandler.COLUMN_NOTE + TEXT_TYPE + DELIMITER +
                    DBHandler.COLUMN_PICTURE + BLOB_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DBHandler.TABLE_ITEMS;


    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    // Same as onUpgrade, but keeps the actual version number.
    public void resetDB(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


    /**
     * Import a SQLite database from file.
     *
     * Helpful : http://stackoverflow.com/a/6542214/1975002
     * @param context Context.
     * @param importedDBFilepath the path (on ExternalStorage) where the database file to import is located.
     * @return <code>true</code> if the export was successful, <code>false</code> otherwise.
     */
    public boolean importDB(Context context, String importedDBFilepath) {

        File applicationDB = context.getDatabasePath(DATABASE_NAME);
        FileChannel src = null, dst = null;
        try {
            File importedDB = new File(importedDBFilepath);

            if (!applicationDB.exists()) {
                // Open and close the SQLiteOpenHelper so it will commit the created empty database to internal storage.
                getWritableDatabase();
                close();
            }
            //TODO-optional: if database already exists, ask for confirmation before overwriting.
            src = new FileInputStream(importedDB).getChannel();
            dst = new FileOutputStream(applicationDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            return true;

        } catch(IOException e) {
            e.printStackTrace();

        } finally {
            if (src != null) {
                try {
                    src.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dst != null) {
                try {
                    dst.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Export (copy) the SQLite database file to the location specified.
     * @param context Application context.
     * @param absolutePath the absolute path to the exported database. (/.../mydir/database.db)
     * @return <code>true</code> if the export was successful, <code>false</code> otherwise.
     */
    public boolean exportDB(Context context, String absolutePath) {
        boolean result = false;
        FileChannel src = null, dst = null;

        File applicationDB = context.getDatabasePath(DATABASE_NAME);

        if(applicationDB.exists()) {
            if (isExternalStorageWritable()) {
                try {
                    new File(absolutePath.substring(0,absolutePath.lastIndexOf("/"))).mkdirs(); // Create all dirs from filepath if they don't already exists
                    File exportedDB = new File(absolutePath);
                    src = new FileInputStream(applicationDB).getChannel();
                    dst = new FileOutputStream(exportedDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    result = true;

                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    if (src != null) {
                        try {
                            src.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (dst != null) {
                        try {
                            dst.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Erreur, impossible d'écrire dans la mémoire.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(context, "Erreur, base de données inexistante. Avez-vous déjà ajouté des données ?", Toast.LENGTH_LONG).show();
        }
        return result;
    }


    /* Checks if external storage is available for read and write */
    // Source : https://developer.android.com/guide/topics/data/data-storage.html#filesExternal
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }
}

