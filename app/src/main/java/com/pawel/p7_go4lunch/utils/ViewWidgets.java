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

        if (actionID == 1) {

            Snackbar snackbar = Snackbar
                    .make(view, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snack_action_btn, v -> {
                        Snackbar snackBarError = Snackbar.make(view, R.string.thanks, Snackbar.LENGTH_SHORT);
                        snackBarError.show();
                    })
                    .setActionTextColor(Color.WHITE);

            View snackView = snackbar.getView();
            TextView textView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);

            snackbar.show();

        } else {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}
