package com.example.musicpracticerecord;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;

public class PracSess extends AppCompatActivity {

    private Context context;
    private DatabaseHandler dh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pracsess);
        context= getApplicationContext();
        dh = DatabaseHandler.getInstance(context);

        getSupportActionBar().hide();

        ((TextView)findViewById(R.id.pracsessTitle)).setText( getIntent().getExtras().getString("title") );

        //MakeTv1(findViewById(R.id.pracsessBg));
        new Thread(this::Setup).start();

                                    //get main loop for Toast UI
        Handler DurationTxtWatch = new Handler(Looper.getMainLooper());
        DurationTxtWatch.postDelayed(()->{
        ((EditText)findViewById(R.id.pracsessDuration)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                DurationTxtWatch.removeCallbacksAndMessages(null);
                DurationTxtWatch.postDelayed(()->{

                    ContentValues CV = new ContentValues();
                    CV.put(DatabaseHandler.DbStructure.PracticeSession.Duration.class.getSimpleName(),charSequence+"");

                    dh.getWritableDatabase().update(DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName(),CV, DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName()+" = "+getIntent().getExtras().getString("title").replaceAll("\\D+",""),null);

                    Toast.makeText(context,"Duration saved!", Toast.LENGTH_SHORT).show();
                },2*1000);
            }
        });
        },2*1000);

        //SET onclicks

        //Able to reload itself
        findViewById(R.id.pracsessTitle).setOnClickListener(v-> startActivity(getIntent().addFlags(FLAG_ACTIVITY_NO_HISTORY)) );

        findViewById(R.id.pracsessBackArrow).setOnClickListener(v->{

                //WORKS
            //Del from db if no childs in table
            if( ((TableLayout)findViewById(R.id.pracsessTable)).getChildCount() == 0){
                //System.out.println("Empty to del");

                dh.getReadableDatabase().delete(DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName(), DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName() + " = "+ getIntent().getExtras().getString("title").replaceAll("\\D+",""),null);
            }

            /*System.out.println(
                dh.CursorSorter(
                    dh.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName(),null)
                )
            );*/

            startActivity(new Intent(this,Home.class));
        });
        findViewById(R.id.pracsessDel).setOnClickListener(v->{
            for(int i=0;i<((TableLayout)findViewById(R.id.pracsessTable)).getChildCount();i++ ){
                DelFromJT( (TableRow)
                ((TableLayout)findViewById(R.id.pracsessTable)).getChildAt(i)
                );
            }
        });
        findViewById(R.id.pracsessPlus).setOnClickListener(v->{
            /*
            [ Song | Artist ] (can input.. update on few secs no interaction)
            [ ScrollableView {
                PLUS | Song | Artist | BIN
                PLUS | Song2 | Artist2 | BIN
                 <bin = del, plus = add to PracSess, name = overwrite searchBar>
            }]
            */
            findViewById(R.id.pracsessFL).setZ(100);
            getSupportFragmentManager().beginTransaction().replace(R.id.pracsessFL,SearchFragment.class, null).commit();
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

        TL.setOnClickListener(v->{System.out.println("TEST");});

        for (var music : res) {
            TL.post(()->{TL.addView(SetupTablerow(music)); } );
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

        Iv.setOnClickListener(v-> {
            System.out.println("bin clicked");
            DelFromJT((TableRow) v.getParent());
        });

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

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT " + DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName() + " FROM " + DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName() + " WHERE " + DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName() + " = '"+song+"' AND " + DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName() + " = '"+artist+"' ;", null
        ));

        var PrSsIDsplit = ((TextView)findViewById(R.id.pracsessTitle)).getText().toString().split("/");

        for(var HmId : res){
            dh.getWritableDatabase().delete(DatabaseHandler.DbStructure.Prac2Muse.class.getSimpleName(),
                    //SQL string = '' not ""
                String.format(" %1$s = "+String.join("",PrSsIDsplit)+" AND %2$s = "+HmId.get(DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName())+" ",
                    DatabaseHandler.DbStructure.Prac2Muse.PrSsID.class.getSimpleName(), DatabaseHandler.DbStructure.Prac2Muse.MuPeID.class.getSimpleName())
                ,null
            );
        }

        var NumOfRows = dh.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHandler.DbStructure.Prac2Muse.class.getSimpleName() + " WHERE " + DatabaseHandler.DbStructure.Prac2Muse.PrSsID.class.getSimpleName()+" = "+String.join("",PrSsIDsplit)+" ;", null).getCount();

        if (NumOfRows==0){
            //Del if not connected to any music pieces
            dh.getWritableDatabase().execSQL(
                String.format("DELETE FROM %1$s WHERE `%2$s` = %3$s ;",
                    DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName(), DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName(), String.join("",PrSsIDsplit)
                    )
            );
            System.out.println("\nDEL FROM DB\n");

            System.out.println(dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT * FROM " + DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName()+" ;",null)));
        }

        //Force a reload
        findViewById(R.id.pracsessTitle).performClick();
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.pracsessBackArrow).performClick();
        super.onBackPressed();
    }

    private int DP2Pixel(float dp){
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics()) );
    }
}
