package jpstarkey.symptracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
 * {@link EditDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditDialog extends DialogFragment implements TextView.OnEditorActionListener
{
    private EditText mSymptomName;
    private EditText mSymptomDescription;

    private Symptom mSymptomToEdit;

    private OnFragmentInteractionListener mListener;

    public EditDialog()
    {
        // Required empty public constructor
    }

    public interface EditDialogListener
    {
        void onFinishEditDialog(long id, String inputName, String inputDesc);
    }

    public static EditDialog newInstance(String title, long id, Symptom symptomToEdit)
    {
        EditDialog fragment = new EditDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putLong("itemId", id);
        args.putString("name", symptomToEdit.getName());
        args.putString("description", symptomToEdit.getDescription());
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
            return true;
        }
        return false;
    }

    //Send data back to parent fragment instead of activity
    public void sendBackResult()
    {
        EditDialogListener listener = (EditDialogListener) getTargetFragment();
        long id = getArguments().getLong("itemId");
        listener.onFinishEditDialog(id, mSymptomName.getText().toString(), mSymptomDescription.getText().toString());
        dismiss();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get fields from view
        mSymptomName = (EditText) view.findViewById(R.id.txt_edit_symptom_name);
        mSymptomDescription = (EditText) view.findViewById(R.id.txt_edit_symptom_description);

        //Set editText name and desc from the selected symptom:
        mSymptomName.setText(getArguments().getString("name"));
        mSymptomDescription.setText(getArguments().getString("description"));

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
        return inflater.inflate(R.layout.fragment_edit_dialog, container, false);
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
