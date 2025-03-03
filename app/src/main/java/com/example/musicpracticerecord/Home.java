package com.example.musicpracticerecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    private Context context;
    private DatabaseHandler dh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);

        context = getApplicationContext();

        getSupportActionBar().hide();

        dh = DatabaseHandler.getInstance(context);

        dh.ResetTables();
        //TestDb();

        //TestSvgCol();
    }

    private void TestDb(){
        dh.MockData();

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT * FROM Prac2Muse AS pm " +
                "INNER JOIN PracticeSession AS ps " +
                "ON pm.PrSsID = ps.Date " +
                "INNER JOIN MusicPiece AS mp " +
                "ON pm.MuPeSong = mp.Song AND pm.MuPeArtist = mp.Artist;"
            ,null)
        );

        System.out.println(res);
    }
}
