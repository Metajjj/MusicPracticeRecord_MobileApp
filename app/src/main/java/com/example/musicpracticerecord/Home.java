package com.example.musicpracticerecord;

import android.annotation.SuppressLint;
import android.content.Context;
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

        System.out.println("A");

        dh.ResetTables();

        dh.MockData();
        //TODO test for junc  tbl
        /*
        INSERT INTO MusicPiece (song, artist) VALUES  ('Song1', 'Artist1'), ('Song2','Artist1'), ('Song1','Artist2') ;

INSERT INTO PracticeSession (`Date`, Duration) VALUES (5, 240), (3,30) ;

INSERT INTO Prac2Muse (PrSsID, Song, Artist) VALUES (5, 'Song1', 'Artist1');
        */

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT * FROM Prac2Muse AS pm " +
                "INNER JOIN PracticeSession AS ps " +
                "ON pm.PrSsID = ps.Date " +
                "INNER JOIN MusicPiece AS mp " +
                "ON pm.MuPeSong = mp.Song AND pm.MuPeArtist = mp.Artist;"
            ,null)
        );

        System.out.println(res);


    }

    private Context context;
}
