package jpstarkey.symptracker;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.value;
import static jpstarkey.symptracker.R.id.view;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Symptom_view.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Symptom_view#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Symptom_view extends Fragment implements
        EditDialog.EditDialogListener,
        EditDialog.OnFragmentInteractionListener
{


    private OnFragmentInteractionListener mListener;
    private Button btnSymBack;
    private Button btnSymEdit;
    private Button btnSymDelete;
    private TextView tvSymptomName;
    private TextView tbSymptomDescription;
    private Symptom mSymptom;
    private DatabaseHelper db;

    public Symptom_view()
    {
        // Required empty public constructor
    }

    public static Symptom_view newInstance()
    {
        Symptom_view fragment = new Symptom_view();
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
            Log.i("TAG", "args are null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_symptom_view, container, false);

        btnSymBack = (Button) view.findViewById(R.id.btnSymBack);
        btnSymEdit = (Button) view.findViewById(R.id.btnSymEdit);
        btnSymDelete = (Button) view.findViewById(R.id.btnSymDelete);

        tvSymptomName = (TextView) view.findViewById(R.id.tvSymptomName);
        tbSymptomDescription = (TextView) view.findViewById(R.id.tbSymptomDescription);

        db = DatabaseHelper.getInstance(this.getContext());

        //Edit button
        btnSymEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editSymptom();
            }
        });

        //Back button
        btnSymBack.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                goBackToSymptoms();

            }
        });

        //Delete button
        btnSymDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteSymptom();
            }
        });


        getSymptomDetails(getArguments().getInt("ID"));

        return view;
    }

    public void goBackToSymptoms()
    {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fSymptoms = new Symptoms();

        fragmentManager
                .beginTransaction()
                .replace(R.id.flContent, fSymptoms)
                .commit();
    }

    public void getSymptomDetails(int symptomId)
    {
        if(db == null)
        {
            db = DatabaseHelper.getInstance(this.getContext());
        }

        mSymptom = db.getSymptomById(symptomId);;
        tvSymptomName.setText(mSymptom.getName());
        tbSymptomDescription.setText(mSymptom.getDescription());
    }

    public void editSymptom()
    {
        if (mSymptom != null)
        {
            FragmentManager fm = getFragmentManager();
            EditDialog editDialog = EditDialog.newInstance("Edit Symptom", mSymptom._id, mSymptom);
            editDialog.setTargetFragment(Symptom_view.this, 300);
            editDialog.show(fm, "edit_symptom");

            Log.i("Symptoms", "edit Symptom clicked");
        }
    }

    public void deleteSymptom()
    {
        if (mSymptom != null)
        {
            if(db == null)
            {
                db = DatabaseHelper.getInstance(this.getContext());
            }

            //Display an alertDialog with 'Are you sure?' message:
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to delete this?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int i)
                {
                    //User is sure.. close dialog and delete
                    String deletedName = mSymptom.getName();

                    db.deleteSymptomById(mSymptom.get_id());
                    createToast("Deleted : " + deletedName);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int i)
                {
                    //Don't delete anything
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

            Log.i("Symptoms", "delete Symptom clicked");

            //Return to symptoms page
            goBackToSymptoms();
        }
    }

    public void createToast(String textToShow)
    {
        Toast.makeText(this.getContext(), textToShow, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFinishEditDialog(long id, String inputName, String inputDesc)
    {
        Log.i("SymptomsEdit", inputName + " " + inputDesc + Long.toString((id)));
        //Update this symptom in the DB
        Symptom editedSymptom = new Symptom(inputName, inputDesc, 0);

        DatabaseHelper handler = DatabaseHelper.getInstance(this.getContext());
        if (handler != null)
        {
            handler.updateSymptomById((int)id, editedSymptom);
        }

        if (getArguments() != null)
        {
            getSymptomDetails(getArguments().getInt("ID"));
        }
        createToast(inputName + " edited.");
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
