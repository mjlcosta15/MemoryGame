package pt.isec.pem.memory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import pt.isec.pem.memory.Score;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SCORES = "scores";

    private static final String KEY_ID = "id";
    private static final String KEY_USER = "user";
    private static final String KEY_SCORE = "score";

    //Instrucao SQL para a criacao da tabela SCORES
    private static final String CREATE_TABLE_SCORES = "CREATE TABLE "
            + TABLE_SCORES + "(" + KEY_ID + " INTEGER PRIMARY KEY autoincrement,"
            + KEY_USER + " TEXT," + KEY_SCORE + " INTEGER" + ")";


    public MyDBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Criacao das tabelas
        db.execSQL(CREATE_TABLE_SCORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Remocao das tabelas anteriores
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);

        //Criacao das novas tabelas
        onCreate(db);
    }

    public void closeDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        if(db != null && db.isOpen())
            db.close();
    }

    public boolean addScore(String user, int score){

        removeLowestScore();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_USER,user);
        values.put(KEY_SCORE, score);

        return db.insert(TABLE_SCORES,null,values) >= 0;
    }

    public void removeLowestScore(){

        List<Score> scores = getScores();
        Score score;

        if( scores != null && scores.size() > 9) {
            score = scores.get(scores.size() - 1);
        }else
            return;

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_SCORES, KEY_ID + " = ?", new String[] {String.valueOf(score.getId())});
    }

    public List<Score> getScores(){
        String query;
        List<Score> scores = new ArrayList<>();

        query = "SELECT * FROM " + TABLE_SCORES + " ORDER BY "+ KEY_ID +" DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query,null);

        //Iterar sobre todos os registos da tabela scores
        if(c.moveToFirst()){
            do{
                Score score = new Score();
                score.setUser(c.getString(c.getColumnIndex(KEY_USER)));
                score.setScore(c.getInt(c.getColumnIndex(KEY_SCORE)));
                score.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                scores.add(score);
            }while(c.moveToNext());
        }
        return scores;
    }
}
