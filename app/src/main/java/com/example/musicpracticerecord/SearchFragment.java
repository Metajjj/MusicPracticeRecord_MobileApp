package com.example.musicpracticerecord;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import kotlin.NotImplementedError;

public class SearchFragment extends DialogFragment {

    private Context context;
    private DatabaseHandler dh;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        return inflater.inflate(R.layout.searchfrag, container, false);
    }

    EditText EditSong, EditArtist;

    Handler SongSearch = new Handler(), ArtistSearch = new Handler();

    @Override
    public void onStart() {
        super.onStart();

        int Interval = Math.round(1000 * 1.4f);
        dh = DatabaseHandler.getInstance(context);
        EditArtist = getActivity().findViewById(R.id.FragArtist);
        EditSong = getActivity().findViewById(R.id.FragSong);

        //onclicks
        getActivity().findViewById(R.id.FragBg).setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            getActivity().findViewById(R.id.pracsessFL).setZ(-100);

            //Trigger reload of activity
            getActivity().findViewById(R.id.pracsessTitle).performClick();
        });
        getActivity().findViewById(R.id.FragNew).setOnClickListener(v->{
            AddMusPie();
        });

        //TextWatcher
        EditSong.addTextChangedListener(new TextWatcher() {

            //Edit 1 : BTC => OTC
            //Edit 2+ : ATC => BTC => OTC
            // ATC == BTC , OTC = after change , BTC + OTC have same data info

            @Override
            public void beforeTextChanged(CharSequence charSequence, int startPosOfChange, int lengthOfDel, int lengthOfChange) {
//                System.out.printf("\nBTC { charSeq : %1$s | i : %2$s | i1 : %3$s | i2 : %4$s%n", charSequence,startPosOfChange,lengthOfDel,lengthOfChange );
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int startPosOfChange, int lengthOfDel, int lengthOfChange) {
//                System.out.printf("\nOTC { charSeq : %1$s | i : %2$s | i1 : %3$s | i2 : %4$s%n", charSequence,startPosOfChange,lengthOfDel,lengthOfChange );
                //Remove pending searches and restart if user is still typing/using
                SongSearch.removeCallbacksAndMessages(null);
                SongSearch.postDelayed(() -> {
                    UpdateResults();
                },Interval);
            }

            @Override
            public void afterTextChanged(Editable editable) {
//                System.out.printf("\nATC: "+editable );
            }
        });
        EditArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ArtistSearch.removeCallbacksAndMessages(null);
                ArtistSearch.postDelayed(() -> {
                    UpdateResults();
                },Interval);
            }
        });
    }

    private void UpdateResults(){
        var TL = (TableLayout) getActivity().findViewById(R.id.FragTable);
        while(TL.getChildCount() >1){
            TL.removeViewAt(1);
        }

        var res = dh.CursorSorter(
            dh.getReadableDatabase().rawQuery(String.format(
                "SELECT * FROM %1$s WHERE %2$s LIKE '%%%3$s%%' AND %4$s LIKE '%%%5$s%%' ;"
                    ,DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName(), DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName(), EditSong.getText(),DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName(), EditArtist.getText())
                ,null)
        );

        var LyPm = new TableLayout.LayoutParams(); LyPm.setMargins(0,DP2Pixel(5),0,0);

        for(var row : res){
            String Song = row.get(DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName()), Artist = row.get(DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName());

            TL.post(()-> TL.addView(SetupTR(Song,Artist), LyPm ) );
        }
    }

    private TableRow SetupTR(String Song, String Artist){
        TableRow tr = new TableRow(context);
        TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.Background, R.attr.Foreground, R.attr.Accent});

        TableRow.LayoutParams LytPrms = new TableRow.LayoutParams(0,DP2Pixel(20),20);

        //TxtVw
        TextView Tv = SetupTv(Song);
        Tv.setTextColor(ta.getColor(1,0xFFFF0000));
        Tv.setOnClickListener(v->
            EditSong.setText( ((TextView)v).getText() )
        );
        tr.addView(Tv,LytPrms);

        Tv = SetupTv(Artist);
        Tv.setTextColor(ta.getColor(1,0xFF00FF00));
        Tv.setOnClickListener(v->
            EditArtist.setText( ((TextView)v).getText() )
        );
        tr.addView(Tv,LytPrms);

        //ImgVw
        LytPrms = new TableRow.LayoutParams(DP2Pixel(20),DP2Pixel(20),1);
        var Iv = new ImageView(context);
        Iv.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.bin, getActivity().getTheme()));
        Iv.setOnClickListener(v-> DelMusPie((TableRow) v.getParent()) );

        tr.addView(Iv,LytPrms);

        Iv = new ImageView(context);
        Iv.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.plus, getActivity().getTheme()));
        Iv.setOnClickListener(v-> AddJT((TableRow) v.getParent()) );
        tr.addView(Iv,0,LytPrms); //Insert into 1st place

        //tr / onClicks
        tr.setBackgroundColor(ta.getColor(0,0xFF0000FF));



        ta.recycle();
        return tr;
    }
    private TextView SetupTv(String display){
        TextView tv = new TextView(context);

        tv.setGravity(Gravity.CENTER);
        tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        tv.setPadding(DP2Pixel(1), DP2Pixel(1), DP2Pixel(1), DP2Pixel(1));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE); tv.setMarqueeRepeatLimit(999999);
        tv.setSelected(true);
        tv.setSingleLine(true);
        tv.setLines(1);
        tv.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        tv.setText(display);

        return tv;
    }

    private void DelMusPie(TableRow tr){
        String song = ((TextView)tr.getChildAt(1)).getText()+"",
            artist = ((TextView)tr.getChildAt(2)).getText()+"";

        String id = dh.CursorSorter(dh.getReadableDatabase().rawQuery(
            String.format("SELECT %1$s FROM %2$s WHERE %3$s = '%4$s' AND %5$s = '%6$s' ;", DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName(), DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName(), DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName(), song, DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName(), artist
            ),null
        )).get(0).get(DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName());

        //TRANSACTIONS - begin , set successful, end (auto rollback if not successful)

        dh.getReadableDatabase().beginTransaction();
        try {
            dh.getReadableDatabase().execSQL("DELETE FROM " + DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName() + " WHERE " + DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName() + " = " + id + " ;");

            dh.getReadableDatabase().delete(DatabaseHandler.DbStructure.Prac2Muse.class.getSimpleName(),
                DatabaseHandler.DbStructure.Prac2Muse.MuPeID.class.getSimpleName() + " = " + id + " OR " + DatabaseHandler.DbStructure.Prac2Muse.MuPeID.class.getSimpleName() + " IS NULL"
                , null);

            dh.getReadableDatabase().setTransactionSuccessful();
        } catch (Exception e){ System.err.println(e);
            Toast.makeText(context,"Err deleting music!",Toast.LENGTH_LONG);
        }
        finally { dh.getReadableDatabase().endTransaction(); }

        //Update the displayed list to avoid showing it since its been deleted!
        UpdateResults();
    }

    private void AddMusPie(){
        dh.getReadableDatabase().beginTransaction();
        try{
            ContentValues CV = new ContentValues();

            CV.put(DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName(),EditSong.getText()+"");
            CV.put(DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName(),EditArtist.getText()+"");

            //System.out.println("Inserting: "+CV);

            dh.getReadableDatabase().insert(DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName(),null,CV);

            //TODO catch doesn't run on duplicate insertion ??

            dh.getReadableDatabase().setTransactionSuccessful();
        } catch (Exception e) { System.err.println(e);
            Toast.makeText(context,"Err adding music!",Toast.LENGTH_LONG).show();
        }
        finally { dh.getReadableDatabase().endTransaction(); }

        UpdateResults();
    }

    private void AddJT(TableRow tr){
        String song = ((TextView)tr.getChildAt(1)).getText()+"",
            artist = ((TextView)tr.getChildAt(2)).getText()+"",
            PS_ID = String.join("",((TextView)getActivity().findViewById(R.id.pracsessTitle)).getText().toString().split("/")),
            MP_ID = dh.CursorSorter(dh.getReadableDatabase().rawQuery(
                String.format("SELECT %1$s FROM %2$s WHERE %3$s = '%4$s' AND %5$s = '%6$s' ;", DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName(), DatabaseHandler.DbStructure.MusicPiece.class.getSimpleName(), DatabaseHandler.DbStructure.MusicPiece.Song.class.getSimpleName(), song, DatabaseHandler.DbStructure.MusicPiece.Artist.class.getSimpleName(), artist
                ),null
            )).get(0).get(DatabaseHandler.DbStructure.MusicPiece.MusicPieceID.class.getSimpleName());

        dh.getReadableDatabase().beginTransaction();
        try {
            ContentValues CV = new ContentValues();

            CV.put(DatabaseHandler.DbStructure.Prac2Muse.PrSsID.class.getSimpleName(),PS_ID);
            CV.put(DatabaseHandler.DbStructure.Prac2Muse.MuPeID.class.getSimpleName(),MP_ID);

            var i = dh.getReadableDatabase().insert(DatabaseHandler.DbStructure.Prac2Muse.class.getSimpleName(),null,CV);

            //System.out.printf("Added to JT %1$sfully\n", i>0?"success":"fail");

            dh.getReadableDatabase().setTransactionSuccessful();
        } catch (Exception e){ System.err.println(e);
            Toast.makeText(context,"Err adding to JT!",Toast.LENGTH_LONG);
        }
        finally {
            dh.getReadableDatabase().endTransaction();
        }

        //Remove fragment + refresh
        getActivity().findViewById(R.id.FragBg).performClick();
        getActivity().startActivity(getActivity().getIntent()); //Reload itself
    }

    private int DP2Pixel(float dp){
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics()) );
    }
}
