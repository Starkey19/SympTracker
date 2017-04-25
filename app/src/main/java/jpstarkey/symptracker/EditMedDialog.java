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
 * {@link EditMedDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditMedDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditMedDialog extends DialogFragment implements TextView.OnEditorActionListener
{
    private EditText mMedicationName;
    private EditText mMedicationDesc;
    private EditText mMedicationAmount;
    private EditText mMedicationFreq;

    private Medication mMedicationToEdit;

    private OnFragmentInteractionListener mListener;

    public EditMedDialog()
    {
        // Required empty public constructor
    }

    public interface EditMedDialogListener
    {
        void onFinishEditMedDialog(long id, String inputName, String inputDesc, int inputAmount, int inputFrequency);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment EditMedDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static EditMedDialog newInstance(String title, long id, Medication medicationToEdit)
    {
        EditMedDialog fragment = new EditMedDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putLong("itemId", id);
        args.putString("name", medicationToEdit.getName());
        args.putString("description", medicationToEdit.getDescription());
        args.putInt("amount", medicationToEdit.getAmount());
        args.putInt("frequency", medicationToEdit.getFrequency());
        fragment.setArguments(args);
        return fragment;
    }

    //Fires when the done button from keyboard is pressed
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
        EditMedDialogListener listener = (EditMedDialogListener) getTargetFragment();
        long id = getArguments().getLong("itemId");
        listener.onFinishEditMedDialog(id, mMedicationName.getText().toString(),
                mMedicationDesc.getText().toString(),
                Integer.parseInt(mMedicationAmount.getText().toString()),
                Integer.parseInt(mMedicationFreq.getText().toString()));
        dismiss();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get fields from view
        mMedicationName = (EditText) view.findViewById(R.id.txt_edit_medication_name);
        mMedicationDesc = (EditText) view.findViewById(R.id.txt_edit_medication_description);
        mMedicationAmount = (EditText) view.findViewById(R.id.txt_edit_medication_amount);
        mMedicationFreq = (EditText) view.findViewById(R.id.txt_medication_edit_frequency);


        //Set editText name and desc from the selected symptom:
        mMedicationName.setText(getArguments().getString("name"));
        mMedicationDesc.setText(getArguments().getString("description"));
        mMedicationAmount.setText(Integer.toString(getArguments().getInt("amount")));
        mMedicationFreq.setText(Integer.toString(getArguments().getInt("frequency")));

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mMedicationName.requestFocus();
        //Setup a callback when the done button is pressed on keyboard
        mMedicationName.setOnEditorActionListener(this);
        mMedicationDesc.setOnEditorActionListener(this);
        mMedicationAmount.setOnEditorActionListener(this);
        mMedicationFreq.setOnEditorActionListener(this);

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
        return inflater.inflate(R.layout.fragment_edit_med_dialog, container, false);
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
