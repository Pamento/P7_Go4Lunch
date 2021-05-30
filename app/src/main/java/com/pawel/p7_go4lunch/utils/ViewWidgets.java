package com.pawel.p7_go4lunch.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.pawel.p7_go4lunch.R;

public abstract class ViewWidgets {
    /**
     * @param actionID 1 for warning; 0 for info message
     * @param view     for which snackBar must by applied
     * @param message  String of content of message.
     */
    public static void showSnackBar(int actionID, View view, String message) {

        Snackbar snackbar;
        if (actionID == 1) {
            snackbar = Snackbar
                    .make(view, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snack_action_btn, v -> {
                        // Set the "OK" button, is for be sur that the user get the message
                    })
                    .setActionTextColor(Color.WHITE);

            View snackView = snackbar.getView();
            TextView textView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);

        } else {
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        }
        snackbar.show();
    }
}
