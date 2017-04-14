package jpstarkey.symptracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static android.R.attr.priority;

/**
 * Created by Joshs on 31/03/2017.
 */

public class MedicationCursorAdapter extends CursorAdapter
{
    public MedicationCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        //Get fields
        TextView medName = (TextView) view.findViewById(R.id.medicationName);
        //TextView sympBody = (TextView) view.findViewById(R.id.symptomBody);

        //Extract from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        //String body = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        //pain level int

        //Populate fields with extracted properties
        medName.setText(name);
        //sympBody.setText(body);
    }
}
