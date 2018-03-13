package ru.com.penza.myfinalapp.datasources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.com.penza.myfinalapp.datamodel.Person;



public class MyDBHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "Persons.db";
    private static final String TABLE_NAME = "Persons";
    private static final int VERSION = 10;

    private static final int CURSOR_ID = 0;
    private static final int CURSOR_LAST_NAME = 1;
    private static final int CURSOR_FIRST_NAME = 2;
    private static final int CURSOR_SECOND_NAME = 3;
    private static final int CURSOR_COLOR = 4;
    private static final int CURSOR_PHONE = 5;
    private static final int CURSOR_IMAGE_URL = 6;
    private static final int CURSOR_ADDRESS = 7;
    private static final int CURSOR_LONGTITUDE = 8;
    private static final int CURSOR_LATITUDE = 9;



    public MyDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                " ( " + Person.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Person.LAST_NAME + " TEXT NOT NULL, " +
                Person.FIRST_NAME + "  TEXT, " +
                Person.SECOND_NAME + " TEXT, " +
                Person.COLOR + " TEXT, " +
                Person.PHONE + " TEXT, "   +
                Person.IMAGE_URL + " TEXT, " +
                Person.ADDRESS  + " TEXT, " +
                Person.LONGTITUDE  + " DOUBLE, " +
                Person.LATITUDE + " DOUBLE)");
    }


    public  void resetDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE " + TABLE_NAME);
        onCreate(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion){
            db.execSQL("DROP TABLE " + TABLE_NAME);
            onCreate(db);
        }

    }

    public void delPerson(Person person) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, Person.ID + " = ?",
                new String[]{String.valueOf(person.getId())});
        db.close();
    }


    public void addPerson(Person person) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Person.LAST_NAME, person.getLastName());
        contentValues.put(Person.FIRST_NAME, person.getFirstName());
        contentValues.put(Person.SECOND_NAME, person.getSecondName());
        contentValues.put(Person.PHONE, person.getPhone());
        contentValues.put(Person.COLOR, person.getColor());
        contentValues.put(Person.IMAGE_URL,person.getImageUrl());
        contentValues.put(Person.ADDRESS,person.getAddress());
        contentValues.put(Person.LONGTITUDE,person.getLongtitude());
        contentValues.put(Person.LATITUDE,person.getLatitude());
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }


    public void updatePerson(Person person) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put(Person.ID, person.getId());
        values.put(Person.LAST_NAME, person.getLastName());
        values.put(Person.FIRST_NAME, person.getFirstName());
        values.put(Person.SECOND_NAME, person.getSecondName());
        values.put(Person.COLOR, person.getColor());
        values.put(Person.PHONE,person.getPhone());
        values.put(Person.IMAGE_URL, person.getImageUrl());
        values.put(Person.ADDRESS, person.getAddress());
        values.put(Person.LONGTITUDE, person.getLongtitude());
        values.put(Person.LATITUDE, person.getLatitude());
        db.update(TABLE_NAME, values, Person.ID + " = ?",
                new String[]{String.valueOf(person.getId())});
    }

    public Person getPersonById (int id){
        Person person = new Person();
        String selectQuery = "SELECT " + Person.ID + ", "
                + Person.LAST_NAME + ", " + Person.FIRST_NAME + ", " + Person.SECOND_NAME
                + ", " + Person.COLOR + ", " + Person.PHONE  + " ," + Person.IMAGE_URL
                + ", " + Person.ADDRESS + ", " + Person.LONGTITUDE  + " ," + Person.LATITUDE
                + " FROM " + TABLE_NAME
                + " WHERE " + Person.ID + "=" + String.valueOf(id);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            person.setId(Integer.parseInt(cursor.getString(CURSOR_ID)));
            person.setLastName(cursor.getString(CURSOR_LAST_NAME));
            person.setFirstName(cursor.getString(CURSOR_FIRST_NAME));
            person.setSecondName(cursor.getString(CURSOR_SECOND_NAME));
            person.setColor(cursor.getString(CURSOR_COLOR));
            person.setPhone(cursor.getString(CURSOR_PHONE));
            person.setImageURL(cursor.getString(CURSOR_IMAGE_URL));
            person.setAddress(cursor.getString(CURSOR_ADDRESS));
            person.setLongtitude(cursor.getDouble(CURSOR_LONGTITUDE));
            person.setLatitude(cursor.getDouble(CURSOR_LATITUDE));

        }
        cursor.close();
        return person;

    }

    public int getPersonCount(){
        int result=0;
        String selectQuery = "SELECT COUNT(" + Person.ID + ")" + " FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);

        }
        cursor.close();
        return result;
    }





    public List<Person> getPagedPersons(int limit, int offset, boolean sortOrder) {
        List<Person> persons = new ArrayList<>();
        String selectQuery = "SELECT " + Person.ID + ", "
                + Person.LAST_NAME + ", " + Person.FIRST_NAME + ", " + Person.SECOND_NAME
                + ", " + Person.COLOR + ", " + Person.PHONE   + ", " + Person.IMAGE_URL
                + ", " + Person.ADDRESS + ", " + Person.LONGTITUDE  + ", " + Person.LATITUDE
                + " FROM " + TABLE_NAME
                + " ORDER BY " + Person.LAST_NAME;
        if (sortOrder){
            selectQuery = selectQuery + " DESC";
        }
        selectQuery = selectQuery + " LIMIT " + String.valueOf(limit)
                + " OFFSET " + String.valueOf(offset);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Person person = new Person();
                person.setId(cursor.getInt(CURSOR_ID));
                person.setLastName(cursor.getString(CURSOR_LAST_NAME));
                person.setFirstName(cursor.getString(CURSOR_FIRST_NAME));
                person.setSecondName(cursor.getString(CURSOR_SECOND_NAME));
                person.setColor(cursor.getString(CURSOR_COLOR));
                person.setPhone(cursor.getString(CURSOR_PHONE));
                person.setImageURL(cursor.getString(CURSOR_IMAGE_URL));
                person.setAddress(cursor.getString(CURSOR_ADDRESS));
                person.setLongtitude(cursor.getDouble(CURSOR_LONGTITUDE));
                person.setLatitude(cursor.getDouble(CURSOR_LATITUDE));
                persons.add(person);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return persons;
    }
}
