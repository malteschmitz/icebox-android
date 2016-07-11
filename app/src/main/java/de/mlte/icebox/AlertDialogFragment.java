package de.mlte.icebox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class AlertDialogFragment extends DialogFragment {

    public static final String ARG_TITLE = "de.mlte.icebox.ARG_TITLE";
    public static final String ARG_MESSAGE = "de.mlte.icebox.ARG_MESSAGE";

    String title;
    String message;

    @Override
    public void setArguments(Bundle args) {
        if (args != null) {
            this.title = args.getString(ARG_TITLE);
            this.message = args.getString(ARG_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setArguments(savedInstanceState);
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_TITLE, title);
        outState.putString(ARG_MESSAGE, message);
        super.onSaveInstanceState(outState);
    }
}
