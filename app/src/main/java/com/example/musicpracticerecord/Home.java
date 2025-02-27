package com.example.musicpracticerecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    @SuppressLint("Range")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);

        context = getApplicationContext();

        getSupportActionBar().hide();

        var dh = DatabaseHandler.getInstance(context);

        dh.ResetTable();

        //TODO err clm doesnt exist for given tbl!

        dh.MockData();

        Cursor c = dh.getReadableDatabase().rawQuery(String.format("SELECT * FROM %1$s AS mp LEFT JOIN %2$s AS ps ON mp.%3$s = ps.%4$s",
            DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName(),
            DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName(),
            DatabaseHandler.DbStructure.MusicPiece.PracSessID.class.getSimpleName(), DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName()
        ),null);

        if(c.moveToFirst()){
            String res = "";
            do{
                for(int i=0;i<c.getColumnCount();i++) {
                    res += c.getColumnNames()[i] +" : "+c.getString(i)+" | ";
                }
                res+="\n";
            }
            while(c.moveToNext());
            System.out.println(res);
        }

        c.close();
    }

    private Context context;
}
