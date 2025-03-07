package com.example.musicpracticerecord;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;

public class PracSess extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pracsess);
        context= getApplicationContext();

        ((TextView)findViewById(R.id.pracsessTitle)).setText( getIntent().getExtras().getString("title") );

        //MakeTv1(findViewById(R.id.pracsessBg));

        //SET onclicks
        findViewById(R.id.pracsessBackArrow).setOnClickListener(v->{
            startActivity(new Intent(this,Home.class));
        });
        findViewById(R.id.pracsessDel).setOnClickListener(v->{
            for(int i=0;i<((TableLayout)findViewById(R.id.pracsessTable)).getChildCount();i++ ){
                DelFromJT( (TableRow)
                ((TableLayout)findViewById(R.id.pracsessTable)).getChildAt(i)
                );
            }

            //TODO fix del from jt
            //WHERE Song = SongLongNameVery
            //Default Iv doesnt work!

            //TODO TODO auto-focus on EditText

        });
        findViewById(R.id.pracsessPlus).setOnClickListener(v->{
            //TODO search song + del from MP db (fragment??)
            /*
            [ Song | Artist ] (can input.. update on few secs no interaction)
            [ ScrollableView {
                PLUS | Song | Artist | BIN
                PLUS | Song2 | Artist2 | BIN
                 <bin = del, any other = add to PracSess>
            }]
            */
        });
    }

    private void Setup(){
        var JoinTitle = String.join("",getIntent().getExtras().getString("title").split("/"));

        var dh = DatabaseHandler.getInstance(context);

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT "+DatabaseHandler.DbStructure.PracticeSession.Duration.class.getSimpleName()+" FROM " + DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName()+" WHERE "+DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName()+" = "+JoinTitle,null));

        //System.out.println("TEST: "+JoinTitle);
        //System.out.println("TEST: "+res);

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

        //System.out.println("related Music: "+res); //works

        TableLayout TL = findViewById(R.id.pracsessTable);
        TL.removeAllViews();

        for (var music : res) {
            TL.addView(SetupTablerow(music) );
        }
    }

    private TableRow SetupTablerow(HashMap<String,String> hm){

        TableLayout TL = findViewById(R.id.pracsessTable);
        TableRow tr = new TableRow(context);
        var LytPrms = new TableRow.LayoutParams();

        TypedArray ta = this.obtainStyledAttributes(new int[]{R.attr.Background, R.attr.Foreground, R.attr.Accent});

        //TxtVw
        LytPrms = new TableRow.LayoutParams( 0 , DP2Pixel(40) ,4);
        tr.addView(SetupTv( TL.getChildCount()+1+"",ta.getColor(0,-1) ),LytPrms);

        LytPrms = new TableRow.LayoutParams( 0 , DP2Pixel(40) ,10);
        tr.addView(SetupTv( hm.get(DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName()) ,ta.getColor(0,-1) ),LytPrms);

        LytPrms = new TableRow.LayoutParams( 0 , DP2Pixel(40) ,10);
        tr.addView(SetupTv( hm.get(DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName()),ta.getColor(0,-1) ),LytPrms);

        //ImgVw
        LytPrms = new TableRow.LayoutParams( DP2Pixel(40) , DP2Pixel(40) ,1);
        var Iv = new ImageView(context);
        Iv.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.bin, getTheme()));
        Iv.setBackgroundTintList(ColorStateList.valueOf(ta.getColor(0,-1)));

        Iv.setOnClickListener(v->DelFromJT((TableRow) v.getParent()));

        tr.addView(Iv,LytPrms);

        //Tr
        tr.setBackgroundColor(ta.getColor(1,-1));

        ta.recycle();
        return tr;
    }

    private TextView SetupTv(String txt, int Fg){
        TextView tv = new TextView(context);

        //Setup
        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(DP2Pixel(1), DP2Pixel(1), DP2Pixel(1), DP2Pixel(1));
        tv.setTypeface(null, Typeface.BOLD);

        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE); tv.setMarqueeRepeatLimit(999999);
        tv.setSelected(true);
        tv.setSingleLine(true);
        tv.setLines(1);
        tv.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        tv.setTextColor(Fg);
        tv.setText(txt);

        tv.setOnLongClickListener(view -> {
            Toast.makeText(context,tv.getText(), Toast.LENGTH_LONG).show();
            return true;
        });

        return tv;
    }

    private void DelFromJT(TableRow tr){
        var dh = DatabaseHandler.getInstance(context);
        String song = ((TextView)tr.getChildAt(1)).getText()+"", artist = ((TextView)tr.getChildAt(2)).getText()+"";

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT " + DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName() + " FROM " + DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName() + " WHERE " + DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName() + " = "+song+" AND " + DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName() + " = "+artist+" ;", null
        ));

        System.out.println("DEL FROM JT!");

        for(var HmId : res){

            var PrSsIDsplit = ((TextView)findViewById(R.id.pracsessTitle)).getText().toString().split("/");

            dh.getWritableDatabase().delete(DatabaseHandler.DbStructure.Prac2Muse.class.getSimpleName(),
                String.format(" %1$s = ? AND %2$s = ? ",
                    DatabaseHandler.DbStructure.Prac2Muse.PrSsID.class.getSimpleName(), DatabaseHandler.DbStructure.Prac2Muse.MuPeID.class.getSimpleName())
                ,new String[]{
                    String.join("",PrSsIDsplit), HmId.get(DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName())
                }
            );
        }

        startActivity(getIntent()); //Reload itself
    }

    private int DP2Pixel(float dp){
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics()) );
    }
}
