package br.alysson.crudsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alysson on 13/10/2015.
 */
public class PersonDao {

    SQLiteHelper sqLiteHelper;
    SQLiteDatabase sqliteDatabase;

    private static final String TAG = "CRUDSQLite";

    public PersonDao(Context context){
        sqLiteHelper = new SQLiteHelper(context);

    }

    public void open() throws SQLException{ sqliteDatabase = sqLiteHelper.getWritableDatabase(); }

    public void close(){
        sqliteDatabase.close();
    }


    public void create(Person person) throws SQLException{
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteHelper.COLLUM_NAME, person.getName());
        contentValues.put(SQLiteHelper.COLLUM_PHONE, person.getPhone());
        contentValues.put(SQLiteHelper.COLLUM_ADDRESS, person.getAddress());

       long bct = sqliteDatabase.insert(SQLiteHelper.TABLE_NAME, null, contentValues);

        Log.d(TAG, "BCT: " + bct);
    }

    public List<Person> read(Person person) throws SQLException{

        String where = null, whereArg[] = null;
        if(person.getName()!=null){
            where = SQLiteHelper.COLLUM_NAME+" LIKE ?";
            whereArg = new String[]{"%"+person.getName()+"%"};
        }

        Cursor cursor = sqliteDatabase.query(SQLiteHelper.TABLE_NAME, null,
                where, whereArg, null, null, null);

        //Cursor cursor = sqliteDatabase.rawQuery("select * from people",null);
        List<Person> people = new ArrayList<>();


        if(cursor.moveToFirst() && cursor.getCount()>0) {

            while(!cursor.isAfterLast()){
                Person p = new Person();
                p.setId((long) cursor.getInt(0));
                p.setName(cursor.getString(1));
                p.setPhone(cursor.getString(2));
                p.setAddress(cursor.getString(3));

                people.add(p);

                cursor.moveToNext();
            }
        }

        cursor.close();
        return people;

    }

    public void update(Person person) throws SQLException{
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLiteHelper.COLLUM_NAME, person.getName());
        contentValues.put(SQLiteHelper.COLLUM_PHONE, person.getPhone());
        contentValues.put(SQLiteHelper.COLLUM_ADDRESS, person.getAddress());

        String id = String.valueOf(person.getId());

        sqliteDatabase.update(SQLiteHelper.TABLE_NAME, contentValues,"id = ?", new String[]{id});

    }

    public void delete(Person person) throws SQLException{
        String id = String.valueOf(person.getId());

        sqliteDatabase.delete(SQLiteHelper.TABLE_NAME,"id = ?", new String[]{id});
    }


}
