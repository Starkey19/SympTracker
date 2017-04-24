package jpstarkey.symptracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static jpstarkey.symptracker.R.id.sympItems;
import static jpstarkey.symptracker.R.id.text;
import static jpstarkey.symptracker.R.id.view;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Daily.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Daily#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Daily extends Fragment
{
    private OnFragmentInteractionListener mListener;
    private TextView currentTotalSteps;
    private TextView currentDate;
    private SeekBar seekPainLevel;
    private TextView tvPainLevel;
    private Button btnAddDailySymptoms;
    private EditText etDailyNotes;

    private ArrayList mSelectedSymptoms;

    public Daily()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Daily.
     */
    // TODO: Rename and change types and number of parameters
    public static Daily newInstance(String param1, String param2)
    {
        Daily fragment = new Daily();
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
        //Force Landscape orientation for the graph:
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_daily, container, false);

        currentTotalSteps = (TextView) view.findViewById(R.id.tvTotalSteps);
        currentDate = (TextView) view.findViewById(R.id.tvCurrentDate);
        tvPainLevel = (TextView) view.findViewById(R.id.tvPainLevel);
        btnAddDailySymptoms = (Button) view.findViewById(R.id.btnAddDailySymptoms);
        etDailyNotes = (EditText) view.findViewById(R.id.etDailyNotes);
        etDailyNotes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                etDailyNotes.requestFocus();
            }
        });

        etDailyNotes.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if (hasFocus)
                {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
                else
                {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });


        seekPainLevel = (SeekBar) view.findViewById(R.id.seekPainLevel);
        //Initialize tvPainLevel
        tvPainLevel.setText(seekPainLevel.getProgress() + "/" + seekPainLevel.getMax());
        //Listener for the seekBar pain level
        seekPainLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                progress = i; // i = progress value
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //Update tvPainLevel
                tvPainLevel.setText(progress + "/" + seekBar.getMax());
            }
        });

        //Button to raise dialog to add symptoms for a daily log
        btnAddDailySymptoms.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                createSymptomsCheckBoxDlg();
            }
        });

        //Format the date for currentDate textView:
        Date now = new Date();
        String currentDateTimeString = DateFormat.getDateInstance().format(now);
        currentDate.setText(currentDateTimeString);


        GlobalState state = ((GlobalState) this.getContext().getApplicationContext());
        if (currentTotalSteps != null)
        {
            currentTotalSteps.setText(Float.toString(state.getDailySteps()));
        }


        return view;
    }

    public void createSymptomsCheckBoxDlg()
    {
        //Query DB for all symptoms user has stored
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        final List<Symptom> symptoms = db.getAllSymptoms();
        final List<Symptom> selectedSymptoms = new ArrayList<>();
        List<String> symptomNames = new ArrayList<>();

        for (Symptom symptom: symptoms)
        {
            symptomNames.add(symptom.name);
        }

        final CharSequence[] items = symptomNames.toArray(new CharSequence[symptomNames.size()]);
        mSelectedSymptoms = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Check any symptoms that apply today");
        builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int indexSelected, boolean bChecked)
            {
                if (bChecked)
                {
                    mSelectedSymptoms.add(indexSelected);
                    selectedSymptoms.add(symptoms.get(indexSelected));
                }
                else if (mSelectedSymptoms.contains(indexSelected))
                {
                    //It already exists, its being checked again so remove it
                    mSelectedSymptoms.remove(Integer.valueOf(indexSelected));
                    selectedSymptoms.remove(symptoms.get(indexSelected));
                }
            }
        }).setPositiveButton("Submit Daily Log", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                //User is finished adding symptoms
                //Update dailyLog in DB with added symptoms
                DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                //Dates are stored as DD/MM/YYYY in DB so need to format to this first
                Date cDate = new Date(); //Current date
                String sDate = new SimpleDateFormat("dd-MM-yyyy").format(cDate);
                //Insert daily log into DB
                DailyLog dailyLog = new DailyLog(sDate, seekPainLevel.getProgress());
                db.addDailyLogWithSymptoms(dailyLog, selectedSymptoms);
                createToast("Pain level and symptoms submitted for " + sDate);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                //User cancelled added any symptoms.
            }
        })
        .create()
            .show();
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
