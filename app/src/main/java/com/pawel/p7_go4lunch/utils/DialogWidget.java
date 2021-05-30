package com.pawel.p7_go4lunch.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class DialogWidget extends AppCompatDialogFragment {

    private DialogWidgetListener listener;
    private AlertDialog.Builder ADBuilder;
    private final boolean modeDialog;
    private final String mTitle;
    private final String mMessage;
    private final String mNegativeBtnTx;
    private final String mPositiveBtnTx;

    public DialogWidget(boolean modeDialog, Context context, String title, String message, String negativeBtnTx, String positiveBtnTx) {
        this.modeDialog = modeDialog;
        mTitle = title;
        mMessage = message;
        mNegativeBtnTx = negativeBtnTx;
        mPositiveBtnTx = positiveBtnTx;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity != null) {
            ADBuilder = new AlertDialog.Builder(activity);
            ADBuilder.setTitle(mTitle)
                    .setMessage(mMessage)
                    .setNegativeButton(mNegativeBtnTx, (dialog, which) -> listener.OnNegativeBtnAlertDialogClick())
                    .setPositiveButton(mPositiveBtnTx, (dialog, which) -> listener.OnPositiveBtnAlertDialogClick());
        }

        return ADBuilder.create();
    }

    public interface DialogWidgetListener {
        void OnPositiveBtnAlertDialogClick();
        void OnNegativeBtnAlertDialogClick();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        try {
            listener = (DialogWidgetListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(cce.toString()
                    + " must implement DialogWidgetListener");
        }
        super.onAttach(context);
    }

    /**
     * Set color of negative and positive buttons in alertDialog.
     */
    @Override
    public void onStart() {
        super.onStart();
        int positive;
        int negative;
        if (modeDialog) {
            positive = Color.RED;
            negative = Color.BLUE;
        } else {
            positive = Color.BLUE;
            negative = Color.BLACK;
        }
        Button positiveBtn = ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveBtn.setTextColor(positive);
        Button negativeBtn = ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeBtn.setTextColor(negative);
    }
}
