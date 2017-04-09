package jpstarkey.symptracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.os.FileObserver.CREATE;
import static android.provider.Contacts.SettingsColumns.KEY;
import static jpstarkey.symptracker.R.id.daily;

/**
 * Created by Joshs on 30/03/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static DatabaseHelper sInstance; //For singleton implementation

    //Database info
    private static final String DATABASE_NAME = "sympDatabase";
    private static final int DATABASE_VERSION = 1;
    private static String DB_PATH = "";

    //Table names
    private static final String TABLE_SYMPTOMS = "Symptoms";
    private static final String TABLE_MEDICATIONS = "Medications";
    private static final String TABLE_DAILY = "Daily";
    private static final String TABLE_GOALS = "Goals";
    private static final String TABLE_PAIN_LEVELS = "Pain_Levels";

    //Daily table columns
    private static final String KEY_DAILY_ID = "_id";
    private static final String KEY_DAILY_PAIN = "daily_pain"; //Overall/average daily pain level? TODO
    private static final String KEY_DAILY_DATE = "date";

    //goals table columns
    private static final String KEY_GOAL_ID = "_id";
    private static final String KEY_GOAL_NAME = "name";
    private static final String KEY_GOAL_DESCRIPTION = "description";
    private static final String KEY_GOAL_ACCOMPLISH_DATE = "accomplish_date";
    private static final String KEY_GOAL_ACCOMPLISHED = "accomplished";

    //Pain levels table
    private static final String KEY_PAIN_LEVELS_ID = "_id";
    private static final String KEY_PAIN_LEVELS_SYMPTOM_ID_FK = "symptom_id";
    private static final String KEY_PAIN_LEVELS_LEVEL = "pain_level";
    private static final String KEY_PAIN_LEVELS_DATE = "date";

    //Symptoms table columns
    private static final String KEY_SYMPTOM_ID = "_id";
    private static final String KEY_SYMPTOM_NAME = "name";
    private static final String KEY_SYMPTOM_DESCRIPTION = "description";
    private static final String KEY_SYMPTOM_PAIN = "pain";

    //Medication table columns
    private static final String KEY_MEDICATION_ID = "_id";
    private static final String KEY_MEDICATION_NAME = "name";
    private static final String KEY_MEDICATION_DESCRIPTION = "description";
    private static final String KEY_MEDICATION_AMOUNT = "amount";
    private static final String KEY_MEDICATION_FREQUENCY = "frequency"; // amnt / day e.g 2/day


    //region Database setup methods

    // In any activity just pass the context and use the singleton method:
    //      PostsDatabaseHelper helper = PostsDatabaseHelper.getInstance(this);
    //
    public static synchronized DatabaseHelper getInstance(Context context)
    {
        //use the application context which will ensure that you don't
        //accidentallly leak and Activity's context
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null)
        {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //Constructor should be private to prevent direct instantiation
    //so instantiation is always handled by "getInstance()" instead
    private DatabaseHelper(Context context)
    {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            DB_PATH = context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        }
        else
            {
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }
    }

    //Called when the db connection is being configured,
    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //Called when the daabase is created the FIRST time.
    //Not called if a database of the same name exists
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_SYMPTOMS_TABLE = "CREATE TABLE " + TABLE_SYMPTOMS +
                "(" +
                    KEY_SYMPTOM_ID + " INTEGER PRIMARY KEY, " + //Primary key
                    KEY_SYMPTOM_NAME + " TEXT," +
                    KEY_SYMPTOM_DESCRIPTION + " TEXT, " +
                    KEY_SYMPTOM_PAIN + " INTEGER" +
                ")";

        String CREATE_MEDICATIONS_TABLE = "CREATE TABLE " + TABLE_MEDICATIONS +
                "(" +
                    KEY_MEDICATION_ID + " INTEGER PRIMARY KEY," + //Primary key
                    KEY_MEDICATION_NAME + " TEXT," +
                    KEY_MEDICATION_AMOUNT + " INTEGER," +
                    KEY_MEDICATION_DESCRIPTION + "TEXT, " +
                    KEY_MEDICATION_FREQUENCY + "INTEGER" +
                ")";

        String CREATE_DAILY_TABLE = "CREATE TABLE " + TABLE_DAILY +
                "(" +
                    KEY_DAILY_ID + " INTEGER PRIMARY KEY, " +
                    KEY_DAILY_DATE + "INTEGER, " +              //DATE stored as int (eg system.CurrentTimeMillis() so can be converted to and fro
                    KEY_DAILY_PAIN + "INTEGER, " +
                ")";

        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS +
                "(" +
                    KEY_GOAL_ID + " INTEGER PRIMARY KEY, " +
                    KEY_GOAL_NAME + " TEXT, " +
                    KEY_GOAL_DESCRIPTION + " TEXT, " +
                    KEY_GOAL_ACCOMPLISH_DATE + " INTEGER, " +  //DATE stored as int (eg system.CurrentTimeMillis() so can be converted to and fro
                    KEY_GOAL_ACCOMPLISHED + " INTEGER" + //Boolean
                ")";

        String CREATE_PAIN_LEVELS_TABLE = "CREATE TABLE " + TABLE_PAIN_LEVELS +
                "(" +
                    KEY_PAIN_LEVELS_ID + " INTEGER PRIMARY KEY, " +
                    KEY_PAIN_LEVELS_SYMPTOM_ID_FK + " INTEGER, " +
                    KEY_PAIN_LEVELS_LEVEL + " INTEGER, " +
                    KEY_PAIN_LEVELS_DATE + " INTEGER, " +   //DATE stored as int (eg system.CurrentTimeMillis() so can be converted to and fro
                    " FOREIGN KEY ("+KEY_PAIN_LEVELS_SYMPTOM_ID_FK+") REFERENCES "+TABLE_SYMPTOMS+"("+KEY_SYMPTOM_ID+")";

        db.execSQL(CREATE_SYMPTOMS_TABLE);
        db.execSQL(CREATE_MEDICATIONS_TABLE);
        db.execSQL(CREATE_DAILY_TABLE);
        db.execSQL(CREATE_GOALS_TABLE);
        db.execSQL(CREATE_PAIN_LEVELS_TABLE);


    }

    //Called when the database needs to be upgraded
    //only if the database already exists but DATABASE_VERSION differs
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion != newVersion)
        //Drop all old tables and recreated them
        //TODO will need changing with other DB schmea changes
        {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYMPTOMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
            onCreate(db);
        }
    }
    //endregion


    public void addSymptom(Symptom symptom)
    {
        //create/open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        //Wrap insert inside a transaction for performance
        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_SYMPTOM_NAME, symptom.name);
            values.put(KEY_SYMPTOM_DESCRIPTION, symptom.description);
            values.put(KEY_SYMPTOM_PAIN, symptom.pain);

            db.insertOrThrow(TABLE_SYMPTOMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add symptom to database");
        } finally
        {
            db.endTransaction();
        }
    }

    public void addMedication(Medication medication)
    {
        //create/open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        //Wrap insert inside a transaction for performance
        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_MEDICATION_NAME, medication.name);
            values.put(KEY_MEDICATION_AMOUNT, medication.amount);
            values.put(KEY_MEDICATION_DESCRIPTION, medication.description);
            values.put(KEY_MEDICATION_FREQUENCY, medication.frequency);

            db.insertOrThrow(TABLE_MEDICATIONS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Medication to database");
        } finally
        {
            db.endTransaction();
        }
    }
    //TODO
    /*
        String CREATE_PAIN_LEVELS_TABLE = "CREATE TABLE " + TABLE_PAIN_LEVELS +
                "(" +
                    KEY_PAIN_LEVELS_ID + " INTEGER PRIMARY KEY, " +
                    KEY_PAIN_LEVELS_SYMPTOM_ID_FK + " INTEGER, " +
                    KEY_PAIN_LEVELS_LEVEL + " INTEGER, " +
                    KEY_PAIN_LEVELS_DATE + " INTEGER, " +   //DATE stored as int (eg system.CurrentTimeMillis() so can be converted to and fro
                    " FOREIGN KEY ("+KEY_PAIN_LEVELS_SYMPTOM_ID_FK+") REFERENCES "+TABLE_SYMPTOMS+"("+KEY_SYMPTOM_ID+")";
     */

    public void addDailyLog(DailyLog daily)
    {
        //create/open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        //Wrap insert inside a transaction for performance
        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_DAILY_DATE, daily.date);
            values.put(KEY_DAILY_PAIN, daily.pain);

            long _id = db.insertOrThrow(TABLE_DAILY, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add daily log  to database");
        } finally
        {
            db.endTransaction();
        }
    }

    public void addGoal(Goal goal)
    {
        //create/open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        //Wrap insert inside a transaction for performance
        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_GOAL_NAME, goal.name);
            values.put(KEY_GOAL_DESCRIPTION, goal.description);
            values.put(KEY_GOAL_ACCOMPLISH_DATE, goal.accomplishDate);
            values.put(KEY_GOAL_ACCOMPLISHED, goal.accomplished);

            db.insertOrThrow(TABLE_GOALS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Goal to database");
        } finally
        {
            db.endTransaction();
        }
    }

    //public void addPainLevel

    public void copyDB() throws IOException
    {
        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite())
        {
            String currentDbPath = DATABASE_NAME;
            String backupDbPath = "sympDatabase.db";
            File currentDB = new File(DB_PATH, currentDbPath);
            File backupDB = new File(sd, backupDbPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        }
    }


    //region Querying
    public List<Symptom> getAllSymptoms() {
        List<Symptom> symptoms = new ArrayList<>();

        //SELECT * FROM SYMPTOMS
        String SYMPTOMS_SELECT_QUERY =
                String.format("SELECT * FROM %s", TABLE_SYMPTOMS);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SYMPTOMS_SELECT_QUERY, null);
        try
        {
            if(cursor.moveToFirst())
            {
                do
                {
                    Symptom newSymptom = new Symptom();
                    newSymptom.name = cursor.getString(cursor.getColumnIndex(KEY_SYMPTOM_NAME));

                    symptoms.add(newSymptom);

                } while(cursor.moveToNext());
            }
        } catch (Exception e)
        {
            Log.d(TAG, "Error while trying to get symptoms from database");
        } finally {
            if (cursor != null && !cursor.isClosed())
            {
                cursor.close();
            }
        }
        return symptoms;
    }

    //public Symptom getSymptomBy

    public List<Medication> getAllMedications() {
        List<Medication> meds = new ArrayList<>();

        //SELECT * FROM SYMPTOMS
        String MEDICATIONS_SELECT_QUERY =
                String.format("SELECT * FROM %s", TABLE_MEDICATIONS);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(MEDICATIONS_SELECT_QUERY, null);
        try
        {
            if(cursor.moveToFirst())
            {
                do
                {
                    Medication newMed = new Medication();
                    newMed.name = cursor.getString(cursor.getColumnIndex(KEY_MEDICATION_NAME));
                    newMed.amount = cursor.getInt(cursor.getColumnIndex(KEY_MEDICATION_AMOUNT));
                    meds.add((newMed));
                } while(cursor.moveToNext());
            }
        } catch (Exception e)
        {
            Log.d(TAG, "Error while trying to get medications from database");
        } finally {
            if (cursor != null && !cursor.isClosed())
            {
                cursor.close();
            }
        }
        return meds;
    }
    //endregion

    //region updating
    //endregion

    //region deletion of records
    public void deleteAllSymptoms()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try
        {
            //If we have any FK relationships, the order of deletion is important
            db.delete(TABLE_SYMPTOMS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            Log.d(TAG, "Error whilst trying to delete all symptoms");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAllMedications()
    {
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        try
        {
            //If we have any FK relationships, the order of deletion is important
            db.delete(TABLE_MEDICATIONS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            Log.d(TAG, "Error whilst trying to delete all medications");
        } finally {
            db.endTransaction();
        }
    }
    //endregion



}

