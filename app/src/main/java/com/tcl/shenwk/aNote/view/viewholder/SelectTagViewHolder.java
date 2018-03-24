package com.tcl.shenwk.aNote.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;

/**
 * Created by shenwk on 2018/3/20.
 */

public class SelectTagViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    public CheckBox checkBox;
    public SelectTagViewHolder(View itemView) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.checkBox);
        textView = itemView.findViewById(R.id.tag_item_name);
    }
}
