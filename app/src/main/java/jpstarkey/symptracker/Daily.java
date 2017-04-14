package jpstarkey.symptracker;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.os.Build.VERSION_CODES.M;

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
    private Button submitPainLevel;

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
        submitPainLevel = (Button) view.findViewById(R.id.btnSubmitPainLevel);

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

        //Button submit pain level for current day
        submitPainLevel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DatabaseHelper db = DatabaseHelper.getInstance(getActivity());

                //Dates are stored as DD/MM/YYYY in DB so need to format to this first
                Date cDate = new Date(); //Current date
                String sDate = new SimpleDateFormat("dd-MM-yyyy").format(cDate);

                //Insert daily log into DB
                DailyLog dailyLog = new DailyLog(sDate, seekPainLevel.getProgress());

                db.addDailyLog(dailyLog);
                Toast.makeText(view.getContext(), "Pain level submitted for " + sDate, Toast.LENGTH_SHORT).show();
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
