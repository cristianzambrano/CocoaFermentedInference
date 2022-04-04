package data;

import android.provider.BaseColumns;


public class registrosContract {

    public static abstract class RegistroEntry implements BaseColumns{
        public static final String TABLE_NAME ="registros";

        public static final String ID = "id";
        public static final String SCORE = "score";
        public static final String CATEGORIA = "categoria";
        public static final String FECHA = "fecha";
        public static final String IMGURI = "imgUri";
    }
}
