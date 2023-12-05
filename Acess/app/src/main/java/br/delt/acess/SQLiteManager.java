package br.delt.acess;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLiteManager extends SQLiteOpenHelper
{
    private static SQLiteManager sqLiteManager;

    private static final String DATABASE_NAME = "UsersDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Users";
    private static final String COUNTER = "Counter";
    private static final String ID_FIELD = "id";
    private static final String RFIDKEY_FIELD ="rfidKey";
    private static  final String CARGO_FIELD = "cargo";
    private static final  String PICTUREURI_FIELD = "pictureURI";
    private static final String LAST_ACESS_FIELD = "lastAcess";
    private static final String NOME_FIELD= "nome";

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public SQLiteManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SQLiteManager instanceOfDatabase(Context context)
    {
        if(sqLiteManager == null)
            sqLiteManager = new SQLiteManager(context);

        return sqLiteManager;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append("(")
                .append(COUNTER)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(ID_FIELD)
                .append(" INT, ")
                .append(RFIDKEY_FIELD)
                .append(" TEXT, ")
                .append(CARGO_FIELD)
                .append(" TEXT, ")
                .append(PICTUREURI_FIELD)
                .append(" TEXT, ")
                .append(LAST_ACESS_FIELD)
                .append(" TEXT, ")
                .append(NOME_FIELD)
                .append(" TEXT)");

        sqLiteDatabase.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
    }

    public void addUserToDatabase(User user)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID_FIELD, user.getId());
        contentValues.put(RFIDKEY_FIELD, user.getRfidKey());
        contentValues.put(CARGO_FIELD, user.getCargo());
        contentValues.put(PICTUREURI_FIELD, user.getPictureURI());
        contentValues.put(LAST_ACESS_FIELD, getStringFromDate(user.getLastAcess()));
        contentValues.put(NOME_FIELD, user.getNome());

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);


    }

    public void populateUserListArray()
    {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        try (Cursor result = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null))
        {
            if(result.getCount() != 0)
            {
                while (result.moveToNext())
                {
                    int id = result.getInt(1);
                    String rfidKey= result.getString(2) ;
                    String cargo= result.getString(3) ;
                    String pictureURI= result.getString(4) ;
                    String lastAcessString = result.getString(5);
                    String nome = result.getString(6);

                    Date lastAcess = getDateFromString(lastAcessString);

                    User user = new User(id, rfidKey,cargo,pictureURI,lastAcess,nome);
                    User.userArrayList.add(user);

                }
            }
        }
    }

    public void updateUserInDB(User user)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ID_FIELD, user.getId());
        contentValues.put(RFIDKEY_FIELD, user.getRfidKey());
        contentValues.put(CARGO_FIELD, user.getCargo());
        contentValues.put(PICTUREURI_FIELD, user.getPictureURI());
        contentValues.put(LAST_ACESS_FIELD, getStringFromDate(user.getLastAcess()));
        contentValues.put(NOME_FIELD, user.getNome());

        sqLiteDatabase.update(TABLE_NAME, contentValues, ID_FIELD + " =? ", new String[]{String.valueOf(user.getId())});

    }
    public void deleteUserInDB(User user)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME,ID_FIELD + " =? ", new String[]{String.valueOf(user.getId())});

    }


    public static String getStringFromDate(Date date)
    {
        if(date == null)
            return null;
        return dateFormat.format(date);
    }

    public static Date getDateFromString(String string)
    {
        try
        {
            return dateFormat.parse(string);
        }
        catch (ParseException | NullPointerException e)
        {
            return null;
        }
    }

}
