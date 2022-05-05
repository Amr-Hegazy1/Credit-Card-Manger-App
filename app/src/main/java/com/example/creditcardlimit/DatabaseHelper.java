package com.example.creditcardlimit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "CardDatabaseHelper";

    private static final String CARDS_TABLE_NAME = "Cards";
    private static final String HISTORY_TABLE_NAME = "History";

    private static final String COL0 = "ID";
    private static final String COL1 = "CardNum";
    private static final String COL2 = "Amount";
    private static final String COL3 = "History";



    public DatabaseHelper(Context context){
        super(context, CARDS_TABLE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCardTable = "CREATE TABLE " + CARDS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 +" TEXT ," + COL2 + " TEXT DEFAULT '10000')";
        db.execSQL(createCardTable);
        String createHistoryTable = "CREATE TABLE " + HISTORY_TABLE_NAME + " (CardNum TEXT, " +
                COL3 +" TEXT)";
        db.execSQL(createHistoryTable);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CARDS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String item,String cardNum,String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (tableName == CARDS_TABLE_NAME)
            contentValues.put(COL1, item);

        else{
            contentValues.put(COL3, item);
            contentValues.put("CardNum", cardNum);
        }

        long result = db.insert(tableName, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }




    public Cursor getCardsData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + CARDS_TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
    public Cursor getHistoryData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + HISTORY_TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
    public void updateCardsTable(String cardNum , String newAmount){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + CARDS_TABLE_NAME + " SET " + COL2 + " =" + newAmount +  " WHERE " + COL1 + "= " + cardNum );
    }
}
