package com.slensky.focussis.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slensky.focussis.R;

/**
 * Created by slensky on 4/28/17.
 */

public class NetworkErrorFragment extends Fragment {
    public NetworkErrorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_network_failure, container, false);
        return view;
    }
}
