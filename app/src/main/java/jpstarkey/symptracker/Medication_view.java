package jpstarkey.symptracker;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static jpstarkey.symptracker.R.id.btnMedBack;
import static jpstarkey.symptracker.R.id.btnSymBack;
import static jpstarkey.symptracker.R.id.btnSymDelete;
import static jpstarkey.symptracker.R.id.btnSymEdit;
import static jpstarkey.symptracker.R.id.tbSymptomDescription;
import static jpstarkey.symptracker.R.id.tvSymptomName;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Medication_view.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Medication_view#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Medication_view extends Fragment implements
        EditMedDialog.EditMedDialogListener,
        EditMedDialog.OnFragmentInteractionListener

{


    private OnFragmentInteractionListener mListener;
    private Button btnMedBack;
    private Button btnMedEdit;
    private Button btnMedDelete;
    private TextView tvMedicationName;
    private TextView tvMedicationDescription;
    private TextView tvMedicationAmount;
    private TextView tvMedicationFrequency;
    private Medication mMedication;
    private DatabaseHelper db;

    public Medication_view()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment Medication_view.
     */
    // TODO: Rename and change types and number of parameters
    public static Medication_view newInstance()
    {
        Medication_view fragment = new Medication_view();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medication_view, container, false);

        btnMedBack = (Button) view.findViewById(R.id.btnMedBack);
        btnMedEdit = (Button) view.findViewById(R.id.btnMedEdit);
        btnMedDelete = (Button) view.findViewById(R.id.btnMedDelete);

        tvMedicationName = (TextView) view.findViewById(R.id.tvMedicationName);
        tvMedicationDescription = (TextView) view.findViewById(R.id.tvMedicationDescription);
        tvMedicationAmount = (TextView) view.findViewById(R.id.tvMedicationAmount);
        tvMedicationFrequency = (TextView) view.findViewById(R.id.tvMedicationFrequency);

        db = DatabaseHelper.getInstance(this.getContext());

        //Edit button
        btnMedEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editMedication();
            }
        });

        //Back button
        btnMedBack.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                goBackToMedications();

            }
        });

        //Delete button
        btnMedDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteMedication();
            }
        });

        getMedicationDetails(getArguments().getInt("ID"));

        return view;
    }

    public void goBackToMedications()
    {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fMedications = new Medications();

        fragmentManager
                .beginTransaction()
                .replace(R.id.flContent, fMedications)
                .commit();
    }

    public void getMedicationDetails(int medicationId)
    {
        if(db == null)
        {
            db = DatabaseHelper.getInstance(this.getContext());
        }

        mMedication = db.getMedicationById(medicationId);;
        tvMedicationName.setText(mMedication.getName());
        tvMedicationDescription.setText(mMedication.getDescription());
        tvMedicationAmount.setText(String.valueOf(mMedication.getAmount()));
        tvMedicationFrequency.setText(String.valueOf(mMedication.getFrequency()));
    }

    public void editMedication()
    {
        if (mMedication != null)
        {
            FragmentManager fm = getFragmentManager();
            EditMedDialog editMedDialog = EditMedDialog.newInstance("Edit medication", mMedication._id, mMedication);
            editMedDialog.setTargetFragment(Medication_view.this, 300);
            editMedDialog.show(fm, "edit_medication");

            Log.i("Medications", "edit Medication clicked");
        }
    }

    public void deleteMedication()
    {
        if (mMedication != null)
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
                    String deletedName = mMedication.getName();

                    //createToast("Deleted : " + deletedName);
                    dialog.dismiss();
                    db.deleteMedicationById(mMedication.getId());
                    //Return to symptoms page
                    goBackToMedications();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int i)
                {
                    //Don't delete anything
                    dialog.dismiss();
                    //Return to symptoms page
                    goBackToMedications();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

            Log.i("Symptoms", "delete Symptom clicked");
        }
    }

    public void createToast(String textToShow)
    {
        if (this.getContext() != null)
        {
            Toast.makeText(this.getContext(), textToShow, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.d("CreateToast", "this.Context is null");
        }
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
    public void onFinishEditMedDialog(long id, String inputName, String inputDesc, int inputAmount, int inputFrequency)
    {
        Log.i("MedicationsEdit", inputName + " " + inputDesc + " " + Long.toString((id)));
        //Update this medication in the DB
        Medication editedMedication = new Medication(inputName, inputDesc, inputAmount, inputFrequency);

        DatabaseHelper handler = DatabaseHelper.getInstance(this.getContext());
        if (handler != null)
        {
            handler.updateMedicationById((int)id, editedMedication);
        }

        if (getArguments() != null)
        {
            getMedicationDetails(getArguments().getInt("ID"));
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
