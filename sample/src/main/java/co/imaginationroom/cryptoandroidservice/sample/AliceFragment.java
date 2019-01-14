package com.imaginationroom.cryptoandroidservice.sample;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.UUID;

import com.imaginationroom.cryptoandroidservice.CryptoResponse;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AliceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AliceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AliceFragment extends BoundBaseFragment {

    private static final String MY_UUID = UUID.randomUUID().toString();
    private static final String OTHER_UUID = UUID.randomUUID().toString();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private FloatingActionButton fab;

    public AliceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AliceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AliceFragment newInstance(String param1, String param2) {
        AliceFragment fragment = new AliceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_alice, container, false);

        fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBound()) {
                    onFabClicked();
                } else {
                    Snackbar.make(view, "Not Bound", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected void onServiceBound() {
        updateUi();
    }

    private void updateUi() {
        if (!isBound()) {
            Snackbar.make(getView(), "Not Bound", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            try {
                if (getCryptoService().hasSession(OTHER_UUID)) {
                    if (fab != null) {
                        fab.setImageResource(android.R.drawable.ic_input_add);
                    } else {
                        fab.setImageResource(android.R.drawable.ic_dialog_email);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onFabClicked() {
        try {
            if (!mCryptoService.hasSession(OTHER_UUID)) {
                // TODO create some random string, encrypt and send to Bob
//                mCryptoService.addSessionBob(OTHER_UUID, "Fake Data".getBytes());
            } else {
                String clearText = UUID.randomUUID().toString();
                // long op: do on bg thread (wrap in Rx goodness
                CryptoResponse response = mCryptoService.encrypt(OTHER_UUID, clearText.getBytes());
                if (response.getErrorMessage() == null) {
                    mListener.sendToBob(response.getBytes());
                }
            }
        } catch (RemoteException e) {
            Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void onServiceUnbound() {

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void sendToBob(byte[] bytes);
    }
}
