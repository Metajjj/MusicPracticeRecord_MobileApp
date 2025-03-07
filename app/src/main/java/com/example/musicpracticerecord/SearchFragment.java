package com.example.musicpracticerecord;

import androidx.fragment.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class SearchFragment extends DialogFragment {

    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        context = getContext().getApplicationContext();

        //TransitionInflater TI = TransitionInflater.from(context); setEnterTransition(TI.inflateTransition(R.transition.transition)); setExitTransition(TI.inflateTransition(R.anim.frag_out));
        //Animations give more control than transitions

        return inflater.inflate(R.layout.searchfrag, container, false);
    }
}
