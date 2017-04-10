package jpstarkey.symptracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDialog extends DialogFragment implements TextView.OnEditorActionListener
{

    private EditText mSymptomName;
    private EditText mSymptomDescription;

    private OnFragmentInteractionListener mListener;

    public AddDialog()
    {
        // Required empty public constructor
    }

    //Interface listener for adding symmptoms via dialogs
    public interface AddDialogListener
    {
        void onFinishEditDialog(String inputName, String inputDesc);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static AddDialog newInstance(String title)
    {
        AddDialog fragment = new AddDialog();
        Bundle args = new Bundle();
        args.putString("title", title);

        fragment.setArguments(args);
        return fragment;
    }

    // Fires whenever the textfield has an action performed
    // In this case, when the "Done" button is pressed
    // REQUIRES a 'soft keyboard' (virtual keyboard)
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text back to activity through the implemented listener

             sendBackResult();
//            AddDialogListener listener = (AddDialogListener) getTargetFragment();
//            listener.onFinishEditDialog(mEditText.getText().toString());
//            // Close the dialog and return back to the parent activity
//            dismiss();
            return true;
        }
        return false;
    }

    //Send data back to parent fragment instead of activity
    public void sendBackResult()
    {
        AddDialogListener listener = (AddDialogListener) getTargetFragment();
        listener.onFinishEditDialog(mSymptomName.getText().toString(), mSymptomDescription.getText().toString());
        dismiss();
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
        return inflater.inflate(R.layout.fragment_add_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get fields from view
        mSymptomName = (EditText) view.findViewById(R.id.txt_symptom_name);
        mSymptomDescription = (EditText) view.findViewById(R.id.txt_symptom_description);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mSymptomName.requestFocus();
        //Setup a callback when the done button is pressed on keyboard
        mSymptomName.setOnEditorActionListener(this);
        mSymptomDescription.setOnEditorActionListener(this);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
