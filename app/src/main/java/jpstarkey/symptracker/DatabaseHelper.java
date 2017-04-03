package jpstarkey.symptracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.provider.Contacts.SettingsColumns.KEY;

/**
 * Created by Joshs on 30/03/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static DatabaseHelper sInstance; //For singleton implementation

    //Database info
    private static final String DATABASE_NAME = "symptDatabase";
    private static final int DATABASE_VERSION = 1;

    //Table names
    private static final String TABLE_SYMPTOMS = "symptoms";
    private static final String TABLE_MEDICATIONS = "Medications";

    //Symptoms table columns
    private static final String KEY_SYMPTOM_ID = "id";
    private static final String KEY_SYMPTOM_NAME = "SymptomName";

    //Medication table columns
    private static final String KEY_MEDICATION_ID = "id";
    private static final String KEY_MEDICATION_NAME = "MedicationName";
    private static final String KEY_MEDICATION_AMOUNT = "MedicationAmount";
    //TODO - Medication frequency etc..

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
        //TODO this will need changing once more columns, fields are added !
        String CREATE_SYMPTOMS_TABLE = "CREATE TABLE " + TABLE_SYMPTOMS +
                "(" +
                    KEY_SYMPTOM_ID + " INTEGER PRIMARY KEY," + //Primary key
                    KEY_SYMPTOM_NAME + " TEXT" +
                ")";

        String CREATE_MEDICATIONS_TABLE = "CREATE TABLE " + TABLE_MEDICATIONS +
                "(" +
                    KEY_MEDICATION_ID + " INTEGER PRIMARY KEY," + //Primary key
                    KEY_MEDICATION_NAME + " TEXT," +
                    KEY_MEDICATION_AMOUNT + " INTEGER" +
                ")";

        db.execSQL(CREATE_SYMPTOMS_TABLE);
        db.execSQL(CREATE_MEDICATIONS_TABLE);
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

    //region Insert a symptom into the database:
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

            db.insertOrThrow(TABLE_SYMPTOMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add symptom to database");
        } finally
        {
            db.endTransaction();
        }
    }
    //endregion

    //region Insert a medication into the database:
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

            db.insertOrThrow(TABLE_SYMPTOMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add symptom to database");
        } finally
        {
            db.endTransaction();
        }
    }


    //endregion

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
