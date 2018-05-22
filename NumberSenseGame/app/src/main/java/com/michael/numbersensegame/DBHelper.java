package com.michael.numbersensegame;

import java.io.File;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {


    //Creates only dbHelper and not the database
    public DBHelper(Context context, String dbName) {
        super(context, dbName , null, 1);
    }

    //Runs when database is created (database is created by this.getWritableDatabase();)
    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public boolean createTableIfNotExists(String tableName, String[] columnNames, String[] columnDataTypes){
        SQLiteDatabase myDatabase = this.getWritableDatabase();
        String queryString = "CREATE TABLE IF NOT EXISTS " + tableName + " (id TINYINT AUTO_INCREMENT NOT NULL PRIMARY KEY, ";
        for(int i = 0; i < columnNames.length; i++){
            queryString += columnNames[i] + " " + columnDataTypes[i] + ", ";
        }
        queryString = queryString.substring(0, queryString.length()-2) + ");";

        try {
            myDatabase.execSQL(queryString);
            myDatabase.close();
        }catch (SQLiteException e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean insertTableRow  (String tableName, String[] columnNames, int[] insertValues) {

        SQLiteDatabase myDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", 0);
        for(int i = 0; i < columnNames.length; i++){
            contentValues.put(columnNames[i],insertValues[i]);
        }
        myDatabase.insert(tableName, null, contentValues);
        myDatabase.close();
        return true;
    }

    public boolean updateTableValue (String tableName, String columnName, int id, int value ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(columnName, value);
        db.update(tableName, contentValues, "id=? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public int getTableValue(String tableName, String columnName, int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE id=" + id + ";", null);
        cursor.moveToFirst();
        int tableValue = cursor.getInt(cursor.getColumnIndex(columnName));
        cursor.close();
        return tableValue;
    }

    public boolean isDatabaseExists(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }
}