package com.example.musicpracticerecord;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Collections;

public class PracSess extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pracsess);
        context= getApplicationContext();

        var title = getIntent().getExtras().getString("title");

        ((TextView)findViewById(R.id.pracsessTitle)).setText( title );

        //MakeTv1(findViewById(R.id.pracsessBg));

        //SET onclicks
        findViewById(R.id.praccsessBackArrow).setOnClickListener(v->{
            startActivity(new Intent(this,Home.class));
        });

        var JoinTitle = String.join("",title.split("/"));

        var dh = DatabaseHandler.getInstance(context);

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT "+DatabaseHandler.DbStructure.PracticeSession.Duration.class.getSimpleName()+" FROM " + DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName()+" WHERE "+DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName()+" = "+JoinTitle,null));

        System.out.println("TEST: "+JoinTitle);
        System.out.println("TEST: "+res);

        if(res.size()>0){
            //Exists a record of this!
            ((TextView)findViewById(R.id.pracsessDuration)).setText(
                res.get(0).get(DatabaseHandler.DbStructure.PracticeSession.Duration.class.getSimpleName())
            );
        }

        res = dh.CursorSorter(dh.getReadableDatabase().rawQuery(
            " SELECT * FROM "+DatabaseHandler.DbStructure.Prac2Muse.class.getSimpleName()+" AS jt " +
            " LEFT JOIN "+DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName()+" AS mp "+
            " ON jt."+DatabaseHandler.DbStructure.Prac2Muse.MuPeID.class.getSimpleName()+" = mp."+DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName()+
            " WHERE jt."+DatabaseHandler.DbStructure.Prac2Muse.PrSsID.class.getSimpleName()+" = "+JoinTitle+
            " ;"
            ,null
        ));

        System.out.println("related Music: "+res); //works

        ((TableLayout)findViewById(R.id.pracsessTable)).removeAllViews();

        for (var music : res) {

        }

    }

    private void MakeTv1(View v){
        try{
            for (int i=0; i< ((ViewGroup)v).getChildCount(); i++ ) {
                MakeTv1(((ViewGroup) v).getChildAt(i));
            }
        }catch (Exception e){
            //fails to cast to viewgroup means its of type view
            if(v instanceof TextView) {
                v.setSelected(true);
            }
        }
    }
}
