/*
 * InfoDialog
 * JDGSoundboard
 *
 * Copyright (c) 2019 Vincent Lammin
 */

package com.atmx.android.jdgspl.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.atmx.android.jdgspl.R;

public class InfoDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_info, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setTitle(this.getString(R.string.about_title));

        builder.setPositiveButton(
            this.getString(R.string.close),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Do nothing : default is close
                }
            }
        );

        return builder.create();
    }
}
