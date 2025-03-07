package com.example.musicpracticerecord;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.*;
import java.util.stream.Collectors;

public class Home extends AppCompatActivity {

    private Context context;
    private DatabaseHandler dh;

    private TypedArray ta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home);

        context = getApplicationContext();

        getSupportActionBar().hide();

        dh = DatabaseHandler.getInstance(context);

        findViewById(R.id.homeTitle).setOnClickListener(v->{
            TestDb();
        });

        //TestSvg();

        findViewById(R.id.homeBg).setOnTouchListener(this::TouchGestureListener);

        new Thread(()->{
            ((TableLayout) findViewById(R.id.homeTable)).removeAllViews();
            SetupTable(findViewById(R.id.homeTable),LocalDate.now().getYear(), 1970,"Y");
        }).start();
    }

    @Override
    protected void onStart() {
        ta = this.obtainStyledAttributes(new int[]{R.attr.Background, R.attr.Foreground, R.attr.Accent});

        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ta.recycle();
        dh.close();

        super.onDestroy();
    }

    private TableLayout SetupTable(TableLayout Tbl, int big, int small, String tag){

        for(int i = big; i >= small; i--){

            var LytPrms = new TableLayout.LayoutParams();

            LytPrms.setMargins(DP2Pixel(5),DP2Pixel(5),DP2Pixel(5),DP2Pixel(5));

            Tbl.post(()->{
                Tbl.setPadding(DP2Pixel(10),DP2Pixel(5), DP2Pixel(10), DP2Pixel(5));}
            );

            var I = i;
            Tbl.post(()->{
                Tbl.addView( SetupRow(I,tag) , LytPrms );
            });
        }

        return Tbl;
    }

    private TableRow SetupRow(int DisplayNum, String tag)
    {
        TableRow tr = new TableRow(context);
        var LytPrms = new TableRow.LayoutParams();

        //TxtVw
        TextView tv = new TextView(context);

        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(DP2Pixel(3), DP2Pixel(3), DP2Pixel(3), DP2Pixel(3));
        tv.setTypeface(null, Typeface.BOLD);

        tv.setText( DisplayNum < 10 ? "0"+DisplayNum : DisplayNum+"" );
        tv.setTextColor(ta.getColor(0,-1));
        tv.setBackgroundColor(ta.getColor(2,-1));

        LytPrms = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
            ,
            //DP to pixel equivalent
            DP2Pixel(40)
            ,
            100);
        tr.addView(tv,LytPrms);

        //ImgVw
        LytPrms = new TableRow.LayoutParams(DP2Pixel(40), DP2Pixel(40),1);
        ImageView iv = new ImageView(context);

        if(! tag.contains("D")) {
            iv.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.arrow, getTheme()));
            iv.setRotation(-90);
            tr.addView(iv, LytPrms);
        }else{
            //is D day!
            var dh = DatabaseHandler.getInstance(context);

            //TODO get VALS
            var res = new ArrayList<String>();
            for(var row : dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT " + DatabaseHandler.DbStructure.PracticeSession.Date.class.getSimpleName()+" FROM "+DatabaseHandler.DbStructure.PracticeSession.class.getSimpleName()+" ;",null))
            ){ res.add(row.values().toArray()[0].toString()); }

            //check if tag (int only) is match
            var pureInt="";
            var m = Pattern.compile("\\d+").matcher(tag + tv.getText().toString());
            while(m.find()){ pureInt += m.group(); };
            if( res.contains( pureInt ) )
            {
                //Bin
                iv.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bin, getTheme()));
                iv.setOnClickListener(v->ArrowClicked(tr,true));
                tr.addView(iv, LytPrms);

                //Pencil
                iv = new ImageView(context);
                iv.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pencil, getTheme()));
                tr.addView(iv, LytPrms);
            }else{
                //Doesnt exist in DB
                iv.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.plus, getTheme()));
                tr.addView(iv, LytPrms);
            }
        }

        //Tr
        tr.setBackgroundColor(ta.getColor(0,-1));
        tr.setOnClickListener(v->{ArrowClicked((TableRow) v);});
        tr.setTag(tag + tv.getText().toString());

        return tr;
    }

    private  void ArrowClicked (TableRow tr){ArrowClicked(tr,false);}

    private void ArrowClicked(TableRow Vw, boolean bin){
        //System.out.println("Your tag: "+Vw.getTag());

        var ImgVw = Vw.getChildAt(1);
        var PrntTbl = ((TableLayout)Vw.getParent());

        if(Vw.getTag().toString().contains("D")) {

            if(bin){
                //Prompt to del
                //System.out.println("bin");

                System.err.println("TODO: delete from DB");
                new AlertDialog.Builder(context)
                    .setTitle("Confirm deletion")
                    .setPositiveButton("Delete", null)
                    .setNeutralButton("Cancel",null)
                    .setOnCancelListener(null)
                ;

            } else{
                //Move to edit page
                //System.out.println("edit");

                var Date="";
                var m = Pattern.compile("\\d+").matcher(Vw.getTag()+"");
                while(m.find()){
                    Date += (m.group().toString().length()<2 ? "0" : "") + m.group()+"/";
                }
                Date = Date.substring(0,Date.length()-1);

                startActivity(
                    new Intent(this,PracSess.class)
                        .putExtra("title",Date)
                );
            }
            //Edit or plus or bin!

        } else if(ImgVw.getRotation()==-90) {
            ImgVw.setRotation(0);

            ImgVw.setBackgroundTintList(ColorStateList.valueOf(ta.getColor(1, -1)));
            //PrntTbl.setBackgroundColor(ta.getColor(2,-1));

            if (Vw.getTag().toString().contains("M")) {

                var matcher = Pattern.compile("\\d+").matcher(Vw.getTag() + "");

                //needs to find else group returns err!
                matcher.find();
                int Yr = Integer.parseInt(matcher.group());

                matcher.find(); //YxxMxxDxx
                int Mt = Integer.parseInt(matcher.group());

                if(Mt>=12) { Yr++; Mt=0; }

                TableLayout DTL = SetupTable(new TableLayout(context), LocalDate.of(Yr, Mt + 1, 1).minusDays(1).getDayOfMonth(), 1
                    , Vw.getTag()+"D");

                PrntTbl.post(() -> {
                    PrntTbl.addView(DTL, PrntTbl.indexOfChild(Vw) + 1);
                });
            } else if (Vw.getTag().toString().contains("Y")) {

                TableLayout TL = SetupTable(new TableLayout(context), 12, 1, Vw.getTag() + "M");

                PrntTbl.post(() -> {
                    PrntTbl.addView(TL, PrntTbl.indexOfChild(Vw) + 1);
                });

            }


        }else{
            ImgVw.setRotation(-90);
            //Reset to nothing
            ImgVw.setBackgroundTintList(null);

            //PrntTbl.setBackgroundColor(ta.getColor(0,-1));
            if (Vw.getTag().toString().contains("D")) {
                PrntTbl.removeViewAt(PrntTbl.indexOfChild(Vw) + 1);
            }
            else if (Vw.getTag().toString().contains("M")) {
                    PrntTbl.removeViewAt(PrntTbl.indexOfChild(Vw)+1);
            }
            else if (Vw.getTag().toString().contains("Y")) {
                    PrntTbl.removeViewAt(PrntTbl.indexOfChild(Vw)+1);
            }
        }
    }

    private int DP2Pixel(float dp){
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics()) );
    }

    private boolean TouchGestureListener(View v, MotionEvent me){
        switch(me.getAction()){
            case MotionEvent.ACTION_UP:
                System.out.println("Released on: "+v.getClass().getSimpleName());
        }

        return true;
    }

    private void TestDb(){
        dh.ResetTables();

        dh.MockData();

        var res = dh.CursorSorter(dh.getReadableDatabase().rawQuery("SELECT * FROM Prac2Muse AS pm " +
                "INNER JOIN PracticeSession AS ps " +
                "ON pm.PrSsID = ps.Date " +
                "INNER JOIN MusicPiece AS mp " +
                "ON pm.MuPeID = mp.MusicPieceID;"
            ,null)
        );

        System.out.println(res);
    }

    private  void  TestSvg(){
        //TODO layout
        var ImgVw = (ImageView) findViewById(R.id.homeTable).findViewWithTag(2024);

        new Thread(()->{
            while(true){
                try {
                    Thread.sleep(5000);

                        //Random 16-bit col for R,G,B then | or flag to join them
                    int ranCol = ( (int) (Math.random() * 256) << 16 | (int) (Math.random() * 256) << 8 | (int) (Math.random() * 256) );

                    ImgVw.post(()->{
                        /*ImgVw.setColorFilter(
                            0xFF000000 | ranCol
                        ,
                            PorterDuff.Mode.SRC_IN
                        );*/
                        ImgVw.setBackgroundTintList(ColorStateList.valueOf(0xFF000000 | ranCol));
                    });

                    System.out.println("Col Change");

                    ImgVw.setRotation(45 + ImgVw.getRotation());

                } catch (Exception e){}
            }
        }).start();

        //ImgVw.setImageResource(R.drawable.pencil);
    }
}
