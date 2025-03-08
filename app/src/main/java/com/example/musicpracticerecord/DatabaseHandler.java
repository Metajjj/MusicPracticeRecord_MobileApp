package com.example.musicpracticerecord;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static class DbStructure {
        public static class PracticeSession{
            public static class Date{
                public String
                    Name = this.getClass().getSimpleName() , Type = "INTEGER PRIMARY KEY NOT NULL";
            }
            public static class Duration{
                public String
                    Name = this.getClass().getSimpleName() , Type = "INTEGER";
            }

            public static String[] Constraints = new String[]{};
        }
        public static class MusicPiece{
            public static class MusicPieceID{
                public String Name= this.getClass().getSimpleName(), Type="INTEGER PRIMARY KEY AUTOINCREMENT";
            }
            public static class Song{
                public String Name= this.getClass().getSimpleName(), Type="TEXT";
            }
            public static class Artist{
                public String Name=this.getClass().getSimpleName(), Type="TEXT";
            }
            public static String[] Constraints = new String[]{
                String.format("UNIQUE(%1$s , %2$s)",new Song().Name, new Artist().Name )
              //unique(col1, col2) = (d,a)(d,a) not allowed (d,a)(d,b) allowed
                //PK = unique & notNull
            };
        }
        public static class Prac2Muse {
            public static class PrSsID{
                public String Name = this.getClass().getSimpleName(), Type = "INTEGER";
            }
            public static class MuPeID{
                public String Name = this.getClass().getSimpleName(), Type = "INTEGER";
            }
            public static String[] Constraints = new String[]{
                String.format("FOREIGN KEY (%1$s) REFERENCES %2$s (`%3$s`) ON DELETE CASCADE ON UPDATE CASCADE"
                    ,
                    new Prac2Muse.PrSsID().Name, PracticeSession.class.getSimpleName(), new PracticeSession.Date().Name )
                ,
                //Order doesnt matter but table must contain same colName as PK
                //Can be opposite to PK constraint order
                //Must be same order on the ForeignKey Constraint
                String.format("FOREIGN KEY (%1$s) REFERENCES %2$s (`%3$s`) ON DELETE CASCADE ON UPDATE CASCADE"
                    ,
                    new Prac2Muse.MuPeID().Name, MusicPiece.class.getSimpleName(), new MusicPiece.MusicPieceID().Name )
            };
        }
    }

    /*
    Replace SQLITE with a pure .txt file ?
        ==STRUCTURE==

        Plain .txt{
            MusicPiece.txt{
             Song|Artist|Duration
            }
            PracticeSession.txt{
             Duration|Array[MusicPieceIDs]
            }
        }

        DATABASE {
            TABLE MusicPiece {
                PK : INT AUTO_I

                Song : (string) UNIQUE
                Artist: (string) UNIQUE

                FK : PracSesID

                --composite PK based on song and artist , skips need for PK col, more complex joins
                -- MUST CHECK IF SONG N ARTIST ARE UNIQUE ? EXIST?
            }
            TABLE PracticeSessions{
                Date : YYYYMMDD (no time) UNIQUE PK (is unique)
                Duration : SUM (Piece.Duration)
                MusicArray[commaSeperated,1,2,545]
            }

            FK on Music to Link to Practice

            1 PracticeSession - MANY MusicPiece
        }
        `` [] ""  all allow whitespace ` for delimiters too
        ' single quote for strings!

        SQL PK and FK as key and keyhole! (how to join and fit tables together)

        ==SQL syntax==
        CREATE TABLE MusicPiece (
            ID INT PRIMARY KEY NOT NULL ,
            Song TEXT NOT NULL UNIQUE,
            Artist TEXT NOT NULL UNIQUE,
            Duration INT NOT NULL ,
            --CONSTRAINT MusicPieceID PRIMARY KEY ( Song , Artist )
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
    private static DatabaseHandler DBinstance = null;

    //synchronise = lock() - 1 thread per time
    public static synchronized DatabaseHandler getInstance(Context c){

        if(DBinstance == null){ DBinstance = new DatabaseHandler(c); }
        return DBinstance;
    }

    private DatabaseHandler(Context c) {
        super(c, DBname, null, 1);
    }

    protected synchronized void WipeDB(Context c){
        this.getReadableDatabase().close();
        this.getWritableDatabase().close();
        c.deleteDatabase(DBname);
    }

    protected synchronized void ResetTables(){
        try(SQLiteDatabase sqldb = this.getWritableDatabase()){
            ResetTables(sqldb);
        }catch (Exception e){System.err.println(e);}
    }

    protected synchronized void ResetTables(SQLiteDatabase s) {
        ArrayList<String> Cmds = new ArrayList<>();

        Cmds.add("PRAGMA foreign_keys = ON;");
        //Allows the ON DELETE constraints to work

        try {
            for (Class<?> C : DbStructure.class.getDeclaredClasses()){
                //Clean up Tbls
                Cmds.add("DROP TABLE IF EXISTS "+C.getSimpleName()+";");

                String cmd = "";
                //Create Tbl
                cmd += "CREATE TABLE " + C.getSimpleName() + " ( ";

                //Add columns
                for (Class<?> C2 : C.getDeclaredClasses()) {
                    //cmd += C2.getSimpleName();
                    for(Field F : C2.getFields()){

                        if(F.getName()=="Name"){
                            cmd += "`"+F.get(C2.newInstance()) + "` ";
                        }else {
                            cmd += F.get(C2.newInstance()) + " ";
                        }
                    }

                        //If not last class, means another col to add
                    if(C2 != C.getDeclaredClasses()[C.getDeclaredClasses().length-1]){
                        cmd+=", ";
                    }
                }

                String[] Constraints = (String[]) C.getField("Constraints").get(null);

                //Add constraints
                if(Constraints != null && Constraints.length>0){
                    cmd+= ", " + String.join(" , ",Constraints)+" ";
                }

                cmd += ") ; "; //\n ruining SQL cmd?

                Cmds.add(cmd);
            }
        } catch (Exception e){ System.err.println(e); }

        //execSQL cannot execute multiple cmds at once!
        for(String cmd : Cmds){
            System.out.println("Executing: "+cmd);
            s.execSQL(cmd);
        }
    }

    protected synchronized void MockData(){
        //PracSess
        this.getWritableDatabase().execSQL("INSERT INTO PracticeSession (`Date`, Duration) VALUES (20251228, 240), (20240229,30) ;");

        //MusPie
        this.getWritableDatabase().execSQL("INSERT INTO MusicPiece (song, artist) VALUES  ('Song1', 'Artist1'), ('Song2','Artist1'), ('Song1','Artist2') ;");

        //Junc
        this.getWritableDatabase().execSQL("INSERT INTO Prac2Muse (PrSsID, MuPeID) VALUES (20251228, 1);");
    }

    protected synchronized ArrayList<HashMap<String,String>> CursorSorter(Cursor c){

        ArrayList<HashMap<String,String>> res = new ArrayList<>();

        if(c.moveToFirst()){
            do{
                HashMap<String,String> row = new HashMap<>();
                for(int i=0;i<c.getColumnCount();i++) {
                    row.put(c.getColumnNames()[i],c.getString((i)));
                }
                res.add(row);
            }
            while(c.moveToNext());
        }

        c.close();
        return res;
    }

    @Override
    protected void finalize() throws Throwable { this.close(); super.finalize(); }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        ResetTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
