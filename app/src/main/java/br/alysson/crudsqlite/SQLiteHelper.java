package br.alysson.crudsqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alysson on 12/10/2015.
 */
public class SQLiteHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "CRUD";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "people";

    public static final String COLLUM_ID = "id";
    public static final String COLLUM_NAME = "name";
    public static final String COLLUM_PHONE = "phone";
    public static final String COLLUM_ADDRESS = "address";

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" " +
                                                "( "+COLLUM_ID+" INTEGER PRIMARY KEY," +
                                                COLLUM_NAME+" TEXT,"+COLLUM_PHONE+" TEXT, " +
                                                COLLUM_ADDRESS+" TEXT);";


    public SQLiteHelper(Context context){

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
