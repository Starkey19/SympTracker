package jpstarkey.symptracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Symptoms.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Symptoms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Medications extends Fragment implements
        AddMedDialog.AddMedDialogListener,
        AddMedDialog.OnFragmentInteractionListener

{

    private Button btnAddMedication;
    private ListView lvItems;
    private MedicationCursorAdapter cursorAdapter;
    private DatabaseHelper handler;


    private OnFragmentInteractionListener mListener;

    public Medications()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Symptoms.
     */
    // TODO: Rename and change types and number of parameters
    public static Symptoms newInstance(String param1, String param2)
    {
        Symptoms fragment = new Symptoms();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
        }

        // Fragment screen orientation normal both portait and landscape
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medications, container, false);

        //Get instances of the add and edit buttons:
        btnAddMedication = (Button) view.findViewById(R.id.btnAddMedication);
        btnAddMedication.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fm = getFragmentManager();
                AddMedDialog addMedDialog = AddMedDialog.newInstance("Add a new Medication");
                addMedDialog.setTargetFragment(Medications.this, 300);
                addMedDialog.show(fm, "add_medication");

                Log.i("Medications", "Add medication clicked");
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        populateMedications();
    }

    public void populateMedications()
    {
        handler = DatabaseHelper.getInstance(this.getContext());
        SQLiteDatabase db = handler.getWritableDatabase();
        Cursor medCursor = db.rawQuery("SELECT * FROM Medications", null);

        //Find the listView we want to populate
        lvItems = (ListView) this.getView().findViewById(R.id.medItems);
        setUpLongClick();
        //Setup cursor adapter, added getview,getConext here
        cursorAdapter = new MedicationCursorAdapter(this.getView().getContext(), medCursor);
        //Attach the cursor adapter to the listView
        lvItems.setAdapter(cursorAdapter);
    }

    public void setUpLongClick()
    {
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
            {
                //TODO: Bring up edit dialog for medication with this ID:
                Log.i("TAG", "ID: " + Long.toString(l));
                registerForContextMenu(adapterView);

            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "Edit");
        menu.add(0, v.getId(), 0, "Delete");

        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        //long itemId = info.id;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle()=="Edit")
        {
            editMedication(item);

        }
        else if(item.getTitle()=="Delete")
        {
            deleteMedication(item);
        }
        else
        {
            return false;
        }
        return true;
    }

    public void editMedication(MenuItem item)
    {
        //Retrieve medication from cursor using id
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long itemId = info.id;
        Cursor cursor = (cursorAdapter).getCursor();
        cursor.moveToPosition((int) itemId);
        String prevName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String prevDesc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        int prevAmount = cursor.getInt(cursor.getColumnIndexOrThrow("amount"));
        int prevFreq = cursor.getInt(cursor.getColumnIndexOrThrow("frequency"));

        Medication med = new Medication(prevName, prevDesc, prevAmount, prevFreq);

        //TODO: wont work
//        FragmentManager fm = getFragmentManager();
//        EditDialog editDialog = EditDialog.newInstance("Edit Medication", itemId, med);
//        editDialog.setTargetFragment(Medications.this, 300);
//        editDialog.show(fm, "edit_medication");
//
//        Log.i("Medications", "edit Medication clicked");
    }

    public void deleteMedication(final MenuItem item)
    {
        //Display an alertDialog with 'Are you sure?' message:
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to delete this?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                //User is sure.. close dialog and delete
                delete(item);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                //Don't delete anything
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();





        Log.i("Medications", "delete Medication clicked");
    }

    public void delete(MenuItem item)
    {
        //Retrieve Medication from cursor using id
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long itemId = info.id;
        if (handler != null)
        {
            handler.deleteMedicationById((int) itemId);
        }
        else
        {
            Log.d("Medications", "db handler is null");
        }

        populateMedications();
        //Todo toast
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFinishAddDialog(String inputName, String inputDesc, int amount, int freq)
    {
        Log.i("Medications", inputName + " " + inputDesc );

        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        //Insert medication into DB
        Medication newMed = new Medication(inputName, inputDesc, amount, freq); //Set pain level to 0 initially
        db.addMedication(newMed);

        //"Refresh" the fragment with new content
        populateMedications();
    }

    //TODO
//    @Override
//    public void onFinishEditDialog(long id, String inputName, String inputDesc)
//    {
//        Log.i("SymptomsEdit", inputName + " " + inputDesc + Long.toString((id)));
//        //Update this symptom in the DB
//        Symptom editedSymptom = new Symptom(inputName, inputDesc, 0);
//
//        if (handler != null)
//        {
//            handler.updateSymptomById((int)id, editedSymptom);
//        }
//
//        //"Refresh" the fragment with new content
//        populateMedications();
//    }


    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
