package com.example.musicpracticerecord;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {

    /*
    Replace SQLITE with a pure .txt file ?
        ==STRUCTURE==

        Plain .txt{
            MusicPiece.txt{
             ID|Song|Artist|Duration
            }
            PracticeSession.txt{
             Duration|Array[MusicPieceIDs]
            }
        }

        DATABASE {
            TABLE MusicPiece {
                MP_ID : INT
                Song : (string) UNIQUE
                Artist: (string) UNIQUE
                Duration (secs) : ss (int)
                -- MUST CHECK IF SONG N ARTIST ARE UNIQUE ? EXIST?
            }
            TABLE PracticeSessions{
                Date : YYYYMMDD (no time) UNIQUE PK (is unique)
                Duration : SUM (Piece.Duration)
            }

                --Used for the 1-to-many (robust go-to)
            TABLE JunctionTable{

            }
            1 PracticeSession - MANY MusicPiece
        }
        `` [] ""  all allow whitespace ` for delimiters too

        ==SQL syntax==
        CREATE TABLE MusicPiece (
            ID INT PRIMARY KEY NOT NULL ,
            Song TEXT NOT NULL UNIQUE,
            Artist TEXT NOT NULL UNIQUE,
            Duration INT NOT NULL ,
            --CONSTRAINT MusicPiece_ID PRIMARY KEY ( Song , Artist )
        );
            --constraint = name for the cols together, doesnt make new one to ref outside

        CREATE TABLE PracticeSession (
            ID INT NOT NULL,
            Date INT NOT NULL ,
            Duration INT NOT NULL, --needs to be sum of pieces duration

            --PlaceHolders for when joining other tables ? treat like making a child class from a super class to access other data?
            Song TEXT NOT NULL ,
            Artist TEXT NOT NULL ,
            CONSTRAINT MusicPiece_FK FOREIGN KEY (Song, Artist) REFERENCES MusicPiece.(Song, Artist) ON UPDATE CASCADE;
        );
    */

    private static final String DBname = "MusicPracticeTracker";

    private int AutoIncrementVal;
    protected synchronized int getAutoIncrement(){
        RecheckAutoIncrement(); return ++AutoIncrementVal;
    }

    protected synchronized void RecheckAutoIncrement(){


        //Loop thru database = highest ID + 1
        ArrayList<HashMap<String,String>> Datas = CursorSorter( this.getReadableDatabase().query(TBLname,new String[]{new ID().Name},null,null,null,null, new ID().Name + " ASC") );

        for (int i=1;i<=Datas.size();i++ ) {
            String CurrID = Datas.get( i-1 ).get( new ID().Name );
            //System.out.println("i: "+ i +" | ID: "+CurrID);
            if(! CurrID.equals(i+"")){
                //Update to fill up empty spots in ID
                AutoIncrementVal=--i;
                break;
            }
            //Update innate A_I to match my new latest ID
            if(i==Datas.size()){ AutoIncrementVal=i; }
        }

        //DatabaseHandler.this.close();
    }

    private static DatabaseHandler DBinstance = null;

    //synchronise = lock() - 1 thread per time
    public static synchronized DatabaseHandler getInstance(Context c){

        if(DBinstance == null){
            DBinstance = new DatabaseHandler(c);
        }
        return DBinstance;
    }

    private DatabaseHandler(Context c) {
        super(c, DBname, null, 1);
    }

    protected void ResetTable(){
        try(SQLiteDatabase sqldb = this.getWritableDatabase()){

            ResetTable(sqldb);

        }catch (Exception e){}
    }

    protected void ResetTable(SQLiteDatabase s) {
        //s.beginTransaction(); //TODO research begingTransact

            //Cleans tables
        s.execSQL("DROP TABLE IF EXISTS `MusicPiece`;");
        s.execSQL("DROP TABLE IF EXISTS `PracticeSession`;");

        //A_I allows PK insertion!!
        String cmd = "CREATE TABLE `MusicPiece` (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , Song TEXT UNIQUE, Artist TEXT UNIQUE, Duration INT);";
        cmd += "CREATE TABLE `PracticeSession` (ID INT PRIMARY KEY NOT NULL , Date INT);";

        //TODO practice https://sqliteonline.com/
        /*
DROP TABLE IF EXISTS `MusicPiece`;
CREATE TABLE `MusicPiece` (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , Song TEXT UNIQUE , Artist TEXT UNIQUE , Duration INT);


DROP TABLE IF EXISTS `PracticeSession`;
CREATE TABLE `PracticeSession` (ID INT PRIMARY KEY NOT NULL , Date INT);

INSERT INTO `MusicPiece` (Song, Artist, Duration, ID) VALUES ('S' , 'D' , 60, 5);

SELECT * FROM MusicPiece;
        */


        s.execSQL(cmd);
        //s.close();
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //create db
        /* Skip this? alrdy creates in ResetTable
        sqLiteDatabase.execSQL(
                "CREATE TABLE `" + DBname + "` (`" + ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, `" + YMDHMS + "` INT, `" + TITLE + "` TEXT, `"+NOTE+"` TEXT, `"+R_TIME+"` TEXT)"
        );*/

        ResetTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
