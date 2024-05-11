package com.example.mainapp.mainfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.mainapp.R;


public class ShortsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 프래그먼트의 UI 초기화
        View view = inflater.inflate(R.layout.shorts_fragment, container, false);

        return view;
    }
}
