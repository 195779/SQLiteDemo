package com.example.sqlitedemo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
    private static final String DB_NAME = "people.db";
    private static final String DB_TABLE = "peopleinfo";
    private static final int DB_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_AGE = "age";
    public static final String KEY_HEIGHT = "height";

    private SQLiteDatabase db;
    private final Context context;
    private DBOpenHelper dbOpenHelper;

    public DBAdapter(Context _context) {
        context = _context;
    }

    /**
     * Close the database
     */
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    /**
     * Open the database
     */
    public void open() throws SQLiteException {
        dbOpenHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);
        /*dbOpenHelper执行onCreate方法：创建数据库*/
        try {
            db = dbOpenHelper.getWritableDatabase();
            /*getWritableDatabase()和getReadableDatabase()都可以创建或打开一个现有的数据库
                （如果数据库已经存在则直接打开,否则创建一个新的数据库）
                并返回一个可对数据库进行读写操作的SQLiteDatabase类型的对象
                （当数据库不可写入的时候(如磁盘空间已满)，
                getReadbleDatabase()方法返回的对象将以只读的方式去打开数据库，
                而getWritableDatabase()方法将出现异常）*/
        } catch (SQLiteException ex) {
            db = dbOpenHelper.getReadableDatabase();
        }
    }


    public long insert(People people) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_NAME, people.Name);
        newValues.put(KEY_AGE, people.Age);
        newValues.put(KEY_HEIGHT, people.Height);
        return db.insert(DB_TABLE, null, newValues);
        /*插入people对象的全部数据*/
    }


    public People[] queryAllData() {
        Cursor results = db.query(DB_TABLE, new String[]{KEY_ID, KEY_NAME, KEY_AGE, KEY_HEIGHT},
                null, null, null, null, null);
        /*游标指向全表的第一个数据的位置*/
        return ConvertToPeople(results);
    }

    public People[] queryOneData(long id) {
        Cursor results = db.query(DB_TABLE, new String[]{KEY_ID, KEY_NAME, KEY_AGE, KEY_HEIGHT},
                KEY_ID + "=" + id, null, null, null, null);
        /*游标指向当前id下的第一个数据的位置*/
        return ConvertToPeople(results);
    }

    @SuppressLint("Range")
    private People[] ConvertToPeople(Cursor cursor) {
        int resultCounts = cursor.getCount();
        if (resultCounts == 0 || !cursor.moveToFirst()) {
            return null;
        }
        People[] peoples = new People[resultCounts];
        for (int i = 0; i < resultCounts; i++) {
            peoples[i] = new People();
            peoples[i].ID = cursor.getInt(0);
            peoples[i].Name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            peoples[i].Age = cursor.getInt(cursor.getColumnIndex(KEY_AGE));
            peoples[i].Height = cursor.getFloat(cursor.getColumnIndex(KEY_HEIGHT));
            cursor.moveToNext();
            /*moveToFirst把cursor的指针向后挪动*/
        }
        cursor.close();
        /*用完以后关闭游标*/
        return peoples;
    }

    public long deleteAllData() {
        return db.delete(DB_TABLE, null, null);
        /*删除成功返回>0的值*/
    }

    public long deleteOneData(long id) {
        return db.delete(DB_TABLE, KEY_ID + "=" + id, null);
        /*按照id删除指定行*/
        /*删除成功返回>0的值*/
    }

    public long updateOneData(long id, People people) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(KEY_NAME, people.Name);
        updateValues.put(KEY_AGE, people.Age);
        updateValues.put(KEY_HEIGHT, people.Height);
        return db.update(DB_TABLE, updateValues, KEY_ID + "=" + id, null);
        /*将表中此id的行的所有内容用当前的people对象的各个数据做更新*/
    }

    /**
     * 静态Helper类，用于建立、更新和打开数据库
     */
    private static class DBOpenHelper extends SQLiteOpenHelper {
        /*继承自SQLiteOpenHelper*/

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        private static final String DB_CREATE = "create table " +
                DB_TABLE + " (" + KEY_ID + " integer primary key autoincrement, "
                + KEY_NAME + " text not null, " + KEY_AGE + " integer," + KEY_HEIGHT + " float);";


        @Override
        public void onCreate(SQLiteDatabase _db) {
            /*创建数据库*/
            _db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            /*如果版本号更新则代表表的结构变化，则执行onUprade，删除旧表，新建新表*/
            _db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(_db);
        }
    }

}
