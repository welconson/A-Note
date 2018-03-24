package com.tcl.shenwk.aNote.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;

/**
 * Created by shenwk on 2018/3/23.
 */

public class TagManagerViewHolder extends RecyclerView.ViewHolder{
    public TextView tagName;
    public TextView tagNumber;

    public TagManagerViewHolder(View itemView) {
        super(itemView);
        tagName = itemView.findViewById(R.id.tag_name);
        tagNumber = itemView.findViewById(R.id.inside_tag_info);
    }
}
