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
//Todo rename to symptoms
public class aCursorAdapter extends CursorAdapter
{
    public aCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.item_symptom, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        //Get fields
        TextView sympName = (TextView) view.findViewById(R.id.symptomName);
        TextView sympBody = (TextView) view.findViewById(R.id.symptomBody);

        //Extract from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        //pain level int

        //Populate fields with extracted properties
        sympName.setText(name);
        sympBody.setText(body);
    }
}
