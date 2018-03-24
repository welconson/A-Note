package com.tcl.shenwk.aNote.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.adapter.TagManagerAdapter;

import java.util.List;

/**
 * Tags item's user interface of navigation drawer, implementing with a fragment.
 * Created by shenwk on 2018/3/23.
 */

public class TagManagerFragment extends Fragment {
    private static final String TAG = "TagManagerFragment";
    private RecyclerView recyclerView;
    private List<NoteTagEntry> noteTagEntries;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        noteTagEntries = ((HomePageActivity) getActivity()).getNoteTagEntries();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tag_manager_layout, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);

        TagManagerAdapter tagManagerAdapter = new TagManagerAdapter(noteTagEntries, getActivity().getLayoutInflater());

        recyclerView.setAdapter(tagManagerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Log.i(TAG, "onCreateView: ");
        return view;
    }
}
