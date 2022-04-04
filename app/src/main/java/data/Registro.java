package data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.UUID;


public class Registro {
    private String id;
    private String categoria;
    private String score;
    private String fecha;
    private String imgUri;

    public Registro(String ID, String score, String categoria,
                    String fecha, String imgUri) {
        this.id = ID;
        this.score = score;
        this.categoria = categoria;
        this.fecha = fecha;
        this.imgUri = imgUri;
    }

    @SuppressLint("Range")
    public Registro(Cursor cursor) {
        id = cursor.getString(cursor.getColumnIndex(registrosContract.RegistroEntry.ID));
        score = cursor.getString(cursor.getColumnIndex(registrosContract.RegistroEntry.SCORE));
        categoria = cursor.getString(cursor.getColumnIndex(registrosContract.RegistroEntry.CATEGORIA));
        fecha = cursor.getString(cursor.getColumnIndex(registrosContract.RegistroEntry.FECHA));
        imgUri = cursor.getString(cursor.getColumnIndex(registrosContract.RegistroEntry.IMGURI));
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(registrosContract.RegistroEntry.ID, id);
        values.put(registrosContract.RegistroEntry.SCORE, score);
        values.put(registrosContract.RegistroEntry.CATEGORIA, categoria);
        values.put(registrosContract.RegistroEntry.FECHA, fecha);
        values.put(registrosContract.RegistroEntry.IMGURI, imgUri);
        return values;
    }

    public String getId() {
        return id;
    }

    public String getScore() {
        return score;
    }

    public String getFecha() {       return fecha;
    }

    public String getImgUri() {
        return imgUri;
    }

    public String getCategoria () {     return categoria;    }
}
