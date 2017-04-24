package jpstarkey.symptracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DailyLogHistory.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DailyLogHistory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DailyLogHistory extends Fragment
{
    private ListView lvItems;

    private OnFragmentInteractionListener mListener;

    public DailyLogHistory()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DailyLogHistory.
     */
    // TODO: Rename and change types and number of parameters
    public static DailyLogHistory newInstance(String param1, String param2)
    {
        DailyLogHistory fragment = new DailyLogHistory();
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
        View view = inflater.inflate(R.layout.fragment_daily_log_history, container, false);
        //lvItems = (ListView) view.findViewById(R.id.contentlist);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        populateHistory();
    }

    public void populateHistory()
    {
        DatabaseHelper handler = DatabaseHelper.getInstance(this.getContext());
        //TODO 13/04/17
        //NOT GONNA WORK THIS WAY: define a new cursorAdapter or forget it
        //SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.history_row, cursor, )

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
