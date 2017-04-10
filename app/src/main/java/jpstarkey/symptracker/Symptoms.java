package jpstarkey.symptracker;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import static jpstarkey.symptracker.R.id.view;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Symptoms.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Symptoms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Symptoms extends Fragment implements
        AddDialog.AddDialogListener,
        AddDialog.OnFragmentInteractionListener
{

    Button btnAddSymptom;
    Button btnEditSymptom;


    private OnFragmentInteractionListener mListener;

    public Symptoms()
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
        View view = inflater.inflate(R.layout.fragment_symptoms, container, false);

        //Get instances of the add and edit buttons:
        btnAddSymptom = (Button) view.findViewById(R.id.btnAddSymptom);
        btnAddSymptom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fm = getFragmentManager();
                AddDialog addDialog = AddDialog.newInstance("Add a new Symptom");
                addDialog.setTargetFragment(Symptoms.this, 300);
                addDialog.show(fm, "add_symptom");

                Log.i("Symptoms", "Add Symptom clicked");
            }
        });

        btnEditSymptom = (Button) view.findViewById(R.id.btnEditSymptom);
        btnEditSymptom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i("Symptoms", "Edit Symptom clicked");
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
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //TEST ADD A SYMPTOM TO DB
        DatabaseHelper handler = DatabaseHelper.getInstance(this.getContext());
        SQLiteDatabase db = handler.getWritableDatabase();
        Cursor sympCursor = db.rawQuery("SELECT * FROM Symptoms", null);

        //Find the listView we want to populate
        ListView lvItems = (ListView) this.getView().findViewById(R.id.sympItems);
        //Setup cursor adapter, added getview,getConext here
        aCursorAdapter cursorAdapter = new aCursorAdapter(this.getView().getContext(), sympCursor);
        //Attach the cursor adapter to the listView
        lvItems.setAdapter(cursorAdapter);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFinishEditDialog(String inputSymptomName, String inputSymptomDesc)
    {
        Log.i("Symptoms", inputSymptomName + " " + inputSymptomDesc);

        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        //Insert symptom into DB
        Symptom newSymptom = new Symptom(inputSymptomName, inputSymptomDesc, 0); //Set pain level to 0 initially
        db.addSymptom(newSymptom);
    }

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
