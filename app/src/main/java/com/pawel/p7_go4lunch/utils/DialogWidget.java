package com.pawel.p7_go4lunch.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.pawel.p7_go4lunch.R;

public class DialogWidget extends AppCompatDialogFragment {
    private DialogWidgetListener listener;
    private AlertDialog.Builder ADBuilder;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity != null) {
            ADBuilder = new AlertDialog.Builder(activity);
            ADBuilder.setTitle(R.string.alert_title)
                    .setMessage(R.string.alert_message)
                    .setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                        // do nothing
                    })
                    .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                        listener.OnPositiveBtnAlertDialogClick();
                    });
        }

        return ADBuilder.create();
    }

    public interface DialogWidgetListener {
        void OnPositiveBtnAlertDialogClick();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogWidgetListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString()
                    + "must implement DialogWidgetListener");
        }
    }
}
