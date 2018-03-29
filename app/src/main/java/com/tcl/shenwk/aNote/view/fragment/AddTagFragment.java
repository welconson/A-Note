package com.tcl.shenwk.aNote.view.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;

/**
 * Add tag fragment inside TagManagerFragment.
 * Created by shenwk on 2018/3/29.
 */

public class AddTagFragment extends DialogFragment {
    private TextView textView;
    private ImageButton done;
    private OnDoneListener onDoneListener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.add_tag_layout);
        dialog.setTitle("input a tag name");

        textView = dialog.findViewById(R.id.edit_tag_name);
        done = dialog.findViewById(R.id.add_tag);
        done.setOnClickListener(doneListener);
        return dialog;
    }

    View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tagName = textView.getText().toString();
            if(onDoneListener != null){
                if(onDoneListener.onDone(tagName)){
                    dismiss();
                }
            }else dismiss();
        }
    };

    public interface OnDoneListener{
        boolean onDone(String tagName);
    }

    public void setOnDoneListener(OnDoneListener onDoneListener){
         this.onDoneListener = onDoneListener;
    }
}
