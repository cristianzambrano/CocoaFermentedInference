package data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;


public class RegistrosDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Registro.db";

    public RegistrosDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + registrosContract.RegistroEntry.TABLE_NAME + " ("
                + registrosContract.RegistroEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + registrosContract.RegistroEntry.ID + " TEXT NOT NULL,"
                + registrosContract.RegistroEntry.SCORE + " TEXT NOT NULL,"
                + registrosContract.RegistroEntry.CATEGORIA + " TEXT NOT NULL,"
                + registrosContract.RegistroEntry.FECHA + " TEXT NOT NULL,"
                + registrosContract.RegistroEntry.IMGURI + " TEXT,"
                + "UNIQUE (" + registrosContract.RegistroEntry.ID + "))");


    }



    public long mockRegistro(SQLiteDatabase db, Registro registro) {
        return db.insert(
                registrosContract.RegistroEntry.TABLE_NAME,
                null,
                registro.toContentValues());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

    public long saveRegistro(Registro registro) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                registrosContract.RegistroEntry.TABLE_NAME,
                null,
                registro.toContentValues());

    }

    public ArrayList<Registro>  getArrayAllRegistros() {
        ArrayList<Registro> lstRegistros = new ArrayList<Registro> ();

        Cursor c=  getAllRegistros();
        while(c.moveToNext()){
            lstRegistros.add(new Registro(c));
        }

        return  lstRegistros;
    }

    public Cursor getAllRegistros() {
        return getReadableDatabase()
                .query(
                        registrosContract.RegistroEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    public Cursor getRegistroById(String registroId) {
        Cursor c = getReadableDatabase().query(
                registrosContract.RegistroEntry.TABLE_NAME,
                null,
                registrosContract.RegistroEntry.ID + " LIKE ?",
                new String[]{registroId},
                null,
                null,
                null);
        return c;
    }

    public int deleteRegistro(String registroId) {
        return getWritableDatabase().delete(
                registrosContract.RegistroEntry.TABLE_NAME,
                registrosContract.RegistroEntry.ID + " LIKE ?",
                new String[]{registroId});
    }


}
